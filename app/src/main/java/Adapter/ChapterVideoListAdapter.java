package Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import com.intertrust.wasabi.Runtime;
import com.squareup.picasso.Picasso;
import com.visualphysics.ApiCall;
import com.visualphysics.ChapterVideoScreenActivity;
import com.visualphysics.R;
import com.visualphysics.VideoPlayerScreenActivity;
import com.visualphysics.WebViewActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import Database.DataBase;
import Model.ChapterVideoDetail;
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
import interfaces.OnGotItClickListener;
import mixpanel.MixPanelClass;

//import com.pyze.android.PyzeEvents;

/**
 * Created by admin on 5/20/2016.
 */
public class ChapterVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, OnTaskCompleted.CallBackListener, OnGotItClickListener {

    ArrayList<Videos> mVideoArrayList;
    Context context;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private DataBase db;
    private SharedPrefrences mSharedPref;
    private AssetManager am;
    private final String tokenFileName = "token1.xml";
    private OnVideoDownloadListener onVideoDownloadListener;
    private String tokenData;
    private static Videos mVideoDetail;
    private LicenseListener OnVideoPlayLicenseListener, onVideoDownloadLicenseListener;
    private int REQUEST_WRITE_STORAGE, REQUEST_PERMISSION_CODE;
    private static ImageView mImgSelectDownloadImage;
    private int mSelectedVideoPosition;

    /*Reference:https://stackoverflow.com/questions/25914003/recyclerview-and-handling-different-type-of-row-inflation*/
    /*Item Type to play video*/
    public static final int ITEM_TYPE_CONCEPT = 1;
    public static final int ITEM_TYPE_QUESTION = 2;

    /*Item Type to open WebView*/
    public static final int ITEM_TYPE_QUIZ = 3;
    public static final int ITEM_TYPE_NOTES = 4;

    /*Default type if no data is available*/
    public static final int ITEM_TYPE_DEFAULT = 1;

    public ChapterVideoListAdapter(ArrayList<Videos> VideoArrayList, Context context, int REQUEST_WRITE_STORAGE) {
        this.mVideoArrayList = VideoArrayList;
        this.context = context;
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(context);
        db = new DataBase(context);
        mSharedPref = new SharedPrefrences(context);
        am = context.getAssets();
        this.REQUEST_WRITE_STORAGE = REQUEST_WRITE_STORAGE;
        this.REQUEST_PERMISSION_CODE = REQUEST_WRITE_STORAGE + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_CONCEPT || viewType == ITEM_TYPE_QUESTION) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_name_content_screen, null);
            return new ChapterNameViewHolder(itemView); // view holder for normal items
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_quiz, null);
            return new QuizViewHolder(itemView); // view holder for header items
        }

        /*View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_name_content_screen, null);
        ChapterNameViewHolder viewHolder = new ChapterNameViewHolder(itemview);
        return viewHolder;*/
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

        final int itemType = getItemViewType(position);

        if (itemType == ITEM_TYPE_CONCEPT || itemType == ITEM_TYPE_QUESTION) {

            /*View Holder for Concept and Questions which are Videos*/
            ChapterNameViewHolder holder = ((ChapterNameViewHolder) viewHolder);

            /*Put in a try catch to handle last item*/
            try {

                if (position == mVideoArrayList.size()) {
                    holder.divider.setVisibility(View.GONE);
                    holder.mLike.setVisibility(View.GONE);
                    holder.mDownload.setVisibility(View.INVISIBLE);
                    holder.mTxtVideoTitle.setVisibility(View.INVISIBLE);
                    holder.mTxtVideoDuration.setVisibility(View.INVISIBLE);
                    holder.mTxtVideoDesc.setVisibility(View.INVISIBLE);
                    holder.mImgVideoImage.setVisibility(View.GONE);
                    holder.mTxtVideoSize.setVisibility(View.GONE);
                    holder.separatorOne.setVisibility(View.GONE);
                } else {

                    holder.divider.setVisibility(View.VISIBLE);
                    holder.mLike.setVisibility(View.VISIBLE);
                    holder.mDownload.setVisibility(View.VISIBLE);
                    holder.mTxtVideoTitle.setVisibility(View.VISIBLE);
                    holder.mTxtVideoDuration.setVisibility(View.VISIBLE);
                    holder.mTxtVideoDesc.setVisibility(View.VISIBLE);
                    holder.mImgVideoImage.setVisibility(View.VISIBLE);
                    holder.mTxtVideoSize.setVisibility(View.VISIBLE);
                    holder.separatorOne.setVisibility(View.VISIBLE);

                    ChapterVideoDetail VideoDetail = db.getVideoDetail(mVideoArrayList.get(position).VideoID);
                    if (VideoDetail != null) {
                        if (VideoDetail.isLikeStatus())
                            holder.mLike.setImageResource(R.drawable.likefill);
                        else
                            holder.mLike.setImageResource(R.drawable.like);
                    } else
                        holder.mLike.setImageResource(R.drawable.like);


                    //Check Video is download on SD card
                    if (mAppUtils.isVideoDownload(mVideoArrayList.get(position).DownloadURL))
                        holder.mDownload.setImageResource(R.drawable.download_2);
                    else
                        holder.mDownload.setImageResource(R.drawable.download_1);

                    if (!TextUtils.isEmpty(mVideoArrayList.get(position).Title))
                        holder.mTxtVideoTitle.setText(mVideoArrayList.get(position).Title);
                    holder.mTxtVideoSize.setText(mVideoArrayList.get(position).Size + " MB");
                    holder.mTxtVideoDuration.setText(mVideoArrayList.get(position).Duration + " mins");
                    holder.mTxtVideoDesc.setText(mVideoArrayList.get(position).Description);

                    if (!TextUtils.isEmpty(mVideoArrayList.get(position).Image)) {
                        SharedPrefrences mSharedPref = new SharedPrefrences(context);

                        /*Updated version of Picasso Library Update*/
                        Picasso.get()
                                .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mVideoArrayList.get(position).Image)
                                .error(R.drawable.default_img)
                                .placeholder(R.drawable.default_img)
                                .into(holder.mImgVideoImage);

                        /*Commented after Library Update*/
                        /*Picasso.with(context)
                                .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mVideoArrayList.get(position).Image)
                                .error(R.drawable.default_img)
                                .placeholder(R.drawable.default_img)
                                .into(holder.mImgVideoImage);*/
                    } else {
                        holder.mImgVideoImage.setImageResource(R.drawable.default_img);
                    }

                    holder.mLike.setOnClickListener(this);
                    holder.mLike.setTag(position);

                /* holder.mImgVideoImage.setOnClickListener(this);
                   holder.mImgVideoImage.setTag(position);*/
                    holder.mRootLayout.setOnClickListener(this);
                    holder.mRootLayout.setTag(position);

                    holder.mDownload.setOnClickListener(this);
                    holder.mDownload.setTag(position);

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (itemType == ITEM_TYPE_QUIZ || itemType == ITEM_TYPE_NOTES) {

             /*View Holder for Notes and Quiz which are Videos*/
            QuizViewHolder holder = ((QuizViewHolder) viewHolder);

            try {

                holder.txtTitle.setText(mVideoArrayList.get(position).Title);
                holder.txtDescription.setText(mVideoArrayList.get(position).Description);

                if (itemType == ITEM_TYPE_QUIZ)
                    holder.txtDetails.setText(mVideoArrayList.get(position).Duration + " Questions");
                else
                    holder.txtDetails.setText(mVideoArrayList.get(position).Duration + " Notes");

                if (!TextUtils.isEmpty(mVideoArrayList.get(position).Image)) {
                    SharedPrefrences mSharedPref = new SharedPrefrences(context);


                    /*Updated version of Picasso Library Update*/
                    Picasso.get()
                            .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mVideoArrayList.get(position).Image)
                            .error(R.drawable.default_img)
                            .placeholder(R.drawable.default_img)
                            .into(holder.imgVideoImageChapterVideoListItem);

                    /*Commented after Library Update*/
                    /*Picasso.with(context)
                            .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + mVideoArrayList.get(position).Image)
                            .error(R.drawable.default_img)
                            .placeholder(R.drawable.default_img)
                            .into(holder.imgVideoImageChapterVideoListItem);*/
                } else {
                    holder.imgVideoImageChapterVideoListItem.setImageResource(R.drawable.default_img);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*Testing URL*/
                        //String dummyUrl = "https://www.visualphysics.nlytn.in/test/test02/";
                        //openWebViewFromURL(dummyUrl);

                        openWebViewFromURL(mVideoArrayList.get(position).DownloadURL);

                        /*Code to get actual link from Deep Link*/
                       /* Uri myUri = Uri.parse("https://n88nw.app.goo.gl/prom");
                        FirebaseDynamicLinks.getInstance().getDynamicLink(myUri).addOnCompleteListener(new OnCompleteListener<PendingDynamicLinkData>() {
                            @Override
                            public void onComplete(@NonNull Task<PendingDynamicLinkData> task) {
                                if (task != null) {
                                    if (task.getResult() != null) {
                                        String url = task.getResult().getLink().toString();

                                    }
                                }

                            }
                        });*/
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    void animation(final View v) {
        Animation comeagain;
        comeagain = AnimationUtils.loadAnimation(context, R.anim.scale_profile);
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
    }

    @Override
    public int getItemCount() {
        if (mVideoArrayList != null) {

            /*Added to show blank item on last position*/
            return mVideoArrayList.size() + 1;

        } else {
            return 0;
        }
    }


    @Override
    public int getItemViewType(int position) {

        if (mVideoArrayList != null) {

            /*To show blank space for last item*/
            if (position == mVideoArrayList.size()) {
                return ITEM_TYPE_CONCEPT;

            } else {
                if (mVideoArrayList.get(position).Tag != null) {
                    if (!TextUtils.isEmpty(mVideoArrayList.get(position).Tag))
                        return Integer.parseInt(mVideoArrayList.get(position).Tag);
                    else
                    return ITEM_TYPE_DEFAULT;

                } else {
                    return ITEM_TYPE_DEFAULT;
                }
            }

        } else {
            return ITEM_TYPE_DEFAULT;
        }
    }

    @Override
    public void onClick(View v) {
        int position;

        try {

            switch (v.getId()) {
                case R.id.like:
                    position = (Integer) v.getTag();
                    ImageView imgLike = (ImageView) v;
                    imgLike.setImageResource(R.drawable.likefill);
                    animation(imgLike);
                    addVideoLike(position);
                    addLikeEvent(position);

                    //IZISS MPE to Send Like Event
                    sendToMixpanel(mVideoArrayList.get(position), false);

                    break;

                //case R.id.imgVideoImageChapterVideoListItem:
                case R.id.tab4clickable_LinearLayout:
                    position = (Integer) v.getTag();
                    mSelectedVideoPosition = position;
                    //tokenData = readFileFromAsset(tokenFileName);
                    mVideoDetail = mVideoArrayList.get(position);
                /*try {
                    Runtime.checkLicense(tokenData);

                } catch (Exception e) {
                    Log.e("Token Processing error:",e.getMessage());
                    try {
                        new TokenHandler().execute(tokenData);
                    }
                    catch (Exception ex) {
                        Log.e("Token Processing error:",ex.getMessage());
                    }
                }*/
                    if (!LicenseUtil.isLicenseExpiry(context)) {
                   /* if ((Build.VERSION.SDK_INT >= 23)) {
                        Activity activity = (Activity) context;
                        boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                        if (!hasPermission) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION_CODE);

                        } else {
                            Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                            i.putExtra("VideoDetail", mVideoDetail);
                            context.startActivity(i);
                        }
                    } else {
                        Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                        i.putExtra("VideoDetail", mVideoDetail);
                        context.startActivity(i);
                    }*/
                        playVideoScreen();

                    } else {
                        mAppUtils.displaySnackBarWithMessage(((Activity) context).findViewById(android.R.id.content),
                                context.getResources().getString(R.string.msg_license_expired));
                        setOnVideoPlayLicenseProcessListener();
                        LicenseUtil License = new LicenseUtil(this.OnVideoPlayLicenseListener);
                        License.acquireLicence(context, mSharedPref.getLoginUser().getStudentID());

                    }
                    break;

                case R.id.imgDownloadChapterVideoListItem:

                    position = (Integer) v.getTag();
                    mVideoDetail = mVideoArrayList.get(position);
                    ImageView imgDownload = (ImageView) v;
                    mImgSelectDownloadImage = imgDownload;

                    //Check if file already downloaded then delete the download video
                    if (mAppUtils.isVideoDownload(mVideoDetail.DownloadURL)) {
                        showDeleteVideoDialog(context, "Do you want to delete this video ?",
                                imgDownload, mVideoArrayList.get(position));

                    } else {
                        onDownloadClick(imgDownload, position);
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static class ChapterNameViewHolder extends RecyclerView.ViewHolder {
        View divider;
        ImageView mDownload, mLike, mImgVideoImage;
        TextView mTxtVideoTitle, mTxtVideoSize, mTxtVideoDuration, mTxtVideoDesc;
        LinearLayout mRootLayout;
        View separatorOne, separatorTwo;

        public ChapterNameViewHolder(View itemView) {
            super(itemView);
            divider = (View) itemView.findViewById(R.id.divider_chaptername);
            mDownload = (ImageView) itemView.findViewById(R.id.imgDownloadChapterVideoListItem);
            mLike = (ImageView) itemView.findViewById(R.id.like);
            mImgVideoImage = (ImageView) itemView.findViewById(R.id.imgVideoImageChapterVideoListItem);
            mTxtVideoTitle = (TextView) itemView.findViewById(R.id.txtVideoTitleChapterVideoListItem);
            mTxtVideoSize = (TextView) itemView.findViewById(R.id.txtVideoSizeChapterVideoListItem);
            mTxtVideoDuration = (TextView) itemView.findViewById(R.id.txtVideoDurationChapterVideoListItem);
            mTxtVideoDesc = (TextView) itemView.findViewById(R.id.txtVideoDescChapterVideoListItem);
            mRootLayout = (LinearLayout) itemView.findViewById(R.id.tab4clickable_LinearLayout);

            separatorOne = (View) itemView.findViewById(R.id.separatorOne);
            separatorTwo = (View) itemView.findViewById(R.id.separatorTwo);

        }
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {

        View divider;
        ImageView imgVideoImageChapterVideoListItem;
        TextView txtTitle, txtDescription, txtDetails;

        public QuizViewHolder(View itemView) {
            super(itemView);

            divider = (View) itemView.findViewById(R.id.divider);
            imgVideoImageChapterVideoListItem = (ImageView) itemView.findViewById(R.id.imgVideoImageChapterVideoListItem);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            txtDetails = (TextView) itemView.findViewById(R.id.txtDetails);

        }
    }


    /***
     * Add the Like count of Video
     *
     * @param position
     */
    private void addVideoLike(int position) {

        //Save data in Database and on splash screen check if data are remaining to upload and
        //internet connectivity available then upload on the server
        ChapterVideoDetail VideoDetail = new ChapterVideoDetail(mVideoArrayList.get(position).VideoID,
                mVideoArrayList.get(position).ChapterID,
                mSharedPref.getLoginUser().getStudentID());
        VideoDetail.setLikeStatus(true);

        if (!db.doAddVideoLike(VideoDetail)) {
            //notifyDataSetChanged();
            Activity activity = (Activity) context;
            mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                    context.getResources().getString(R.string.Error_Msg_Try_Later));
        } else {
            //notifyDataSetChanged();
            // If Internet avilable add the video like
            if (mAppUtils.getConnectionState()) {

                mApiCall.doAddVideoLike(mVideoArrayList.get(position).VideoID,
                        mSharedPref.getLoginUser().getStudentID(), mAppUtils.getDeviceID(),
                        new OnTaskCompleted(this), mApiCall.AddVideoLike);

            }
        }
    }


    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        if (Method.equals(mApiCall.AddVideoLike)) {
            parseResponseForAddVideoLikeData(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {

    }

    /***
     * Parse the response of Add Video Like
     *
     * @param response
     */
    private void parseResponseForAddVideoLikeData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddVideoLike);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 0) {

                mJsonObj = mJsonObj.getJSONObject("data");
                int VideoID = Integer.parseInt(mJsonObj.getString("VideoID"));
                db.updateUploadVideoStatus(VideoID, "UploadLikeStatus");
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }

    private String readFileFromAsset(String filePath) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        InputStream is;

        String line;
        try {
            is = am.open(filePath);

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            ErrorLog.SaveErrorLog(e);
            Log.i("Token Not Loaded", e.getMessage());
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } finally {
            if (br != null) {
                try {
                    br.close();

                } catch (IOException e) {
                    ErrorLog.SaveErrorLog(e);
                    e.printStackTrace();
                    ErrorLog.SendErrorReport(e);
                }
            }
        }

        return sb.toString();

    }

    private class TokenHandler extends AsyncTask<String, Void, Void> {
        ProgressDialog progress;
        boolean completedLicenseAcquisition = false;

        String tokenData;

        protected void onPreExecute() {

            try {
                //tokenData = readFileFromAsset(tokenFileName);
                //Looper.prepare();
                if (!completedLicenseAcquisition)
                    progress = ProgressDialog.show(context, "Please wait", "Aquiring Licenese ...", true);

            } catch (Exception e) {
                ErrorLog.SaveErrorLog(e);
                Log.e("TokenHandler:", e.getMessage());
                ErrorLog.SendErrorReport(e);
            }
        }

        protected Void doInBackground(String... params) {

            try {
                //mVideoDetail = params[0];

                // The action token is processed here. the method automatically stores the license to the local DB.
                Runtime.processServiceToken(params[0]);


            } catch (Exception e) {
                ErrorLog.SaveErrorLog(e);
                Log.e("TokenHandler:", e.getMessage());
                ErrorLog.SendErrorReport(e);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {


            if (progress != null) {
                progress.dismiss();
                completedLicenseAcquisition = true;

               /* Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                i.putExtra("VideoDetail",mVideoDetail);
                context.startActivity(i);*/
                playVideoScreen();
            }


        }
    }

    public void showDownLoadDialog(Context activity, String msg, final ImageView imageDownload,
                                   final Videos VideoDetail) {
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

                //imageDownload.setImageDrawable(context.getResources().getDrawable(R.drawable.download_1));
                dialog.dismiss();

            }


        });

        Button yes = (Button) dialog.findViewById(R.id.btn_yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*imageDownload.setVisibility(View.GONE);
                imageDownload.setVisibility(View.VISIBLE);*/
                dialog.dismiss();
                if (imageDownload != null)
                    animation(imageDownload);

                try {
                    if (!LicenseUtil.isLicenseExpiry(context)) {
                        Download_Manager mDownloadManger = new Download_Manager(context);
                        setOnVideoDownloadListener();
                        mDownloadManger.init(VideoDetail.DownloadURL,
                                VideoDetail.Title, VideoDetail.VideoID, VideoDetail.ChapterID,
                                false, onVideoDownloadListener);
                    } else {
                        mAppUtils.displaySnackBarWithMessage(((Activity) context).findViewById(android.R.id.content),
                                context.getResources().getString(R.string.msg_license_expired));
                        setOnVideoDownloadLicenseProcessListener();
                        LicenseUtil License = new LicenseUtil(onVideoDownloadLicenseListener);
                        License.acquireLicence(context, mSharedPref.getLoginUser().getStudentID());
                    }
                } catch (Exception e) {
                    Activity activity = (Activity) context;
                    mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                            "Not able to acquire token detail, Please try later");
                    ErrorLog.SendErrorReport(e);
                }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    public void showDeleteVideoDialog(Context activity, String msg, final ImageView imageDownload,
                                      final Videos VideoDetail) {
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

                //imageDownload.setImageDrawable(context.getResources().getDrawable(R.drawable.download_1));
                dialog.dismiss();

            }


        });

        Button yes = (Button) dialog.findViewById(R.id.btn_yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dialog.dismiss();
                try {
                    Activity activity = (Activity) context;
                    //Check if file already downloaded then cancel the download
                    if (mAppUtils.isVideoDownload(mVideoDetail.DownloadURL)) {
                        File dir = new File(Environment.getExternalStorageDirectory() + "/"
                                + context.getResources().getString(R.string.app_name) + "/" + mVideoDetail.DownloadURL);
                        if (dir.exists()) {
                            if (dir.delete()) {
                                mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                        "Video deleted.");
                                notifyDataSetChanged();
                            } else
                                mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                                        "Not able to delete the video, Please try later");
                        }


                    }

                } catch (Exception e) {
                    Activity activity = (Activity) context;
                    mAppUtils.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                            "Not able to delete the video, Please try later");
                    ErrorLog.SendErrorReport(e);
                }


            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void setOnVideoDownloadListener() {

        this.onVideoDownloadListener = new OnVideoDownloadListener() {
            @Override
            public void onVideoDownload() {
                notifyDataSetChanged();

                //IZISS MPE
                sendToMixpanel(mVideoDetail, true);


            }
        };
    }


    /***
     * If Token process successfully then start the Video
     */
    private void setOnVideoPlayLicenseProcessListener() {

        this.OnVideoPlayLicenseListener = new LicenseListener() {
            @Override
            public void onProcessToken() {
                /*Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                i.putExtra("VideoDetail", mVideoDetail);
                context.startActivity(i);*/
                playVideoScreen();
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
                Download_Manager mDownloadManger = new Download_Manager(context);
                setOnVideoDownloadListener();
                mDownloadManger.init(mVideoDetail.DownloadURL,
                        mVideoDetail.Title, mVideoDetail.VideoID, mVideoDetail.ChapterID,
                        false, onVideoDownloadListener);
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

                showDownLoadDialog(context, "Do you want to download this video ?",
                        mImgSelectDownloadImage, mVideoDetail);

            } else {
                Toast.makeText(context, "The app was not allowed to write to your storage. Hence, " +
                                "it cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG).show();
            }


        } else if (REQUEST_PERMISSION_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_PERMISSION_CODE) {
                    /*Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                    i.putExtra("VideoDetail", mVideoDetail);
                    context.startActivity(i);*/
                    playVideoScreen();
                }
            } else {
                Toast.makeText(context, "The app was not allowed to write to your storage. Hence, " +
                                "it cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG).show();
            }
        }

    }


    /**
     * Got to video screen , before check permission
     */
    private void playVideoScreen() {
        if ((Build.VERSION.SDK_INT >= 23)) {
            Activity activity = (Activity) context;
            boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);

            } else {

                //UNDO ME 27 Sep 2018
                //Intent i = new Intent(context, NewVideoPlayerScreenActivity.class);
                Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                //i.putExtra("VideoDetail", mVideoDetail);
                i.putExtra("videos", mVideoArrayList);
                i.putExtra("selectedVideoPosition", mSelectedVideoPosition);
                context.startActivity(i);
            }
        } else {
            //UNDO ME 27 Sep 2018
            //Intent i = new Intent(context, NewVideoPlayerScreenActivity.class);
            Intent i = new Intent(context, VideoPlayerScreenActivity.class);
            //i.putExtra("VideoDetail", mVideoDetail);
            i.putExtra("videos", mVideoArrayList);
            i.putExtra("selectedVideoPosition", mSelectedVideoPosition);
            context.startActivity(i);
        }
    }

    /**
     * Add Event on Video like
     */
    private void addLikeEvent(int Position) {
        String ContentName = mVideoArrayList.get(Position).Title; //Video Title
        String CategoryName = ChapterVideoScreenActivity.ChapterName; //ChpaterName
        String ContentID = String.valueOf(mVideoArrayList.get(Position).VideoID); //Video ID
        //PyzeEvents.PyzeMedia.postRatedThumbsUp(ContentName, CategoryName, ContentID, null);
    }


    @Override
    public void onGotItClick(ImageView imgDownload, int position) {

        onDownloadClick(imgDownload, position);

    }

    /***
     * Created by IZISS to manage download
     *
     * @param imgDownload
     * @param position
     */
    private void onDownloadClick(ImageView imgDownload, int position) {

        if (mSharedPref.getBooleanPreferences(SharedPrefrences.IS_DOWNLOAD_POPUP_SHOWN, false)) {

            if ((Build.VERSION.SDK_INT >= 23)) {
                Activity activity = (Activity) context;
                boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);

                } else {
                    showDownLoadDialog(context, "Do you want to download this video ?",
                            imgDownload, mVideoArrayList.get(position));
                }
            } else {
                showDownLoadDialog(context, "Do you want to download this video ?",
                        imgDownload, mVideoArrayList.get(position));
            }


        } else {
            AppUtil.showOneTimeDialog(context, imgDownload, position, this);
        }

    }

    /***
     * Send data to mixpanel
     *
     * @param mVideos
     */
    private void sendToMixpanel(Videos mVideos, boolean isVideoDownload) {
        {
            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            DataBase mDataBase = new DataBase(context);
            Chapters mChapters = mDataBase.getChapterDetail(mVideos.ChapterID);

            if (mChapters != null)
                hashMap.put("CHAPTER_NAME", "" + mChapters.ChapterName);

            //Key and their values which will be send to Mix Panel
            hashMap.put("CHAPTER_ID", "" + mVideos.ChapterID);
            hashMap.put("VIDEO_NAME", mVideos.Title);
            hashMap.put("VIDEO_ID", "" + mVideos.VideoID);

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(context);

            //If first chapter after first login, this parameter is set to true on ReferalCodeScreenActivity
            if (isVideoDownload)
                mixPanelClass.sendData(MixPanelClass.MPE_DOWNLOAD_VIDEO, hashMap);
            else {
                mixPanelClass.sendData(MixPanelClass.MPE_LIKE_VIDEO_THUMBS_UP, hashMap);
            }

        }
    }

    /**
     * This will get the required values from URL and open them in Web Activity
     *
     * @param url
     */
    private void openWebViewFromURL(String url) {

        Intent mIntent = new Intent(context, WebViewActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        /*Direct URL*/
        mIntent.putExtra(WebViewActivity.IS_DIRECT_URL, true);
        mIntent.putExtra(WebViewActivity.URL, url);


        /* Commented as we will directly load actual urls
        String pageNameToLoad = AppUtil.getIds(url)[3];
        String pageToLoad = AppUtil.getIds(url)[4];

        mIntent.putExtra(WebViewActivity.PAGE_NAME_TO_LOAD, pageNameToLoad);
        mIntent.putExtra(WebViewActivity.PAGE_TO_LOAD, pageToLoad);*/


        context.startActivity(mIntent);
    }

}