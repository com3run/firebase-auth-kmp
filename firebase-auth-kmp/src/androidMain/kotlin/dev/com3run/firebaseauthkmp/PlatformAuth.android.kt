package dev.com3run.firebaseauthkmp

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual fun platformAuthBackend(): AuthBackend = AndroidFirebaseAuthBackend()

// Holds a weak reference to current Activity to be used by platform flows
object ActivityHolder {
    @Volatile
    var current: Activity? = null
}

object GoogleSignInInterop {
    private const val RC_GOOGLE_SIGN_IN = 9001
    private var pending: ((requestCode: Int, resultCode: Int, data: Intent?) -> Unit)? = null

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pending?.invoke(requestCode, resultCode, data)
    }

    suspend fun requestIdToken(activity: Activity): String? = suspendCancellableCoroutine { cont ->
        Log.d("GoogleSignIn", "Requesting Google ID token")
        val defaultClientIdResId = activity.resources.getIdentifier(
            "default_web_client_id",
            "string",
            activity.packageName
        )
        if (defaultClientIdResId == 0) {
            Log.e("GoogleSignIn", "default_web_client_id not found in resources. Make sure google-services.json is configured correctly.")
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val clientId = activity.getString(defaultClientIdResId)
        Log.d("GoogleSignIn", "Using client ID: $clientId")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientId)
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        val intent = client.signInIntent

        pending = { rc, _, data ->
            if (rc == RC_GOOGLE_SIGN_IN) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    Log.d("GoogleSignIn", "Successfully got Google account: ${account.email}")
                    Log.d("GoogleSignIn", "ID token present: ${account.idToken != null}")
                    safeResume(cont, account.idToken)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Failed to get Google sign-in result: ${e.message}", e)
                    safeResume(cont, null)
                } finally {
                    pending = null
                }
            }
        }
        activity.startActivityForResult(intent, RC_GOOGLE_SIGN_IN)
        Log.d("GoogleSignIn", "Launched Google Sign-In intent")

        cont.invokeOnCancellation { pending = null }
    }
}

// Actual expect implementation: launches Google Sign-In UI and returns ID token
actual suspend fun requestGoogleIdToken(): String? = withContext(Dispatchers.Main) {
    val activity = ActivityHolder.current
    if (activity == null) {
        Log.e("GoogleSignIn", "Activity is null. Make sure to set ActivityHolder.current in your MainActivity")
        return@withContext null
    }
    Log.d("GoogleSignIn", "Activity found, proceeding with Google Sign-In")
    GoogleSignInInterop.requestIdToken(activity)
}

actual suspend fun requestAppleIdToken(): String? {
    // Apple Sign-In is not typically available on Android
    // Return null or throw an exception
    return null
}

actual fun isAppleSignInAvailable(): Boolean = false

// Helper to safely resume continuation only once
private fun <T> safeResume(cont: kotlin.coroutines.Continuation<T>, value: T) {
    try {
        cont.resume(value)
    } catch (_: IllegalStateException) {
        // Already resumed
    }
}
