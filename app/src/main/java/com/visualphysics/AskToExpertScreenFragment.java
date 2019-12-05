package com.visualphysics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import Adapter.AskToExpertList_ListAdpter;
import Model.AskToExpertList;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnLoadMoreListener;
import Utils.OnTaskCompleted;
import Utils.RecyclerItemClickListener;

/**
 * Created by admin on 5/19/2016.
 */
public class AskToExpertScreenFragment extends Fragment implements OnTaskCompleted.CallBackListener, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private View parentView;
    private ArrayList<AskToExpertList> mAskToExpertArrayList;
    private AskToExpertList_ListAdpter mAskToExpertListAdapter;
    private FloatingActionButton mFabBtnAddComment;

    private int PageSize = 10, CurrentPage = 1,LastFetchRecord = 0;
    private boolean isPageLoad = false;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private boolean isLoading;
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private OnLoadMoreListener mOnLoadMoreListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(
                R.layout.ask_to_expert_screen_fragment, container, false);
        mDeclaration();
        return parentView;
    }

    /***
     * Initialize the resources
     */
    private void mDeclaration() {

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        mFabBtnAddComment = (FloatingActionButton) parentView.findViewById(R.id.fabAddCommentAskToExpertScreen);
        mFabBtnAddComment.setOnClickListener(this);

        mRecyclerView = (RecyclerView) parentView.findViewById(R.id.recyclerViewAskToExpertScreen);
        // setAddClickListener();


        getAskToExpertList();


    }

    private void setAddClickListener() {
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click
                        Intent i = new Intent(getActivity(), AskToExpertDiscussionListScreenActivity.class);
                        i.putExtra("AskToExpertID", mAskToExpertArrayList.get(position).getAskToExpertID());
                        i.putExtra("TitleName", mAskToExpertArrayList.get(position).getTitle());
                        startActivity(i);
                    }
                })
        );
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.AskToExpertList)) {
            parseResponseForAskToExpertList(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        hideProgressBar();
        mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                getResources().getString(R.string.Error_Msg_Try_Later));
    }


    /***
     * Check the internet connection if connection available then get the list of
     */
    private void getAskToExpertList() {
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(getActivity().findViewById(android.R.id.content));
        } else {
            if(!isPageLoad){
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();
                isPageLoad = true;
            }

            mApiCall.getAskToExpertList(PageSize, CurrentPage,
                    new OnTaskCompleted(this), mApiCall.AskToExpertList);

        }
    }

    /***
     * Parse the reponse of Ask to Expert Question List
     * @param response
     */
    private void parseResponseForAskToExpertList(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AskToExpertList);
            int ErrorCode = mJsonObj.getInt("Error");

            //Remove the Null Objec
            hideProgressBar();

            if (ErrorCode == 1) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        getResources().getString(R.string.Error_Msg_Try_Later));

            } else if (ErrorCode == 2) {
                mAppUtils.displaySnackBarWithMessage(getActivity().findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
                LastFetchRecord = 0;
            }
            else if (ErrorCode == 0) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                JsonParser jparsor = new JsonParser();
                JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();

                AskToExpertList[] AskToExpertListArray = gson.fromJson(jaArray, AskToExpertList[].class);

                if (mAskToExpertArrayList != null && mAskToExpertArrayList.size() > 0 && mAskToExpertListAdapter != null) {
                    ArrayList<AskToExpertList> localAskToExpertListArray = new ArrayList<AskToExpertList>(Arrays.asList(AskToExpertListArray));
                    mAskToExpertArrayList.addAll(localAskToExpertListArray);
                    mAskToExpertListAdapter.notifyDataSetChanged();
                    LastFetchRecord = localAskToExpertListArray.size();
                } else {
                    mAskToExpertArrayList = new ArrayList<AskToExpertList>(Arrays.asList(AskToExpertListArray));
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                    // 3. create an adapter
                    mAskToExpertListAdapter = new AskToExpertList_ListAdpter(mAskToExpertArrayList,
                            getActivity(), mRecyclerView);
                    // 4. set adapter
                    mRecyclerView.setAdapter(mAskToExpertListAdapter);
                    LastFetchRecord = mAskToExpertArrayList.size();
                    setListener();
                }

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
            case R.id.fabAddCommentAskToExpertScreen:
                startActivityForResult(new Intent(getActivity(), AddCommentAskToExpertScreenActivity.class),101);
                break;
            default:
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if(resultCode == Activity.RESULT_OK){
                PageSize = 10;
                CurrentPage = 1;
                LastFetchRecord = 0;
                isPageLoad = false;
                if(mAskToExpertArrayList!=null){

                    mAskToExpertArrayList.clear();
                    mAskToExpertListAdapter.notifyDataSetChanged();

                }

                getAskToExpertList();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void setListener(){
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && LastFetchRecord > PageSize-1 && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });


        setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mAskToExpertArrayList.add(null);
                mAskToExpertListAdapter.notifyItemInserted(mAskToExpertArrayList.size() - 1);

                CurrentPage = (mAskToExpertArrayList.size() / 10) + 1;
                getAskToExpertList();
                //Load more data for reyclerview


            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void setLoaded() {
        isLoading = false;
    }

    private void hideProgressBar() {
        if (mAskToExpertArrayList != null && mAskToExpertArrayList.size() > 0) {
            mAskToExpertArrayList.remove(mAskToExpertArrayList.size() - 1);
            mAskToExpertListAdapter.notifyItemRemoved(mAskToExpertArrayList.size());

            mAskToExpertListAdapter.notifyDataSetChanged();
            setLoaded();
        }
    }

}
