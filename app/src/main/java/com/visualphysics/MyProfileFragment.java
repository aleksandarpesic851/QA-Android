package com.visualphysics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import mixpanel.MixPanelClass;

/**
 * Created by admin on 5/19/2016.
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener, OnTaskCompleted.CallBackListener {

    private View mParent;
    private EditText mEdtFullName, mEdtEmail, mEdtMobile, mEdtUserID, mEdtSubscriptionEndDate, mEdtCouponCode;
    private Button mBtnGetSubscriptionEndDate, mBtnApplyCouponCode, btnRedeemCredits;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private SharedPrefrences mSharedPref;
    private LoginUser User;

    //IZISS
    String COUPON_CODE = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {

            Bundle bundle = getArguments();
            if (bundle != null) {
                if (bundle.getString("COUPON_CODE") != null) {

                    COUPON_CODE = bundle.getString("COUPON_CODE");

                }
            }
        } catch (Exception ex) {
            ErrorLog.SendErrorReport(ex);
        }

        mParent = inflater.inflate(
                R.layout.myprofilefrag, container, false);

        mDeclaration();
        return mParent;
    }

    /***
     * Initialize member variables
     */
    void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mSharedPref = new SharedPrefrences(getActivity());

        mEdtFullName = (EditText) mParent.findViewById(R.id.edtFullNameProfileScreen);
        mEdtFullName.setInputType(InputType.TYPE_NULL);

        mEdtEmail = (EditText) mParent.findViewById(R.id.edtEmailProfileScreen);
        mEdtEmail.setInputType(InputType.TYPE_NULL);

        mEdtMobile = (EditText) mParent.findViewById(R.id.edtMobileProfileScreen);
        mEdtMobile.setInputType(InputType.TYPE_NULL);

        mEdtUserID = (EditText) mParent.findViewById(R.id.edtUserIDProfileScreen);
        mEdtUserID.setInputType(InputType.TYPE_NULL);

        mEdtSubscriptionEndDate = (EditText) mParent.findViewById(R.id.edtSubscriptionEndDateProfileScreen);
        mEdtSubscriptionEndDate.setInputType(InputType.TYPE_NULL);

        mEdtCouponCode = (EditText) mParent.findViewById(R.id.edtCouponCodeProfileScreen);
        //mEdtCouponCode.setInputType(InputType.TYPE_NULL);

        mBtnGetSubscriptionEndDate = (Button) mParent.findViewById(R.id.btnGetSubscriptionEndDateProfileSceen);
        mBtnGetSubscriptionEndDate.setOnClickListener(this);

        mBtnApplyCouponCode = (Button) mParent.findViewById(R.id.btnApplyCouponProfileSceen);
        mBtnApplyCouponCode.setOnClickListener(this);

        ImageView mEdit = (ImageView) mParent.findViewById(R.id.edit_MyProfileFrag);
        mEdit.setOnClickListener(this);


        //BY IZISS
        mEdtCouponCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i("", "Enter pressed");

                    mBtnApplyCouponCode.performClick();
                }
                return false;
            }
        });

        setUserData();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.edit_MyProfileFrag:

                Intent intent = new Intent(getActivity(), EditProfileScreen.class);
                startActivity(intent);

                break;
            case R.id.btnGetSubscriptionEndDateProfileSceen:

                String DeviceID = mAppUtils.getDeviceID();
                if (!mAppUtils.getConnectionState()) {
                    mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
                } else if (DeviceID == null)
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.error_device_id_not_found));
                else {
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.show();
                    DebugLog.v("Device", "Studen ID " + User.getStudentID());
                    mApiCall.getUserSubscription(User.getStudentID(), DeviceID,
                            new OnTaskCompleted(this), mApiCall.GetUserSubscription);

                }

                break;

            case R.id.btnApplyCouponProfileSceen:
                String CouponCode = mEdtCouponCode.getText().toString().trim();
                CouponCode = CouponCode.toUpperCase();
                if (TextUtils.isEmpty(CouponCode)) {
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            "Please add coupon code.");
                } else {
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.show();
                    mApiCall.addAddSubscriptionByCoupon(User.getStudentID(), CouponCode, mAppUtils.getDeviceID(),
                            new OnTaskCompleted(this), mApiCall.AddSubscriptionByCoupon);
                }
                break;
        }
    }

    /***
     * Set User data
     */
    private void setUserData() {

        User = mSharedPref.getLoginUser();

        if (User != null) {

            String strSubscriptionEndDate = User.getSubscriptionEndDate();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String strDate = mAppUtils.convertUTCToLocalTime(simpleDateFormat,
                    strSubscriptionEndDate);

            mEdtSubscriptionEndDate.setText(strDate);

            /* try{
                Date d = new Date(strDate);
                String mydate = simpleDateFormat.format(d);
                Date newDate = simpleDateFormat.parse(mydate);

                simpleDateFormat = new SimpleDateFormat("MM-dd-yy hh:mm a");
                String date = simpleDateFormat.format(d);
                strSubscriptionEndDate = date;
            }
            catch(Exception e){
                e.printStackTrace();
            }*/

            mEdtFullName.setText(User.getFullName());
            mEdtEmail.setText(User.getEmail());
            mEdtMobile.setText(User.getCellPhone());
            mEdtUserID.setText(User.getReferralCode());

            mEdtCouponCode.setText("" + COUPON_CODE);

            //mEdtSubscriptionEndDate.setText(strSubscriptionEndDate);
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetUserSubscription)) {
            parseResponseForGetSubscriptionEndDateData(result);
        } else if (Method.equals(mApiCall.AddSubscriptionByCoupon))
            parseResponseAddSubscriptionByCoupon(result);

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
     * Parse the response of Getting Subscription end date data
     *
     * @param response
     */
    private void parseResponseForGetSubscriptionEndDateData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetUserSubscription);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                mJsonObj = mJsonObj.getJSONObject("data");
                LoginUser User = mSharedPref.getLoginUser();
                User.setSubscriptionPeriod(mJsonObj.getString("SubscriptionPeriod"));
                User.setSubscriptionEndDate(mJsonObj.getString("SubscriptionEndDate"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, jsonString);

                //IZISS MPE before clear coupon code
                sendToMixpanel(false);

                setUserData();

            }
        } catch (Exception e) {
            e.printStackTrace();

            /*mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));*/

            /*Added by IZISS to show Encryption error if any*/
            mAppUtils.displaySnackBarWithEncryptionErrorMessage(getActivity(), getActivity().findViewById(android.R.id.content), response);
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * @param response
     */
    private void parseResponseAddSubscriptionByCoupon(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddSubscriptionByCoupon);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
                mJsonObj = mJsonObj.getJSONObject("data");
                LoginUser User = mSharedPref.getLoginUser();
                //User.setSubscriptionPeriod(mJsonObj.getString("SubscriptionPeriod"));
                User.setSubscriptionEndDate(mJsonObj.getString("SubscriptionEndDate"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharedPref.setPreferences(mSharedPref.USERDETAIL, jsonString);

                //IZISS MPE before clear coupon code
                sendToMixpanel(true);

                setUserData();

                mEdtCouponCode.setText("");

            }
        } catch (Exception e) {
            e.printStackTrace();

            /*mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));*/

            /*Added by IZISS to show Encryption error if any*/
            mAppUtils.displaySnackBarWithEncryptionErrorMessage(getActivity(), getActivity().findViewById(android.R.id.content), response);
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
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
     * Send data to mixpanel
     *
     * @param
     */
    private void sendToMixpanel(boolean isFromCoupon) {
        {
            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            //Key and their values which will be send to Mix Panel
            hashMap.put("COUPON_CODE", "" + mEdtCouponCode.getText().toString());
            hashMap.put("DATE", AppUtil.getCurrentDateTime(getActivity()));

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(getActivity());

            if (isFromCoupon) {
                //Send data to our function which will be further sent to Mix Panel
                mixPanelClass.sendData(MixPanelClass.MPE_COUPON_USED, hashMap);
            }

            LoginUser mUser = new SharedPrefrences(getActivity()).getLoginUser();

            if (mUser != null) {

                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_SUBSCRIPTION_END_DATE, mUser.getSubscriptionEndDate(), false);

            }

        }


    }
}
