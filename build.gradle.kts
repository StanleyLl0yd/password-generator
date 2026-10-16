buildscript {
    repositories {
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
            content { includeModule("com.android.tools", "r8") }
        }
    }
    dependencies {
        // Keep AGP's JavaPoet classpath on the checksum-verified artifact.
        classpath(libs.javapoet)
        // Patch transitive build tools without adding them to the Android runtime.
        classpath(libs.r8)
        classpath(platform(libs.netty.bom))
        classpath(libs.commons.compress)
        classpath(libs.jose4j)
        classpath(libs.bcprov)
        classpath(libs.bcpkix)
        classpath(libs.bcutil)
        classpath(libs.jdom)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
}
