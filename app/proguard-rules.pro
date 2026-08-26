-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @javax.inject.Singleton class * { *; }

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class androidx.datastore.** { *; }

-keep class com.sl.passwordgenerator.domain.model.** { *; }

-dontwarn androidx.compose.**

# Preserve source locations in obfuscated stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
