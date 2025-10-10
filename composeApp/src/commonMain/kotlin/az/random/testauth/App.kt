package az.random.testauth

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import az.random.testauth.di.appModule
import az.random.testauth.ui.AuthScreen
import az.random.testauth.ui.AuthViewModel
import az.random.testauth.ui.SampleAuthUi
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinApplication(
        application = {
            modules(appModule)
        }
    ) {
        MaterialTheme {
            SampleAuthUi()
        }
    }
}