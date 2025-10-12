plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("com.gradleup.nmcp") version "0.0.8"
}

nmcp {
    publishAllProjectsProbablyBreakingProjectIsolation {
        username = project.findProperty("centralPortalUsername") as String? ?: System.getenv("CENTRAL_PORTAL_USERNAME")
        password = project.findProperty("centralPortalPassword") as String? ?: System.getenv("CENTRAL_PORTAL_PASSWORD")
        publicationType = "USER_MANAGED"
    }
}