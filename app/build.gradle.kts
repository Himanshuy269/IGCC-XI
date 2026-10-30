plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val vCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 10304
val vName = project.findProperty("versionName")?.toString() ?: "1.3.4"

android {
    namespace = "com.example.igccxi"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.igccxi"
        minSdk = 24
        targetSdk = 35
        versionCode = vCode
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreFile = file("keystore/release.jks")
    if (keystoreFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = keystoreFile
                storePassword = "igccxicricketpass"
                keyAlias = "igccxikey"
                keyPassword = "igccxicricketpass"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    buildTypes {
        release {
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
        }
        debug {
            // Also sign debug builds with the proper release key so testers don't get 'testOnly' / Play Protect blocked warnings
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

}

// Rename release APK to app name — configuration-cache compatible
val renameReleaseApk by tasks.registering {
    // Use lazy Providers — no Project reference captured (config-cache safe)
    val outputDir = layout.buildDirectory.dir("outputs/apk/release")
    val targetName = "IGCC-XI-v${vName}.apk"
    outputs.upToDateWhen { false }
    doLast {
        val dir = outputDir.get().asFile
        dir.listFiles()
            ?.filter { it.extension == "apk" && it.name != targetName }
            ?.forEach { apk ->
                apk.renameTo(File(dir, targetName))
                println("Renamed APK: ${apk.name} → $targetName")
            }
    }
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy(renameReleaseApk)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)

    // JSON Persistence for offline cache
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
