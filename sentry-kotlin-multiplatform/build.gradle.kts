import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 16
        targetSdk = 31
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    // linking the manifest file manually due to having it in the "androidMain" source set
    sourceSets.getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
    jvm()
    ios()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.sentry:sentry-android:6.3.1") {
                    // avoid duplicate dependencies since we depend on commonJvmMain
                    exclude("io.sentry", "sentry")
                }
            }
        }
        val androidTest by getting
        val jvmMain by getting
        val jvmTest by getting

        val commonJvmMain by creating {
            dependsOn(commonMain)
            jvmMain.dependsOn(this)
            androidMain.dependsOn(this)
            dependencies {
                implementation("io.sentry:sentry:6.3.1")
            }
        }
        val commonJvmTest by creating {
            dependsOn(commonTest)
            jvmTest.dependsOn(this)
            androidTest.dependsOn(this)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

        val iosMain by getting
        val iosSimulatorArm64Main by getting
        val iosTest by getting
        val iosSimulatorArm64Test by getting

        val commonIosMain by creating {
            iosMain.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val commonIosTest by creating {
            iosTest.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        val commonAppleMain by creating {
            dependsOn(commonMain)
            commonIosMain.dependsOn(this)
        }
        val commonAppleTest by creating {
            dependsOn(commonTest)
            commonIosTest.dependsOn(this)
        }

        cocoapods {
            summary = "Official Sentry SDK Kotlin Multiplatform"
            homepage = "https://github.com/getsentry/sentry-kotlin-multiplatform"

            pod("Sentry", "~> 7.24.1")

            ios.deploymentTarget = "9.0"
        }
    }

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations.getByName("main") {
            cinterops.create("Sentry.NSException") {
                includeDirs("$projectDir/src/nativeInterop/cinterop/SentryNSException")
            }
            cinterops.create("Sentry.Scope") {
                includeDirs("$projectDir/src/nativeInterop/cinterop/SentryScope")
            }
        }
    }

    // workaround for https://youtrack.jetbrains.com/issue/KT-41709 due to having "Meta" in the class name
    // if we need to use this class, we'd need to find a better way to work it out
    targets.withType<KotlinNativeTarget>().all {
        compilations["main"].cinterops["Sentry"].extraOpts(
            "-compiler-option",
            "-DSentryMechanismMeta=SentryMechanismMetaUnavailable"
        )
    }
}
