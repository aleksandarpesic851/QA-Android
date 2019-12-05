package com.visualphysics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import Adapter.AskToExpertDiscussionList_ListAdpter;
import Model.AskToExpertDiscusssionList;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

/**
 * Get the list of discussion of Question
 */
public class AskToExpertDiscussionListScreenActivity extends AppCompatActivity implements OnTaskCompleted.CallBackListener, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private View parentView;
    private ArrayList<AskToExpertDiscusssionList> mAskToExpertDisscussionArrayList;
    private AskToExpertDiscussionList_ListAdpter mAskToExpertDiscussionListAdapter;
    private FloatingActionButton mFabBtnAddComment;
    private EditText mEdtComment;
    private TextView mTxtTitle;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;
    private String strComment,TitleName = "";

    private int AskToExpertID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_to_expert_disussion_list_screen_activity);


        mDeclaration();
    }

    /***
     * Initialize the resources
     */
    private void mDeclaration(){

        Intent i = getIntent();
        if(i.hasExtra("AskToExpertID")){
            AskToExpertID = i.getIntExtra("AskToExpertID",-1);
            TitleName = i.getStringExtra("TitleName");
        }

        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.custom_toolbar, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        getSupportActionBar().setTitle("Discussion");
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(viewActionBar,params);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(AskToExpertDiscussionListScreenActivity.this);
        mProgressDialog = new ProgressDialog(AskToExpertDiscussionListScreenActivity.this);
        mProgressDialog.setCancelable(false);

        mEdtComment = (EditText) findViewById(R.id.edtMessageAskToExpertDiscussionListScreen);

        mTxtTitle = (TextView) findViewById(R.id.txtTitleCustomToolBar);
        mTxtTitle.setText(TitleName);

        mFabBtnAddComment = (FloatingActionButton)findViewById(R.id.fabSubmitAskToExpertDiscussionListScreen);
        mFabBtnAddComment.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewAskToExpertDiscussionListScreen);


        getDiscussionOfAskToExpert();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetDiscussionAskToExpert)) {
            parseResponseForAskToExpertDisucssionList(result);
        }
        else if(Method.equals(mApiCall.AddMessageToDiscussionAskToExpert))
            parseResponseForAddMessageToAskToExpertDisucssionData(result);
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                getResources().getString(R.string.Error_Msg_Try_Later));
    }



    /***
     * Check the internet connection if connection available then get the list of
     */
    private void getDiscussionOfAskToExpert() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();

            mApiCall.getDiscussionOfAskToExpertList(String.valueOf(AskToExpertID),
                    new OnTaskCompleted(this), mApiCall.GetDiscussionAskToExpert);

        }
    }

    /***
     * Parse the response of list of discussion of Ask to Expert topic
     * @param response
     */
    private void parseResponseForAskToExpertDisucssionList(JSONObject response){
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetDiscussionAskToExpert);
            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1){
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));

            }
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();

                AskToExpertDiscusssionList[] AskToExpertDisscussionListArray = gson.fromJson(jaArray, AskToExpertDiscusssionList[].class);
                mAskToExpertDisscussionArrayList = new ArrayList<AskToExpertDiscusssionList>(Arrays.asList(AskToExpertDisscussionListArray));

                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                // 3. create an adapter
                mAskToExpertDiscussionListAdapter = new AskToExpertDiscussionList_ListAdpter(mAskToExpertDisscussionArrayList,
                        getApplicationContext());
                // 4. set adapter
                mRecyclerView.setAdapter(mAskToExpertDiscussionListAdapter);


            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * Parse the response of Add comment to Discussion of Ask to Expert topics
     * @param response
     */
    private void parseResponseForAddMessageToAskToExpertDisucssionData(JSONObject response){
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddMessageToDiscussionAskToExpert);
            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1){
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));

            }
            else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
               mEdtComment.setText("");

                Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();

                AskToExpertDiscusssionList[] AskToExpertDisscussionListArray = gson.fromJson(jaArray, AskToExpertDiscusssionList[].class);
                mAskToExpertDisscussionArrayList = new ArrayList<AskToExpertDiscusssionList>(Arrays.asList(AskToExpertDisscussionListArray));

                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                // 3. create an adapter
                mAskToExpertDiscussionListAdapter = new AskToExpertDiscussionList_ListAdpter(mAskToExpertDisscussionArrayList,
                        getApplicationContext());
                // 4. set adapter
                mRecyclerView.setAdapter(mAskToExpertDiscussionListAdapter);

            }
        } catch (Exception e) {
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabSubmitAskToExpertDiscussionListScreen:
                strComment = mEdtComment.getText().toString().trim();
                if(!TextUtils.isEmpty(strComment)){
                    if (!mAppUtils.getConnectionState()) {
                        mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                    } else {
                        mProgressDialog.setMessage("Please wait...");
                        mProgressDialog.show();

                        SharedPrefrences mSharePref = new SharedPrefrences(getApplicationContext());
                        mApiCall.doAddMessageToAskToExpertDiscussion(mSharePref.getLoginUser().getStudentID(),
                                "Student",strComment,String.valueOf(AskToExpertID),
                                new OnTaskCompleted(this), mApiCall.AddMessageToDiscussionAskToExpert);

                    }
                }
                break;
            default:
                break;
        }
    }
}
