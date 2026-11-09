# My Pen — R8/ProGuard rules for release builds.
# R8 code shrinking, obfuscation, optimization, and resource shrinking are
# enabled (`isMinifyEnabled = true`, `isShrinkResources = true`) in release builds.

# Keep line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable

# Keep Kotlin metadata (used by reflection in some libraries)
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keep class kotlin.Metadata { *; }

# Backup/restore models and enums are (de)serialized via org.json; keep them
# so enum values and model members remain intact
-keep class com.mypen.app.model.** { *; }

# WorkManager instantiates the worker via its (Context, WorkerParameters) constructor reflection
-keep class com.mypen.app.notification.ExpiryCheckWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
    *;
}

# App widget provider is instantiated by the system through the manifest
-keep class com.mypen.app.widget.** { *; }
-keep class com.mypen.app.notification.NotificationActionReceiver { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
