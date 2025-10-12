import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "0.0.8"
}

group = "dev.com3run"
version = "1.0.0"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FirebaseAuthKMP"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.firebase.auth.ktx)
            implementation(libs.play.services.auth)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "dev.com3run.firebaseauthkmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    repositories {
        mavenLocal()
    }

    publications.withType<MavenPublication> {
        pom {
            name.set("Firebase Auth KMP")
            description.set("Firebase Authentication library for Kotlin Multiplatform (Android & iOS)")
            url.set("https://github.com/com3run/testauth")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("com3run")
                    name.set("Kamran Mammadov")
                    email.set("info@com3run.dev")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/com3run/testauth.git")
                developerConnection.set("scm:git:ssh://git@github.com:com3run/testauth.git")
                url.set("https://github.com/com3run/testauth")
            }
        }
    }
}

// Signing configuration for Maven Central
signing {
    // Only sign if credentials are available (won't break local builds)
    val signingKey = project.findProperty("signing.keyId") as String?
    if (signingKey != null) {
        sign(publishing.publications)
    }
}
