import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

val ksPath = System.getenv("KEYSTORE_PATH") ?: keystoreProps.getProperty("storeFile")
val ksPassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProps.getProperty("storePassword")
val ksKeyAlias = System.getenv("KEY_ALIAS") ?: keystoreProps.getProperty("keyAlias")
val ksKeyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps.getProperty("keyPassword")
val hasReleaseKeystore = !ksPath.isNullOrBlank() && rootProject.file(ksPath).exists() && !ksPassword.isNullOrBlank()

android {
    namespace = "com.puretube.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.puretube.player"
        minSdk = 24
        targetSdk = 35
        // Version format: X.X.XXX (increment the 3-digit patch number unless 1st/2nd part is explicitly requested)
        versionCode = 15
        versionName = "0.3.012"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    dependenciesInfo {
        // Disables Google dependency metadata blob for F-Droid & reproducible FOSS builds
        includeInApk = false
        includeInBundle = false
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("puretube") {
                storeFile = rootProject.file(ksPath!!)
                storePassword = ksPassword
                keyAlias = ksKeyAlias
                keyPassword = ksKeyPassword ?: ksPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("puretube") else signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("puretube") else signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Media3 ExoPlayer & MediaSession
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // NewPipeExtractor & OkHttp & Coil
    implementation(libs.newpipe.extractor)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
