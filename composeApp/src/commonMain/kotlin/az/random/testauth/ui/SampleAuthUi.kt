package az.random.testauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import az.random.testauth.auth.requestGoogleIdToken
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SampleAuthUi() {
    val viewModel = koinInject<AuthViewModel>()
    AuthScreen(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("test@example.com") }
    var password by remember { mutableStateOf("password123") }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firebase Auth Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auth State Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState) {
                        is AuthUiState.SignedIn -> MaterialTheme.colorScheme.primaryContainer
                        is AuthUiState.Error -> MaterialTheme.colorScheme.errorContainer
                        is AuthUiState.Success -> MaterialTheme.colorScheme.tertiaryContainer
                        is AuthUiState.Loading -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Auth Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    when (val state = uiState) {
                        is AuthUiState.SignedOut -> {
                            Text("Not signed in")
                        }
                        is AuthUiState.SignedIn -> {
                            Text("✓ Signed in as: ${state.displayName}")
                        }
                        is AuthUiState.Loading -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Text("Loading...")
                            }
                        }
                        is AuthUiState.Error -> {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is AuthUiState.Success -> {
                            Text(
                                text = "✓ ${state.message}",
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email & Password Section
            Text(
                text = "Email & Password Authentication",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text("Sign In")
                }

                OutlinedButton(
                    onClick = { showSignUpDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text("Sign Up")
                }
            }

            TextButton(
                onClick = { showPasswordResetDialog = true },
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text("Forgot Password?")
            }

            HorizontalDivider()

            // Social Authentication Section
            Text(
                text = "Social Authentication",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    scope.launch {
                        val idToken = requestGoogleIdToken()
                        if (idToken != null) {
                            viewModel.signInWithGoogle(idToken)
                        } else {
                            // Handle cancellation or error
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Sign in with Google")
            }

            Button(
                onClick = { /* TODO: Implement Apple Sign In */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Sign in with Apple")
            }

            HorizontalDivider()

            // Guest Section
            Text(
                text = "Guest Access",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { viewModel.signInAnonymously() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text("Continue as Guest")
            }

            HorizontalDivider()

            // Sign Out Section (only show when signed in)
            if (uiState is AuthUiState.SignedIn) {
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            }
        }
    }

    // Sign Up Dialog
    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = { showSignUpDialog = false },
            title = { Text("Sign Up") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Create a new account with:")
                    Text("Email: $email")
                    Text("Password: $password")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signUp(email, password)
                        showSignUpDialog = false
                    }
                ) {
                    Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignUpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Password Reset Dialog
    if (showPasswordResetDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your email to receive a password reset link:")
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordResetEmail(resetEmail)
                        showPasswordResetDialog = false
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
