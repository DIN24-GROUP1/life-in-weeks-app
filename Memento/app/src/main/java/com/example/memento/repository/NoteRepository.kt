package com.example.memento.repository

import android.util.Log
import com.example.memento.db.WeekNoteDao
import com.example.memento.model.WeekNote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val dao: WeekNoteDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private suspend fun ensureUserId(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    private suspend fun notesRef() = firestore
        .collection("users")
        .document(ensureUserId())
        .collection("notes")

    val weeksWithNotes: Flow<Set<Int>> = dao.getWeekIndicesWithNotes().map { it.toSet() }

    fun noteForWeek(weekIdx: Int): Flow<String> =
        dao.getNoteForWeek(weekIdx).map { it ?: "" }

    suspend fun save(weekIdx: Int, note: String) {
        if (note.isBlank()) {
            dao.delete(weekIdx)
            runCatching { notesRef().document(weekIdx.toString()).delete().await() }
                .onFailure { Log.w("NoteRepository", "Failed to delete note $weekIdx from Firestore", it) }
        } else {
            dao.upsert(WeekNote(weekIdx, note))
            runCatching {
                notesRef().document(weekIdx.toString()).set(mapOf("note" to note)).await()
            }.onFailure { Log.w("NoteRepository", "Failed to sync note $weekIdx to Firestore", it) }
        }
    }
}
