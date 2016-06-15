/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.goodev.droidddle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.DribbbleAuthService;
import org.goodev.droidddle.pojo.AccessToken;
import org.goodev.droidddle.utils.CompatUri;
import org.goodev.droidddle.utils.OAuthUtils;

import java.util.Set;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DribbbleLogin extends AppCompatActivity {

    boolean isDismissing = false;
    private ProgressBar loading;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_login);
        loading = (ProgressBar) findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        mWebView = (WebView) findViewById(R.id.webview);

        setupWebView();

        checkAuthCallback(getIntent());
    }

    private void setupWebView() {
        WebView wv = mWebView;
        WebSettings webSettings = wv.getSettings();
        webSettings.setSaveFormData(false);

        wv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                WebView wv = (WebView) v;
                if (keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()) {
                    wv.goBack();
                    return true;
                }
                return false;
            }
        });

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        wv.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress != 0 && newProgress != 100) {
                    showLoading();
                }
            }

        });

        wv.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                interceptUrlCompat(view, url, true);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                onError(description);
            }

            private boolean interceptUrlCompat(WebView view, String url, boolean loadUrl) {
                if (isFinishing() || hasDestroyed()) {
                    return false;
                }
                String redirectUri = OAuthUtils.CALLBACK_URI;

                Log.e("tag", "url: " + url + ", redirect: " + redirectUri + ", callback: " + isRedirectUriFound(url, redirectUri));
                if (isRedirectUriFound(url, redirectUri)) {
                    Uri uri = Uri.parse(url);
                    getAccessToken(uri.getQueryParameter("code"));
                    return true;
                }
                if (loadUrl) {
                    view.loadUrl(url);
                }
                return false;
            }

        });
    }


    static boolean isRedirectUriFound(String uri, String redirectUri) {
        Uri u = null;
        Uri r = null;
        try {
            u = Uri.parse(uri);
            r = Uri.parse(redirectUri);
        } catch (NullPointerException e) {
            return false;
        }
        if (u == null || r == null) {
            return false;
        }
        boolean rOpaque = r.isOpaque();
        boolean uOpaque = u.isOpaque();
        if (rOpaque != uOpaque) {
            return false;
        }
        if (rOpaque) {
            return TextUtils.equals(uri, redirectUri);
        }
        if (!TextUtils.equals(r.getScheme(), u.getScheme())) {
            return false;
        }
        if (!TextUtils.equals(r.getAuthority(), u.getAuthority())) {
            return false;
        }
        if (r.getPort() != u.getPort()) {
            return false;
        }
        if (!TextUtils.isEmpty(r.getPath()) && !TextUtils.equals(r.getPath(), u.getPath())) {
            return false;
        }
        Set<String> paramKeys = CompatUri.getQueryParameterNames(r);
        for (String key : paramKeys) {
            if (!TextUtils.equals(r.getQueryParameter(key), u.getQueryParameter(key))) {
                return false;
            }
        }
        String frag = r.getFragment();
        if (!TextUtils.isEmpty(frag) && !TextUtils.equals(frag, u.getFragment())) {
            return false;
        }
        return true;
    }

    private void onError(String description) {
        Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
    }

    private boolean hasDestroyed() {
        if (isDismissing) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return isDestroyed();
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkAuthCallback(intent);
    }

    public void dismiss(View view) {
        isDismissing = true;
        setResult(Activity.RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    private void showLoading() {
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loading.setVisibility(View.INVISIBLE);
    }

    private void showLogin() {
        checkAuthCallback(getIntent());
        loading.setVisibility(View.VISIBLE);
    }

    private void checkAuthCallback(Intent intent) {
        if (intent != null
                && intent.getData() != null
                && !TextUtils.isEmpty(intent.getData().getAuthority())
                ) {
            showLoading();
            mWebView.loadUrl(intent.getData().toString());
        }
    }

    private void getAccessToken(String code) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(DribbbleAuthService.ENDPOINT)
                .build();

        DribbbleAuthService dribbbleAuthApi = restAdapter.create((DribbbleAuthService.class));

        dribbbleAuthApi.getAccessToken(OAuthUtils.getClientId(),
                OAuthUtils.getClientSecret(),
                code, "", new Callback<AccessToken>() {
                    @Override
                    public void success(AccessToken accessToken, Response response) {
                        OAuthUtils.saveAccessToken(DribbbleLogin.this, accessToken.access_token);
                        ApiFactory.resetApiService();
                        setResult(Activity.RESULT_OK);
                        supportFinishAfterTransition();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(getClass().getCanonicalName(), error.getMessage(), error);
                        // TODO snackbar?
                        int status = error.getResponse() == null ? -1 : error.getResponse().getStatus();
                        Toast.makeText(getApplicationContext(), "Log in failed: " + status, Toast.LENGTH_LONG).show();
                        showLogin();
                    }
                });
    }

}
