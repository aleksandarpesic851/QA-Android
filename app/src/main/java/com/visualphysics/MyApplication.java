package com.visualphysics;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.activeandroid.ActiveAndroid;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.pyze.android.Pyze;
import com.zoho.commons.ChatComponent;
import com.zoho.livechat.android.MbedableComponent;
import com.zoho.livechat.android.ZohoLiveChat;
import com.zoho.salesiqembed.ZohoSalesIQ;

import zoho.ZohoUtils;
//import android.support.multidex.MultiDex;

/**
 * Created by admin on 5/21/2016.
 */
public class MyApplication extends MultiDexApplication {

    public static final String TAG = MyApplication.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private static MyApplication MY_APPLICATION;
    private static Context mContext;

    public static final String ZOHO_APPLICATION_KEY = "3bqRzzXlKPAHulRZF0E25yRq9MT%2FdiQ8GWfbXZhhJO9jMw%2Fyc64Z5GU%2B1QDgMuBwLzPiHPoT0k573LenG9NrpA%3D%3D";
    public static final String ZOHO_ACCESS_TOKEN = "N1qGDZYyp6mIixDvJAemYaedhcPogIXxGXO4IkTVk%2FDsjCGnFBW3rBFAf49HgcgQxvi5WFHv1LXnFsvI6u8WluoyQxyAFZQlJd0xTDXfoGF8JzJAYj5JdDHEPwU2RT6%2Brw05CKcZtraRAJcSwK7rXhARlZ1hXwiF";

    @Override
    public void onCreate() {
        super.onCreate();
        init();

        //set Custom Typeface
        FontsOverride.setDefaultFont(this, "monospace", "Lato_Regular.ttf");

        /*ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);*/
    }

    /***
     * Initialize the resource at the appliation level
     *
     * @return
     */
    private void init() {
        MY_APPLICATION = this;

        mContext = getApplicationContext();

        /*Configuration.Builder configurationBuilder = new Configuration.Builder(this);
        configurationBuilder.addModelClasses(AppMenu.class);
*/
        ActiveAndroid.initialize(this);
        Pyze.initializeEvents(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //Initialize crisp for chat to support
        ZohoSalesIQ.init(this, ZOHO_APPLICATION_KEY, ZOHO_ACCESS_TOKEN);

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();


    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized MyApplication getInstance() {
        return MY_APPLICATION;
    }

    public static Context getContext() {
        return mContext;
    }


}
