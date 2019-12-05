package com.visualphysics;

import android.content.Context;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import Database.DataBase;
import Model.ChapterVideoDetail;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

/**
 * Add Download Count and Video Play count
 * If Network avialable then send  count to server
 * If Network not avialable then add locally and  send data if network avilable from Navigation screen
 */
public class AddCountToVideo implements OnTaskCompleted.CallBackListener {

    private Context mContext;
    private int VideoID =-1, ChapterID = -1;
    private ApiCall mApiCall;
    private DataBase db;
    private AppUtil mAppUtils;
    private SharedPrefrences mSharedPref;

    public AddCountToVideo(Context Context){
        mContext = Context;
        mApiCall = new ApiCall();
        db = new DataBase(mContext);
        mAppUtils = new AppUtil(mContext);
        mSharedPref = new SharedPrefrences(mContext);
    }

    /***
     *
     * @param VideoID
     * @param ChapterID
     */
    public void addVideoDownloadCount(int VideoID,int ChapterID){
        this.VideoID = VideoID;
        this.ChapterID = ChapterID;
        if (mAppUtils.getConnectionState()) {
            mApiCall.doAddVideoDownloadCount(VideoID,
                    mSharedPref.getLoginUser().getStudentID(),mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.AddVideoDownloadCount);
        }
        else{
            addVideoDownloadCountLocal(VideoID,ChapterID);
        }

    }

    /***
     *
     * @param VideoID
     * @param ChapterID
     */
    public void addPlayVideoCount(int VideoID,int ChapterID){
        this.VideoID = VideoID;
        this.ChapterID = ChapterID;
        if (mAppUtils.getConnectionState()) {
            mApiCall.doAddVideoPlayCount(VideoID,
                    mSharedPref.getLoginUser().getStudentID(),mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.AddVideoPlayCount);
        }
        else{
            addVideoPlayCountLocal(VideoID,ChapterID);
        }

    }

    /***
     * Add the Download Video Count in local database
     * @param VideoID
     * @param ChapterID
     */
    private void addVideoDownloadCountLocal(int VideoID,int ChapterID){
        try{
            ChapterVideoDetail VideoDetail = new ChapterVideoDetail(VideoID,
                    ChapterID,
                    mSharedPref.getLoginUser().getStudentID());
            VideoDetail.setVideoDownloadCount(1);
            db.doAddVideoDownloadCount(VideoDetail);
        }
        catch (Exception e){
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Add the Play Video Count in local database
     * @param VideoID
     * @param ChapterID
     */
    private void addVideoPlayCountLocal(int VideoID,int ChapterID){
        try{
            ChapterVideoDetail VideoDetail = new ChapterVideoDetail(VideoID,
                    ChapterID,
                    mSharedPref.getLoginUser().getStudentID());
            VideoDetail.setVideoPlayCount(1);
            db.doAddVideoPlayCount(VideoDetail);
        }
        catch (Exception e){
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        if (Method.equals(mApiCall.AddVideoDownloadCount)) {
            parseResponseForAddVideoDownloadCountData(result);
        }
        else if(Method.equals(mApiCall.AddVideoPlayCount)){
            parseResponseForAddVideoPlayCountData(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        if (Method.equals(mApiCall.AddVideoDownloadCount)) {
            addVideoDownloadCountLocal(VideoID,ChapterID);
        }
        else if(Method.equals(mApiCall.AddVideoPlayCount)){
            addVideoPlayCountLocal(VideoID,ChapterID);
        }
    }

    /***
     * Parse the response of Add Video Download Count
     *
     * @param response
     */
    private void parseResponseForAddVideoDownloadCountData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddVideoDownloadCount);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode != 0) {
                addVideoDownloadCountLocal(VideoID,ChapterID);
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }


    /***
     * Parse the response of Add Video Play Count
     *
     * @param response
     */
    private void parseResponseForAddVideoPlayCountData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddVideoPlayCount);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode != 0) {
                addVideoPlayCountLocal(VideoID,ChapterID);
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }
}
