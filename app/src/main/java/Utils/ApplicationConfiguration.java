package Utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Application file is used for all Configuration that are used in the Application
 */
public class ApplicationConfiguration {

    private Context mContext;
    public static enum UserRegType {
        Email,
        Facebook,
        Google
    }
    public static boolean isRATE_POPUP_SHOWN = false;

    public ApplicationConfiguration(Context Ctx) {
        mContext = Ctx;

    }

    /***
     * get the Unique identification for devices
     * @return
     */
    public String getDeviceID(){
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
