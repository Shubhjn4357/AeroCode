package com.aerotech.aerocode.data.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var auth: FirebaseAuth? = null
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                auth?.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    _currentUser.value = user?.toUserProfile()
                }
                _currentUser.value = auth?.currentUser?.toUserProfile()
            }
        } catch (e: Exception) {
            // Firebase not initialized yet
        }
    }

    val isConfigured: Boolean
        get() = auth != null

    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))

        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            val webClientId = if (resId != 0) context.getString(resId) else null

            if (webClientId.isNullOrBlank()) {
                // If client id is not configured, fall back to guest/anonymous or guide the user
                return@withContext signInAnonymously("Developer Guest")
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = currentAuth.signInWithCredential(authCredential).await()
                val profile = authResult.user?.toUserProfile()
                    ?: return@withContext Result.failure(Exception("Failed to obtain signed-in user."))
                _currentUser.value = profile
                Result.success(profile)
            } else {
                Result.failure(Exception("Unsupported credential type received."))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            val result = currentAuth.signInWithEmailAndPassword(email, password).await()
            val profile = result.user?.toUserProfile()
                ?: return@withContext Result.failure(Exception("No user profile found after email sign-in."))
            _currentUser.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            val result = currentAuth.createUserWithEmailAndPassword(email, password).await()
            val profile = result.user?.toUserProfile()
                ?: return@withContext Result.failure(Exception("Failed to create user account."))
            _currentUser.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(customName: String? = null): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            val result = currentAuth.signInAnonymously().await()
            val baseProfile = result.user?.toUserProfile()
            val profile = baseProfile?.copy(
                displayName = customName ?: "Anonymous Developer",
                isAnonymous = true
            ) ?: UserProfile(uid = "guest_developer", displayName = customName ?: "Guest Developer", isAnonymous = true)
            _currentUser.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            // If anonymous sign-in failed (e.g. offline or disabled), simulate local user session
            val localProfile = UserProfile(
                uid = "offline_local_dev",
                displayName = customName ?: "Local Developer",
                email = "developer@aerocode.local",
                isAnonymous = true
            )
            _currentUser.value = localProfile
            Result.success(localProfile)
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
    }

    private fun FirebaseUser.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            displayName = displayName ?: if (isAnonymous) "Guest Developer" else email?.substringBefore('@'),
            email = email,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous,
            lastLoginTimestamp = System.currentTimeMillis()
        )
    }
}
