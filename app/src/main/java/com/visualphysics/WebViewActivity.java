package com.visualphysics;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private String urlToLoad = "http://www.nlytn.in/";
    private String URL_TO_COMPARE = "www.nlytn.in";
    private String pageNameToLoadValue = "", pageToLoadValue = "";

    public static final String PAGE_TO_LOAD = "pageToLoad";
    public static final String PAGE_NAME_TO_LOAD = "pageNameToLoad";

    /*If is direct URL*/
    public static final String IS_DIRECT_URL = "isDirectUrl";
    public static final String URL = "url";

    private ImageView mBack;
    private TextView mTxtChapterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        if (getIntent() != null) {
            if (getIntent().hasExtra(PAGE_TO_LOAD)) {

                pageNameToLoadValue = getIntent().getStringExtra(PAGE_NAME_TO_LOAD);
                pageToLoadValue = getIntent().getStringExtra(PAGE_TO_LOAD);
            }
        }

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.chapter_actionbarlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();

        mTxtChapterName = (TextView) findViewById(R.id.txtChapterNameVideoScreen);
        mTxtChapterName.setText(pageToLoadValue);

        mBack = (ImageView) findViewById(R.id.back_Arrow);
        mBack.setOnClickListener(this);

        webView = (WebView) findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient());
        // webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebViewClient(new CustomWebViewClient());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        String urlToLoadInWebView = urlToLoad + pageNameToLoadValue + "/" + pageToLoadValue;

        if (getIntent() != null) {
            if (getIntent().hasExtra(IS_DIRECT_URL)) {

                urlToLoadInWebView = getIntent().getStringExtra(URL);
            }
        }

        Log.i("URL in Web View >>", urlToLoadInWebView);
        webView.loadUrl(urlToLoadInWebView);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back_Arrow:
                onBackPressed();
                break;
        }

    }

    /***
     * Custom class to handle redirection from app and if url matches then redirect to our app only
     */
    class CustomWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            final Uri uri = request.getUrl();
            return handleUri(uri);

        }

        private boolean handleUri(final Uri uri) {

            Log.i("WebViewActivity", "Uri =" + uri);

            try {

                final String host = uri.getHost();
                final String scheme = uri.getScheme();

                // Based on some condition you need to determine if you are going to load the url
                // in your web view itself or in a browser.
                // You can use `host` or `scheme` or any part of the `uri` to decide.
                if (uri.toString().contains(NavigationScreen.DEEP_LINK_PREFIX)) {

                    // Returning false means that you are going to load this url in the webView itself
                    Intent intent = new Intent(WebViewActivity.this, NavigationScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setData(uri);
                    startActivity(intent);
                    finish();

                    return false;

                    //If current host matches to deep link preference for the app
                } else if (host.equalsIgnoreCase(NavigationScreen.DEEP_LINK_PREFIX_GOOGLE)) {

                    //Get actual link from deep link
                    getLinkFromDeepLink(uri);

                    return false;

                } else {

                    //Only nlytn url will load in webview
                    //Changed on 17th Oct to open it in same app with or without http or https
                    if (uri.toString().contains(URL_TO_COMPARE)) {

                        return false;

                    } else {

                        // Returning true means that you need to handle what to do with the url
                        // e.g. open web page in a Browser
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(WebViewActivity.this, getResources().getString(R.string.no_apps_to_handle_intent), Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }

                }

            } catch (Exception ex) {
                // Returning true means that you need to handle what to do with the url
                // e.g. open web page in a Browser
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(WebViewActivity.this, getResources().getString(R.string.no_apps_to_handle_intent), Toast.LENGTH_SHORT).show();
                }

                return true;
            }

        }
    }


    /***
     * This will redirect if the deep link is available
     *
     * @return
     */
    private void getLinkFromDeepLink(Uri mUri) {

        try {

            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(mUri)
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                            // Get deep link from result (may be null if no link is found) already handled with other code
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {

                                deepLink = pendingDynamicLinkData.getLink();

                                Log.w("Pending Deep Link", "" + deepLink);

                                // Returning false means that you are going to load this url in the webView itself
                                Intent intent = new Intent(WebViewActivity.this, NavigationScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setData(deepLink);
                                startActivity(intent);
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("DYNAMIC FAILURE", "getDynamicLink:onFailure", e);
                        }
                    });

        } catch (Exception ex) {

            ex.printStackTrace();

        }
    }

    private boolean isValidURL(String urlToMatch) {
        return Patterns.WEB_URL.matcher(urlToMatch).matches();
    }


}
