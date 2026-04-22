package com.example.memento.repository

import android.util.Log
import com.example.memento.db.LifePhaseDao
import com.example.memento.model.LifePhase
import com.example.memento.model.defaultLifePhases
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LifePhaseRepository @Inject constructor(
    private val dao: LifePhaseDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    // Prevents seedDefaultPhasesIfEmpty and syncFromFirestore from interleaving,
    // which would leave only the first phase if sign-in happens mid-seeding.
    private val mutex = Mutex()

    private suspend fun ensureUserId(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    private suspend fun phasesRef() = firestore
        .collection("users")
        .document(ensureUserId())
        .collection("phases")

    /** Room is the source of truth — UI observes this Flow. */
    val phases: Flow<List<LifePhase>> = dao.getAll()

    suspend fun addPhase(phase: LifePhase) {
        val roomId = dao.insert(phase)
        syncToFirestore(phase.copy(id = roomId.toInt()))
    }

    suspend fun updatePhase(phase: LifePhase) {
        dao.update(phase)
        syncToFirestore(phase)
    }

    suspend fun deletePhase(phase: LifePhase) {
        dao.delete(phase)
        runCatching {
            phasesRef().document(phase.id.toString()).delete().await()
        }.onFailure { Log.w("LifePhaseRepository", "Failed to delete phase ${phase.id} from Firestore", it) }
    }

    private suspend fun syncToFirestore(phase: LifePhase) = runCatching {
        phasesRef().document(phase.id.toString()).set(phase.toMap()).await()
    }.onFailure { Log.w("LifePhaseRepository", "Failed to sync phase ${phase.id} to Firestore", it) }

    /** Public entry point — acquires the mutex so it can't interleave with syncFromFirestore. */
    suspend fun seedDefaultPhasesIfEmpty(birthday: LocalDate) = mutex.withLock {
        seedInternal(birthday)
    }

    /**
     * Called on every UID change (sign-in / sign-out).
     * Clears Room and restores this user's phases from Firestore.
     * If Firestore is empty (new account), seeds the 5 defaults instead.
     */
    suspend fun syncFromFirestore(birthday: LocalDate) = mutex.withLock {
        val snapshot = runCatching { phasesRef().get().await() }
            .onFailure { Log.w("LifePhaseRepository", "Failed to fetch phases from Firestore", it) }
            .getOrNull()

        dao.clearAll()

        if (snapshot == null || snapshot.isEmpty) {
            seedInternal(birthday)
            return@withLock
        }

        val phases = snapshot.documents.mapNotNull { doc ->
            runCatching {
                LifePhase(
                    id = doc.id.toInt(),
                    name = doc.getString("name") ?: return@mapNotNull null,
                    colorArgb = (doc.getLong("colorArgb") ?: return@mapNotNull null).toInt(),
                    startEpochDay = doc.getLong("startEpochDay") ?: return@mapNotNull null,
                    endEpochDay = doc.getLong("endEpochDay") ?: return@mapNotNull null,
                )
            }.getOrNull()
        }
        dao.insertAll(phases)
    }

    // Internal seeding — called only from within the mutex lock.
    private suspend fun seedInternal(birthday: LocalDate) {
        if (dao.count() > 0) return
        defaultLifePhases.forEach { phase ->
            val start = birthday.plusYears(phase.startYear.toLong()).toEpochDay()
            val end   = birthday.plusYears(phase.endYear.toLong()).minusDays(1).toEpochDay()
            addPhase(LifePhase(name = phase.name, colorArgb = phase.colorArgb,
                               startEpochDay = start, endEpochDay = end))
        }
    }

    private fun LifePhase.toMap() = mapOf(
        "name" to name,
        "colorArgb" to colorArgb,
        "startEpochDay" to startEpochDay,
        "endEpochDay" to endEpochDay,
    )
}
