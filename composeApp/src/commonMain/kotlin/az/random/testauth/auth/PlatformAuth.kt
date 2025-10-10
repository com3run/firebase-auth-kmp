package az.random.testauth.auth

expect fun platformAuthBackend(): AuthBackend

// Starts the native Google Sign-In flow and returns an ID token if successful.
// Returns null if the flow is cancelled or unavailable on the platform.
expect suspend fun requestGoogleIdToken(): String?

expect suspend fun requestAppleIdToken(): String?

// Returns true if Apple Sign-In is available on this platform (iOS only)
expect fun isAppleSignInAvailable(): Boolean

