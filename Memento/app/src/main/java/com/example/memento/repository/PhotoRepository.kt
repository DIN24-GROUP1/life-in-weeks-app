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

    private suspend fun photosRef() =
        storage.reference.child("users/${ensureUserId()}/photos")

    val weeksWithPhotos: Flow<Set<Int>> = dao.getWeekIndicesWithPhotos().map { it.toSet() }

    fun photoForWeek(weekIdx: Int): Flow<WeekPhoto?> = dao.getPhotoForWeek(weekIdx)

    suspend fun save(weekIdx: Int, sourceUri: Uri) {
        val localPath = copyToInternalStorage(weekIdx, sourceUri)
        dao.upsert(WeekPhoto(weekIdx, localPath))
        uploadToStorage(weekIdx, File(localPath))
    }

    suspend fun delete(weekIdx: Int) {
        dao.delete(weekIdx)
        internalFile(weekIdx).delete()
        runCatching {
            photosRef().child("$weekIdx.jpg").delete().await()
        }.onFailure { Log.w("PhotoRepository", "Failed to delete photo $weekIdx from Storage", it) }
    }

    /**
     * Called on every UID change (sign-in / sign-out).
     * Performs a differential sync — only downloads photos missing locally
     * and removes any that no longer exist in Storage.
     */
    suspend fun syncFromStorage() {
        val listing = runCatching { photosRef().listAll().await() }
            .onFailure { Log.w("PhotoRepository", "Failed to list photos from Storage", it) }
            .getOrNull() ?: return

        val remoteIndices = listing.items
            .mapNotNull { it.name.removeSuffix(".jpg").toIntOrNull() }
            .toSet()

        val localIndices = dao.getAllWeekIndices().toSet()

        // Remove entries that were deleted on another device
        (localIndices - remoteIndices).forEach { weekIdx ->
            dao.delete(weekIdx)
            internalFile(weekIdx).delete()
        }

        // Download only photos missing locally or whose file was lost from disk
        listing.items.forEach { ref ->
            val weekIdx = ref.name.removeSuffix(".jpg").toIntOrNull() ?: return@forEach
            val file = internalFile(weekIdx)
            if (weekIdx in localIndices && file.exists()) return@forEach

            runCatching {
                ref.getFile(file).await()
                dao.upsert(WeekPhoto(weekIdx, file.absolutePath))
            }.onFailure { Log.w("PhotoRepository", "Failed to download photo $weekIdx", it) }
        }
    }

    private fun copyToInternalStorage(weekIdx: Int, uri: Uri): String {
        val file = internalFile(weekIdx)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    private fun photoDir(): File =
        File(context.filesDir, "photos").apply { mkdirs() }

    private fun internalFile(weekIdx: Int): File =
        File(photoDir(), "$weekIdx.jpg")

    // Uses putStream instead of putFile(Uri) — more reliable with internal storage files
    private suspend fun uploadToStorage(weekIdx: Int, file: File) {
        runCatching {
            file.inputStream().use { stream ->
                photosRef().child("$weekIdx.jpg").putStream(stream).await()
            }
        }.onFailure { Log.w("PhotoRepository", "Failed to upload photo $weekIdx to Storage", it) }
    }
}
