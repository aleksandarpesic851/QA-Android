package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import Adapter.PackageAdapter;
import Model.Packages;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import buy.BuyFragmentWithTabs;
import buy.BuyViewPagerAdapter;
import buy.OnPackageSelectionListener;

/**
 * Created by admin on 5/19/2016.
 */
public class BuyFrag extends Fragment implements OnTaskCompleted.CallBackListener, View
        .OnClickListener, OnPackageSelectionListener {

    private View parentView;
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private RecyclerView mPackageRecyclerView;
    private ArrayList<Packages> mPackageArrayList;
    private PackageAdapter mPackageAdapter;
    private int SUCCESS_REQUEST_CODE = 101;

    public static String KEY_EXPIRY_DATE = "keyExpiryDate";
    public static String KEY_PACKAGE_CATEGORY_ID = "keyPackageCategoryID";

    private long expiryTimeInMillis = 0;
    public static String categoryPackageID = "";
    public Button btnNextPackageScreen;

    public int selectedPackageID = 0;
    //Get currency type
    private String currencyType = BuyViewPagerAdapter.CURRENCY_TYPE_INR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.buyfrag, container, false);

        mDeclaration();

        return parentView;

    }

    /***
     * Initialize the resources
     */
    private void mDeclaration() {

        //To show toast
        //mApiCall = new ApiCall(getActivity());

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        mPackageRecyclerView = (RecyclerView) parentView.findViewById(R.id.recyclerViewPackageScreen);
        //parentView.findViewById(R.id.btnPaypalPackageScreen).setOnClickListener(this);
        //parentView.findViewById(R.id.btnPaytmPackageScreen).setOnClickListener(this);

        //Make it static so that we can access it in another fragment to manage its click
        btnNextPackageScreen = parentView.findViewById(R.id.btnNextPackageScreen);

        //This is listener on the item
        btnNextPackageScreen.setOnClickListener(this);

        parentView.findViewById(R.id.txtMoreOptionPackageScreen).setOnClickListener(this);

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(getActivity());

        expiryTimeInMillis = Long.parseLong(mSharedPrefrences.getPreferences(BuyFrag.KEY_EXPIRY_DATE, "0"));

        //The time has expired; expiry date is less than current date
        if (expiryTimeInMillis > AppUtil.getCurrentTimeInMillis()) {

            categoryPackageID = mSharedPrefrences.getPreferences(BuyFrag.KEY_PACKAGE_CATEGORY_ID, "");

        } else {
            categoryPackageID = "";
        }

        //Get packages according to category
        getPackages(categoryPackageID);

    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {

        if (Method.equals(mApiCall.GetPackages)) {
            parseResponseForPackageData(result);
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
     * Check the internet connection if connection available then get the packages
     */
    private void getPackages(String categoryPackageID) {

        if (!mAppUtils.getConnectionState()) {

            mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            mApiCall.getPackage(new OnTaskCompleted(this), mApiCall.GetPackages, categoryPackageID);

        }
    }

    /***
     * Parse the reponse of package data
     *
     * @param response
     */
    private void parseResponseForPackageData(JSONObject response) {
        try {

            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetPackages);
            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));

            } else if (ErrorCode == 2) {

                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));

            } else if (ErrorCode == 0) {

                Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();

                Packages[] PackageArray = gson.fromJson(jaArray, Packages[].class);
                mPackageArrayList = new ArrayList<Packages>(Arrays.asList(PackageArray));


                //Commented to open new buy fragment
                /*mPackageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                // 3. create an adapter
                mPackageAdapter = new PackageAdapter(mPackageArrayList,
                        getActivity());
                // 4. set adapter
                mPackageRecyclerView.setAdapter(mPackageAdapter);*/


                //This will call new fragment
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new BuyFragmentWithTabs(mPackageArrayList, this)).commit();


            }
        } catch (Exception e) {

            e.printStackTrace();

            if (mAppUtils != null) {
                if (getActivity() != null) {

                    /*mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            getResources().getString(R.string.Error_Msg_Try_Later));*/

                    /*Added by IZISS to show Encryption error if any*/
                    mAppUtils.displaySnackBarWithEncryptionErrorMessage(getActivity(), getActivity().findViewById(android.R.id.content), response);

                }
            }

            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {
            dismissDialog();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
           /* case R.id.btnPaypalPackageScreen:
                if(mPackageAdapter.getPackageID() == 0){
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            "Please select any one package");
                }
                else{
                    Intent intentPayPal = new Intent(getActivity(),PaymentWebViewScreenActivity.class);
                    intentPayPal.putExtra("PackageID",mPackageAdapter.getPackageID());
                    intentPayPal.putExtra("PaymentType","Paypal");
                    startActivity(intentPayPal);
                }
                break;
            case R.id.btnPaytmPackageScreen:
                if(mPackageAdapter.getPackageID() == 0){
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            "Please select any one package");
                }
                else{
                    Intent intentPaytm = new Intent(getActivity(),PaymentWebViewScreenActivity.class);
                    intentPaytm.putExtra("PackageID",mPackageAdapter.getPackageID());
                    intentPaytm.putExtra("PaymentType","Paytm");
                    startActivity(intentPaytm);
                }
                break;*/


            //Comment this because handled on other fragment
            case R.id.btnNextPackageScreen:

                if (selectedPackageID == 0) {
                    mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                            "Please select subscription");
                } else {
                    Intent intentPaytm = new Intent(getActivity(), PaymentOptionScreenActivity.class);
                    intentPaytm.putExtra("PackageID", selectedPackageID);
                    intentPaytm.putExtra(BuyViewPagerAdapter.KEY_CURRENCY_TYPE, currencyType);
                    startActivityForResult(intentPaytm, SUCCESS_REQUEST_CODE);
                }

                break;

            case R.id.txtMoreOptionPackageScreen:

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/yStokb"));
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nlytn.in/buy.php"));

                startActivity(browserIntent);

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SUCCESS_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                NavigationScreen navigationScreen = (NavigationScreen) getActivity();
                navigationScreen.onNavigationItemSelected(1);
            }
        }
    }

    @Override
    public void onPackageSelected(Packages mPackages, int position, String currencyType) {

        if (mPackages != null) {
            selectedPackageID = mPackages.getPackageID();
            this.currencyType = currencyType;
        } else {
            selectedPackageID = 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {

            //Handle issues in Redmi/Moto devices due to illegal state exception
            BuyFragmentWithTabs f = (BuyFragmentWithTabs) getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

            if (f != null)
                getActivity().getSupportFragmentManager().beginTransaction().remove(f).commit();
        } catch (Exception ex) {

        }

    }
}
