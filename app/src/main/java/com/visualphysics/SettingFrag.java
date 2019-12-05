package com.visualphysics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Database.DataBase;
import Model.AppMenu;
import Model.Chapters;
import Model.LoginUser;
import Model.Videos;
import UIControl.CustomTextViewRegular;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.ErrorLog;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import fragment.FreeTrialFragment;
import fragment.Questionnaire1Fragment;
import fragment.model.Questionnaire;
import mixpanel.MixPanelClass;

import static Utils.AppUtil.hideKeyBoard;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by admin on 5/19/2016.
 * Sync data having the same logic in login screen
 */
public class SettingFrag extends Fragment implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private View mParent;
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;

    //For Preference updation
    private RelativeLayout mOption21;
    private RelativeLayout mOption22;
    private RelativeLayout mOption23;
    private RelativeLayout mOption24;

    private CustomTextViewRegular mOption21Ans;
    private CustomTextViewRegular mOption22Ans;
    private CustomTextViewRegular mOption23Ans;
    private CustomTextViewRegular mOption24Ans;

    //private EditText mOption24Ans;
    private Button mSubmitBtn;

    private ScrollView scrollView;
    private Button btnExams;
    private ImageView ivExam;
    private FrameLayout frameLayoutExams;

    private int mSelectedOption2 = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mParent = inflater.inflate(
                R.layout.settingfrag, container, false);

        mDeclaration();

        return mParent;
    }


    void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mSharedPref = new SharedPrefrences(getActivity());

        Button mChangePassword = (Button) mParent.findViewById(R.id.btn_ChangePasswordSettingFrag);
        mChangePassword.setOnClickListener(this);

        ((Button) mParent.findViewById(R.id.btn_SyncDataSettingFrag)).setOnClickListener(this);

   /*     Button mPrivacyPolicy=(Button)mParent.findViewById(R.id.btn_PrivacyPolicySettingFrag);
        mPrivacyPolicy.setOnClickListener(this);
*/
        ((Button) mParent.findViewById(R.id.btnDelinkDeviceSettingFrag)).setOnClickListener(this);

        if (mSharedPref.getLoginUser().getRegType().equals(ApplicationConfiguration.UserRegType.Google.toString())
                || mSharedPref.getLoginUser().getRegType().equals(ApplicationConfiguration.UserRegType.Facebook.toString()))

        {
            ((FrameLayout) mParent.findViewById(R.id.frameLayoutChangePasswordSettingScreen)).setVisibility(View.GONE);
            ((View) mParent.findViewById(R.id.speratorChangePasswordSettingScreen)).setVisibility(View.GONE);
        }

        //To update preference
        mOption21 = mParent.findViewById(R.id.option_21);
        mOption22 = mParent.findViewById(R.id.option_22);
        mOption23 = mParent.findViewById(R.id.option_23);
        mOption24 = mParent.findViewById(R.id.option_24);

        mOption21Ans = mParent.findViewById(R.id.option_21_ans);
        mOption22Ans = mParent.findViewById(R.id.option_22_ans);
        mOption23Ans = mParent.findViewById(R.id.option_23_ans);
        mOption24Ans = mParent.findViewById(R.id.option_24_ans);

        mSubmitBtn = mParent.findViewById(R.id.btnSave);

        scrollView = mParent.findViewById(R.id.scrollView);
        btnExams = mParent.findViewById(R.id.btnExams);
        ivExam = mParent.findViewById(R.id.ivExam);
        frameLayoutExams = mParent.findViewById(R.id.frameLayoutExams);

        mSubmitBtn.setOnClickListener(this);
        btnExams.setOnClickListener(this);

        mOption21.setOnClickListener(this);
        mOption22.setOnClickListener(this);
        mOption23.setOnClickListener(this);
        mOption24.setOnClickListener(this);
        mOption24Ans.setOnClickListener(this);

        MixPanelClass mixPanelClass = new MixPanelClass(getActivity());

        switch (mixPanelClass.getTagID()) {
            case "1":
                mOption21.performClick();
                break;
            case "2":
                mOption22.performClick();
                break;
            case "3":
                mOption23.performClick();
                break;
            case "4":
                mOption24.performClick();
                break;

        }

        //If user has already played the questionnaire then the value is not blank
        if (!mixPanelClass.isQuestionnaireAnswered() || !mixPanelClass.isValidSavedTagID()) {
            //We need to check first wether he is an Indian user or not if not then we need to keep the current flow
            checkIndianUser();
        }

    }

    @Override
    public void onClick(View v) {

        Intent intent = null;

        /*Start IZISS*/
        int previousSelectionOption2 = mSelectedOption2;
        boolean isOptionSelected2 = false;

        /*End IZISS*/

        switch (v.getId()) {

            case R.id.btn_ChangePasswordSettingFrag:
                intent = new Intent(getActivity(), ChangePasswordScreen.class);
                startActivity(intent);
                break;

            case R.id.btn_SyncDataSettingFrag:
                if (!mAppUtils.getConnectionState()) {
                    android.app.AlertDialog.Builder myAlertDialog = new android.app.AlertDialog.Builder(getActivity(),
                            R.style.AppCompatAlertDialogStyle);
                    myAlertDialog.setTitle(R.string.app_name);
                    myAlertDialog.setMessage("Please connect to internet for updating database.");
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
                } else {
                    getLatestUpdate(); // Update app
                }
                break;


            case R.id.btnExams:

                if (scrollView.getVisibility() == View.VISIBLE) {

                    ivExam.setImageResource(R.drawable.right_arrow);
                    scrollView.setVisibility(View.GONE);

                } else {

                    ivExam.setImageResource(R.drawable.down_arrow);
                    scrollView.setVisibility(View.VISIBLE);

                }

                break;


            /*case R.id.btn_PrivacyPolicySettingFrag:
                intent=new Intent(getActivity(),PrivacyPolicyScreen.class);
                startActivity(intent);
                break;*/

           /* case R.id.btn_RateAppSettingFrag:
                Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                }
                break;*/

            case R.id.btnDelinkDeviceSettingFrag:
                final OnTaskCompleted listener = new OnTaskCompleted(this);
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle("")
                        .setMessage(R.string.msg_confirm_delink_device)
                        .setPositiveButton("Yes", (dialog, whichButton) -> {
                            if (!mAppUtils.getConnectionState()) {
                                mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
                            } else {
                                SharedPrefrences mSharedPref = new SharedPrefrences(getActivity());
                                mProgressDialog.setMessage("Please wait...");
                                mProgressDialog.show();
                                /*mApiCall.doDeleteAccount(mSharedPref.getLoginUser().getStudentID(),
                                        listener, mApiCall.DeleteAccount);*/
                                mApiCall.delinkDevice(mSharedPref.getLoginUser().getStudentID(), mAppUtils.getDeviceID(),
                                        listener, mApiCall.DelinkDevice);
                            }
                        }).setNegativeButton("No", (dialog, whichButton) -> {
                            // Do nothing.
                        }
                ).show();
                break;


            /*IZISS Start to handel preference click*/

            case R.id.option_21:
                mSelectedOption2 = 1;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_22:
                mSelectedOption2 = 2;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_23:
                mSelectedOption2 = 3;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_24:
                mSelectedOption2 = 4;
                isOptionSelected2 = true;

                break;
            case R.id.option_24_ans:
                mSelectedOption2 = 4;
                isOptionSelected2 = true;
                break;
        }

        if ((isOptionSelected2) && (previousSelectionOption2 != mSelectedOption2)) {
            if (previousSelectionOption2 > 0) {
                handlingBackground(getBackgroundViewOfPosition(previousSelectionOption2), getTextViewOfPosition(previousSelectionOption2), false);
            }
            if (mSelectedOption2 > 0) {
                handlingBackground(getBackgroundViewOfPosition(mSelectedOption2), getTextViewOfPosition(mSelectedOption2), true);
            }
            return;
        }

        if (v.getId() == R.id.btnSave) {
            if (mSelectedOption2 > 0) {
                if (mSelectedOption2 == 4) {
                    saveOptions();

                } else {
                    saveOptions();
                }

            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_choose_an_option), Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.DelinkDevice)) {
            parseResponseForDelinkDeviceData(result);
        } else if (Method.equals(mApiCall.CheckUpdates)) {
            parseResponseForLatestUpdateData(result);
        } else if (Method.equals(mApiCall.SendQuestionnaireData)) {
            parseResponseForQuestionnaireData(result);
        }else if (Method.equals(mApiCall.GetLocation)) {
            parseLocationResponse(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                getResources().getString(R.string.Error_Msg_Try_Later));
    }

    /***
     * Parse the response of Delete Account
     *
     * @param response
     */
    private void parseResponseForDelinkDeviceData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.DelinkDevice);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 5) {
                Toast.makeText(getActivity(), mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                SharedPrefrences mSharedPref = new SharedPrefrences(getActivity());
                mSharedPref.clearAll();

                LicenseUtil License = new LicenseUtil();
                License.removeLicenseOnDelinkDevice();

                Intent intent = new Intent(getActivity(), LoginScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            } else if (ErrorCode == 0) {
                mAppUtils.displayToastWithMessage(getActivity(),
                        mJsonObj.getString("Message"));
                SharedPrefrences mSharedPref = new SharedPrefrences(getActivity());
                mSharedPref.clearAll();

                LicenseUtil License = new LicenseUtil();
                License.removeLicenseOnDelinkDevice();

                Intent intent = new Intent(getActivity(), LoginScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();

            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Get the latest update if any avilable
     */
    private void getLatestUpdate() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
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

            mProgressDialog.setMessage("Sync in Progress, Please wait.");
            mProgressDialog.show();

            String UserID = "";
            if (mSharedPref.getLoginUser() != null)
                UserID = mSharedPref.getLoginUser().getStudentID();

            mApiCall.getLatestUpdate(UserID,
                    mAppUtils.getDeviceID(), mSharedPref.getPreferences(mSharedPref.LAST_SYNC_DATE, ""), strUTCDateTime,
                    new OnTaskCompleted(this), mApiCall.CheckUpdates);

        }
    }

    /***
     * Parse the response of Check Update
     *
     * @param response
     */
    private void parseResponseForLatestUpdateData(JSONObject response) {
        int CurrentAppVersion = -1;
        DataBase db;
        db = new DataBase(getActivity());
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.CheckUpdates);
            int ErrorCode = mJsonObj.getInt("Error");

            mJsonObj = mJsonObj.getJSONObject("data");

            //Set the default count for the APP Open
            // setDefaultCountForAppOpen();

            //Add Last Syn UTC date time
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String strUTCDateTime = format.format(UTCDateTime);
            mSharedPref.setPreferences(mSharedPref.LAST_SYNC_DATE, strUTCDateTime);

            //When device is delink
            if (ErrorCode == 5) {
                try {
                    LicenseUtil License = new LicenseUtil();
                    License.removeLicenseOnDelinkDevice();
                } catch (Exception e) {
                    ErrorLog.SendErrorReport(e);
                }


                if (mSharedPref.getLoginUser() != null) {
                    String DelinkMsg = response.getJSONObject(mApiCall.CheckUpdates).getString("Message");

                    Toast.makeText(getApplicationContext(), DelinkMsg, Toast.LENGTH_SHORT).show();
                    mSharedPref.clearAll();
                    Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            } else if (ErrorCode == 1) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        response.getJSONObject(mApiCall.CheckUpdates).getString("Message"));
                //Set Config data
                try {
                    //Video base URL Detail
                    mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                    mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                    mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                    mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }
            } else if (ErrorCode == 0) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.msg_sync_data_successfully));
                //Get Chapters
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("chapter"));
                ArrayList mChapterArrayList = Chapters.fromJson(jsonArray);
                if (mChapterArrayList != null && mChapterArrayList.size() > 0)
                    db.doAddChapters(mChapterArrayList);


                //Videos
                jsonArray = new JSONArray(mJsonObj.getString("video"));
                ArrayList mVideoArrayList = Videos.fromJson(jsonArray);
                if (mVideoArrayList != null && mVideoArrayList.size() > 0)
                    db.doAddChapterVideos(mVideoArrayList);

                try {
                    //Video base URL Detail
                    mSharedPref.setPreferences(mSharedPref.STREAM_URL, mJsonObj.getJSONObject("config").getString("OnlineURL"));
                    mSharedPref.setPreferences(mSharedPref.DOWNLOAD_URL, mJsonObj.getJSONObject("config").getString("OfflineURL"));
                    mSharedPref.setPreferences(mSharedPref.SUPPORT_EMAIL, mJsonObj.getJSONObject("config").getString("SupportEmail"));
                    mSharedPref.setPreferences(mSharedPref.APP_VERSION, mJsonObj.getJSONObject("config").getString("AppVersion"));

                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }
            }
            dismissDialog();

            //Get App Menu
            if (mJsonObj.has("appmenu")) {
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("appmenu"));
                ArrayList mAppMenuArrayList = AppMenu.fromJson(jsonArray);
                //Insert the Category Data into Database
                if (db.doAddAppMenu(mAppMenuArrayList)) {

                } else
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));
            }

        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();

            /*mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));*/

            /*Added by IZISS to show Encryption error if any*/
            mAppUtils.displaySnackBarWithEncryptionErrorMessage(getActivity(), getActivity().findViewById(android.R.id.content), response);
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        } finally {

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

    //By Iziss to save preference TagID

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handlingBackground(mOption21, null, false);
        handlingBackground(mOption22, null, false);
        handlingBackground(mOption23, null, false);
        handlingBackground(mOption24, null, false);

        /*mOption24Ans.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    onClick(mOption24Ans);
                }
            }
        });*/
    }

    private void handlingBackground(View view, TextView textView, boolean isSelected) {
        if (isSelected) {
            GradientDrawable background = (GradientDrawable) view.getBackground();
            background.setStroke((int) (Resources.getSystem().getDisplayMetrics().density) * 1, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            if (textView != null) {
                textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            }
        } else {
            GradientDrawable background = (GradientDrawable) view.getBackground();
            background.setStroke((int) (Resources.getSystem().getDisplayMetrics().density) * 1, ResourcesCompat.getColor(getResources(), R.color.grey, null));
            if (textView != null) {
                textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_87, null));
            }
        }
    }

    private View getBackgroundViewOfPosition(int position) {
        View view = null;
        switch (position) {
            case 1:
                view = mOption21;
                break;
            case 2:
                view = mOption22;
                break;
            case 3:
                view = mOption23;
                break;
            case 4:
                view = mOption24;
                break;
        }

        return view;
    }

    private TextView getTextViewOfPosition(int position) {
        TextView textView = null;
        switch (position) {
            case 1:
                textView = mOption21Ans;
                break;
            case 2:
                textView = mOption22Ans;
                break;
            case 3:
                textView = mOption23Ans;
                break;
            case 4:
                textView = mOption24Ans;
                break;

        }

        return textView;
    }

    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {

        boolean status = false;

        String description2 = mOption24Ans.getText().toString();

        if (!TextUtils.isEmpty(description2)) {
            status = true;
        } else {
            Toast.makeText(getActivity(), "You have to write something in Others Field", Toast.LENGTH_SHORT).show();
        }


        return status;
    }


    /***
     * On Submit Form; Navigate to next screen
     */
    private void onSubmitSuccess() {


    }


    /*Changes done by IZISS to send data*/

    /***
     * This will call API to store Questionnaire details to VP server
     */
    private void sendQuestionnaireData() {

        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
        } else {
            LoginUser User = mSharedPref.getLoginUser();
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            String StudentID = User.getStudentID();
            String StudentEmailID = User.getEmail();
            String VPUse = getQuestionPreference("Q1").getOtherOptionAnswer();
            String VPExam = getQuestionPreference("Q2").getOtherOptionAnswer();
            String VPYear = getQuestionPreference("Q3").getOtherOptionAnswer();
            String IsUpdate = "1";

            //Parameters total 7 for varargs are StudentID, StudentEmailID, VPUse,VPExam,VPYear
            mApiCall.sendQuestionnaireData(new OnTaskCompleted(this), mApiCall.SendQuestionnaireData, StudentID, StudentEmailID, VPUse, VPExam, VPYear, IsUpdate);
        }

    }

    /***
     * This will return Questionnaire model against saved shared preference json
     *
     * @param preferenceName
     * @return
     */
    private Questionnaire getQuestionPreference(String preferenceName) {

        Questionnaire mQuestionnaire = new Questionnaire();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Questionnaire", Context.MODE_PRIVATE);

        String questionJson = sharedPreferences.getString(preferenceName, "{}");

        try {

            JSONObject mJsonObject = new JSONObject(questionJson);

            mQuestionnaire = new Gson().fromJson(mJsonObject.toString(), Questionnaire.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mQuestionnaire;

    }

    /***
     * Parse the response of send Questionnaire data
     *
     * @param response
     */
    private void parseResponseForQuestionnaireData(JSONObject response) {

        try {

            SharedPrefrences mSharePref = new SharedPrefrences(getApplicationContext());

            JSONObject mJsonObj = response.getJSONObject(mApiCall.SendQuestionnaireData);

            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {

            } else if (ErrorCode == 2) {

            } else if (ErrorCode == 0) {

                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content), mJsonObj.getString("Message"));

                mJsonObj = mJsonObj.getJSONObject("data");

                LoginUser User = mSharePref.getLoginUser();

                //This will set Selected TagId to user preference
                User.setTagID(mJsonObj.optString("TagID"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User detail shared preference
                mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);


                //This will update the People property on Mixpanel
                String VPExam = getQuestionPreference("Q2").getOtherOptionAnswer();
                new MixPanelClass(getActivity()).updateQuestionnairePreference(VPExam);


            }
        } catch (Exception e) {
            e.printStackTrace();

            /*mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));*/

            /*Added by IZISS to show Encryption error if any*/
            mAppUtils.displaySnackBarWithEncryptionErrorMessage(getActivity(), getActivity().findViewById(android.R.id.content), response);

        }

    }

    /***
     * This will save and or update questionnaire
     */
    private void saveOptions() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Questionnaire", Context.MODE_PRIVATE);

        Questionnaire questionnaire2 = new Questionnaire();
        questionnaire2.setQuestionNo(2);
        questionnaire2.setAnswer(mSelectedOption2);

        if (mSelectedOption2 == 4) {
            questionnaire2.setOtherOptionAnswer(mOption24Ans.getText().toString());
        }

         /*Added by IZISS for Mixpanel*/

        else if (mSelectedOption2 == 3) {
            questionnaire2.setOtherOptionAnswer(mOption23Ans.getText().toString());
        } else if (mSelectedOption2 == 2) {
            questionnaire2.setOtherOptionAnswer(mOption22Ans.getText().toString());
        } else if (mSelectedOption2 == 1) {
            questionnaire2.setOtherOptionAnswer(mOption21Ans.getText().toString());
        }

         /*End by IZISS for Mixpanel*/
        String q2 = new Gson().toJson(questionnaire2);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Q2", q2);

        editor.commit();

        //This will send questionnaire data to server
        sendQuestionnaireData();


    }

    /**
     * Detect if the phone's IP is in INDIA COUNTRY to show a form to user other wise move ahead
     *
     * @return true->For Indian Users; false-> Users of other countries (will keep current flow of application)
     */

    private void checkIndianUser() {

        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();

        mApiCall.getLocationFromIpAddress(new OnTaskCompleted(this), mApiCall.GetLocation);

    }

    /***
     * Parse the response of location via IP address
     * Detect if the phone's IP is in INDIA COUNTRY to show a form to user other wise move ahead
     *
     * @param response
     */
    private void parseLocationResponse(JSONObject response) {

        // true->For Indian Users; false-> Users of other countries (will keep current flow of application)
        boolean isIndianUser = false;

        try {

            try {

                Log.i("Location Response>>", "" + response);

                if (response.optString(ReferalCodeScreenActivity.KEY_INDIA_COUNTRY_NAME).equalsIgnoreCase(ReferalCodeScreenActivity.INDIA_COUNTRY_NAME) && response.optString(ReferalCodeScreenActivity.KEY_INDIA_COUNTRY_CODE).equalsIgnoreCase(ReferalCodeScreenActivity.INDIA_COUNTRY_CODE)) {
                    isIndianUser = true;
                }

            } catch (Exception e) {

                e.printStackTrace();
                ErrorLog.SendLocationError(e);
            }

            /*IF NOT AN INDIAN USER*/
            if (!isIndianUser) {
                //Keep current flow for non-indian users
                frameLayoutExams.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                ((View) mParent.findViewById(R.id.separatorExam)).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
