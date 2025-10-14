package dev.com3run.firebaseauthkmp

/**
 * Desktop/JVM actual implementation of platform-specific auth functions.
 */
actual fun platformAuthBackend(): AuthBackend {
    return DesktopFirebaseAuthBackend()
}

/**
 * Desktop OAuth flows require external browser-based implementation.
 *
 * For desktop applications, Google Sign-In requires:
 * 1. Opening system browser with OAuth URL
 * 2. Handling redirect to localhost callback server
 * 3. Extracting authorization code
 * 4. Exchanging code for ID token
 * 5. Passing ID token to signInWithGoogle()
 *
 * This functionality is not built into the library to keep dependencies minimal.
 * Users can implement this using libraries like:
 * - OkHttp for local callback server
 * - Desktop.browse() for opening browser
 * - Google OAuth 2.0 client libraries
 *
 * @return null (OAuth flow not directly supported on desktop without external implementation)
 */
actual suspend fun requestGoogleIdToken(): String? {
    // Desktop: OAuth flow requires external browser implementation
    // Users must implement their own OAuth flow for desktop
    println("Desktop Google Sign-In requires external OAuth implementation")
    println("Please implement browser-based OAuth flow and pass the ID token to signInWithGoogle()")
    return null
}

/**
 * Apple Sign-In on desktop requires similar OAuth flow as Google.
 *
 * @return null (Apple Sign-In requires external implementation on desktop)
 */
actual suspend fun requestAppleIdToken(): String? {
    // Desktop: Apple Sign-In requires external browser-based OAuth flow
    println("Desktop Apple Sign-In requires external OAuth implementation")
    println("Please implement browser-based OAuth flow and pass the ID token to signInWithApple()")
    return null
}

/**
 * Apple Sign-In is not natively available on desktop platforms.
 * It requires browser-based OAuth flow implementation.
 *
 * @return false (not natively available on desktop)
 */
actual fun isAppleSignInAvailable(): Boolean = false
