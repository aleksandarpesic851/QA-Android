package com.visualphysics;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Adapter.NewChapterAdapter;
import Database.DataBase;
import Model.Chapters;
import Model.LoginUser;
import Model.Videos;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import mixpanel.MixPanelClass;

import static Utils.AppUtil.isTagIDUpdated;
import static Utils.AppUtil.isTagUpdatedForVideos;

/**
 * Created by iziss on 15/11/17.
 */
public class NewHomeScreenFragment extends Fragment {

    private View rootView;
    private AssetManager am;
    private DataBase database;
    private ArrayList<Chapters> mChapterArrayList;
    private NewChapterAdapter mChapterAdapter;
    private RecyclerView mChaptersRecyclerView;
    private int REQUEST_WRITE_STORAGE = 203;

    /*To check whether the DB is updated*/
    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private ProgressDialog mProgressDialog;

    private final String tokenFileName = "bb_lic_token_bigs_bug_bunny.xml";

    LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(
                R.layout.tab1fragment_recyclerview, container, false);
        mDeclaration();
        return rootView;
    }

    /***
     * Declarable member variable
     */
    private void mDeclaration() {

        database = new DataBase(getActivity());

        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        //This will fetch all active chapters
        //mChapterArrayList = getSortedChapters((ArrayList<Chapters>) database.getAllChapterList());

        setDataOnList();
    }

    /***
     * This will set data after sorting on recyclerview
     */
    private void setDataOnList() {

        mChaptersRecyclerView = rootView.findViewById(R.id.recyclerview_tab1Frag);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        mChaptersRecyclerView.setLayoutManager(linearLayoutManager);

        //This will fetch all active chapters according to saved TagID
        mChapterArrayList = getPreferredChapters((ArrayList<Chapters>) database.getAllChapterList());

        if (mChapterArrayList != null) {

            /*Changed on 05 Oct 2018 to handle crash
            * https://console.firebase.google.com/u/0/project/visualphysics-162720/crashlytics/app/android:com.visualphysics/issues/5b932c406007d59fcd06e493?time=last-thirty-days&sessionId=5BB64F55019A0001427F97DAB5167CB7_DNE_0_v2*/
            if (getActivity() != null) {
                mChapterAdapter = new NewChapterAdapter(mChapterArrayList, getActivity(), REQUEST_WRITE_STORAGE);
                mChaptersRecyclerView.setAdapter(mChapterAdapter);

                //This will show guide to user
                ((NavigationScreen) getActivity()).showGuide();
            }

        } else {
            Toast.makeText(getActivity(), "No chapter available", Toast.LENGTH_SHORT).show();
        }

        // getChildViewForPosition(0);
    }

    /***
     * This will return chapter list; if user has a valid tagId then it will return according to it other wise all chapters list
     *
     * @param mChapterArrayList
     * @return
     */
    private ArrayList<Chapters> getPreferredChapters(ArrayList<Chapters> mChapterArrayList) {

        try {

            MixPanelClass mixPanelClass = new MixPanelClass(getActivity());

            LoginUser mUser = new SharedPrefrences(getActivity()).getLoginUser();

            if (mixPanelClass.isValidSavedTagID()) {

                ArrayList<Chapters> preferredList = new ArrayList<>();

                for (Chapters mChapters : mChapterArrayList) {

                    boolean isPreferredTag = false;

                    if (mChapters.TagID != null) {

                        if (mChapters.TagID.length() > 0) {

                            String[] splitArray = mChapters.TagID.split(",");

                            for (int i = 0; i < splitArray.length; i++) {

                                if (splitArray[i].equalsIgnoreCase(mUser.getTagID())) {
                                    isPreferredTag = true;
                                    break;
                                }
                            }
                        } else {
                            isPreferredTag = true;
                        }
                    } else {
                        isPreferredTag = true;
                    }

                    if (isPreferredTag) {
                        preferredList.add(mChapters);
                    }

                }
                return getSortedChapters(preferredList);
            } else {
                return getSortedChapters(mChapterArrayList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return getSortedChapters(mChapterArrayList);

    }

    /***
     * This will sort the list in ascending order for chapter order column
     *
     * @param mChapterArrayList
     * @return
     */
    private ArrayList<Chapters> getSortedChapters(ArrayList<Chapters> mChapterArrayList) {

        Collections.sort(mChapterArrayList, (o1, o2) -> {
            //return Integer.parseInt(o2.VideoCount) - Integer.parseInt(o1.VideoCount);

            //Log.i("Name>>" + o1.ChapterName, "" + o1.ChapterOrder);
            return o2.ChapterOrder - o1.ChapterOrder;

        });

        return mChapterArrayList;

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

    /*public void getChildViewForPosition(int position) {

        *//*View mView = mChaptersRecyclerView.getChildAt(position);

        ImageView mImageView = (ImageView) mView.findViewById(R.id.tab1Fragdownload1);

        float x = mImageView.getX();
        float y = mImageView.getY();

        Log.i("Position:X:Y", "" + x + ";" + y);*//*


        RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder)
                mChaptersRecyclerView.findViewHolderForAdapterPosition(position);

        if (null != holder) {

            ImageView mImageView = (ImageView) holder.itemView.findViewById(R.id.tab1Fragdownload1);

            mImageView.setVisibility(View.GONE);

            float x = mImageView.getX();
            float y = mImageView.getY();

            Log.i("Position:X:Y", "" + x + ";" + y);

        }

    }*/
}

