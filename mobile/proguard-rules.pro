# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
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

# Needed to keep generic types and @Key annotations accessed via reflection
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

-keepclasseswithmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepclasseswithmembers class * {
  @com.google.api.client.util.Value <fields>;
}

-keepnames class com.google.api.client.http.HttpTransport

# Needed by google-http-client-android when linking against an older platform version
-dontwarn com.google.api.client.extensions.android.**

# Needed by google-api-client-android when linking against an older platform version
-dontwarn com.google.api.client.googleapis.extensions.android.**

# Do not obfuscate but allow shrinking of android-oauth-client
-keepnames class com.wuman.android.auth.** { *; }

-dontwarn java.lang.invoke.*
-dontwarn java.awt.*
-dontwarn com.google.appengine.api.**
-dontwarn java.nio.**
-dontwarn  sun.misc.Unsafe
-dontwarn org.codehaus.mojo.**

-dontwarn butterknife.internal.**
-dontwarn com.squareup.okhttp.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}
-keep class org.goodev.droidddle.pojo.** { *; }
-keepclassmembers class org.goodev.droidddle.ShotDetailsActivity {
   protected *;
}

-keepattributes *Annotation*
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class android.support.v7.widget.SearchView { *; }

-keepattributes InnerClasses,EnclosingMethod,Deprecated,Exceptions
-keep public class * extends android.app.Activity
-keep public class com.appflood.AppFlood {
    <methods>;
    public static <fields>;
    public <fields>;
}
-keep public class com.appflood.AppFlood$* {
    <methods>;
    public static <fields>;
    public <fields>;
}
-keep public class com.appflood.mraid.AFBannerWebView$* {
    <methods>;
    public static <fields>;
    public <fields>;
}
-keep public class com.appflood.AFReferralReceiver {
    <methods>;
    public static <fields>;
    public <fields>;
}
-keep public class com.appflood.AFBannerView {
    <methods>;
    public static <fields>;
    public <fields>;
}

-keep class org.lucasr.twowayview.** { *; }

-dontwarn com.baidu.mobstat.**
-keep class com.baidu.mobstat.** { *;}
-keep class * extends com.baidu.mobstat.**
-keep class com.baidu.kirin.** {*; }
-keep class com.baidu.*.*

-keepnames public class * extends io.realm.RealmObject
-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**

-keep public class * implements com.bumptech.glide.module.GlideModule

-keepclassmembers class org.goodev.droidddle.drawee.TranslateDraweeView {
   public *;
}

# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class org.goodev.design.ScrollAwareFABBehavior {*; }

#-libraryjars libs/pollfish.jar
-keep class com.pollfish.** { *; }

-dontwarn com.millennialmedia.**
# Keep public classes and methods.
-keepclassmembers class com.mopub.** { public *; }
-keep public class com.mopub.**

# Explicitly keep any custom event classes in any package.
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}
-keep class * extends com.mopub.mobileads.CustomEventRewardedVideo {}

# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}

-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
-keep class com.inmobi.** { *;}
-keep class com.facebook.** { *;}

-dontwarn android.**
-dontwarn com.facebook.**
-dontwarn com.google.android.**
-dontwarn org.apache.**
-keep class org.apache.** { *;}
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.client.ApacheClient$GenericEntityHttpRequest
-dontwarn retrofit.client.ApacheClient$GenericHttpRequest
-dontwarn retrofit.client.ApacheClient$TypedOutputEntity
-dontwarn com.google.api.client.http.apache.ContentEntity
-dontwarn com.google.api.client.http.apache.HttpExtensionMethod

-keep class android.support.v7.widget.LinearLayoutManager { *; }

-keep class com.baidu.** {
  *;
}


-dontwarn com.adxmi.customizedad.**
-keep class com.adxmi.customizedad.** {
    *;
}
