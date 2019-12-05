package Utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.visualphysics.AddCountToVideo;
import com.visualphysics.R;

import org.json.JSONObject;

import java.io.File;

import Adapter.NewChapterAdapter;
import Model.Videos;

/**
 * Created by India on 6/27/2016.
 */
public class Download_Manager {

    private Context mContext;
    private DownloadManager mDownloadManager;
    private String strDownloadURL = "";
    private long myDownloadRef;
    private BroadcastReceiver receiverDownloadComplete, receiverNotificationClicked;

    private AppUtil mAppUtil;
    private OnVideoDownloadListener onVideoDownloadListener;
    private boolean isChapterDownload = false;
    private SharedPrefrences mSharePref;


    public Download_Manager(Context Context) {
        this.mContext = Context;
        mAppUtil = new AppUtil(mContext);
        mSharePref = new SharedPrefrences(mContext);
    }

    /***
     *
     */
    public void init(String DownloadFileURL, String DownloadDescription, int VideoID, int ChapterID,
                     boolean isChapterDownload,
                     OnVideoDownloadListener onVideoDownloadListener) {

        this.isChapterDownload = isChapterDownload;
        //Check if file already downloaded then cancel the download
        if (mAppUtil.isVideoDownload(DownloadFileURL)) {
            Activity activity = (Activity) mContext;
            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                    mContext.getResources().getString(R.string.Error_Video_Already_Downloaded));

            //check if is chapter download true then download next video
            if (isChapterDownload) {
                downloadChapterVideo();
            }

        } else if (!mAppUtil.getConnectionState()) {
            mAppUtil.displayNoInternetSnackBar(((Activity) mContext).findViewById(android.R.id.content));
        } else {
            try {
                this.onVideoDownloadListener = onVideoDownloadListener;

                mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                //strDownloadURL = "http://192.168.1.108/VisualPhysicsAdmin/trunk/uploads/" + DownloadFileURL;

                //strDownloadURL = "http://pettbooks.com/vp/uploads/" + DownloadFileURL;
                strDownloadURL = mSharePref.getPreferences(mSharePref.DOWNLOAD_URL, "") + DownloadFileURL;

                Uri uri = Uri.parse(strDownloadURL);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE);
                request.setMimeType("*/*");

                //Set the Download file tile and desc
                request.setDescription(mContext.getResources().getString(R.string.app_name))
                        .setTitle(DownloadDescription);

                String[] FileNameArray = DownloadFileURL.split("/");
                String FileName = FileNameArray[FileNameArray.length - 1];

                //if(DownloadFileURL == null || FileNameArray.length <= 2){
                if (DownloadFileURL == null || FileNameArray.length <= 1) {
                    AppUtil.displaySnackBarWithMessage(((Activity) mContext).findViewById(android.R.id.content),
                            "Video not available");
                } else {
                    //Set Path to download the file
                    /*File dir = new File(Environment.getExternalStorageDirectory() + "/"
                            + mContext.getResources().getString(R.string.app_name) + "/" + FileNameArray[0] + "/" + FileNameArray[1]);*/
                    File dir = new File(Environment.getExternalStorageDirectory() + "/"
                            + mContext.getResources().getString(R.string.app_name) + "/" + FileNameArray[0]);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    // request.setDestinationInExternalFilesDir(mContext, dir.getAbsoluteFile().toString()
                    // , "Q13.mlv");


                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    /*request.setDestinationInExternalPublicDir(mContext.getResources().getString(R.string.app_name) + "/" + FileNameArray[0] + "/" + FileNameArray[1]
                            , FileName);*/
                    request.setDestinationInExternalPublicDir(mContext.getResources().getString(R.string.app_name) + "/" + FileNameArray[0]
                            , FileName);

                    addReceiver(isChapterDownload, VideoID);
                    //Queue download

                    myDownloadRef = mDownloadManager.enqueue(request);
                    DebugLog.v("myDownloadRef", "Before myDownloadRef ==== " + myDownloadRef);
                    JSONObject mJsonObj = new JSONObject();

                    mJsonObj.put("VideoID", VideoID);
                    mJsonObj.put("ChapterID", ChapterID);
                    mJsonObj.put("iSChapterDownload", isChapterDownload);
                    mSharePref.setPreferences(String.valueOf(myDownloadRef), mJsonObj.toString());

                }
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }


        }


    }

    /***
     * Download all videos of chapter
     *
     * @param onVideoDownloadListener
     */
    public void addAllChapterVideos(OnVideoDownloadListener onVideoDownloadListener) {
        this.onVideoDownloadListener = onVideoDownloadListener;
        downloadChapterVideo();
    }

    /***
     * Download Chapter video
     */
    private void downloadChapterVideo() {
        try {


            //Comment By IZISS to download videos 22 Dec 2017
            /*if(ChapterTabAapter.mChapterVideosArrayList!=null && ChapterTabAapter.mChapterVideosArrayList.size()>0){
                if(ChapterTabAapter.videoIndex < ChapterTabAapter.mChapterVideosArrayList.size()){
                    Videos VideoDetail = ChapterTabAapter.mChapterVideosArrayList.get(ChapterTabAapter.videoIndex);
                    ChapterTabAapter.videoIndex = ChapterTabAapter.videoIndex + 1;
                    init(VideoDetail.DownloadURL,VideoDetail.Title,VideoDetail.VideoID,VideoDetail.ChapterID,
                            true,this.onVideoDownloadListener);
                }
            }*/

            if (NewChapterAdapter.mChapterVideosArrayList != null && NewChapterAdapter.mChapterVideosArrayList.size() > 0) {
                if (NewChapterAdapter.videoIndex < NewChapterAdapter.mChapterVideosArrayList.size()) {
                    Videos VideoDetail = NewChapterAdapter.mChapterVideosArrayList.get(NewChapterAdapter.videoIndex);
                    NewChapterAdapter.videoIndex = NewChapterAdapter.videoIndex + 1;
                    init(VideoDetail.DownloadURL, VideoDetail.Title, VideoDetail.VideoID, VideoDetail.ChapterID,
                            true, this.onVideoDownloadListener);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            try {
                Toast.makeText(mContext, "Not able to download video, Please try later.", Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }


    public void addReceiver(boolean isChapterDownload, int VideoID) {
        final Activity activity = (Activity) mContext;

        //Notification receiver
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        receiverNotificationClicked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String extraID = DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                long[] refrences = intent.getLongArrayExtra(extraID);
                /*for (long refrence : refrences){
                    if(refrence == myDownloadRef){

                    }
                }*/

            }
        };


        // Filter for Dowload on complete
        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int VideoID, ChapterID;
                boolean isChapterDownload = false;
                SharedPrefrences mSharePref = new SharedPrefrences(context);

                long refrence = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Log.i("myDownloadRef", "After  refrence ==== " + refrence);
                /*int VideoID = intent.getIntExtra("VideoID",-1);
                boolean iChapterDownload = intent.getBooleanExtra("isChapterDownload",false);
                mAppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        "VideoID "+VideoID +" iChapterDownload = "+iChapterDownload);***/
                String strJson = mSharePref.getPreferences(String.valueOf(refrence), "");
                if (!TextUtils.isEmpty(strJson)) {
                    try {
                        JSONObject jsonData = new JSONObject(strJson);
                        DebugLog.v("", "JsonData == " + String.valueOf(refrence) + "= " + jsonData.toString());
                        VideoID = jsonData.getInt("VideoID");
                        ChapterID = jsonData.getInt("ChapterID");
                        isChapterDownload = jsonData.getBoolean("iSChapterDownload");
                        mSharePref.setPreferences(String.valueOf(refrence), "");

                        AddCountToVideo AddCount = new AddCountToVideo(context);
                        AddCount.addVideoDownloadCount(VideoID, ChapterID);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorLog.SaveErrorLog(e);
                        ErrorLog.SendErrorReport(e);
                    }

                }
                if (myDownloadRef == refrence) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(refrence);
                    Cursor cursor = mDownloadManager.query(query);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (columnIndex == -1)
                        return;
                    try {
                        int status = cursor.getInt(columnIndex);

                      /*  int fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        String saveFilePath = cursor.getString(fileNameIndex);*/

                        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = cursor.getInt(columnReason);


                        switch (status) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                mAppUtil.displayToastWithMessage(mContext,
                                        "Download complete.");
                                try {
                                    onVideoDownloadListener.onVideoDownload();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ErrorLog.SaveErrorLog(e);
                                    ErrorLog.SendErrorReport(e);
                                }
                                break;

                            case DownloadManager.STATUS_FAILED:
                                mAppUtil.displayToastWithMessage(mContext,
                                        "Download failed, please try later.");
                                break;

                            case DownloadManager.STATUS_PAUSED:
                                mAppUtil.displayToastWithMessage(mContext,
                                        "Download paused.");
                                break;

                            case DownloadManager.STATUS_PENDING:
                                mAppUtil.displayToastWithMessage(mContext,
                                        "Download pending.");
                                break;

                            case DownloadManager.STATUS_RUNNING:
                                mAppUtil.displayToastWithMessage(mContext,
                                        "Download running.");
                                break;
                        }
                        activity.unregisterReceiver(receiverDownloadComplete);
                        activity.unregisterReceiver(receiverNotificationClicked);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorLog.SendErrorReport(e);
                    }


                }

                //check if is chapter download true then download next video
                if (isChapterDownload) {
                    downloadChapterVideo();
                }

            }
        };

        activity.registerReceiver(receiverNotificationClicked, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        activity.registerReceiver(receiverDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }


}
