package Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import Model.LoginUser;
import deeplink.DeepLinkGenerator;
import mixpanel.MixPanelClass;

/**
 * Created by India on 6/22/2016.
 */
public class SharedPrefrences {

    private Context mContext;
    private static final String MYPREFS = "VisualPhysicsSharedPreference";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    public final String USERDETAIL = "User";
    public final String LAST_SYNC_DATE = "LastSyncDate";
    public final String STREAM_URL = "StreamURL";
    public final String DOWNLOAD_URL = "DownloadURL";
    public final String SUPPORT_EMAIL = "SupportEmail";
    public final String LAST_APP_OPEN_DATE = "LastAppOpenDate";
    public final String APP_OPEN_COUNT = "AppOpenCount";
    public final String APP_VERSION = "AppVersion";
    public final String IS_RATE_APP = "RateAppStatus";

    public static final String IS_DOWNLOAD_POPUP_SHOWN = "isPopUpShown";

    public SharedPrefrences(Context Ctx) {
        mContext = Ctx;
        settings = mContext.getSharedPreferences(MYPREFS, 0);
        editor = settings.edit();
    }

    // Set the Access Token and Other Important Keys in SharedPrefernces
    public void setPreferences(String Key, String Value) {
        editor.putString(Key, Value);
        editor.commit();
    }

    // get the Access Token and Other Important Keys in SharedPrefernces
    public String getPreferences(String Key, String Value) {
        return settings.getString(Key, Value);
    }


    // Set the Access Token and Other Important Keys in SharedPrefernces
    public void setPreferences(String Key, int Value) {
        editor.putInt(Key, Value);
        editor.commit();
    }

    // get the Access Token and Other Important Keys in SharedPrefernces
    public int getPreferences(String Key, int Value) {
        return settings.getInt(Key, Value);
    }

    // Set the Status and Other Important Keys in SharedPrefernces
    public void setPreferences(String Key, boolean Value) {
        editor.putBoolean(Key, Value);
        editor.commit();
    }

    // get the Status and Other Important Keys in SharedPrefernces
    public Boolean getPreferences(String Key, boolean Value) {
        return settings.getBoolean(Key, Value);
    }

    // get the Status and Other Important Keys in SharedPrefernces
    public boolean getBooleanPreferences(String Key, boolean Value) {
        return settings.getBoolean(Key, Value);
    }


    // Get Login User
    public LoginUser getLoginUser() {
        Gson gson = new Gson();
        LoginUser user = gson.fromJson(getPreferences(USERDETAIL, ""), LoginUser.class);
        return user;
    }

    public void clearAll() {

        editor.remove(USERDETAIL);
        editor.remove(APP_OPEN_COUNT);
        editor.remove(IS_DOWNLOAD_POPUP_SHOWN);
        editor.commit();

        try {

            MixPanelClass mixPanelClass = new MixPanelClass(mContext);
            mixPanelClass.clearMixPanelPreference();

            /*Clear Local Preference and need context*/
            DeepLinkGenerator mDeepLinkGenerator = new DeepLinkGenerator(mContext);
            mDeepLinkGenerator.clearMixPanelPreference();

        } catch (Exception ex) {
            ErrorLog.SendErrorReportMixPanel(ex);
            ex.printStackTrace();
        }


    }
}
