package com.example.memento.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.memento.db.WeekPhotoDao
import com.example.memento.model.WeekPhoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val dao: WeekPhotoDao,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context,
) {
    private suspend fun ensureUserId(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    val weeksWithPhotos: Flow<Set<Int>> = dao.getWeekIndicesWithPhotos().map { it.toSet() }

    fun photoForWeek(weekIdx: Int): Flow<WeekPhoto?> = dao.getPhotoForWeek(weekIdx)

    suspend fun save(weekIdx: Int, sourceUri: Uri) {
        val localPath = copyToInternalStorage(weekIdx, sourceUri)
        dao.upsert(WeekPhoto(weekIdx, localPath))
        uploadToStorage(weekIdx, File(localPath))
    }

    suspend fun delete(weekIdx: Int) {
        val photo = dao.getPhotoForWeek(weekIdx)
        dao.delete(weekIdx)
        File(internalFile(weekIdx).absolutePath).delete()
        runCatching {
            storage.reference.child("users/${ensureUserId()}/photos/$weekIdx.jpg").delete().await()
        }.onFailure { Log.w("PhotoRepository", "Failed to delete photo $weekIdx from Storage", it) }
    }

    private fun copyToInternalStorage(weekIdx: Int, uri: Uri): String {
        val file = internalFile(weekIdx)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    private fun internalFile(weekIdx: Int): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        return File(dir, "$weekIdx.jpg")
    }

    private suspend fun uploadToStorage(weekIdx: Int, file: File) {
        runCatching {
            storage.reference
                .child("users/${ensureUserId()}/photos/$weekIdx.jpg")
                .putFile(Uri.fromFile(file)).await()
        }.onFailure { Log.w("PhotoRepository", "Failed to upload photo $weekIdx to Storage", it) }
    }
}
