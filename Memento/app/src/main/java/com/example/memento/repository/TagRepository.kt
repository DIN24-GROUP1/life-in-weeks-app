package com.example.memento.repository

import android.util.Log
import com.example.memento.db.WeekTagDao
import com.example.memento.model.WeekTag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val dao: WeekTagDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private suspend fun ensureUserId(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    private suspend fun tagsRef() = firestore
        .collection("users")
        .document(ensureUserId())
        .collection("tags")

    val weeksWithTags: Flow<List<Int>> = dao.getWeekIndicesWithTags()
    val allUsedTagNames: Flow<List<String>> = dao.getAllUsedTagNames()

    fun tagsForWeek(weekIdx: Int): Flow<List<String>> = dao.getTagsForWeek(weekIdx)

    suspend fun addTag(weekIdx: Int, tagName: String) {
        dao.insert(WeekTag(weekIdx, tagName))
        runCatching {
            tagsRef().document("${weekIdx}__${tagName}")
                .set(mapOf("weekIdx" to weekIdx, "tagName" to tagName)).await()
        }.onFailure { Log.w("TagRepository", "Failed to sync tag ($weekIdx, $tagName) to Firestore", it) }
    }

    suspend fun removeTag(weekIdx: Int, tagName: String) {
        dao.delete(weekIdx, tagName)
        runCatching {
            tagsRef().document("${weekIdx}__${tagName}").delete().await()
        }.onFailure { Log.w("TagRepository", "Failed to delete tag ($weekIdx, $tagName) from Firestore", it) }
    }
}
