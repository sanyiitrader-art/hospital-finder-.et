# Keep osmdroid's configuration & tile-source classes (reflection-based init)
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Keep ViewBinding generated classes
-keep class com.hospitalfinder.app.databinding.** { *; }

# Standard Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable