import java.io.File
import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.AlphaComposite

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
        // No release signing config is set up — this build is not yet
        // intended for distribution/updates. It uses Android's
        // auto-generated debug keystore, which is fine for personal use
        // and sideloading but should NOT be used for public release
        // (the debug key is not a private, unique identity).
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

/**
 * Generates every required launcher-icon density from the root-level
 * icon.png automatically at build time — legacy raster mipmaps (square
 * + round-masked) for API levels that need them, plus a correctly
 * inset adaptive-icon foreground for API 26+. No manual per-density
 * asset creation is required; icon.png itself is never modified.
 */
abstract class GenerateLauncherIconsTask : DefaultTask() {

    @get:org.gradle.api.tasks.InputFile
    abstract val sourceIcon: RegularFileProperty

    @get:org.gradle.api.tasks.OutputDirectory
    abstract val outputResDir: DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun generate() {
        val srcFile = sourceIcon.get().asFile
        if (!srcFile.exists()) {
            throw GradleException(
                "icon.png not found at project root (${srcFile.path}). " +
                "Place a single square PNG named icon.png in the repository root."
            )
        }
        val source = ImageIO.read(srcFile)
            ?: throw GradleException("Could not read icon.png — is it a valid PNG?")

        val outDir = outputResDir.get().asFile
        outDir.deleteRecursively()
        outDir.mkdirs()

        val legacySizes = mapOf(
            "mipmap-mdpi" to 48,
            "mipmap-hdpi" to 72,
            "mipmap-xhdpi" to 96,
            "mipmap-xxhdpi" to 144,
            "mipmap-xxxhdpi" to 192
        )
        legacySizes.forEach { (dir, size) ->
            val folder = File(outDir, dir).apply { mkdirs() }
            writeSquare(source, size, File(folder, "ic_launcher.png"))
            writeRound(source, size, File(folder, "ic_launcher_round.png"))
        }

        val adaptiveSizes = mapOf(
            "mipmap-mdpi" to 108,
            "mipmap-hdpi" to 162,
            "mipmap-xhdpi" to 216,
            "mipmap-xxhdpi" to 324,
            "mipmap-xxxhdpi" to 432
        )
        adaptiveSizes.forEach { (dir, size) ->
            val folder = File(outDir, dir).apply { mkdirs() }
            writeInsetForeground(source, size, File(folder, "ic_launcher_foreground.png"))
        }
    }

    private fun scaledSquareImage(source: java.awt.image.BufferedImage, size: Int): java.awt.image.BufferedImage {
        val out = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(source, 0, 0, size, size, null)
        g.dispose()
        return out
    }

    private fun writeSquare(source: java.awt.image.BufferedImage, size: Int, dest: File) {
        ImageIO.write(scaledSquareImage(source, size), "png", dest)
    }

    private fun writeRound(source: java.awt.image.BufferedImage, size: Int, dest: File) {
        val square = scaledSquareImage(source, size)
        val round = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = round.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.clip = Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
        g.drawImage(square, 0, 0, null)
        g.dispose()
        ImageIO.write(round, "png", dest)
    }

    private fun writeInsetForeground(source: java.awt.image.BufferedImage, canvasSize: Int, dest: File) {
        val contentSize = (canvasSize * 0.66f).toInt()
        val offset = (canvasSize - contentSize) / 2
        val out = java.awt.image.BufferedImage(canvasSize, canvasSize, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.composite = AlphaComposite.SrcOver
        g.drawImage(source, offset, offset, contentSize, contentSize, null)
        g.dispose()
        ImageIO.write(out, "png", dest)
    }
}

val generateLauncherIcons = tasks.register<GenerateLauncherIconsTask>("generateLauncherIcons") {
    sourceIcon.set(rootProject.file("icon.png"))
    outputResDir.set(generatedIconResDir)
}

tasks.named("preBuild") {
    dependsOn(generateLauncherIcons)
}