package Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.visualphysics.R;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Model.Chapters;
import Model.Videos;
import Model.VideosWithChapter;
import interfaces.OnGotItClickListener;

import static android.support.design.widget.Snackbar.make;

/**
 * Created by India on 6/6/2016.
 */
public class AppUtil implements AppConstansts {

    private Context mContext;

    public static int SELECTED_POSITION = 0;

    public static String EVENT_PLAY_VIDEO = "EventViewVideo";
    public static String EVENT_LAST_VIDEO_PLAY = "EventLastVideoPlay";
    public static String EVENT_LAST_APP_OPEN = "EventLastAppOpen";
    public static String EVENT_REFERRAL = "EventReferral";
    public static String EVENT_FEEDBACK = "EventFeedback";
    public static String EVENT_SPREAD_A_WORD = "EventSpreadAWord";
    public static String EVENT_CONVERSION = "EventConversion";
    public static String EVENT_GENERAL_FEATURE = "EventGeneralFeature";
    public static String EVENT_SEARCH_FEATURE = "EventGeneralFeature";

    //New Events
    public static String EVENT_SIGN_UP = "EventSignUp";
    public static String EVENT_ECOMMERCE_PURCHASE = "EventE-CommercePurchase";
    public static String EVENT_APP_OPEN = "EventAppOpen";

    public AppUtil(Context Context) {
        mContext = Context;
    }

    /***
     * Check if the Email is valid or not
     *
     * @param email
     * @return
     */
    public boolean isEmailValid(String email) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * Display snack bar for network error
     *
     * @param View
     */
    public void displayNoInternetSnackBar(View View) {
        Snackbar snackbar =
                make(View, "No internet connection!", Snackbar.LENGTH_LONG)
                /*.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })*/;

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    /***
     * Display message
     *
     * @param view
     * @param Message
     */
    public static void displaySnackBarWithMessage(View view, String Message) {
        Snackbar mSnackBar = Snackbar.make(view, Message, Snackbar.LENGTH_LONG);
        TextView textView = (TextView) mSnackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);  // show multiple line
        mSnackBar.show();
    }

    /**
     * Display message for encryption error
     *
     * @param mContext
     * @param view
     * @param response
     */
    public static void displaySnackBarWithEncryptionErrorMessage(final Context mContext, View view, JSONObject response) {

        String messageToShow = mContext.getResources().getString(R.string.Error_Msg_Try_Later);

        try {

            JSONObject mJsonObj;

            if (response.has("encryption_error")) {

                mJsonObj = response.getJSONObject("encryption_error");

                if (mJsonObj.getInt("Error") == 2) {

                    //If there is encrypt error then throw the user to play store to install the new app
                    if (mJsonObj.has("cryp_Error") && mJsonObj.getInt("cryp_Error") == 6) {

                        messageToShow = mJsonObj.getString("Message");

                    }
                        /*new Handler().postDelayed(new Runnable() {
                            public void run() {
                                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mActivity.getPackageName())));

                            }
                        }, 2000L);*/
                }
            }

            Snackbar mSnackBar = Snackbar.make(view, messageToShow, Snackbar.LENGTH_LONG);
            TextView textView = (TextView) mSnackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);  // show multiple line
            mSnackBar.show();

        } catch (Exception ex) {

            Snackbar mSnackBar = Snackbar.make(view, messageToShow, Snackbar.LENGTH_LONG);
            TextView textView = (TextView) mSnackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);  // show multiple line
            mSnackBar.show();

        }

    }

    /***
     * Display message
     *
     * @param view
     * @param Message
     */
    public static void displaySnackBarWithMessageForTime(View view, String Message, int duration) {
        Snackbar mSnackBar = Snackbar.make(view, Message, duration);
        TextView textView = (TextView) mSnackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);  // show multiple line
        mSnackBar.show();
    }

    public static void displayToastWithMessage(Context context, String Message) {
        Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
    }

    /***
     * @param ctx
     * @param Message
     */
    public void displayAlertDailog(Context ctx, String Message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(ctx, R.style.AppCompatAlertDialogStyle);
        builder.setMessage(Message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        //builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /***
     * To check the internet connection
     *
     * @return
     */
    public boolean getConnectionState() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null)
            return false;
        else
            return true;
    }

    /***
     * Display Alert Message
     *
     * @param Msg
     */
    public void displayAlert(String Msg) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        myAlertDialog.setTitle(R.string.app_name);
        myAlertDialog.setMessage(Msg);
        myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                // do something when the OK button is clicked

            }
        });

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) { // do something


            }
        });

        myAlertDialog.show();
    }

    /***
     * get Hash Key
     */
    public void getKeyHash() {

        PackageInfo info = null;
        try {
            info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            ErrorLog.SaveErrorLog(e1);
            ErrorLog.SendErrorReport(e1);
        }
        for (android.content.pm.Signature signature : info.signatures) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
                ErrorLog.SaveErrorLog(e1);
                ErrorLog.SendErrorReport(e1);
            }
            md.update(signature.toByteArray());
            Log.d("KeyHash:",
                    Base64.encodeToString(md.digest(), Base64.DEFAULT));
        }
    }

    /***
     * Get the device unique id
     *
     * @return
     */
    public String getDeviceID() {
        String AndroidId = null;
        try {
            AndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            return AndroidId;
        }

    }

    /***
     * Get Application version name
     *
     * @return
     */
    public String getAppVersionName() {
        String VersionName = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0);
            VersionName = pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            return VersionName;
        }
    }


    /***
     * Get Applicaiton version Code
     *
     * @return
     */
    public int getAppVersionCode() {

        int VersionID = 1;
        try {
            PackageInfo pInfo = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0);
            VersionID = pInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            return VersionID;
        }
    }

    /***
     * Convert the local time to UTC
     *
     * @return
     */
    public String getUTCTime() {

        String lv_dateFormateInUTC = ""; //Will hold the final converted date

        try {

            SimpleDateFormat lv_formatter = new SimpleDateFormat("dd/MM/yy hh:mm a");
            lv_formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            lv_dateFormateInUTC = lv_formatter.format(new Date());
            Log.v("", "UTC TIme == " + lv_dateFormateInUTC);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }

        return lv_dateFormateInUTC;

    }

    /***
     * Convert the UTC time to local
     *
     * @return
     */
    /*public String convertUTCToLocalTime(SimpleDateFormat simpleDateFormat, String strDate) {
        String LocalDate = null;
        try {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = simpleDateFormat.parse(strDate);
            LocalDate = myDate.toString();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
        }
        return LocalDate;


    }*/

    /***
     * Convert UTC time to local time
     *
     * @param Date
     * @return
     */
    public String convertUTCToLocalTime(SimpleDateFormat format, String Date) {
        String strDate = Date;
        try {

            //SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy hh:mm:ss");
            Date newDate = format.parse(Date);
            strDate = format.format(newDate);

            //Convert UTC to Local
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = format.parse(strDate);
            strDate = myDate.toString();

            SimpleDateFormat formatDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            newDate = formatDate.parse(strDate);

            format = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            strDate = format.format(newDate);

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return strDate;
    }

    /***
     * Check if video is downloaded on sdcard or not
     *
     * @param URL
     * @return
     */
    public boolean isVideoDownload(String URL) {

        boolean status = false;
        File dir = new File(Environment.getExternalStorageDirectory() + "/"
                + mContext.getResources().getString(R.string.app_name) + "/" + URL);
        if (dir.exists()) {
            status = true;
        }
        return status;
    }


    /***
     * Check if video is available in external memory card
     *
     * @param URL
     * @return
     */
    public boolean isVideoAvialableInMemoryCard(String URL) {
        boolean status = false;
        //String SDCardPath = System.getenv("SECONDARY_STORAGE");
        String SDCardPath = getStoragePath(mContext, true);
        if ((null == SDCardPath) || (SDCardPath.length() == 0)) {
            //SDCardPath =  getRemovabeStorageDir(mContext).toString();;
            /*if (Environment.isExternalStorageRemovable()){
                SDCardPath = System.getenv("EXTERNAL_STORAGE");
            } else {
                SDCardPath = getStoragePath(mContext,true);
            }*/
            //SDCardPath = getStoragePath(mContext,true);
            SDCardPath = System.getenv("SECONDARY_STORAGE");
        }

        File dir = new File(SDCardPath + "/"
                + mContext.getResources().getString(R.string.app_name) + "/" + URL);
        if (dir.exists()) {
            status = true;
        }

        return status;
    }

    /***
     * Get the external SD card Path
     *
     * @return
     */
    public String getExternalSDCardPath() {
        boolean status = false;
        //String SDCardPath = System.getenv("SECONDARY_STORAGE");
        String SDCardPath = getStoragePath(mContext, true);
        if ((null == SDCardPath) || (SDCardPath.length() == 0)) {
            //SDCardPath =  getRemovabeStorageDir(mContext).toString();;
            /*if (Environment.isExternalStorageRemovable()){
                SDCardPath = System.getenv("EXTERNAL_STORAGE");
            } else {
                SDCardPath = getStoragePath(mContext,true);
            }*/
            //SDCardPath = getStoragePath(mContext,true);
            SDCardPath = System.getenv("SECONDARY_STORAGE");
        }
        return SDCardPath;
    }

    /***
     * Get the Storage path of External card
     *
     * @param mContext
     * @param is_removale
     * @return
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return null;
    }

   /* public File getRemovabeStorageDir(Context context) {
        try {
            List<File> storages = getRemovabeStorages(context);
            if (!storages.isEmpty()) {
                return storages.get(0);
            }
        } catch (Exception ignored) {
        }
        final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
        if (SECONDARY_STORAGE != null) {
            return new File(SECONDARY_STORAGE.split(":")[0]);
        }
        return null;
    }

    public List<File> getRemovabeStorages(Context context) throws Exception {
        List<File> storages = new ArrayList<>();

        Method getService = Class.forName("android.os.ServiceManager")
                .getDeclaredMethod("getService", String.class);
        if (!getService.isAccessible()) getService.setAccessible(true);
        IBinder service = (IBinder) getService.invoke(null, "mount");

        Method asInterface = Class.forName("android.os.storage.IMountService$Stub")
                .getDeclaredMethod("asInterface", IBinder.class);
        if (!asInterface.isAccessible()) asInterface.setAccessible(true);
        Object mountService = asInterface.invoke(null, service);

        Object[] storageVolumes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            int uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
            Method getVolumeList = mountService.getClass().getDeclaredMethod(
                    "getVolumeList", int.class, String.class, int.class);
            if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
            storageVolumes = (Object[]) getVolumeList.invoke(mountService, uid, packageName, 0);
        } else {
            Method getVolumeList = mountService.getClass().getDeclaredMethod("getVolumeList");
            if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
            storageVolumes = (Object[]) getVolumeList.invoke(mountService, (Object[]) null);
        }

        for (Object storageVolume : storageVolumes) {
            Class<?> cls = storageVolume.getClass();
            Method isRemovable = cls.getDeclaredMethod("isRemovable");
            if (!isRemovable.isAccessible()) isRemovable.setAccessible(true);
            if ((boolean) isRemovable.invoke(storageVolume, (Object[]) null)) {
                Method getState = cls.getDeclaredMethod("getState");
                if (!getState.isAccessible()) getState.setAccessible(true);
                String state = (String) getState.invoke(storageVolume, (Object[]) null);
                if (state.equals("mounted")) {
                    Method getPath = cls.getDeclaredMethod("getPath");
                    if (!getPath.isAccessible()) getPath.setAccessible(true);
                    String path = (String) getPath.invoke(storageVolume, (Object[]) null);
                    storages.add(new File(path));
                }
            }
        }

        return storages;
    }*/


    /***
     * Encrpt the Text
     *
     * @param Text
     * @return
     * @throws GeneralSecurityException
     */
    public static String encrypt(String Text) throws GeneralSecurityException {

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(ENCRPYT_KEY.getBytes(), ALGORITHM);
            Cipher cipherAES = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipherAES.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] cipherText = cipherAES.doFinal(Text.getBytes());
            return new String(Base64.encode(cipherText, Base64.DEFAULT)).trim();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
        return null;

    }


    /***
     * Decrypt the message
     *
     * @param encryptedText
     * @return
     * @throws GeneralSecurityException
     */
    public static String decrypt(byte[] encryptedText) throws GeneralSecurityException {

        SecretKey secret_key = new SecretKeySpec(ENCRPYT_KEY.getBytes(), ALGORITHM);

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secret_key);

        encryptedText = Base64.decode(encryptedText, Base64.DEFAULT);// = cipher.doFinal(encryptedText);
        byte[] decrypted = cipher.doFinal(encryptedText);
        return new String(decrypted).trim();
    }


    private static String convertBinary2Hexadecimal(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /***
     * @param data
     * @return
     */
    public static String bytesToHex(byte[] data) {

        if (data == null)
            return null;

        String str = "";

        for (int i = 0; i < data.length; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
        }

        return str;

    }

    /***
     * Insert UTC date Time to when the app open from Navigation Screen
     */
    public void lastOpenApp(Context mContext) {
        try {
            SharedPrefrences mSharedPref = new SharedPrefrences(mContext);
           /* SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(getUTCTime());
            String strUTCDateTime = format.format(UTCDateTime);
            mSharedPref.setPreferences(mSharedPref.LAST_APP_OPEN_DATE, strUTCDateTime);*/
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(getUTCTime());

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String strUTCDateTime = format.format(UTCDateTime);
            mSharedPref.setPreferences(mSharedPref.LAST_APP_OPEN_DATE, strUTCDateTime);
        } catch (Exception e) {
            ErrorLog.SendErrorReport(e);
        }
    }

    /**
     * Check if the user login has a
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isOwnerUser() {
        boolean isSecondaryUser = false;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            UserHandle uh = android.os.Process.myUserHandle();
            UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            if (null != um) {
                long userSerialNumber = um.getSerialNumberForUser(uh);
                DebugLog.v("USer Serial Number", "userSerialNumber = " + userSerialNumber);
                isSecondaryUser = true;
                return 0 == userSerialNumber;


            } else
                isSecondaryUser = false;
        } else {
            // Do something regular
        }
        return isSecondaryUser;
    }


    /***
     * Check if the application is downloaded from play store or not
     *
     * @param context
     * @return
     */
    public static boolean isDownloadedFromPlayStore(Context context) {

        boolean result = false;

        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            result = !TextUtils.isEmpty(installer);
        } catch (Throwable e) {

        }

        return result;
    }

    /***
     * Delete entire chapter
     *
     * @param DownloadURL
     * @return
     */
    public boolean deleteWholeChapter(String DownloadURL) {

        boolean status = false;
        if (!TextUtils.isEmpty(DownloadURL)) {
            String[] FileNameArray = DownloadURL.split("/");
            if (FileNameArray.length > 0) {
                String ChapterName = FileNameArray[0];
                File dir = new File(Environment.getExternalStorageDirectory() + "/"
                        + mContext.getResources().getString(R.string.app_name) + "/" + ChapterName);
                if (dir.exists()) {
                    for (File child : dir.listFiles()) {
                        if (child.delete()) {

                        }
                    }
                    if (dir.delete()) {
                        status = true;
                    }


                }
            }
        }
        return status;
    }


    /***
     * Show Default Popup
     */
    public static void showOneTimeDialog(final Context mContext, final ImageView imgDownload, final int position, final OnGotItClickListener mGotItClickListener) {

        try {
            android.support.v7.app.AlertDialog.Builder mAlertDialog = new android.support.v7.app.AlertDialog.Builder(mContext);

            // Setting Dialog Title
            mAlertDialog
                    .setTitle("")
                    .setCancelable(false)
                    .setMessage("Downloaded videos can be played offline (without internet), only inside this app and till your subscription is valid. They won't get played, once subscription expires. But you can always subscribe for a paid plan and watch them offline. To delete them, tap this icon again.")
                    .setPositiveButton("Got it.",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {

                                        SharedPrefrences mSharedPrefrences = new SharedPrefrences(mContext);
                                        mSharedPrefrences.setPreferences(SharedPrefrences.IS_DOWNLOAD_POPUP_SHOWN, true);
                                        dialog.cancel();

                                        mGotItClickListener.onGotItClick(imgDownload, position);

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }
                            }).show();

                    /*.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            }).show();*/

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /***
     * Current Date time in UTC
     */
    public static String getCurrentDateTime(Context mContext) {
        try {
            SharedPrefrences mSharedPref = new SharedPrefrences(mContext);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");

            SimpleDateFormat lv_formatter = new SimpleDateFormat("dd/MM/yy hh:mm a");
            lv_formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date UTCDateTime = format.parse(lv_formatter.format(new Date()));
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            String strUTCDateTime = format.format(UTCDateTime);

            return strUTCDateTime;

        } catch (Exception e) {

            ErrorLog.SendErrorReport(e);
        }

        return "" + new Date();
    }


    /*Fo Buy Packages*/

    /***
     * Get Current Time in Millis
     */

    /***
     * @return
     */
    public static long getCurrentTimeInMillis() {

        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();

    }

    /***
     * @return
     */
    public static long getCurrentTimeInMillisPlusDays(int numberOfDays) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, numberOfDays);
        return cal.getTimeInMillis();

    }

    /***
     * Created by IZISS
     * This will used to rate app on google play store
     *
     * @param context
     */
    public static void rateApp(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            viewInBrowser(context, "https://play.google.com/store/apps/details?id=" + context.getPackageName());
        }
    }

    /***
     * Created by IZISS
     * Open a url in browser
     *
     * @param context
     * @param url
     */
    public static void viewInBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (null != intent.resolveActivity(context.getPackageManager())) {
            context.startActivity(intent);
        }
    }

    /**
     * Convert Minutes to hours
     *
     * @param value
     * @return
     */
    public static String convertMinToHrs(int value) {

        if (value > 59) {

            //To convert minute into ratio
            float minutesInRatio = (float) value / 60;

            //Convert into duration string
            String duration = "" + roundUptoOneDecimal("" + minutesInRatio);

            return duration + " hrs";

        } else {
            return value + " mins";
        }
    }

    /***
     * @param doubleString
     * @return
     */
    private static double roundUptoOneDecimal(String doubleString) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(Double.parseDouble(doubleString)));
    }

    /**
     * Get if the developer mode is enabled or not
     *
     * @param mContext
     * @return
     */
    public static boolean isDeveloperModeEnabled(Context mContext) {
        if (Integer.valueOf(Build.VERSION.SDK_INT) <= 16) {
            return android.provider.Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        } else if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 17) {
            return android.provider.Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        } else return false;
    }

    /***
     * Get if debugging is ON
     *
     * @param mContext
     * @return
     */
    public static boolean isDebuggingModeEnabled(Context mContext) {

        boolean isEnabled = false;

        if (Integer.valueOf(Build.VERSION.SDK_INT) <= 16) {
            isEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1;
        } else if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 17) {
            isEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        }

        if (isEnabled) {
            showMessageDialog(mContext, "Error", "Videos can't get played with Debugging Mode ON. Please turn off debugging mode from Phone settings &#8594; Developer Options and then try again.", "OK");
            return true;
        } else {

            return false;
        }

        //Toast.makeText(mContext, "This feature is not available with debugging mode", Toast.LENGTH_SHORT).show();
        /*This will show a alert dialog to user*/

    }

    /***
     * An alert dialog for user on screen
     *
     * @param mContext
     * @param title
     * @param message
     * @param buttonText
     */
    public static void showMessageDialog(Context mContext, String title, String message, String buttonText) {

        try {
            android.support.v7.app.AlertDialog.Builder mAlertDialog = new android.support.v7.app.AlertDialog.Builder(mContext);

            // Setting Dialog Title
            mAlertDialog
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(buttonText,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    dialog.cancel();

                                }
                            }).show();

            mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {


                }
            });

                    /*.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            }).show();*/

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This will return an array with values passed in Deep Link
     *
     * @param url
     * @return
     */
    public static String[] getIds(String url) {

        try {

            String values[] = url.split("/");

            for (int i = 0; i < values.length; i++) {
                Log.i("Value>", "" + values[i]);
            }

            return values;
        } catch (Exception ex) {
            return new String[]{"", "", "", "", "", "", "", "", ""};
        }
    }

    /***
     * check if a value is valid string
     *
     * @return
     */
    public boolean isValidString(String value) {

        if (value != null) {
            if (value.trim().length() > 0) {
                if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase("0")) {
                    return true;
                }
            }
        }

        return false;
    }

    /***
     * Check if app install or not
     *
     * @param uri
     * @param context
     * @return
     */
    public static boolean appInstalledOrNot(String uri, @NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
            ErrorLog.SendErrorReport(e);
        }
        return app_installed;
    }


    /***
     * Check if the Tag for Videos are updated or not else we need to fetch from server
     *
     * @param mVideosList
     * @return
     */
    public static boolean isTagUpdatedForVideos(List<Videos> mVideosList) {

        for (Videos mVideo : mVideosList) {

            if (mVideo.Tag != null) {

                if (mVideo.Tag.length() > 0) {

                    return true;

                }
            }
        }

        /*IMPORTANT DISCLAIMER:*/
        /*Change this value to false when we will enable the Video Tags for Questions, Concepts, Quiz and Notes
         * This will check if there is any Tag present for any of video, it means we are sycned with
         * the DB and we don't need t o fetch data again and again*/
        return false;

        //return true;
    }

    /***
     * Check if the Tag for Videos are updated or not else we need to fetch from server
     *
     * @param mVideosList
     * @return
     */
    public static boolean isTagUpdatedForVideosWithChapter(List<VideosWithChapter> mVideosList) {

        for (VideosWithChapter mVideo : mVideosList) {

            if (mVideo.Tag != null) {

                if (mVideo.Tag.length() > 0) {

                    return true;

                }
            }
        }

        /*IMPORTANT DISCLAIMER:*/
        /*Change this value to false when we will enable the Video Tags for Questions, Concepts, Quiz and Notes
         * This will check if there is any Tag present for any of video, it means we are sycned with
         * the DB and we don't need t o fetch data again and again*/
        return false;

        //return true;
    }

    /***
     * Check if the TagID for chapters are updated or not else we need to fetch from server
     *
     * @param mChapterArrayList
     * @return
     */
    public static boolean isTagIDUpdated(List<Chapters> mChapterArrayList) {

        for (Chapters mChapters : mChapterArrayList) {

            if (mChapters.TagID != null) {

                if (mChapters.TagID.length() > 0) {

                    return true;

                }
            }
        }

        return false;
    }

    public static void hideKeyBoard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
