import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { stream ->
        keystoreProperties.load(stream)
    }
}

val isReleaseBuild = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true)
}
if (isReleaseBuild && !keystorePropertiesFile.exists()) {
    throw GradleException(
        "Release signing requires key.properties. " +
            "Use the Android Release workflow or configure a local keystore.",
    )
}


val launcherIconSource = rootProject.file("artwork/app-icon.png")
val generatedLauncherRes = layout.buildDirectory.dir("generated/launcher-icons/res")
val launcherIconSizes = mapOf(
    "mdpi" to 48,
    "hdpi" to 72,
    "xhdpi" to 96,
    "xxhdpi" to 144,
    "xxxhdpi" to 192,
)

val generateLauncherIcons = tasks.register("generateLauncherIcons") {
    inputs.file(launcherIconSource)
    outputs.dir(generatedLauncherRes)

    doLast {
        val source = ImageIO.read(launcherIconSource)
            ?: throw GradleException("Could not decode launcher icon source: $launcherIconSource")
        val outputRoot = generatedLauncherRes.get().asFile
        outputRoot.deleteRecursively()

        fun writeIcon(target: File, size: Int) {
            target.parentFile.mkdirs()
            val scaled = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val graphics = scaled.createGraphics()
            try {
                graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                )
                graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY,
                )
                graphics.drawImage(source, 0, 0, size, size, null)
            } finally {
                graphics.dispose()
            }

            if (!ImageIO.write(scaled, "png", target)) {
                throw GradleException("Could not write launcher icon: $target")
            }
        }

        writeIcon(
            File(outputRoot, "drawable-nodpi/ic_launcher_foreground.png"),
            512,
        )
        launcherIconSizes.forEach { (density, size) ->
            writeIcon(File(outputRoot, "mipmap-$density/ic_launcher.png"), size)
            writeIcon(File(outputRoot, "mipmap-$density/ic_launcher_round.png"), size)
        }
    }
}

android {
    namespace = "com.sl.passwordgenerator"
    compileSdk = 36

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.sl.passwordgenerator"
        minSdk = 26
        targetSdk = 36
        versionCode = 17
        versionName = "1.5.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main").res.srcDir(generatedLauncherRes)
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateLauncherIcons)
}

configure<DetektExtension> {
    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = false
    baseline = rootProject.file("config/detekt/baseline.xml")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
