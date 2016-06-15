package org.goodev.droidddle;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.notif.FollowingCheckService;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.OAuthUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

//import android.support.multidex.MultiDex;
//import android.support.multidex.MultiDexApplication;

/**
 * Created by goodev on 2014/12/31.
 */
public class App extends Application /*MultiDexApplication*/ {

    final static Object lock = new Object();
    private static final String GMS_PROVIDER = "GmsCore_OpenSSL";
    static boolean initialized;
    static boolean success;
    private static Context sThis;
    private static User sUser;

    public static final Context getContext() {
        return sThis;
    }

    public static void setOAuthUser(User u) {
        sUser = u;
    }

    public static User getUser() {
        return sUser;
    }

    public static void initialize(Context context) {
        try {
            synchronized (lock) {
                if (initialized) {
                    return;
                }

                initialized = true;

                // GMS Conscrypt is already initialized, from outside ion. Leave it alone.
                if (Security.getProvider(GMS_PROVIDER) != null) {
                    success = true;
                    return;
                }

                SSLContext originalDefaultContext = SSLContext.getDefault();
                SSLSocketFactory originalDefaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                try {
                    Class<?> providerInstaller = Class.forName("com.google.android.gms.security.ProviderInstaller");
                    Method mInsertProvider = providerInstaller.getDeclaredMethod("installIfNeeded", Context.class);
                    mInsertProvider.invoke(null, context);

                } catch (Throwable ignored) {
                    Context gms = context
                            .createPackageContext("com.google.android.gms", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    gms.getClassLoader().loadClass("com.google.android.gms.common.security.ProviderInstallerImpl")
                            .getMethod("insertProvider", Context.class).invoke(null, context);
                }

                Provider[] providers = Security.getProviders();
                Provider provider = Security.getProvider(GMS_PROVIDER);
                Security.removeProvider(GMS_PROVIDER);
                Security.insertProviderAt(provider, providers.length);
                SSLContext.setDefault(originalDefaultContext);
                HttpsURLConnection.setDefaultSSLSocketFactory(originalDefaultSSLSocketFactory);
                success = true;
                //                try {
                //                    SSLContext sslContext = null;
                //                    try {
                //                        sslContext = SSLContext.getInstance("TLS", GMS_PROVIDER);
                //                    }
                //                    catch (Exception e) {
                //                    }
                //                    if (sslContext == null)
                //                        sslContext = SSLContext.getInstance("TLS");
                //                    sslContext.init(null, null, null);
                //                }
                //                catch (Exception e) {
                //                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sThis = this;
//        com.squareup.leakcanary.LeakCanary.install(this);
        initialize(this);
//        Glide.get(this).register(GlideUrl.class, InputStream.class,
//                new OkHttpUrlLoader.Factory(ApiFactory.getOkHttpClient(this)));
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, ApiFactory.getOkHttpClient(this))
                .build();
        Fresco.initialize(this, config);

        if (true || OAuthUtils.haveToken(this)) {
//            Intent watchfaceLoader = new Intent();
//            watchfaceLoader.setClass(this, BucketWatchFaceLoader.class);
//            startService(BucketWatchFaceLoader.getServiceIntent(this));
            startService(FollowingCheckService.getServiceIntent(this));
        }
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }

    //TODO if 401 , delete all app data
    public static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
