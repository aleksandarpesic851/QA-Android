package Utils;

import android.Manifest;
import android.os.Environment;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;
import com.visualphysics.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import Model.LoginUser;

/**
 * Created by India on 7/16/2016.
 */
public class ErrorLog implements AppConstansts {

    //Links for Crashlytics
    //https://firebase.google.com/docs/crashlytics/force-a-crash?authuser=0
    //https://firebase.google.com/docs/crashlytics/upgrade-from-crash-reporting?authuser=0
    //https://firebase.google.com/docs/crashlytics/get-started?authuser=0#next_steps

    /**
     * Save Error Log in SD card
     *
     * @param exception
     */
    public static void SaveErrorLog(Exception exception) {

        if (PermissionChecker.checkSelfPermission(MyApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            if (IS_ERROR_LOG_SAVE) {

                // File dir = new File(Environment.getExternalStorageDirectory() + "/VisualPhysics/Log.txt");
                File filePath = new File(Environment.getExternalStorageDirectory() + "/VisualPhysics");

                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                File logFile = new File(filePath + "/Log.txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    // BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    String stackTrace = Log.getStackTraceString(exception);
                    buf.append(stackTrace);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    int hours = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    int seconds = calendar.get(Calendar.SECOND);
                    buf.append("Logged at " + String.valueOf(hours + ":" + minutes + ":" + seconds));
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /*try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(logFile.toString(), true));
                    exception.printStackTrace(pw);
                    pw.close();
                } catch (Exception E) {
                    E.printStackTrace();
                }*/

            }
        }

        //saveErrorLog(exception.getMessage());

    }

    public static void SaveInLogFile(String text) {

        if (PermissionChecker.checkSelfPermission(MyApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            if (IS_ERROR_LOG_SAVE) {

                // File dir = new File(Environment.getExternalStorageDirectory() + "/VisualPhysics/Log.txt");
                File filePath = new File(Environment.getExternalStorageDirectory() + "/VisualPhysics");

                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                File logFile = new File(filePath + "/Log.txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    // BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(text);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    int hours = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    int seconds = calendar.get(Calendar.SECOND);
                    buf.append("Logged at " + String.valueOf(hours + ":" + minutes + ":" + seconds));
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /*try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(logFile.toString(), true));
                    exception.printStackTrace(pw);
                    pw.close();
                } catch (Exception E) {
                    E.printStackTrace();
                }*/

            }
        }

        //saveErrorLog(exception.getMessage());

    }


    /***
     * @param text
     */
    public static void saveErrorLog(String text) {

        if (PermissionChecker.checkSelfPermission(MyApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            if (IS_ERROR_LOG_SAVE) {

                File filePath = new File(Environment.getExternalStorageDirectory() + "/VisualPhysics");

                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                File logFile = new File(filePath + "/Log.txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    // BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(text);
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * Send handled Exception on firebase
     *
     * @param exception
     */
    public static void SendErrorReport(Exception exception) {
        Crashlytics.logException(new Exception(exception));
    }

    /***
     * Send handled Exception on firebase
     *
     * @param exception
     */
    public static void SendSignUpErrorReport(Exception exception) {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());

        String reportMessage = "Unable to Login/SignUp";
        reportMessage += "\nDevice Id :" + new AppUtil(MyApplication.getContext()).getDeviceID();

        //FirebaseCrash.report(new Exception("" + exception + "" + reportMessage));
        Crashlytics.logException(new Exception("" + exception + "" + reportMessage));

    }


    /***
     * Send Express play handled Exception on firebase
     *
     * @param exception
     */
    public static void SendErrorReport(ErrorCodeException exception) {
        //Dont send the error report on specific error code
        //-100408 ==> WSB_ERROR_LS_NO_LICENSE

        //if(exception.getErrorCode() != 100408)
        Crashlytics.logException(new Exception(exception));
    }


    /***
     * Send personalization error to Firebase
     *
     * @param exception
     */
    public static void SendErrorReportPersonalization(ErrorCodeException exception) {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        String reportMessage = "Unable to personalize " + exception;

        reportMessage += "\nName :" + mLoginUser.getFullName();
        reportMessage += "\nEmail Id :" + mLoginUser.getEmail();
        reportMessage += "\nStudent Id :" + mLoginUser.getStudentID();
        reportMessage += "\nDevice Id :" + new AppUtil(MyApplication.getContext()).getDeviceID();

        try {

            String deviceID = Runtime.getProperty(Runtime.Property.NEMO_DEVICE_ID).toString();
            String nodeID = Runtime.getProperty(Runtime.Property.PERSONALITY_NODE_ID).toString();

            reportMessage += "\nPersonalization Device ID:" + deviceID;
            reportMessage += "\nPersonalization NODE ID :" + nodeID;

        } catch (Exception e) {

            reportMessage += "\nPersonalization Device ID:" + "";
            reportMessage += "\nPersonalization NODE ID :" + "";
        }

        Crashlytics.logException(new Exception(reportMessage));

    }


    /***
     * Send Express play handled Exception on firebase when user unable to acquire license
     *
     * @param exception
     */
    public static void SendErrorReportUnableToAcquireLicense(ErrorCodeException exception) {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        String reportMessage = "Unable to acquire license " + exception;

        reportMessage += "\nName :" + mLoginUser.getFullName();
        reportMessage += "\nEmail Id :" + mLoginUser.getEmail();
        reportMessage += "\nStudent Id :" + mLoginUser.getStudentID();
        reportMessage += "\nDevice Id :" + new AppUtil(MyApplication.getContext()).getDeviceID();

        try {

            String deviceID = Runtime.getProperty(Runtime.Property.NEMO_DEVICE_ID).toString();
            String nodeID = Runtime.getProperty(Runtime.Property.PERSONALITY_NODE_ID).toString();

            reportMessage += "\nPersonalization Device ID:" + deviceID;
            reportMessage += "\nPersonalization NODE ID :" + nodeID;

        } catch (Exception e) {

            reportMessage += "\nPersonalization Device ID:" + "";
            reportMessage += "\nPersonalization NODE ID :" + "";
        }

        Crashlytics.logException(new Exception(reportMessage));

    }

    /***
     * Send Express play handled Exception on firebase when user unable to acquire license
     *
     * @param
     */
    public static void ReportedByUsers() {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        String reportMessage = "Unable to acquire license issue is reported. User details are- ";

        reportMessage += "\nName :" + mLoginUser.getFullName();
        reportMessage += "\nEmail Id :" + mLoginUser.getEmail();
        reportMessage += "\nStudent Id :" + mLoginUser.getStudentID();
        reportMessage += "\nDevice Id :" + new AppUtil(MyApplication.getContext()).getDeviceID();

        try {

            String deviceID = Runtime.getProperty(Runtime.Property.NEMO_DEVICE_ID).toString();
            String nodeID = Runtime.getProperty(Runtime.Property.PERSONALITY_NODE_ID).toString();

            reportMessage += "\nPersonalization Device ID:" + deviceID;
            reportMessage += "\nPersonalization NODE ID :" + nodeID;

        } catch (Exception e) {

            reportMessage += "\nPersonalization Device ID:" + "";
            reportMessage += "\nPersonalization NODE ID :" + "";
        }

        Crashlytics.logException(new Exception(reportMessage));
    }


    /***
     * Send Express play handled Exception on firebase when user unable to acquire license
     *
     * @param
     */
    public static void SendErrorReportUnableToPlayVideo(Exception exception) {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(MyApplication.getContext());
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        String reportMessage = "Unable to play video. User details are- ";

        reportMessage += "\nName :" + mLoginUser.getFullName();
        reportMessage += "\nEmail Id :" + mLoginUser.getEmail();
        reportMessage += "\nStudent Id :" + mLoginUser.getStudentID();
        reportMessage += "\nDevice Id :" + new AppUtil(MyApplication.getContext()).getDeviceID();

        try {

            String deviceID = Runtime.getProperty(Runtime.Property.NEMO_DEVICE_ID).toString();
            String nodeID = Runtime.getProperty(Runtime.Property.PERSONALITY_NODE_ID).toString();

            reportMessage += "\nPersonalization Device ID:" + deviceID;
            reportMessage += "\nPersonalization NODE ID :" + nodeID;

        } catch (Exception e) {

            reportMessage += "\nPersonalization Device ID:" + "";
            reportMessage += "\nPersonalization NODE ID :" + "";
        }

        Crashlytics.logException(new Exception("" + exception + "" + reportMessage));
    }


    /***
     * Send Express play handled Exception on firebase when user unable to acquire license
     *
     * @param
     */
    public static void SendErrorReportMixPanel(Exception exception) {

        String reportMessage = "Mix Panel Error- ";

        try {

            Crashlytics.logException(new Exception("" + reportMessage + "" + exception));

        } catch (Exception e) {


        }


    }

    /***
     * Send Express play handled Exception on firebase when user unable to acquire license
     *
     * @param
     */
    public static void SendLocationError(Exception exception) {

        String reportMessage = "Location Error- ";

        try {

            Crashlytics.logException(new Exception("" + reportMessage + "" + exception));

        } catch (Exception e) {


        }


    }


}
