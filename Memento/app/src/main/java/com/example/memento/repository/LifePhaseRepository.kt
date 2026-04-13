package com.example.memento.repository

import android.util.Log
import com.example.memento.db.LifePhaseDao
import com.example.memento.model.LifePhase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LifePhaseRepository @Inject constructor(
    private val dao: LifePhaseDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
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

    private fun LifePhase.toMap() = mapOf(
        "name" to name,
        "colorArgb" to colorArgb,
        "startEpochDay" to startEpochDay,
        "endEpochDay" to endEpochDay,
    )
}
