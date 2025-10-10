# Firebase Auth Library - Usage Examples

Complete examples showing how to use every feature of the Firebase Auth library.

## Table of Contents
- [Setup](#setup)
- [Authentication](#authentication)
- [Password Management](#password-management)
- [Email Verification](#email-verification)
- [Profile Management](#profile-management)
- [Account Linking](#account-linking)
- [Re-authentication](#re-authentication)
- [ViewModel Example](#viewmodel-example)
- [Compose UI Example](#compose-ui-example)

## Setup

```kotlin
import az.random.testauth.auth.*

// Create auth repository (typically in your DI setup)
val authRepository = AuthRepository(
    backend = platformAuthBackend()
)
```

## Authentication

### Email & Password Sign Up

```kotlin
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    suspend fun signUpWithEmail(email: String, password: String) {
        when (val result = authRepository.signUpWithEmailAndPassword(email, password)) {
            is AuthResult.Success -> {
                // User signed up successfully
                val user = result.user
                println("Welcome ${user.email}!")

                // Send verification email
                authRepository.sendEmailVerification()
            }
            is AuthResult.Failure -> {
                when (result.error) {
                    AuthError.EmailAlreadyInUse -> {
                        showError("This email is already registered")
                    }
                    AuthError.WeakPassword -> {
                        showError("Password must be at least 6 characters")
                    }
                    AuthError.InvalidEmail -> {
                        showError("Please enter a valid email address")
                    }
                    is AuthError.Network -> {
                        showError("Network error. Please check your connection")
                    }
                    else -> {
                        showError("Sign up failed. Please try again")
                    }
                }
            }
        }
    }
}
```

### Email & Password Sign In

```kotlin
suspend fun signInWithEmail(email: String, password: String) {
    when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
        is AuthResult.Success -> {
            val user = result.user

            // Check if email is verified
            if (!user.isEmailVerified) {
                showWarning("Please verify your email address")
                // Optionally resend verification
                authRepository.sendEmailVerification()
            } else {
                navigateToHome()
            }
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.UserNotFound -> {
                    showError("No account found with this email")
                }
                AuthError.WrongPassword -> {
                    showError("Incorrect password")
                }
                AuthError.UserDisabled -> {
                    showError("This account has been disabled")
                }
                AuthError.TooManyRequests -> {
                    showError("Too many failed attempts. Please try again later")
                }
                else -> {
                    showError("Sign in failed. Please try again")
                }
            }
        }
    }
}
```

### Anonymous Sign In

```kotlin
suspend fun signInAsGuest() {
    when (val result = authRepository.signInAnonymously()) {
        is AuthResult.Success -> {
            println("Signed in as guest: ${result.user.uid}")
            navigateToHome()
        }
        is AuthResult.Failure -> {
            showError("Failed to sign in as guest")
        }
    }
}

// Later, convert anonymous account to permanent
suspend fun convertAnonymousToEmail(email: String, password: String) {
    when (val result = authRepository.linkWithEmailAndPassword(email, password)) {
        is AuthResult.Success -> {
            showMessage("Account created successfully!")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.EmailAlreadyInUse -> {
                    showError("This email is already registered")
                }
                else -> {
                    showError("Failed to create account")
                }
            }
        }
    }
}
```

### Google Sign In

```kotlin
// Android
suspend fun signInWithGoogle(activity: Activity) {
    // Get ID token from Google Sign-In
    val idToken = requestGoogleIdToken() ?: run {
        showError("Google sign in cancelled")
        return
    }

    when (val result = authRepository.signInWithGoogle(idToken)) {
        is AuthResult.Success -> {
            val user = result.user
            println("Signed in with Google: ${user.email}")
            navigateToHome()
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.InvalidCredential -> {
                    showError("Invalid Google credentials")
                }
                is AuthError.Network -> {
                    showError("Network error. Please try again")
                }
                else -> {
                    showError("Google sign in failed")
                }
            }
        }
    }
}
```

### Apple Sign In

```kotlin
// iOS
suspend fun signInWithApple() {
    val idToken = requestAppleIdToken() ?: run {
        showError("Apple sign in cancelled")
        return
    }

    when (val result = authRepository.signInWithApple(idToken)) {
        is AuthResult.Success -> {
            println("Signed in with Apple: ${result.user.uid}")
            navigateToHome()
        }
        is AuthResult.Failure -> {
            showError("Apple sign in failed")
        }
    }
}
```

## Password Management

### Send Password Reset Email

```kotlin
suspend fun forgotPassword(email: String) {
    // Show loading
    _isLoading.value = true

    when (val result = authRepository.sendPasswordResetEmail(email)) {
        is AuthResult.Success -> {
            showMessage("Password reset email sent! Check your inbox")
            navigateToSignIn()
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.UserNotFound -> {
                    showError("No account found with this email")
                }
                AuthError.InvalidEmail -> {
                    showError("Please enter a valid email address")
                }
                is AuthError.Network -> {
                    showError("Network error. Please try again")
                }
                else -> {
                    showError("Failed to send reset email")
                }
            }
        }
    }

    _isLoading.value = false
}
```

### Update Password (While Signed In)

```kotlin
suspend fun changePassword(
    currentEmail: String,
    currentPassword: String,
    newPassword: String
) {
    // First, re-authenticate for security
    when (val reauth = authRepository.reauthenticateWithEmail(currentEmail, currentPassword)) {
        is AuthResult.Success -> {
            // Now update password
            when (val result = authRepository.updatePassword(newPassword)) {
                is AuthResult.Success -> {
                    showMessage("Password updated successfully")
                }
                is AuthResult.Failure -> {
                    when (result.error) {
                        AuthError.WeakPassword -> {
                            showError("New password is too weak")
                        }
                        else -> {
                            showError("Failed to update password")
                        }
                    }
                }
            }
        }
        is AuthResult.Failure -> {
            showError("Current password is incorrect")
        }
    }
}
```

## Email Verification

### Send Verification Email

```kotlin
suspend fun sendVerificationEmail() {
    when (val result = authRepository.sendEmailVerification()) {
        is AuthResult.Success -> {
            showMessage("Verification email sent! Please check your inbox")
        }
        is AuthResult.Failure -> {
            showError("Failed to send verification email")
        }
    }
}
```

### Check Email Verification Status

```kotlin
fun observeEmailVerification() {
    viewModelScope.launch {
        authRepository.authState.collect { user ->
            if (user != null && !user.isEmailVerified) {
                showBanner("Please verify your email address")
            }
        }
    }
}

// Manually refresh verification status
suspend fun refreshUserData() {
    when (authRepository.reloadUser()) {
        is AuthResult.Success -> {
            // Auth state will be updated automatically
        }
        is AuthResult.Failure -> {
            // Handle error
        }
    }
}
```

## Profile Management

### Update Display Name and Photo

```kotlin
suspend fun updateUserProfile(name: String, photoUrl: String? = null) {
    when (val result = authRepository.updateProfile(
        displayName = name,
        photoUrl = photoUrl
    )) {
        is AuthResult.Success -> {
            val user = result.user
            println("Profile updated: ${user.displayName}")
            showMessage("Profile updated successfully")
        }
        is AuthResult.Failure -> {
            showError("Failed to update profile")
        }
    }
}
```

### Update Email Address

```kotlin
suspend fun updateEmailAddress(
    currentPassword: String,
    newEmail: String
) {
    // Re-authenticate first
    val currentUser = authRepository.authState.value
    val currentEmail = currentUser?.email ?: return

    when (authRepository.reauthenticateWithEmail(currentEmail, currentPassword)) {
        is AuthResult.Success -> {
            // Now update email
            when (val result = authRepository.updateEmail(newEmail)) {
                is AuthResult.Success -> {
                    showMessage("Email updated. Please verify your new email")
                    authRepository.sendEmailVerification()
                }
                is AuthResult.Failure -> {
                    when (result.error) {
                        AuthError.EmailAlreadyInUse -> {
                            showError("This email is already in use")
                        }
                        AuthError.InvalidEmail -> {
                            showError("Invalid email address")
                        }
                        else -> {
                            showError("Failed to update email")
                        }
                    }
                }
            }
        }
        is AuthResult.Failure -> {
            showError("Incorrect password")
        }
    }
}
```

### Delete Account

```kotlin
suspend fun deleteUserAccount(password: String) {
    // Show confirmation dialog first
    showConfirmDialog(
        title = "Delete Account",
        message = "Are you sure? This action cannot be undone.",
        onConfirm = {
            viewModelScope.launch {
                deleteAccountConfirmed(password)
            }
        }
    )
}

private suspend fun deleteAccountConfirmed(password: String) {
    // Re-authenticate first
    val currentUser = authRepository.authState.value
    val email = currentUser?.email

    if (email != null) {
        when (authRepository.reauthenticateWithEmail(email, password)) {
            is AuthResult.Success -> {
                // Delete account
                when (val result = authRepository.deleteAccount()) {
                    is AuthResult.Success -> {
                        showMessage("Account deleted successfully")
                        navigateToWelcome()
                    }
                    is AuthResult.Failure -> {
                        showError("Failed to delete account")
                    }
                }
            }
            is AuthResult.Failure -> {
                showError("Incorrect password")
            }
        }
    }
}
```

## Account Linking

### Link Google Account

```kotlin
suspend fun linkGoogleToCurrentAccount() {
    val idToken = requestGoogleIdToken() ?: return

    when (val result = authRepository.linkWithGoogle(idToken)) {
        is AuthResult.Success -> {
            showMessage("Google account linked successfully")
            // Now user can sign in with Google
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.ProviderAlreadyLinked -> {
                    showError("Google account is already linked")
                }
                AuthError.RequiresRecentLogin -> {
                    showError("Please sign in again to link account")
                    // Prompt re-authentication
                }
                else -> {
                    showError("Failed to link Google account")
                }
            }
        }
    }
}
```

### Link Email/Password to Anonymous Account

```kotlin
suspend fun linkEmailPasswordToAnonymous(email: String, password: String) {
    when (val result = authRepository.linkWithEmailAndPassword(email, password)) {
        is AuthResult.Success -> {
            showMessage("Account created! You can now sign in with email")
            authRepository.sendEmailVerification()
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.EmailAlreadyInUse -> {
                    showError("Email already in use by another account")
                }
                AuthError.WeakPassword -> {
                    showError("Password is too weak")
                }
                else -> {
                    showError("Failed to link account")
                }
            }
        }
    }
}
```

### Unlink Provider

```kotlin
suspend fun unlinkGoogleAccount() {
    // Check that user has multiple providers
    val user = authRepository.authState.value
    if (user?.providerData?.size ?: 0 <= 1) {
        showError("Cannot unlink. You need at least one sign-in method")
        return
    }

    when (val result = authRepository.unlinkProvider("google.com")) {
        is AuthResult.Success -> {
            showMessage("Google account unlinked")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.NoSuchProvider -> {
                    showError("Google account is not linked")
                }
                else -> {
                    showError("Failed to unlink account")
                }
            }
        }
    }
}
```

### Show Linked Providers

```kotlin
@Composable
fun LinkedProvidersSection(user: AuthUser) {
    Column {
        Text("Connected Accounts:", style = MaterialTheme.typography.titleMedium)

        user.providerData.forEach { providerId ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val providerName = when (providerId) {
                    "google.com" -> "Google"
                    "apple.com" -> "Apple"
                    "facebook.com" -> "Facebook"
                    "password" -> "Email/Password"
                    else -> providerId
                }

                Icon(getProviderIcon(providerId), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(providerName)
                Spacer(Modifier.weight(1f))

                // Show unlink button if more than one provider
                if (user.providerData.size > 1) {
                    TextButton(onClick = { unlinkProvider(providerId) }) {
                        Text("Unlink")
                    }
                }
            }
        }
    }
}
```

## Re-authentication

Re-authentication is required for sensitive operations like changing password, updating email, or deleting account.

```kotlin
suspend fun performSensitiveOperation(
    operation: suspend () -> AuthResult
) {
    // Try the operation first
    when (val result = operation()) {
        is AuthResult.Success -> {
            // Success
        }
        is AuthResult.Failure -> {
            if (result.error is AuthError.RequiresRecentLogin) {
                // Prompt user to re-authenticate
                showReauthenticationDialog()
            } else {
                showError("Operation failed")
            }
        }
    }
}

suspend fun reauthenticate(email: String, password: String): Boolean {
    return when (authRepository.reauthenticateWithEmail(email, password)) {
        is AuthResult.Success -> true
        is AuthResult.Failure -> {
            showError("Incorrect password")
            false
        }
    }
}
```

## ViewModel Example

Complete ViewModel showing best practices:

```kotlin
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Expose auth state
    val authState = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = authRepository.signUpWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    // Send verification email
                    authRepository.sendEmailVerification()
                }
                is AuthResult.Failure -> {
                    _error.value = result.error.toUserMessage()
                }
            }

            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    // Navigation handled by observing authState
                }
                is AuthResult.Failure -> {
                    _error.value = result.error.toUserMessage()
                }
            }

            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun AuthError.toUserMessage(): String = when (this) {
        AuthError.EmailAlreadyInUse -> "Email already in use"
        AuthError.WeakPassword -> "Password is too weak"
        AuthError.UserNotFound -> "User not found"
        AuthError.WrongPassword -> "Incorrect password"
        AuthError.InvalidEmail -> "Invalid email address"
        AuthError.UserDisabled -> "Account disabled"
        AuthError.TooManyRequests -> "Too many attempts. Try again later"
        is AuthError.Network -> "Network error: ${this.message}"
        else -> "Authentication failed"
    }
}
```

## Compose UI Example

```kotlin
@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Navigate when authenticated
    LaunchedEffect(authState) {
        if (authState != null) {
            // Navigate to home
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text("Sign In", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signIn(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { /* Navigate to sign up */ }) {
            Text("Don't have an account? Sign Up")
        }

        TextButton(onClick = { /* Navigate to forgot password */ }) {
            Text("Forgot Password?")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { /* Handle Google Sign In */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.signInAnonymously() },
            enabled = !isLoading
        ) {
            Text("Continue as Guest")
        }
    }
}
```

## Provider IDs Reference

```kotlin
object AuthProviders {
    const val EMAIL = "password"
    const val GOOGLE = "google.com"
    const val APPLE = "apple.com"
    const val FACEBOOK = "facebook.com"
}
```
