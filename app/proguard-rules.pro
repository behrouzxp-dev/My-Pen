# My Pen — R8/ProGuard rules for release builds.
#
# NOTE: `isMinifyEnabled` is currently false in app/build.gradle.kts.
# Enable R8 only after validating the release build on a device; this file
# documents the keeps that are expected to be required then.

# Keep line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable

# Keep Kotlin metadata (used by reflection in some libraries)
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keep class kotlin.Metadata { *; }

# Backup/restore models are (de)serialized manually via org.json; keep them
# defensively so reflection-based tools keep working
-keep class com.arena.mypen.model.** { *; }

# WorkManager instantiates the worker via its no-arg constructor reflection
-keep class com.arena.mypen.notification.ExpiryCheckWorker { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
