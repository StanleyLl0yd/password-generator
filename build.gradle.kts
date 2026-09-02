buildscript {
    dependencies {
        // Keep AGP's JavaPoet classpath on the checksum-verified artifact.
        classpath(libs.javapoet)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
}
