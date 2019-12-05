package fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.visualphysics.ApiCall;
import com.visualphysics.IndianFormActivity;
import com.visualphysics.NavigationScreen;
import com.visualphysics.OTPScreen;
import com.visualphysics.R;

import org.json.JSONObject;

import java.util.HashMap;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import fragment.model.Questionnaire;
import mixpanel.MixPanelClass;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by saisasank on 6/3/18.
 */
public class FreeTrialFragment extends Fragment implements OnTaskCompleted.CallBackListener {

    private RelativeLayout mFreeTrialBtn;
    private TextView mFreeTrialTxt, txtThreeDays, help_txt;
    private int mSelectedOption = 1;
    private int mSelectedOption2 = -1;
    private int mSelectedOption3 = -1;

    //For Questionnaire data service
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_trial, container, false);

        mFreeTrialBtn = view.findViewById(R.id.submit_btn);
        mFreeTrialTxt = view.findViewById(R.id.free_trial_txt);

        txtThreeDays = view.findViewById(R.id.txtThreeDays);
        help_txt = view.findViewById(R.id.help_txt);

        ((IndianFormActivity) getActivity()).mActionBarTitle.setText(getResources().getString(R.string.welcome));

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mSharedPref = new SharedPrefrences(getActivity());

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);


        MixPanelClass mixPanelClass = new MixPanelClass(getActivity());
        mixPanelClass.setPref(MixPanelClass.IS_FROM_FREE_TRIAL, true);

        if (MixPanelClass.IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL) {

            txtThreeDays.setVisibility(View.GONE);
            help_txt.setText(getActivity().getResources().getString(R.string.msg_free_trial_taken));

            MixPanelClass.IS_REPEAT_LOGIN_WITH_DIFFERENT_EMAIL = false;
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mSelectedOption = bundle.getInt(Questionnaire1Fragment.OPTION, 1);

            if (mSelectedOption == 1) {
                mSelectedOption2 = bundle.getInt(Questionnaire2Fragment.OPTION_2, 1);
                mSelectedOption3 = bundle.getInt(Questionnaire2Fragment.OPTION_3, 1);

            }
        }

        switch (mSelectedOption) {
            case 1:
                String stringIdentifier = "msg_free_trial_" + mSelectedOption + "_" + mSelectedOption2 + "_" + mSelectedOption3;
                int resId = getContext().getResources().getIdentifier(stringIdentifier, "string", getActivity().getPackageName());

                String freeTrialTxt = getResources().getString(resId);
                mFreeTrialTxt.setText(freeTrialTxt);
                break;
            case 2:
                mFreeTrialTxt.setText(getContext().getString(R.string.msg_free_trial_2));
                break;
            case 3:
                mFreeTrialTxt.setText(getContext().getString(R.string.msg_free_trial_3));
                break;
            case 4:
                mFreeTrialTxt.setText(getContext().getString(R.string.msg_free_trial_4));
                break;
        }

        mFreeTrialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //This method will send the Questionnaire data to server
                sendQuestionnaireData();

                //IZISS has remove navigation flow and shift it to server response
            }
        });
    }

    /***
     * On Submit Form; Navigate to next screen
     */
    private void onSubmitSuccess() {

        SharedPrefrences mSharePref = new SharedPrefrences(getActivity());

        LoginUser User = mSharePref.getLoginUser();

        if (IndianFormActivity.isReferralCodeApplied)
            User.setFriendReferralCode(IndianFormActivity.strReferralCode);

        //Update NewDevice to Null so that at the Time of Splash Screen(Ref MoveTONextScreen()) consider as old device
        User.setNewDevice(null);

        Gson gson = new Gson();
        String jsonString = gson.toJson(User);

        //Update User Detail
        mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

        //Intent to start new activity
        Intent intent;

        //Check if the screen is appeared once and the app is closed
        MixPanelClass mixPanelClass = new MixPanelClass(getActivity());
        mixPanelClass.setPref(MixPanelClass.PREF_IS_QUESTIONNAIRE_COMPLETED, true);


        if (User.getIsOTPChecked().equals("0")) {
            intent = new Intent(getActivity(), OTPScreen.class);
            startActivity(intent);
            getActivity().finish();
        } else {

            intent = new Intent(getActivity(), NavigationScreen.class);
            startActivity(intent);
            getActivity().finish();
        }

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


            //Send Questionnaire data to Mixpanel this is before event so avoid calling identify

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(getActivity());

            //If user login with different email id on same device
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(MixPanelClass.MPA_USE, VPUse);
            hashMap.put(MixPanelClass.MPA_EXAM, VPExam);
            hashMap.put(MixPanelClass.MPA_YEAR, VPYear);
            mixPanelClass.sendData(MixPanelClass.MPE_QUESTIONNAIRE_SUBMIT, hashMap);

            //This will send the questionnaire data only once when user come from Free Trial screen and is Indian User
            //Set Questionnaire data to mixpanel
            // Added on 20 December 2018
            mixPanelClass.setPeopleAttributeForQuestionnaire();

            //Parameters total 7 for varargs are StudentID, StudentEmailID, VPUse,VPExam,VPYear
            mApiCall.sendQuestionnaireData(new OnTaskCompleted(this), mApiCall.SendQuestionnaireData, StudentID, StudentEmailID, VPUse, VPExam, VPYear);
        }

    }

    /***
     * Parse the response of send Questionnaire data
     *
     * @param response
     */
    private void parseResponseForQuestionnaireData(JSONObject response) {

        try {

            JSONObject mJsonObj = response.getJSONObject(mApiCall.SendQuestionnaireData);

            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {

            } else if (ErrorCode == 2) {

            } else if (ErrorCode == 0) {


                //Added to save tagID just after response from server
                SharedPrefrences mSharePref = new SharedPrefrences(getApplicationContext());

                LoginUser User = mSharePref.getLoginUser();

                //This will set Selected TagId to user preference
                User.setTagID(mJsonObj.optString("TagID"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User detail shared preference
                mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

                //Navigate to next screen
                onSubmitSuccess();

            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {

        dismissDialog();

        if (Method.equals(mApiCall.SendQuestionnaireData)) {
            parseResponseForQuestionnaireData(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {

        dismissDialog();

        // mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
        //        getResources().getString(R.string.Error_Msg_Try_Later));

        ErrorLog.SendErrorReport(error);

    }


}
