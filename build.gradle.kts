plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy {
            // Keep Hilt's annotation processor on a compatible JavaPoet release.
            force("com.squareup:javapoet:1.13.0")
        }
    }
}
