plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val generatedIconResDir = layout.buildDirectory.dir("generated/launcherIcons/res")

android {
    namespace = "com.hospitalfinder.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hospitalfinder.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") {
            res.srcDir(generatedIconResDir)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}

val generateLauncherIcons = tasks.register<GenerateLauncherIconsTask>("generateLauncherIcons") {
    sourceIcon.set(rootProject.file("icon.png"))
    outputResDir.set(generatedIconResDir)
}

tasks.named("preBuild") {
    dependsOn(generateLauncherIcons)
}