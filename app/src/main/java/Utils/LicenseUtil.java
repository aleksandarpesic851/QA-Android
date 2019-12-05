package Utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;
import com.intertrust.wasabi.licensestore.License;
import com.intertrust.wasabi.licensestore.LicenseStore;
import com.visualphysics.ApiCall;
import com.visualphysics.LoginScreenActivity;
import com.visualphysics.NavigationScreen;
import com.visualphysics.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import Model.LoginUser;
import mixpanel.MixPanelClass;

/**
 * Created by India on 7/7/2016.
 */
public class LicenseUtil implements OnTaskCompleted.CallBackListener {


    private static String TokenFileName = "license-token.xml";
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private LicenseListener OnLicenseListener;
    private String UserID;

    boolean isReported = false;

    private static int reTryCount = 0;

    public LicenseUtil() {

    }

    public LicenseUtil(LicenseListener OnLicenseListener) {
        this.OnLicenseListener = OnLicenseListener;
    }

    /***
     * Acquire the Expressplay license
     *
     * @param TOkenURL
     * @return
     */
    public static boolean downloadExpressPlayLicense(Context Context, String TOkenURL) {
        boolean status = false;
        try {
            File fileName = new File(Context.getFilesDir().getAbsolutePath() + "/" + TokenFileName);
            deleteToken(fileName);

            String buffer;
            URLConnection conn = new URL(TOkenURL).openConnection();
            conn.setUseCaches(false);
            conn.connect();
            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            FileOutputStream fos = Context.openFileOutput("license-token.xml", Context.MODE_PRIVATE);

            while ((buffer = br.readLine()) != null) {
                fos.write(buffer.getBytes());
            }
            fos.close();
            br.close();
            isr.close();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {
            return status;
        }
    }

    /***
     * Delete Token
     *
     * @param f
     */
    private static void deleteToken(File f) {
        try {
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Check is token already available
     *
     * @param Context
     * @return
     */
    private static boolean isTokenAvialableToken(Context Context) {
        try {
            File fileName = new File(Context.getFilesDir().getAbsolutePath() + "/" + TokenFileName);

            if (fileName.exists()) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
            return false;
        }
    }

    /***
     * Check is License Expiry or not
     * If return true it means license has been expiry
     *
     * @param context
     * @return
     */
    public static boolean isLicenseExpiry(Context context) {

        boolean status = false;

        try {
            LicenseStore licenseStore = new LicenseStore();
            licenseStore.expungeExpiredLicenses();
            License[] licenseArray = licenseStore.enumerateLicenses(LicenseStore.WSB_LS_FLAG_LICENSE_EXPIRE_DATE);
            if (licenseArray == null || licenseArray.length == 0) {
                //get the license
                status = true;
            }
        } catch (ErrorCodeException expresPlayException) {
            expresPlayException.printStackTrace();
            ErrorLog.SaveErrorLog(expresPlayException);
            //Toast.makeText(context,R.string.Error_Msg_Try_Later, Toast.LENGTH_SHORT).show();
            ErrorLog.SendErrorReport(expresPlayException);
            status = true;
        } catch (Exception e) {

            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            status = true;
            ErrorLog.SendErrorReport(e);

        }
        return status;
    }

    /***
     * Remove License on Failure
     */
    private void removeLicenseOnFailure() {
        try {
            LicenseStore licenseStore = new LicenseStore();
            licenseStore.expungeExpiredLicenses();
            License[] licenseArray = licenseStore.enumerateLicenses(LicenseStore.WSB_LS_FLAG_LICENSE_DATA);
            if (licenseArray != null && licenseArray.length > 0) {
                for (int i = 0; i < licenseArray.length; i++) {
                    DebugLog.v("", "License Detail == " + licenseArray.toString());
                    licenseStore.removeLicense(licenseArray[i].getId());
                }
            }
        } catch (ErrorCodeException e) {
            ErrorLog.SendErrorReport(e);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * Remove License on Delink
     */
    public void removeLicenseOnDelinkDevice() {
        try {
            LicenseStore licenseStore = new LicenseStore();
            licenseStore.expungeExpiredLicenses();
            License[] licenseArray = licenseStore.enumerateLicenses(LicenseStore.WSB_LS_FLAG_LICENSE_DATA);
            if (licenseArray != null && licenseArray.length > 0) {
                for (int i = 0; i < licenseArray.length; i++) {
                    DebugLog.v("", "License Detail == " + licenseArray.toString());
                    licenseStore.removeLicense(licenseArray[i].getId());
                }
            }
        } catch (ErrorCodeException e) {
            ErrorLog.SendErrorReport(e);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Print personalized details
     */
    public void getPersonalizedDetails() {

        try {

            String deviceID = Runtime.getProperty(Runtime.Property.NEMO_DEVICE_ID).toString();
            String nodeID = Runtime.getProperty(Runtime.Property.PERSONALITY_NODE_ID).toString();

            Log.i("deviceID", deviceID);
            Log.i("nodeID", nodeID);

            ErrorLog.SaveInLogFile("Device ID>>" + deviceID);
            ErrorLog.SaveInLogFile("Node ID>>" + nodeID);


        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }


    }

    /***
     * @param context
     * @param UserID
     */
    public void acquireLicence(Context context, String UserID) {

        reTryCount = 0;

        mContext = context;
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(context);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        this.UserID = UserID;
        if (!mAppUtils.getConnectionState()) {
            removeLicenseOnFailure();
            mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                    mContext.getResources().getString(R.string.Error_Acquiring_License_Without_Internet));
        } else {

            String strUTCDateTime = "";
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
                Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

                format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                strUTCDateTime = format.format(UTCDateTime);
            } catch (Exception e) {
                ErrorLog.SendErrorReport(e);
            }

            mProgressDialog.setMessage("Acquiring license, please wait…");
            mProgressDialog.show();
            mApiCall.getLicenseToken(UserID, mAppUtils.getDeviceID(), strUTCDateTime, new OnTaskCompleted(this), mApiCall.GetLicenseToken);
        }
    }

    /***
     * Update that License acquire successfully on the user device
     *
     * @param DaysLicence
     */
    public void updateLicenceAcquireSuccess(String DaysLicence) {

        String DeviceOS = "Android";
        String DeviceManufacturerName = Build.MANUFACTURER;
        String DeviceModelNumber = Build.MODEL;
        String DeviceOSVersion = android.os.Build.VERSION.RELEASE;
        String DeviceID = mAppUtils.getDeviceID();
        String AppVersion = String.valueOf(mAppUtils.getAppVersionCode());
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                    mContext.getResources().getString(R.string.Error_Acquiring_License_Without_Internet));
            removeLicenseOnFailure();
        } else {
            mApiCall.updateLicenseTokenAcquireSuccessfully(UserID, DeviceID, DaysLicence, AppVersion,
                    DeviceManufacturerName, DeviceOS, DeviceOSVersion,
                    new OnTaskCompleted(this), mApiCall.UpdateLicenseTokenAcquire);
        }
    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {

        if (Method.equals(mApiCall.GetLicenseToken)) {
            parseResponseForGetLicenseTokenData(result);
        } else if (Method.equals(mApiCall.UpdateLicenseTokenAcquire))
            parseResponseForUpdatingAcquireLicenseSuccessfullData(result);
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                mContext.getResources().getString(R.string.Error_Msg_Try_Later));
    }


    /***
     * Parse the response of Getting License Token
     *
     * @param response
     */
    private void parseResponseForGetLicenseTokenData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetLicenseToken);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {

                dismissDialog();
                // mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                //         mJsonObj.getString("Message"));

                //The pop of subscription expire
                alertDialogSubscriptionExpire(mContext);

            } else if (ErrorCode == 2) {
                dismissDialog();
                mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            } else if (ErrorCode == 5) {
                Toast.makeText(((Activity) mContext), mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                SharedPrefrences mSharedPref = new SharedPrefrences(mContext);
                mSharedPref.clearAll();

                LicenseUtil License = new LicenseUtil();
                License.removeLicenseOnDelinkDevice();

                Intent intent = new Intent(((Activity) mContext), LoginScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
            } else if (ErrorCode == 0) {

                mJsonObj = new JSONObject(mJsonObj.getJSONArray("data").getString(0).toString());
                String TokenData = mJsonObj.getString("LicenceToken").toString();
                String LimitInDays = mJsonObj.getString("LimitInDays").toString();
                DebugLog.v("", "Token == " + TokenData);

                if (TokenData != null) {
                    if (TokenData.length() > 0) {
                        new TokenHandler().execute(TokenData, LimitInDays);
                    } else {
                        dismissDialog();
                        acquireLicence(mContext, new SharedPrefrences(mContext).getLoginUser().getStudentID());
                    }
                } else {
                    dismissDialog();
                    acquireLicence(mContext, new SharedPrefrences(mContext).getLoginUser().getStudentID());
                }

            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();

            /*mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                    mContext.getResources().getString(R.string.Error_Msg_Try_Later));*/

            /*Added by IZISS to show Encryption error if any*/
            mAppUtils.displaySnackBarWithEncryptionErrorMessage(((Activity) mContext), ((Activity) mContext).findViewById(android.R.id.content), response);

            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {

        }
    }

    /***
     * Parse the response on Updating Acquire Token Succesfully on the User Devices
     *
     * @param response
     */
    private void parseResponseForUpdatingAcquireLicenseSuccessfullData(JSONObject response) {
        try {
            dismissDialog();
            JSONObject mJsonObj = response.getJSONObject(mApiCall.UpdateLicenseTokenAcquire);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {
                removeLicenseOnFailure();
                mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                        mContext.getResources().getString(R.string.Error_Msg_Try_Later));
            } else if (ErrorCode == 2) {
                removeLicenseOnFailure();
                mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            } else if (ErrorCode == 0) {
                if (OnLicenseListener != null)
                    OnLicenseListener.onProcessToken();
            }
        } catch (Exception e) {
            removeLicenseOnFailure();
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                    mContext.getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {

        }
    }

    /***
     * Token Acquire Handler
     */
    private class TokenHandler extends AsyncTask<String, Void, Boolean> {
        String LimitInDays = "";
        String TokenData = "";
        int errorCode = 0;

        protected void onPreExecute() {
            try {
            } catch (Exception e) {
                ErrorLog.SaveErrorLog(e);
            }
        }

        protected Boolean doInBackground(String... params) {
            boolean status = false;
            TokenData = params[0];
            LimitInDays = params[1];
            try {
                // The action token is processed here. the method automatically stores the license to the local DB.
                Runtime.processServiceToken(TokenData);
                status = true;
            } catch (ErrorCodeException e) {

                errorCode = e.getErrorCode();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReportUnableToAcquireLicense(e);
            } catch (Exception e) {
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
            return status;
        }

        protected void onPostExecute(Boolean Status) {

            if (!Status) {
                //IZISS
               /* mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                        mContext.getResources().getString(R.string.msg_license_unable_to_acquire));*/

                if (reTryCount <= 1) {
                    new TokenHandler().execute(TokenData, LimitInDays);
                    reTryCount++;
                } else {

                    dismissDialog();
                    mAppUtils.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                            mContext.getResources().getString(R.string.msg_license_unable_to_acquire));

                    reTryCount = 0;

                    if (isNetworkError(errorCode)) {
                        alertDialog("Sorry!", mContext.getResources().getString(R.string.msg_license_not_acquired_network_error), "OK", "1");
                    } else {
                        alertDialog("Sorry!", mContext.getResources().getString(R.string.msg_license_not_acquired), "REPORT IT", "2");
                    }


                }
            } else { //If token processed successfully

                //Update server that user has acquire the Token successfully on the device
                updateLicenceAcquireSuccess(LimitInDays);
                reTryCount = 0;

            }

        }

    }

    /***
     * Dismiss Dialog
     */
    private void dismissDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    //An Alert dialog
    private void alertDialog(String title, String message, String buttonText, final String action) {

        isReported = false;

        try {
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            mAlertDialog
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(buttonText,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {

                                        if (action.equalsIgnoreCase("1")) {

                                        } else {

                                            ErrorLog.ReportedByUsers();
                                            isReported = true;
                                        }

                                        dialog.cancel();

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }
                            }).show();

            mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    if (action.equalsIgnoreCase("1")) {

                    } else {

                        if (!isReported)
                            ErrorLog.ReportedByUsers();
                    }

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


    //An Alert dialog
    private void alertDialogSubscriptionExpire(final Context mContext) {

        isReported = false;

        /*Added to send event when subscription expired*/
        LoginUser mUser = new SharedPrefrences(mContext).getLoginUser();
        if (mUser != null) {

            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            //Key and their values which will be send to Mix Panel
            hashMap.put("USER_ID", mUser.getStudentID());
            hashMap.put("NAME", mUser.getFullName());
            hashMap.put("EMAIL_ID", mUser.getEmail());

            MixPanelClass mixPanelClass = new MixPanelClass(mContext);
            mixPanelClass.sendData(MixPanelClass.MPE_SUBSCRIPTION_EXPIRED, hashMap);
        }
         /*Ended to send event when subscription expired*/

        try {
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            mAlertDialog
                    .setTitle("Your subscription has expired.")
                    .setMessage("You will need to purchase a plan to continue using Visual Physics.")
                    .setPositiveButton("Buy Now",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {

                                        Uri myUri = Uri.parse("http://www.visualphysics.nlytn.in/buyPackage");

                                        Intent intent = new Intent(((Activity) mContext), NavigationScreen.class);
                                        intent.setData(myUri);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        mContext.startActivity(intent);
                                        ((Activity) mContext).finish();

                                        dialog.cancel();

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }
                            })
                    /*Commented to remove refer a friend button*/
                    /*.setNegativeButton("Refer a Friend",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    Uri myUri = Uri.parse("http://www.visualphysics.nlytn.in/referFriend");

                                    Intent intent = new Intent(((Activity) mContext), NavigationScreen.class);
                                    intent.setData(myUri);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    mContext.startActivity(intent);
                                    ((Activity) mContext).finish();

                                    dialog.cancel();
                                }
                            })*/.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private boolean isNetworkError(int errorCode) {

        if ((errorCode >= 100060 && errorCode <= 100067) || errorCode == 55200 || errorCode == 55201
                || errorCode == 55202 || errorCode == 100363 || errorCode == 101202
                || errorCode == 10613 || errorCode == 10614 || errorCode == 20413 || errorCode == 20414) {

            return true;
        }

        return false;
    }

    /***
     * Display Rate us dialog
     */
    public void getCustomDialog(Context mContext) {

        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.rateus_screen);

        Button mBtnYes = (Button) dialog.findViewById(R.id.btnYesRateUsScreen);
        Button mBtnNo = (Button) dialog.findViewById(R.id.btnNoRateUsScreen);

        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        mBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        dialog.show();

    }


}
