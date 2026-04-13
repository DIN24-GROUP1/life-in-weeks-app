package com.example.memento.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.memento.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) {
    val isSignedInWithGoogle: Boolean
        get() = auth.currentUser
            ?.providerData
            ?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true

    val displayName: String? get() = auth.currentUser?.displayName
    val email: String? get() = auth.currentUser?.email

    /**
     * Signs in with Google using Credential Manager.
     * If the current session is anonymous, the account is linked so all existing
     * data is preserved under the same UID.
     * If the Google account already exists in Firebase, signs in normally.
     *
     * Requires R.string.default_web_client_id to be present — this is auto-generated
     * by the google-services plugin once Google Sign-In is enabled in Firebase Console
     * and google-services.json is re-downloaded.
     */
    suspend fun signInWithGoogle(context: Context): Result<Unit> = runCatching {
        val credentialManager = CredentialManager.create(context)
        val webClientId = context.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

        try {
            // Link the anonymous account to the Google credential (data is preserved)
            auth.currentUser?.linkWithCredential(firebaseCredential)?.await()
            Log.d("AuthRepository", "Linked anonymous account to Google: ${auth.currentUser?.uid}")
        } catch (e: FirebaseAuthUserCollisionException) {
            // This Google account already has a Firebase account — sign in normally
            auth.signInWithCredential(firebaseCredential).await()
            Log.d("AuthRepository", "Signed in to existing Google account: ${auth.currentUser?.uid}")
        }
        Unit
    }.onFailure { Log.w("AuthRepository", "Google sign-in failed", it) }

    /** Signs out and immediately creates a new anonymous session. */
    suspend fun signOut() {
        auth.signOut()
        runCatching { auth.signInAnonymously().await() }
            .onFailure { Log.w("AuthRepository", "Anonymous sign-in after sign-out failed", it) }
    }
}
