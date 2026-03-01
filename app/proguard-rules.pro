# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @javax.inject.Singleton class * { *; }

# ── Kotlin / Coroutines ───────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Domain models (data classes used across layers) ──────────────────────────
-keep class com.sl.passwordgenerator.domain.model.** { *; }

# ── Compose — suppress known R8 warnings ──────────────────────────────────────
-dontwarn androidx.compose.**

# ── General ───────────────────────────────────────────────────────────────────
# Preserve line numbers in stack traces for easier debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile