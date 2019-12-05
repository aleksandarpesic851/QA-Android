# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\India\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# Column and Table aren't existing java class file attributes. You'll at least have to specify Annotation


-keepattributes *Annotation*



# Basic ProGuard rules for Picasso Image Library
-dontwarn com.squareup.okhttp.**

# Basic ProGuard rules for Google Play Service
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keep class com.google.android.gms.internal.** { *; }
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}

-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}

-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}


-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}

-dontwarn android.security.NetworkSecurityPolicy

# Basic ProGuard rules for Facebook
-keep class com.facebook.** {
   *;
}

# Basic ProGuard rules for Firebase Android SDK 2.0.0+
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**


-keep class com.parse.*{ *; }
-dontwarn com.parse.**

# Basic ProGuard rules for Active Android Library
-keep class com.activeandroid.**{*;}
-keep public class com.visualphysics.database.**{*;}
-keepattributes Column
-keepattributes Table

# Basic ProGuard rules for Wasabi
-keepclasseswithmembers class * {
    com.intertrust.wasabi.* <methods>;
}
-keepclasseswithmembers interface * {
    com.intertrust.wasabi.* <methods>;
}

-keep class com.intertrust.wasabi.** { *; }
-keep interface com.intertrust.wasabi.* { *; }

-keep class Utils.SharedPrefrences
-keepclassmembers class * {
    private <fields>;
}

-keep class Utils.LicenseUtil

# Gson specific classes
-keep class sun.misc.Unsafe { *; }

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-dontwarn com.squareup.picasso.**
-keepclasseswithmembernames class * {
    native <methods>;
    }
