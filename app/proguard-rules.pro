# Add project specific ProGuard rules here.

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses, Signature
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class **$companion { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep our data models intact
-keep class com.kaizen.khushu.data.entity.** { *; }
-keep class com.kaizen.khushu.data.model.** { *; }

# Haze (Blur effect library)
-keep class dev.chrisbanes.haze.** { *; }

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep @retrofit2.http.* interface * { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# AlAdhan API Models (in PrayerTimeRepository)
-keep class com.kaizen.khushu.data.repository.AlAdhanResponse { *; }
-keep class com.kaizen.khushu.data.repository.AlAdhanData { *; }