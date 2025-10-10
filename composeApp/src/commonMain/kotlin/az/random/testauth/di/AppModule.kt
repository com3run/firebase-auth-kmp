package az.random.testauth.di

import dev.com3run.firebaseauthkmp.AuthBackend
import dev.com3run.firebaseauthkmp.AuthRepository
import dev.com3run.firebaseauthkmp.platformAuthBackend
import az.random.testauth.ui.AuthViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 * Provides AuthBackend, AuthRepository, and AuthViewModel.
 */
val appModule = module {
    // Platform-specific AuthBackend (singleton)
    single<AuthBackend> { platformAuthBackend() }

    // AuthRepository (singleton)
    singleOf(::AuthRepository)

    // AuthViewModel (factory - creates new instance each time, works on all platforms)
    factoryOf(::AuthViewModel)
}
