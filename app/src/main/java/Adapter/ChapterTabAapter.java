package Adapter;

// http://stackoverflow.com/questions/15909672/how-to-set-font-size-for-text-of-dialog-buttons

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.visualphysics.ApiCall;
import com.visualphysics.ChapterVideoScreenActivity;
import com.visualphysics.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import Database.DataBase;
import Model.Chapters;
import Model.Videos;
import Utils.AppUtil;
import Utils.Download_Manager;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.OnVideoDownloadListener;
import Utils.SharedPrefrences;

/**
 * Created by admin on 5/19/2016.
 */
public class ChapterTabAapter extends RecyclerView.Adapter<ChapterTabAapter.TabsViewHolder> implements OnTaskCompleted.CallBackListener, View.OnClickListener {

    private ArrayList<Chapters> mChapterArrayList;
    private Context mcontext;
    private boolean flag;
    private ProgressDialog mProgressDialog;
    private ApiCall mApiCall;
    private int mCategoryId;
    private AppUtil mAppUtils;
    private ArrayList<Videos> mVideoArrayList;
    private DataBase db;
    private OnVideoDownloadListener onVideoDownloadListener;
    public static ArrayList<Videos> mChapterVideosArrayList;
    public static int videoIndex = 0;
    private static Chapters mSelectedChapter;
    private ImageView mSelectedDownloadImage;

    private LicenseListener onVideoDownloadLicenseListener;
    private int REQUEST_WRITE_STORAGE;
    private static boolean isDownloadViewShown = false;

    public ChapterTabAapter(ArrayList<Chapters> ChapterArrayList, Context context, int CategoryID,
                            int REQUEST_WRITE_STORAGE) {
        this.mChapterArrayList = ChapterArrayList;
        this.mcontext = context;
        this.mProgressDialog = new ProgressDialog(context);
        this.mApiCall = new ApiCall();
        this.mCategoryId = CategoryID;
        this.mAppUtils = new AppUtil(context);
        db = new DataBase(mcontext);
        this.REQUEST_WRITE_STORAGE = REQUEST_WRITE_STORAGE;
        setOnVideoDownloadListener();
        LocalBroadcastManager.getInstance(context).registerReceiver(RequestPermissionReceiver,
                new IntentFilter("onRequestPermissionsResult"));
    }

    @Override
    public TabsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab1frag, null);
        TabsViewHolder viewHolder = new TabsViewHolder(itemview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TabsViewHolder holder, final int position) {

        if (position == mChapterArrayList.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        }

        holder.mTextTabCustomeLayout.setText(mChapterArrayList.get(position).ChapterName);
        holder.mTxtNoOfVideos.setText(mChapterArrayList.get(position).VideoCount + " videos");
        holder.mTxtTotalDuration.setText(mChapterArrayList.get(position).TotalDuration + " hrs");
        holder.mTxtTotalSize.setText(mChapterArrayList.get(position).TotalSize + " MB");
        /*holder.mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDownLoadDialog(mcontext, "Are you sure you want to download the whole chapter?", holder);


//                    holder.mDownload.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.download_2));
//                    animation(holder.mDownload);


            }
        });*/

       /* holder.mDownLoad2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDeleteDialog(mcontext, "Delete the download videos of this chapter?", holder);

            }
        });*/

        holder.mClickableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
//                mcontext.startActivity(intent);

                if (db.isChapterVideoAvailable(mChapterArrayList.get(position).ChapterID)) {

                    Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
                    intent.putExtra("CategoryID", mCategoryId);
                    intent.putExtra("ChapterName", mChapterArrayList.get(position).ChapterName);
                    intent.putExtra("ChapterID", mChapterArrayList.get(position).ChapterID);
                    mcontext.startActivity(intent);
                } else {
                    mSelectedChapter = mChapterArrayList.get(position);
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.show();
                    mApiCall.getChapterVideos(mAppUtils.getDeviceID(), mChapterArrayList.get(position).ChapterID,
                            new OnTaskCompleted(ChapterTabAapter.this), mApiCall.GetChapterVideos);

                }


            }
        });

        if (!TextUtils.isEmpty(mChapterArrayList.get(position).Image)) {
            SharedPrefrences mSharedPref = new SharedPrefrences(mcontext);

            /*Updated version of Picasso Library Update*/
            Picasso.get()
                    .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mVideoArrayList.get(position).Image)
                    .error(R.drawable.default_img)
                    .placeholder(R.drawable.default_img)
                    .into(holder.mImgChapterImage);

            /*Commented after Library Update*/
           /* Picasso.with(mcontext)
                    .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL,"")+mChapterArrayList.get(position).Image)
                    .error(R.drawable.default_img)
                    .placeholder(R.drawable.default_img)
                    .into(holder.mImgChapterImage);*/
        } else {
            holder.mImgChapterImage.setImageResource(R.drawable.default_img);
        }

        holder.mDownload.setOnClickListener(this);
        holder.mDownload.setTag(position);


    }

    void animation(final View v) {
        try {
            Animation comeagain;
            comeagain = AnimationUtils.loadAnimation(mcontext, R.anim.scale_profile);
            v.startAnimation(comeagain);
            comeagain.setDuration(200);
            comeagain.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {


                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }

    @Override
    public int getItemCount() {
        return mChapterArrayList.size();
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetChapterVideos)) {
            parseResponseForChapterVideoNumber(result);
        }

    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        Activity activity = (Activity) mcontext;
        mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                mcontext.getResources().getString(R.string.Error_Msg_Try_Later));

        //Toast.makeText(mcontext, "Please Try Later", Toast.LENGTH_LONG).show();
    }

    void parseResponseForChapterVideoNumber(JSONObject response) {
        Activity activity = (Activity) mcontext;
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetChapterVideos);


            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {
                mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        mcontext.getResources().getString(R.string.Error_Msg_Try_Later));


            } else if (ErrorCode == 2)
                mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                    /*Gson gson = new GsonBuilder().serializeNulls().create();
                    JsonParser jparsor = new JsonParser();
                    JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();
                    Videos[] VideoArray = gson.fromJson(jaArray, Videos[].class);
                    mVideoArrayList = new ArrayList<Videos>(Arrays.asList(VideoArray));*/
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                mVideoArrayList = Videos.fromJson(jsonArray);
                db.doAddChapterVideos(mVideoArrayList);

                Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
                intent.putExtra("CategoryID", mCategoryId);
                intent.putExtra("ChapterName", mSelectedChapter.ChapterName);
                intent.putExtra("ChapterID", mSelectedChapter.ChapterID);
                mcontext.startActivity(intent);
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                    mcontext.getResources().getString(R.string.Error_Msg_Try_Later));
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
        int position;
        switch (v.getId()) {
            case R.id.tab1Fragdownload1:

                position = (Integer) v.getTag();
                mSelectedChapter = mChapterArrayList.get(position);
                ImageView imgDownload = (ImageView) v;
                mSelectedDownloadImage = imgDownload;
                ViewDialog viewDialog = new ViewDialog();

                if ((Build.VERSION.SDK_INT >= 23)) {
                    Activity activity = (Activity) mcontext;
                    boolean hasPermission = (ContextCompat.checkSelfPermission(mcontext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    if (!hasPermission) {

                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_WRITE_STORAGE);


                    } else {
                        viewDialog.showDownLoadDialog(mcontext, "Do you want to download the entire chapter ?",
                                imgDownload, mChapterArrayList.get(position));
                    }
                } else {
                    viewDialog.showDownLoadDialog(mcontext, "Do you want to download the entire chapter ?",
                            imgDownload, mChapterArrayList.get(position));
                }
                break;
            default:
                break;
        }
    }


    class TabsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mClickableLayout;
        ImageView mDownload, mDownLoad2, mImgChapterImage;
        View divider;
        TextView mTextTabCustomeLayout, mTxtNoOfVideos, mTxtTotalDuration, mTxtTotalSize;

        public TabsViewHolder(View itemView) {
            super(itemView);
            mClickableLayout = (LinearLayout) itemView.findViewById(R.id.clickable_LinearLayout);

            divider = (View) itemView.findViewById(R.id.divider_tab1);
            mDownload = (ImageView) itemView.findViewById(R.id.tab1Fragdownload1);
            mDownLoad2 = (ImageView) itemView.findViewById(R.id.tab1Fragdownload2);
            mImgChapterImage = (ImageView) itemView.findViewById(R.id.imgChapter_ChapterListItemScreen);

            mTextTabCustomeLayout = (TextView) itemView.findViewById(R.id.text_TabCustomeLayout);
            mTxtNoOfVideos = (TextView) itemView.findViewById(R.id.txtNoOfVideosChapterListItemScreen);
            mTxtTotalDuration = (TextView) itemView.findViewById(R.id.txtTotalDurationChapterListItemScreen);
            mTxtTotalSize = (TextView) itemView.findViewById(R.id.txtTotalSizeChapterListItemScreen);
        }


    }


    class ViewDialog {

        /***
         * Show Dialog will show dialog, at the time of downloaded the whole chapter as well as delete the whole chapter
         * If any video is downloaded for any chapter it will show delete whole chapter alert
         * Else it will display Downloaded whole chapter
         * It will maintain through "isAnyVideoDownloaded" status
         * @param activity
         * @param msg
         * @param ImgDownload
         * @param ChapterDetail
         */
        public void showDownLoadDialog(final Context activity, final String msg, final ImageView ImgDownload,
                                       final Chapters ChapterDetail) {
            final boolean isAnyVideoDownloaded = checkDownloadedChapterVideos();

            final Dialog dialog = new Dialog(mcontext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = (Button) dialog.findViewById(R.id.btn_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    if (ImgDownload != null)
                        ImgDownload.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.download_1));

                    //if Any video download then delete whole chapter
                    if (isAnyVideoDownloaded) {
                        Activity activity = (Activity) mcontext;
                        if (mAppUtils.deleteWholeChapter(getDownloadURL())) {

                            mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    mcontext.getResources().getString(R.string.msg_chapter_delete_successfully));
                        } else {
                            mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    mcontext.getResources().getString(R.string.Error_Chapter_Delete));
                        }
                    }

                }


            });

            Button yes = (Button) dialog.findViewById(R.id.btn_yes);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    if (ImgDownload != null)
                        animation(ImgDownload);

                    if (!LicenseUtil.isLicenseExpiry(mcontext)) {
                        Download_Manager mDownloadManger = new Download_Manager(mcontext);
                        mChapterVideosArrayList = (ArrayList<Videos>)
                                db.doGetVideosList(ChapterDetail.ChapterID);
                        if (mChapterVideosArrayList != null && mChapterVideosArrayList.size() > 0) {
                            mDownloadManger.addAllChapterVideos(onVideoDownloadListener);
                        } else {
                            Activity activity = (Activity) mcontext;
                            mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    "No video available to download.");
                        }

                    } else {
                        SharedPrefrences mSharedPref = new SharedPrefrences(mcontext);
                        mAppUtils.displaySnackBarWithMessage(((Activity) mcontext).findViewById(android.R.id.content),
                                mcontext.getResources().getString(R.string.msg_license_expired));
                        setOnVideoDownloadLicenseProcessListener();
                        LicenseUtil License = new LicenseUtil(onVideoDownloadLicenseListener);
                        License.acquireLicence(mcontext, mSharedPref.getLoginUser().getStudentID());
                    }

                }
            });

            //If Any video downloaded for the chapter then show the message of delete entire video
            if (isAnyVideoDownloaded) {
                //change the message and make
                dialog.setCancelable(true);
                no.setText("Delete");
                yes.setText("Download");
                text.setText(mcontext.getResources().getString(R.string.msg_delete_chapter));

            }


            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }


        public void showDeleteDialog(Context activity, String msg, final TabsViewHolder holder) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = (Button) dialog.findViewById(R.id.btn_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownLoad2.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.download_2));
                    dialog.dismiss();
                }


            });

            Button yes = (Button) dialog.findViewById(R.id.btn_yes);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownLoad2.setVisibility(View.GONE);
                    holder.mDownload.setVisibility(View.VISIBLE);
                    animation(holder.mDownload);
                    dialog.dismiss();


                }
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }


    }

    /***
     * Download video listener
     */
    private void setOnVideoDownloadListener() {

        this.onVideoDownloadListener = new OnVideoDownloadListener() {
            @Override
            public void onVideoDownload() {
                notifyDataSetChanged();
            }
        };
    }


    /***
     * If Token process successfully then start the Video Download
     */
    private void setOnVideoDownloadLicenseProcessListener() {

        this.onVideoDownloadLicenseListener = new LicenseListener() {
            @Override
            public void onProcessToken() {
                Download_Manager mDownloadManger = new Download_Manager(mcontext);
                mChapterVideosArrayList = (ArrayList<Videos>)
                        db.doGetVideosList(mSelectedChapter.ChapterID);
                if (mChapterVideosArrayList != null && mChapterVideosArrayList.size() > 0) {
                    mDownloadManger.addAllChapterVideos(onVideoDownloadListener);
                } else {
                    Activity activity = (Activity) mcontext;
                    mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                            "No video available to download.");
                }
            }
        };
    }

    /***
     *
     * On Request Perimssion Result
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (REQUEST_WRITE_STORAGE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDownLoadDialog(mcontext, "Do you want to download the entire chapter ?",
                        mSelectedDownloadImage, mSelectedChapter);
            } else {
                Toast.makeText(mcontext, "The app was not allowed to write to your storage. Hence, " +
                                "it cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG).show();
            }


        }

    }


    private BroadcastReceiver RequestPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (REQUEST_WRITE_STORAGE == intent.getIntExtra("RequestCode", 0) && !isDownloadViewShown) {
                isDownloadViewShown = true;
                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDownLoadDialog(mcontext, "Do you want to download the entire chapter ?",
                        mSelectedDownloadImage, mSelectedChapter);

            }

        }
    };

    /***
     * Check is any video is downloaded
     * @return
     */
    private boolean checkDownloadedChapterVideos() {
        boolean status = false;
        if (mSelectedChapter != null) {
            ArrayList<Videos> mChapterVideosArrayList = (ArrayList<Videos>)
                    db.doGetVideosList(mSelectedChapter.ChapterID);
            if (mChapterVideosArrayList != null && mChapterVideosArrayList.size() > 0) {
                for (Videos VideoDetail : mChapterVideosArrayList) {
                    if (mAppUtils.isVideoDownload(VideoDetail.DownloadURL)) {
                        status = true;
                        break;
                    }
                }
            }
        }
        return status;
    }

    /***
     * Get Downloaded URL of Chapter
     * @return
     */
    private String getDownloadURL() {
        String DownloadURL = null;
        ArrayList<Videos> mChapterVideosArrayList = (ArrayList<Videos>)
                db.doGetVideosList(mSelectedChapter.ChapterID);
        if (mChapterVideosArrayList != null && mChapterVideosArrayList.size() > 0) {
            for (Videos VideoDetail : mChapterVideosArrayList) {
                if (mAppUtils.isVideoDownload(VideoDetail.DownloadURL)) {
                    DownloadURL = VideoDetail.DownloadURL;
                    break;
                }
            }
        }
        return DownloadURL;
    }

}
