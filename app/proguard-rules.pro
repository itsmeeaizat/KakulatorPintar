# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve Line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Keep Data Models & Entities (Moshi, Room, API)
-keep class com.example.data.** { *; }
-keep class com.example.api.** { *; }

# Moshi rules
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep @com.squareup.moshi.JsonClass class * { *; }

# Retrofit rules
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers enum * { *; }

# Keep Compose internal state & previews
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

