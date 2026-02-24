# Keep Koin classes
-keep class org.koin.** { *; }

# Keep kotlinx.serialization classes
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.Serializable { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep our domain models to prevent JSON parsing issues
-keep class com.taptapboom.domain.model.** { *; }
