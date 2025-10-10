# Sample UI Guide

The Firebase Auth Demo app provides a comprehensive testing interface for all authentication methods supported by the library.

## Features

### 📱 Real-Time Auth Status Card
- **Color-coded status indicator**:
  - 🟦 Blue (Primary) - Signed in
  - 🟥 Red (Error) - Error occurred
  - 🟨 Yellow (Secondary) - Loading
  - 🟩 Green (Tertiary) - Success message
  - ⚪ Gray (Surface) - Signed out

### 🔐 Email & Password Authentication

**Features:**
- Email and password input fields
- **Sign In** button - Sign in with existing account
- **Sign Up** button - Create new account (opens confirmation dialog)
- **Forgot Password?** link - Send password reset email

**Default Test Credentials:**
- Email: `test@example.com`
- Password: `password123`

**How It Works:**
1. Enter email and password
2. Click "Sign In" to authenticate existing user
3. Click "Sign Up" to create new account (opens dialog)
4. Click "Forgot Password?" to send reset email (opens dialog)

### 🌐 Social Authentication

**Google Sign In:**
- Button to initiate Google OAuth flow
- Currently marked as TODO (requires platform-specific implementation)

**Apple Sign In:**
- Button to initiate Apple Sign In flow
- Currently marked as TODO (requires platform-specific implementation)

### 👤 Guest Access

**Continue as Guest:**
- ✅ **Fully Implemented**
- Sign in anonymously without credentials
- Perfect for trying out the app
- Can later convert guest account to permanent account

### 🚪 Sign Out

- Only visible when user is signed in
- Red button at the bottom
- Signs out current user

## UI Components

### Auth State Display

```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = when (uiState) {
            is AuthUiState.SignedIn -> primaryContainer
            is AuthUiState.Error -> errorContainer
            is AuthUiState.Success -> tertiaryContainer
            is AuthUiState.Loading -> secondaryContainer
            else -> surfaceVariant
        }
    )
)
```

### States:
- **SignedOut** - Default state
- **SignedIn(displayName)** - User authenticated
- **Loading** - Processing request
- **Error(message)** - Error with user-friendly message
- **Success(message)** - Success feedback (e.g., password reset sent)

## ViewModel Integration

The UI uses Koin for dependency injection:

```kotlin
@Composable
fun SampleAuthUi() {
    val viewModel = koinViewModel<AuthViewModel>()
    AuthScreen(viewModel)
}
```

### Available ViewModel Methods:
- `signIn(email, password)` - Email/password sign in
- `signUp(email, password)` - Email/password sign up
- `signInAnonymously()` - Guest sign in
- `sendPasswordResetEmail(email)` - Request password reset
- `signOut()` - Sign out current user

## Testing the App

### 1. Anonymous Sign In (Easiest)
1. Launch the app
2. Click "Continue as Guest"
3. Status card turns blue: "✓ Signed in as: Unknown"
4. Sign out button appears

### 2. Email Sign Up
1. Enter a valid email format
2. Enter a password (minimum 6 characters)
3. Click "Sign Up"
4. Confirm in dialog
5. Account created and automatically signed in

### 3. Email Sign In
1. Enter email and password
2. Click "Sign In"
3. If credentials are correct, you're signed in
4. Error messages appear for:
   - Wrong password
   - User not found
   - Invalid email
   - Account disabled
   - Too many requests

### 4. Password Reset
1. Click "Forgot Password?"
2. Enter email in dialog
3. Click "Send Reset Link"
4. Success message appears in green card
5. Check email for reset link

### 5. Sign Out
1. While signed in, scroll to bottom
2. Click red "Sign Out" button
3. Returns to signed out state

## Error Handling

All errors are displayed with user-friendly messages:

| Error | Message |
|-------|---------|
| `InvalidCredential` | Invalid credentials |
| `InvalidEmailOrPassword` | Invalid email or password |
| `UserNotFound` | User not found |
| `WrongPassword` | Incorrect password |
| `UserDisabled` | This account has been disabled |
| `TooManyRequests` | Too many attempts. Try again later |
| `InvalidEmail` | Invalid email address |
| `EmailAlreadyInUse` | Email already in use |
| `WeakPassword` | Password is too weak |
| `Network` | Network error |

## UI Layout

```
┌─────────────────────────────────────┐
│     Firebase Auth Demo (Header)    │
├─────────────────────────────────────┤
│ Auth Status Card (Color-coded)     │
│ ✓ Signed in as: [name] / Error /   │
│   Loading... / Not signed in       │
├─────────────────────────────────────┤
│ Email & Password Authentication    │
│ ┌─────────────────────────────────┐ │
│ │ Email: test@example.com        │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Password: ••••••••••••         │ │
│ └─────────────────────────────────┘ │
│ ┌────────────┐ ┌─────────────────┐ │
│ │  Sign In   │ │    Sign Up     │ │
│ └────────────┘ └─────────────────┘ │
│         Forgot Password?            │
├─────────────────────────────────────┤
│ Social Authentication               │
│ ┌─────────────────────────────────┐ │
│ │   Sign in with Google          │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │   Sign in with Apple           │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ Guest Access                        │
│ ┌─────────────────────────────────┐ │
│ │    Continue as Guest           │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ (When signed in)                    │
│ ┌─────────────────────────────────┐ │
│ │  Sign Out (Red)                │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Customization

### Change Default Credentials
```kotlin
var email by remember { mutableStateOf("your@email.com") }
var password by remember { mutableStateOf("yourpassword") }
```

### Add More Auth Methods
The UI has placeholders for Google and Apple sign-in. To implement:

1. **For Google**:
```kotlin
Button(onClick = {
    viewModelScope.launch {
        val idToken = requestGoogleIdToken()
        idToken?.let {
            authRepository.signInWithGoogle(it)
        }
    }
})
```

2. **For Apple**:
```kotlin
Button(onClick = {
    viewModelScope.launch {
        val idToken = requestAppleIdToken()
        idToken?.let {
            authRepository.signInWithApple(it)
        }
    }
})
```

### Styling
All UI uses Material3 theming. Colors automatically adapt to:
- Light/Dark mode
- System color scheme
- Material You (Android 12+)

## Dialogs

### Sign Up Confirmation Dialog
- Shows email and password to be used
- "Create Account" button - Confirms and creates account
- "Cancel" button - Closes dialog

### Password Reset Dialog
- Editable email field (pre-filled with current email)
- "Send Reset Link" button - Sends reset email
- "Cancel" button - Closes dialog

## Best Practices

1. **Always handle loading states** - Disable buttons during operations
2. **Provide clear feedback** - Use color-coded status cards
3. **Validate input** - Check email format and password length
4. **Show helpful errors** - Map technical errors to user-friendly messages
5. **Remember state** - Save email between attempts
6. **Confirm destructive actions** - Use dialogs for sign up, password reset

## Running the Sample

### Android
```bash
./gradlew :composeApp:assembleDebug
# Or click Run in Android Studio
```

### iOS
```bash
# Open iosApp in Xcode
# Click Run (⌘R)
```

## Next Steps

To use this in your own project:
1. Copy `SampleAuthUi.kt` as a starting point
2. Customize the UI to match your app's design
3. Add navigation after successful sign in
4. Implement Google/Apple sign-in flows
5. Add email verification flow
6. Add profile management screens

## Troubleshooting

**"Email already in use"**
- Use a different email or sign in with existing account

**"Weak password"**
- Firebase requires minimum 6 characters
- Use stronger passwords in production

**"No user found"**
- Account doesn't exist, use Sign Up instead

**"Network error"**
- Check internet connection
- Verify Firebase configuration

**UI not updating**
- ViewModel observes `authRepository.authState`
- Updates happen automatically via Flow

## Demo Video Script

1. **Show signed out state**
   - Gray card: "Not signed in"

2. **Test anonymous sign in**
   - Click "Continue as Guest"
   - Loading spinner appears
   - Blue card: "✓ Signed in as: Unknown"
   - Sign out button appears

3. **Test sign up**
   - Click Sign Out
   - Enter email and password
   - Click Sign Up
   - Confirm in dialog
   - Blue card: "✓ Signed in as: [email]"

4. **Test password reset**
   - Click Forgot Password
   - Enter email
   - Click Send Reset Link
   - Green card: "✓ Password reset email sent!"

5. **Test sign in with wrong password**
   - Sign out
   - Enter email with wrong password
   - Click Sign In
   - Red card: "Error: Incorrect password"

6. **Test sign in successfully**
   - Enter correct password
   - Click Sign In
   - Blue card: "✓ Signed in as: [email]"

This comprehensive sample UI demonstrates all core features of your Firebase Auth library!
