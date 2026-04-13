package com.example.memento.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val birthday: String = "",
    val lifeExpectancyYears: Int = 90,
    val genderSliderPosition: Float = 0f,
    val country: String = "",
)

@Singleton
class UserProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private suspend fun profileRef() = firestore
        .collection("users")
        .document(ensureUserId())
        .collection("data")
        .document("profile")

    private suspend fun ensureUserId(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    suspend fun loadProfile(): UserProfile? = runCatching {
        val snapshot = profileRef().get().await()
        if (!snapshot.exists()) return null
        UserProfile(
            birthday = snapshot.getString("birthday") ?: "",
            lifeExpectancyYears = (snapshot.getLong("lifeExpectancyYears") ?: 90L).toInt(),
            genderSliderPosition = (snapshot.getDouble("genderSliderPosition") ?: 0.0).toFloat(),
            country = snapshot.getString("country") ?: "",
        )
    }.onFailure { Log.w("UserProfileRepository", "Failed to load profile", it) }.getOrNull()

    suspend fun saveProfile(profile: UserProfile) = runCatching {
        profileRef().set(
            mapOf(
                "birthday" to profile.birthday,
                "lifeExpectancyYears" to profile.lifeExpectancyYears,
                "genderSliderPosition" to profile.genderSliderPosition,
                "country" to profile.country,
            )
        ).await()
    }.onFailure { Log.w("UserProfileRepository", "Failed to save profile", it) }
}
