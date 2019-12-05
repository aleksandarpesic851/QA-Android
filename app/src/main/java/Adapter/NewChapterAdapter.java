package Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import Database.DataBase;
import Model.Chapters;
import Model.LoginUser;
import Model.Videos;
import Utils.AppUtil;
import Utils.Download_Manager;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.OnVideoDownloadListener;
import Utils.SharedPrefrences;
import interfaces.OnGotItClickListener;
import mixpanel.MixPanelClass;

/**
 * Created by iziss on 15/11/17.
 */
public class NewChapterAdapter extends RecyclerView.Adapter<NewChapterAdapter.TabsViewHolder> implements OnTaskCompleted.CallBackListener, View.OnClickListener, OnGotItClickListener {

    private ArrayList<Chapters> mChapterArrayList;
    private Context mcontext;
    private boolean flag;
    private ProgressDialog mProgressDialog;
    private ApiCall mApiCall;
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

    private LicenseListener OnVideoPlayLicenseListener;
    private SharedPrefrences mSharedPref;

    public ArrayList<TabsViewHolder> mHolderArrayList = new ArrayList<>();


    public NewChapterAdapter(ArrayList<Chapters> ChapterArrayList, Context context, int REQUEST_WRITE_STORAGE) {

        this.mChapterArrayList = ChapterArrayList;
        this.mcontext = context;
        if (context != null)
            this.mProgressDialog = new ProgressDialog(context);
        this.mApiCall = new ApiCall();
        this.mAppUtils = new AppUtil(context);
        db = new DataBase(mcontext);
        this.REQUEST_WRITE_STORAGE = REQUEST_WRITE_STORAGE;
        mSharedPref = new SharedPrefrences(context);

        setOnVideoDownloadListener();

        //IZISS Exception 04 Jan 2017
        //Exception android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@5b90baf is not valid; is your activity running?
       /* LocalBroadcastManager.getInstance(context).registerReceiver(RequestPermissionReceiver,
                new IntentFilter("onRequestPermissionsResult"));*/
    }

    @Override
    public TabsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab1frag, null);
        TabsViewHolder viewHolder = new TabsViewHolder(itemview);

        mHolderArrayList.add(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TabsViewHolder holder, final int position) {

        if (position == mChapterArrayList.size() - 1) {

            /*Added by Iziss to show last item*/
            //holder.divider.setVisibility(View.GONE);
        }

        /*Put in a try catch to handle last item*/
        try {

            if (position == mChapterArrayList.size()) {
                holder.divider.setVisibility(View.INVISIBLE);
                holder.mDownload.setVisibility(View.INVISIBLE);
                holder.mTextTabCustomeLayout.setVisibility(View.INVISIBLE);
                holder.mImgChapterImage.setVisibility(View.GONE);
                holder.mTxtNoOfVideos.setVisibility(View.INVISIBLE);
                holder.mTxtTotalDuration.setVisibility(View.INVISIBLE);
                holder.mTxtTotalSize.setVisibility(View.INVISIBLE);

                holder.separatorOne.setVisibility(View.GONE);
                holder.separatorTwo.setVisibility(View.GONE);
            } else {

                holder.divider.setVisibility(View.VISIBLE);
                holder.mDownload.setVisibility(View.VISIBLE);
                holder.mTextTabCustomeLayout.setVisibility(View.VISIBLE);
                holder.mImgChapterImage.setVisibility(View.VISIBLE);
                holder.mTxtNoOfVideos.setVisibility(View.VISIBLE);
                holder.mTxtTotalDuration.setVisibility(View.VISIBLE);
                holder.mTxtTotalSize.setVisibility(View.VISIBLE);

                holder.separatorOne.setVisibility(View.VISIBLE);
                holder.separatorTwo.setVisibility(View.VISIBLE);

            }


            holder.mTextTabCustomeLayout.setText(mChapterArrayList.get(position).ChapterName);

        /* COMMENTED BY IZISS 20 AUG 2018
        holder.mTxtNoOfVideos.setText(mChapterArrayList.get(position).VideoCount + " videos");
        holder.mTxtTotalDuration.setText(mChapterArrayList.get(position).TotalDuration + " hrs");
        holder.mTxtTotalSize.setText(mChapterArrayList.get(position).TotalSize + " MB");
        */

            /*This will set data on the information fields*/
            setFilteredInformation(holder, mChapterArrayList.get(position));

            holder.mClickableLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
//                mcontext.startActivity(intent);

                    //Comment by IZISS to remove License check on chapter click
                /*if (!LicenseUtil.isLicenseExpiry(mcontext)) {

                    moveForward(position);
                } else {

                    setOnVideoPlayLicenseProcessListener(position);
                    LicenseUtil License = new LicenseUtil(OnVideoPlayLicenseListener);
                    License.acquireLicence(mcontext, mSharedPref.getLoginUser().getStudentID());

                }*/

                    // By IZISS to directly call ChapterVideoScreenActivity
                    moveForward(position);

                }
            });

            if (!TextUtils.isEmpty(mChapterArrayList.get(position).Image)) {
                SharedPrefrences mSharedPref = new SharedPrefrences(mcontext);
                Picasso.get()
                        .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mChapterArrayList.get(position).Image)
                        .error(R.drawable.default_img)
                        .placeholder(R.drawable.default_img)
                        .into(holder.mImgChapterImage);
            } else {
                holder.mImgChapterImage.setImageResource(R.drawable.default_img);
            }

            holder.mDownload.setOnClickListener(this);
            holder.mDownload.setTag(position);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        if (mChapterArrayList != null) {
            /*Added to show blank item on last position*/
            //return mChapterArrayList.size();
            return mChapterArrayList.size() + 1;
        } else {
            return 0;
        }

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
        AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                mcontext.getResources().getString(R.string.Error_Msg_Try_Later));

        //Toast.makeText(mcontext, "Please Try Later", Toast.LENGTH_LONG).show();
    }

    void parseResponseForChapterVideoNumber(JSONObject response) {
        Activity activity = (Activity) mcontext;
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetChapterVideos);


            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {
                AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        mcontext.getResources().getString(R.string.Error_Msg_Try_Later));


            } else if (ErrorCode == 2)
                AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                    /*Gson gson = new GsonBuilder().serializeNulls().create();
                    JsonParser jparsor = new JsonParser();
                    JsonArray jaArray = jparsor.parse(mJsonObj.getString("data")).getAsJsonArray();
                    Videos[] VideoArray = gson.fromJson(jaArray, Videos[].class);
                    mVideoSparsedArray = new ArrayList<Videos>(Arrays.asList(VideoArray));*/
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                mVideoArrayList = Videos.fromJson(jsonArray);
                db.doAddChapterVideos(mVideoArrayList);

                //IZISS MPE
                sendClickToMixpanel(mSelectedChapter, false);

                Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
                intent.putExtra("CategoryID", Integer.parseInt(mSelectedChapter.CategoryID));
                intent.putExtra("ChapterName", mSelectedChapter.ChapterName);
                intent.putExtra("ChapterID", mSelectedChapter.ChapterID);
                mcontext.startActivity(intent);
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
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

                //Customized function by IZISS to manage click on download and popup
                onDownloadClick(imgDownload, position);

                break;

            default:
                break;
        }
    }


    public class TabsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mClickableLayout;
        public ImageView mDownload, mDownLoad2, mImgChapterImage;
        View divider;
        TextView mTextTabCustomeLayout, mTxtNoOfVideos, mTxtTotalDuration, mTxtTotalSize;

        View separatorOne, separatorTwo;

        public TabsViewHolder(View itemView) {
            super(itemView);
            mClickableLayout = itemView.findViewById(R.id.clickable_LinearLayout);

            divider = itemView.findViewById(R.id.divider_tab1);
            mDownload = itemView.findViewById(R.id.tab1Fragdownload1);
            mDownLoad2 = itemView.findViewById(R.id.tab1Fragdownload2);
            mImgChapterImage = itemView.findViewById(R.id.imgChapter_ChapterListItemScreen);

            mTextTabCustomeLayout = itemView.findViewById(R.id.text_TabCustomeLayout);
            mTxtNoOfVideos = itemView.findViewById(R.id.txtNoOfVideosChapterListItemScreen);
            mTxtTotalDuration = itemView.findViewById(R.id.txtTotalDurationChapterListItemScreen);
            mTxtTotalSize = itemView.findViewById(R.id.txtTotalSizeChapterListItemScreen);

            separatorOne = itemView.findViewById(R.id.separatorOne);
            separatorTwo = itemView.findViewById(R.id.separatorTwo);
        }


    }


    class ViewDialog {

        /***
         * Show Dialog will show dialog, at the time of downloaded the whole chapter as well as delete the whole chapter
         * If any video is downloaded for any chapter it will show delete whole chapter alert
         * Else it will display Downloaded whole chapter
         * It will maintain through "isAnyVideoDownloaded" status
         *
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

            TextView text = dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = dialog.findViewById(R.id.btn_no);
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

                            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    mcontext.getResources().getString(R.string.msg_chapter_delete_successfully));
                        } else {
                            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    mcontext.getResources().getString(R.string.Error_Chapter_Delete));
                        }
                    }

                }


            });

            Button yes = dialog.findViewById(R.id.btn_yes);
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
                            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                    "No video available to download.");
                        }

                    } else {
                        SharedPrefrences mSharedPref = new SharedPrefrences(mcontext);
                        AppUtil.displaySnackBarWithMessage(((Activity) mcontext).findViewById(android.R.id.content),
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

            TextView text = dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = dialog.findViewById(R.id.btn_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownLoad2.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.download_2));
                    dialog.dismiss();
                }


            });

            Button yes = dialog.findViewById(R.id.btn_yes);
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

                //IZISS MPE
                sendClickToMixpanel(mSelectedChapter, true);

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
                    AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                            "No video available to download.");
                }
            }
        };
    }

    /***
     * On Request Perimssion Result
     *
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

    /*private BroadcastReceiver RequestPermissionReceiver = new BroadcastReceiver() {
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
    };*/

    /***
     * Check is any video is downloaded
     *
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
     *
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

    /***
     * If Token process successfully then start the Video
     */
    private void setOnVideoPlayLicenseProcessListener(final int position) {

        this.OnVideoPlayLicenseListener = new LicenseListener() {
            @Override
            public void onProcessToken() {

                moveForward(position);

            }
        };
    }


    /***
     * @param position
     */
    private void moveForward(int position) {

        if (db.isChapterVideoAvailable(mChapterArrayList.get(position).ChapterID)) {

            //IZISS MPE
            sendClickToMixpanel(mChapterArrayList.get(position), false);

            Intent intent = new Intent(mcontext, ChapterVideoScreenActivity.class);
            intent.putExtra("CategoryID", Integer.parseInt(mChapterArrayList.get(position).CategoryID));
            intent.putExtra("ChapterName", mChapterArrayList.get(position).ChapterName);
            intent.putExtra("ChapterID", mChapterArrayList.get(position).ChapterID);
            mcontext.startActivity(intent);
        } else {
            mSelectedChapter = mChapterArrayList.get(position);
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getChapterVideos(mAppUtils.getDeviceID(), mChapterArrayList.get(position).ChapterID,
                    new OnTaskCompleted(NewChapterAdapter.this), mApiCall.GetChapterVideos);

        }

    }

    @Override
    public void onGotItClick(ImageView imgDownload, int position) {

        onDownloadClick(imgDownload, position);

    }

    /**
     * When clicked on download button
     *
     * @param imgDownload
     * @param position
     */
    private void onDownloadClick(ImageView imgDownload, int position) {

        if (mSharedPref.getBooleanPreferences(SharedPrefrences.IS_DOWNLOAD_POPUP_SHOWN, false)) {
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
        } else {
            AppUtil.showOneTimeDialog(mcontext, imgDownload, position, this);
        }

    }

    /***
     * Send data to mixpanel
     *
     * @param mChapters
     */
    private void sendClickToMixpanel(Chapters mChapters, boolean isChapterDownload) {

        {
            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            //Key and their values which will be send to Mix Panel
            hashMap.put("CHAPTER_ID", "" + mChapters.ChapterID);
            hashMap.put("CHAPTER_NAME", mChapters.ChapterName);
            hashMap.put("CHAPTER_CATEGORY_ID", "" + mChapters.CategoryID);

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(mcontext);

            if (!isChapterDownload) {

                //If first chapter after first login, this parameter is set to true on ReferalCodeScreenActivity
                if (mixPanelClass.getPref(MixPanelClass.IS_FIRST_CHAPTER, false)) {

                    //Send data to our function which will be further sent to Mix Panel
                    mixPanelClass.sendData(MixPanelClass.MPE_FIRST_CHAPTER_CLICKED, hashMap);

                    mixPanelClass.setPref(MixPanelClass.IS_FIRST_CHAPTER, false);
                } else {

                    mixPanelClass.sendData(MixPanelClass.MPE_CHAPTER_CLICKED, hashMap);

                    //Change to set last chapter clicked
                    mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_LAST_CHAPTER_CLICKED, mChapters.ChapterName, false);
                }

            } else {
                mixPanelClass.sendData(MixPanelClass.MPE_DOWNLOAD_CHAPTER, hashMap);
            }

        }
    }


    public ArrayList<TabsViewHolder> getViewHolder() {
        return this.mHolderArrayList;
    }


    /**
     * This method will set filtered number of videos, duration and size
     *
     * @param holder
     */
    private void setFilteredInformation(TabsViewHolder holder, Chapters mChapters) {

        MixPanelClass mixPanelClass = new MixPanelClass(mcontext);

        if (mixPanelClass.isValidSavedTagID()) {

            int[] countArray = getDifferentCounts(mChapters.ChapterID);

            holder.mTxtNoOfVideos.setText(countArray[0] + " videos");
            holder.mTxtTotalDuration.setText(AppUtil.convertMinToHrs(countArray[1]));
            holder.mTxtTotalSize.setText(countArray[2] + " MB");

        } else {

            holder.mTxtNoOfVideos.setText(mChapters.VideoCount + " videos");
            holder.mTxtTotalDuration.setText(mChapters.TotalDuration + " hrs");
            holder.mTxtTotalSize.setText(mChapters.TotalSize + " MB");

        }
    }

    /**
     * It will return different counts for chapter
     *
     * @param chapterID
     * @return
     */
    private int[] getDifferentCounts(int chapterID) {

        int[] countArray = new int[]{0, 0, 0};
        int videoCount = 0, duration = 0, size = 0;

        DataBase database = new DataBase(mcontext);

        ArrayList<Videos> mChapterVideosArrayList = getPreferredChapters((ArrayList<Videos>) database.getAllVideosForChapter(chapterID));

        if (mChapterVideosArrayList != null) {
            for (int i = 0; i < mChapterVideosArrayList.size(); i++) {

                Videos mVideo = mChapterVideosArrayList.get(i);

                duration += Integer.parseInt(mVideo.Duration);
                size += Integer.parseInt(mVideo.Size);
            }

            videoCount = mChapterVideosArrayList.size();

            countArray[0] = videoCount;
            countArray[1] = duration;
            countArray[2] = size;
        }

        return countArray;

    }

    /***
     * This will return chapter list; if user has a valid tagId then it will return according to it other wise all chapters list
     *
     * @param mVideosArrayList
     * @return
     */
    private ArrayList<Videos> getPreferredChapters(ArrayList<Videos> mVideosArrayList) {

        MixPanelClass mixPanelClass = new MixPanelClass(mcontext);

        LoginUser mUser = new SharedPrefrences(mcontext).getLoginUser();

        if (mixPanelClass.isValidSavedTagID()) {

            ArrayList<Videos> preferredList = new ArrayList<>();

            for (Videos mVideos : mVideosArrayList) {

                boolean isPreferredTag = false;

                if (mVideos.TagID != null) {

                    if (mVideos.TagID.length() > 0) {

                        String[] splitArray = mVideos.TagID.split(",");

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
                    preferredList.add(mVideos);
                }
            }
            return preferredList;
        } else {
            return mVideosArrayList;
        }

    }


}

