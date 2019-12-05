package com.visualphysics;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.media.PlaylistProxy;
import com.pyze.android.PyzeEvents;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Database.DataBase;
import Model.Chapters;
import Model.Videos;
import UIControl.CustomTextViewRegular;
import Utils.AppConstansts;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.DebugLog;
import Utils.DoubleClickListener;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.SharedPrefrences;
import deeplink.DeepLinkGenerator;
import deeplink.OnLinkGenerateListener;
import firebase.FirebaseClass;
import guide.GuideDialog;
import guide.GuideType;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

/**
 * Created by India on 7/6/2016.
 */
public class VideoPlayerScreenActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewClickListener {

    //IZISS Playback
    private VideoView player;
    //private final String contentPath = Environment.getExternalStorageDirectory().getPath() + "/video.mlv";
    //private final String contentPath = "http://www.nlytn.in/app/test/2/stream.mpd";
    private String contentPath;
    private Videos VideoDetail = null;
    private ArrayList<Videos> videoPlayList = null;
    private int selectedVideoPosition = 0;
    private boolean isVideoCompletionAttached = false;
    private AppUtil mAppUtils;
    private ContentTypes contentType;
    private SharedPrefrences mSharedPref;

    //To  restrict mirroring
    private MediaRouter mMediaRouter;
    private VideoPresentation mPresentation;
    private boolean mPaused;
    private FirebaseClass mFirebaseClass;

    private long videoOpenTime = 0;
    private long videoPlayedTime = 0;

    String json = "";

    //IZISS Video Changes
    private LicenseListener OnVideoPlayLicenseListener;
    private ImageView btnUp, btnDown, btnForward, btnBackward, btnFullScreen, btnPlayPause, btnShare;
    private CustomTextViewRegular videoNameTxt;
    private TextView txtSpeed;
    private LinearLayout llOptions, llLeft, llRight, videoControlsLayout, llShareOptions;
    private RelativeLayout fastRewindLayout, fastForwardLayout;
    private ProgressBar progressBar;

    float playBackSpeed = 1.0f;
    private int totalVideoDuration, totalVideoPlayedPercentage;
    private List<PlaybackSpeedScreen.VideoSpeed> mVideoSpeeds;
    private float[] speeds = {0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

    //Undo IZISS
    private Handler mHandlerOptions, mHandlerView, mHandlerSeekBar, mHandlerRewind, mHandlerVideoToast;
    private Runnable mRunnableOptions, mRunnableViews, mRunnableSeekBar, mRunnableRewind, mRunnableVideoToast;
    private final int HIDE_TIME_OUT = 2500;// 2.5 Seconds
    private SeekBar seekBar;
    private TextView playedTimeTxt;
    private TextView videoTimeTxt;
    private LinearLayout seekBarLayout;
    private ImageView videoSpeedIcon, btnRewind;
    private LinearLayout videoSpeedLayout;
    private boolean isVideoPlayed = false;

    //Time in millis to show toast message 5 Secs
    private long TIME_TO_SHOW_MESSAGE = 5000;

    //private boolean isFromVideoShare = false;
    //private boolean isFromValidPause = false;
    // private long lastPositionOfVideoWhenGoInPause = 0;

    int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}, // enabled
            new int[]{-android.R.attr.state_enabled}, // disabled
            new int[]{-android.R.attr.state_checked}, // unchecked
            new int[]{android.R.attr.state_pressed}  // pressed
    };

    @Override
    public void onItemClick(Object o) {
        for (int i = 0; i < mVideoSpeeds.size(); i++) {
            if (mVideoSpeeds.get(i).isSelected()) {
                txtSpeed.setText(mVideoSpeeds.get(i).getPlaybackSpeed() + "x");
                if (player != null) {
                    player.setPlaybackSpeed(mVideoSpeeds.get(i).getPlaybackSpeed());
                }
            }
        }
    }


    enum ContentTypes {
        DASH("application/dash+xml"), HLS("application/vnd.apple.mpegurl"), PDCF(
                "video/mp4"), M4F("video/mp4"), DCF("application/vnd.oma.drm.dcf"), BBTS(
                "video/mp2t"), CFF("video/mp4");
        String mediaSourceParamsContentType = null;

        ContentTypes(String mediaSourceParamsContentType) {
            this.mediaSourceParamsContentType = mediaSourceParamsContentType;
        }

        public String getMediaSourceParamsContentType() {
            return mediaSourceParamsContentType;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the media router service.
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

        // Obtain the Firebase Analytics instance
        mFirebaseClass = new FirebaseClass(this);

        setContentView(R.layout.video_player_screen_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //getSupportActionBar().setTitle("Video Player");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        mAppUtils = new AppUtil(getApplicationContext());

        //Check the last open app UTC date time with current UTC date TIme
        if (checkLastOpenAppDateTime()) {
            mDeclaration();

            Bundle bundle = new Bundle();
            bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, VideoDetail.VideoID);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, VideoDetail.Title);

          /*  mFirebaseClass.sendAnalyticsData(VideoDetail.VideoID, VideoDetail.Title, AppUtil.EVENT_PLAY_VIDEO);
            new FirebaseClass(VideoPlayerScreenActivity.this).sendCustomAnalyticsData("custom_video_play", "Video Played");
			  sendToMixpanel(VideoDetail);*/
        }

        //Show Zoho chat icon
        ZohoUtils.hideZohoChat();

        //This will generate the share link when video is open
        generateDeepLinkForVideoInBG();


    }

    private void mDeclaration() {

        player = findViewById(R.id.videoView);

        //IZISS Video Player
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnShare = findViewById(R.id.share_btn);
        txtSpeed = findViewById(R.id.txtSpeed);
        progressBar = findViewById(R.id.progressBar);
        videoNameTxt = findViewById(R.id.video_name_txt);
        videoSpeedIcon = findViewById(R.id.video_speed_icon);
        videoSpeedLayout = findViewById(R.id.video_speed_layout);
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        videoControlsLayout = findViewById(R.id.video_controls_layout);
        llOptions = findViewById(R.id.llOptions);
        llShareOptions = findViewById(R.id.share_options);
        llLeft = findViewById(R.id.llLeft);
        llRight = findViewById(R.id.llRight);
        fastRewindLayout = findViewById(R.id.fast_rewind_layout);
        fastForwardLayout = findViewById(R.id.fast_forward_layout);

        //Undo Me
        seekBar = findViewById(R.id.seekBar);
        playedTimeTxt = findViewById(R.id.played_time_txt);
        videoTimeTxt = findViewById(R.id.video_time_txt);
        seekBarLayout = findViewById(R.id.seek_bar_layout);
        btnRewind = findViewById(R.id.btnRewind);

        btnFullScreen.setVisibility(View.INVISIBLE);
        llOptions.setVisibility(View.INVISIBLE);
        llShareOptions.setVisibility(View.INVISIBLE);
        videoControlsLayout.setVisibility(View.INVISIBLE);
        seekBarLayout.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();

        //Check Video Detail come from Deep Link
        if (intent.hasExtra("VideoDetail")) {

            VideoDetail = (Videos) intent.getSerializableExtra("VideoDetail");

            DataBase database = new DataBase(this);

            videoPlayList = (ArrayList<Videos>) database.doGetVideosList(VideoDetail.Type, VideoDetail.ChapterID);

            if (getVideoPosition(VideoDetail.VideoID) >= 0) {
                selectedVideoPosition = getVideoPosition(VideoDetail.VideoID);
            } else {
                selectedVideoPosition = 0;
            }

        } else if (intent.hasExtra("videos")) {
            videoPlayList = (ArrayList<Videos>) intent.getSerializableExtra("videos");
            selectedVideoPosition = intent.getIntExtra("selectedVideoPosition", 0);

            VideoDetail = videoPlayList.get(selectedVideoPosition);
        } else {
            AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            finish();
        }

        progressBar.setVisibility(View.VISIBLE);

        //IZISS VIDEO TIME
        updateVideoOpenTime();

        /*//Check video is download or not if download then play from sd card else server
        if (mAppUtils.isVideoDownload(VideoDetail.DownloadURL)) {
            DebugLog.d("File Present", " -----------");
            contentType = ContentTypes.PDCF;
            contentPath = Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;
        } else if (mAppUtils.isVideoAvialableInMemoryCard(VideoDetail.DownloadURL)) { //From External Memory card
            DebugLog.d("File Present", " -----------");
            contentType = ContentTypes.PDCF;
            *//*contentPath = System.getenv("SECONDARY_STORAGE") + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;*//*
            contentPath = mAppUtils.getExternalSDCardPath() + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;
        } else {
            if (!mAppUtils.getConnectionState()) {
                //mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                DebugLog.d("File Not Present", " -----------");
                contentType = ContentTypes.DASH;
                //contentPath = "http://192.168.1.108/VisualPhysicsAdmin/trunk/uploads/"+VideoDetail.StreamingURL;

                //contentPath = "http://www.nlytn.in/app/test/2/stream.mpd";
                contentPath = mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + VideoDetail.StreamingURL;
                DebugLog.d("File Not Present", " ----------- " + contentPath);
            }
        }*/
        //getContentPath();

        //IZISS EDIT
        if (intent.hasExtra("directPlay")) {
            checkLicense(contentPath);
        } else {
            playVideo(contentPath);
        }

        //IZISS Video Changes
        setCustomVideoListeners();

        int color = Color.parseColor("#55000000");
        btnBackward.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        btnForward.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        /*Listener on rewind button click*/
        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoPlayList != null) {
                    if (videoPlayList.size() > 0) {

                        VideoDetail = videoPlayList.get(selectedVideoPosition);
                        contentPath = null;

                        //This will play the video
                        playVideo(null);

                        hideRewindButton(true);

                    }
                }

            }
        });

    }

    private boolean isFilePresent(String filePath) {
        try {

            File f = new File(filePath);

            Log.i("CHECK_FILE", filePath);


            if (f.exists()) {
                Log.i("CHECK_FILE_PRESENT", filePath);

                return true;

            } else {
                Log.i("CHECK_FILE_NOT_PRESENT", filePath);
                return false;
            }


        } catch (Exception e) {

            Log.e("FILE_CHECK_FAILS", e.getLocalizedMessage());
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }

        return false;
    }

    private List<PlaybackSpeedScreen.VideoSpeed> getVideoSpeeds() {
        if (mVideoSpeeds == null) {
            mVideoSpeeds = new ArrayList<>();
            for (float speed : speeds) {
                PlaybackSpeedScreen.VideoSpeed videoSpeed;
                if (speed == 1.0f) {
                    videoSpeed = new PlaybackSpeedScreen.VideoSpeed(speed, true);
                } else {
                    videoSpeed = new PlaybackSpeedScreen.VideoSpeed(speed, false);
                }
                mVideoSpeeds.add(videoSpeed);
            }
        }
        return mVideoSpeeds;
    }

    private void handlePlayListButtons() {

        if (selectedVideoPosition == 0) {
            btnDown.setVisibility(View.INVISIBLE);
        } else {
            btnDown.setVisibility(View.VISIBLE);
        }

        if (selectedVideoPosition == (videoPlayList.size() - 1)) {
            btnUp.setVisibility(View.INVISIBLE);
        } else {
            btnUp.setVisibility(View.VISIBLE);
        }

    }

    private void playVideo(String videoUrl) {
        // Toast.makeText(this, "Playing Video : " + selectedVideoPosition, Toast.LENGTH_LONG).show();


        btnForward.setVisibility(View.INVISIBLE);
        btnBackward.setVisibility(View.INVISIBLE);
        /*btnFullScreen.setVisibility(View.INVISIBLE);
        llOptions.setVisibility(View.INVISIBLE);
        videoControlsLayout.setVisibility(View.INVISIBLE);
        seekBarLayout.setVisibility(View.INVISIBLE);*/


        try {

            //ContentTypes contentType = ContentTypes.DASH; //live
            //ContentTypes contentType = ContentTypes.PDCF;

            //Check if content path is null then get the content path again
            if (TextUtils.isEmpty(contentPath))
                getContentPath();

            PlaylistProxy.MediaSourceParams params = new PlaylistProxy.MediaSourceParams();
            params.sourceContentType = contentType
                    .getMediaSourceParamsContentType();

            //For Subtitle we need a url of a .vtt file
            //params.subtitleUrl = "http://www.nlytn.in/sub/subt.vtt";
            //params.subtitleUrl = "http://content-access.intertrust-dev.com/content/dash/frozen_dash_list_sub/subtitles/eng/subtitles_en.vtt";

            //Language Code reference
            //params.subtitleLang = "eng";

            //Language name
            //params.subtitleName = "English";

            PlaylistProxy playlistProxy = new PlaylistProxy();
            playlistProxy.start();

            // Url to protected content
            String contentTypeValue = contentType.toString();
            String playerUrl = playlistProxy.makeUrl(contentPath, PlaylistProxy.MediaSourceType.valueOf((contentTypeValue == "HLS" || contentTypeValue == "DASH") ? contentTypeValue : "SINGLE_FILE"), params);

            Log.e("LOCAL_PROXY_URL", playerUrl);

            player.setVisibility(View.VISIBLE);

            //player.setVideoPath("http://127.0.0.1:48200/R/9flhCkvIpr2W9PdPYldYuW5Cffc.m3u8");
            //player.setVideoPath(playerUrl);
            player.setVideoURI(Uri.parse(playerUrl));
            player.requestFocus();
            player.start();

            //This will set all listeners
            setListeners();

            // videoNameTxt.setText(VideoDetail.Title);

            mFirebaseClass.sendAnalyticsData(VideoDetail.VideoID, VideoDetail.Title, AppUtil.EVENT_PLAY_VIDEO);
            new FirebaseClass(VideoPlayerScreenActivity.this).sendCustomAnalyticsData("custom_video_play", "Video Played");


        } catch (ErrorCodeException e) {
            ErrorLog.SendErrorReport(e);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private String convertToVideoTimeFormat(long milliseconds) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    /***
     * Set Video player Listeners
     */
    private void setListeners() {

        player.setOnPreparedListener(() -> {

            //IZISS VIDEO TIME to get when video start play
            updateVideoPlayedTime();

            progressBar.setVisibility(View.GONE);

            handlePlayListButtons();
            videoNameTxt.setText(VideoDetail.Title);
            btnFullScreen.setVisibility(View.VISIBLE);
            llOptions.setVisibility(View.VISIBLE);
            llShareOptions.setVisibility(View.VISIBLE);
            videoControlsLayout.setVisibility(View.VISIBLE);
            seekBarLayout.setVisibility(View.VISIBLE);
            addVideoCount();

            //IZISS Playback
            totalVideoDuration = (int) player.getDuration();

            //Undo Me
            seekBar.setMax(totalVideoDuration / 1000);
            videoTimeTxt.setText(convertToVideoTimeFormat(totalVideoDuration));
            playedTimeTxt.setText("0:00");
            videoTimeTxt.setVisibility(View.VISIBLE);
            playedTimeTxt.setVisibility(View.VISIBLE);
            updateSeekBar();

            mRunnableOptions = new Runnable() {
                @Override
                public void run() {

                    changeOptionsVisibility();

                    showGuide();
                }
            };

            mHandlerOptions.postDelayed(mRunnableOptions, 2000);
        });

        //IZISS Playback
        player.setOnErrorListener(e -> {
            progressBar.setVisibility(View.GONE);
            AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    "Unable to play video please try later");

            ErrorLog.SendErrorReportUnableToPlayVideo(e);

            return true;
        });


        if (!isVideoCompletionAttached) {
            isVideoCompletionAttached = true;
            player.setOnCompletionListener(() -> {
                if (!(player.isPlaying()) && (player.getDuration() > 0)) {
                    Log.v("Android ExoMedia", "Played Video No : " + selectedVideoPosition);
                    if ((selectedVideoPosition) < (videoPlayList.size() - 1)) {

                        //Commented by IZISS
                        /*selectedVideoPosition++;
                        VideoDetail = videoPlayList.get(selectedVideoPosition);
                        contentPath = null;
                        progressBar.setVisibility(View.VISIBLE);
                        playVideo(null);*/

                        //IZISS Rewind 17 May 2k18 to show rewind button
                        showRewindButton();

                    } else if (selectedVideoPosition == (videoPlayList.size() - 1)) {

                        player.restart();
                        player.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_video_play);
                        totalVideoPlayedPercentage = 100;

                        if (mHandlerOptions != null) {
                            mHandlerOptions.removeCallbacks(mRunnableOptions);
                        }
                        if (mHandlerView != null) {
                            mHandlerView.removeCallbacks(mRunnableViews);
                        }
                        if (mHandlerSeekBar != null) {
                            mHandlerSeekBar.removeCallbacks(mRunnableSeekBar);
                        }

                        /*Remove Handler  call back when video is changed*/
                        if (mHandlerRewind != null) {
                            mHandlerRewind.removeCallbacks(mRunnableRewind);
                        }

                        /*Remove Handler which show toast*/
                        if (mHandlerVideoToast != null) {
                            mHandlerVideoToast.removeCallbacks(mRunnableVideoToast);
                        }

                        progressBar.setVisibility(View.GONE);


                        //IZISS Rewind 17 May 2k18 to show rewind button
                        showRewindButton();

                    }
                } else {
                    if (totalVideoPlayedPercentage != 100) {
                        contentPath = null;
                        progressBar.setVisibility(View.VISIBLE);
                        playVideo(null);
                    }
                }
            });

        }

        //Added by IZISS on 30 Jan 2019 as a temporary fix for Mirroring
        if (AppUtil.isDebuggingModeEnabled(this)) {
            pauseVideo();
        }

       /* if (player != null) {
            //This will manage our view with default view
            player.getVideoControls().setVisibilityListener(new VideoControlsVisibilityListener() {
                @Override
                public void onControlsShown() {

                    slideUpDown(true);


                }

                @Override
                public void onControlsHidden() {


                    slideUpDown(false);

                }
            });
        }*/
    }

    /***
     * Add Video play count
     */
    private void addVideoCount() {
        AddCountToVideo AddCount = new AddCountToVideo(getApplicationContext());
        AddCount.addPlayVideoCount(VideoDetail.VideoID, VideoDetail.ChapterID);
    }


    //Check the last open app UTC date time with current UTC date TIme
    private boolean checkLastOpenAppDateTime() {
        boolean status = false;
        mSharedPref = new SharedPrefrences(getApplicationContext());
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm a");
            Date UTCDateTime = format.parse(mAppUtils.getUTCTime());

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String strUTCDateTime = format.format(UTCDateTime);

            if (!mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "").equals("")) {
                String strLastAppOpenDate = mSharedPref.getPreferences(mSharedPref.LAST_APP_OPEN_DATE, "");
                Date CurrentDate = null, LastAppOpenDate = null;
                CurrentDate = format.parse(strUTCDateTime);//catch exception
                LastAppOpenDate = format.parse(strLastAppOpenDate);//catch exception

                /*Calendar sDate = getDatePart(LastAppOpenDate);
                Calendar eDate = getDatePart(CurrentDate);
*/

                if (CurrentDate.getTime() < LastAppOpenDate.getTime()) {
                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(VideoPlayerScreenActivity.this,
                            R.style.AppCompatAlertDialogStyle);
                    myAlertDialog.setTitle(R.string.app_name);
                    myAlertDialog.setMessage(R.string.msg_device_time_change);
                    myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // do something when the OK button is clicked
                            finish();
                        }
                    });

                   /* myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) { // do something

                            finish();
                        }
                    });*/

                    myAlertDialog.show();
                } else {
                    status = true;
                    //Add the Last open date
                    mAppUtils.lastOpenApp(getApplicationContext());
                }


            } else {
                status = true;
                //Add the Last open date
                mAppUtils.lastOpenApp(getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        addMediaInfoEvent();

        //IZISS MPE
        sendToMixpanel(VideoDetail);

        if ((mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 0) == AppConstansts.RATE_APP_COUNT_1
                || mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 0) == AppConstansts.RATE_APP_COUNT_2)
                && ApplicationConfiguration.isRATE_POPUP_SHOWN == false) {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                    //Added by IZISS
                    btnPlayPause.setImageResource(R.drawable.ic_video_play);
                }
            }

            //Commented by IZISS on 22 Aug to not show the rate us popup.
           /* //check if User has already click on Yes button when the Rate us dialog appears then don't display the Dialog
            if (mSharedPref.getPreferences(mSharedPref.IS_RATE_APP, false) == false)
                showCustomeDialog();
            else
                finish();*/

        } else
            finish();

        //Undo Me Removed all handlers
        try {
            mHandlerOptions.removeCallbacks(mRunnableOptions);
            mHandlerView.removeCallbacks(mRunnableViews);
            mHandlerSeekBar.removeCallbacks(mRunnableSeekBar);

            if (mHandlerRewind != null) {


                mHandlerRewind.removeCallbacks(mRunnableRewind);
            }

            /*Remove Handler which show toast*/
            if (mHandlerVideoToast != null) {
                mHandlerVideoToast.removeCallbacks(mRunnableVideoToast);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /***
     * Display Rate us dialog
     */
    public void showCustomeDialog() {
        ApplicationConfiguration.isRATE_POPUP_SHOWN = true;
        Dialog dialog = new Dialog(VideoPlayerScreenActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.rateus_screen);

        Button mBtnYes = dialog.findViewById(R.id.btnYesRateUsScreen);
        Button mBtnNo = dialog.findViewById(R.id.btnNoRateUsScreen);

        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPref.setPreferences(mSharedPref.IS_RATE_APP, true);
                startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())), 100);
                //finish();
            }
        });
        mBtnNo.setOnClickListener(v -> finish());

        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100)
            finish();
    }

    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        try {

            // Listen for changes to media routes.
            mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);

            // Update the presentation based on the currently selected route.
            mPaused = false;
            updatePresentation();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {

            //This variable stops a video when you share it else playing
            /*if (isFromVideoShare) {
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();

                        //Added by IZISS
                        btnPlayPause.setImageResource(R.drawable.ic_video_play);
                        isFromVideoShare = false;

                    }
                }
            }*/

            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();

                    //Added by IZISS
                    btnPlayPause.setImageResource(R.drawable.ic_video_play);
                }
            }

            /*else {
                //Other activity like call/message etc. log the current time of video and again play the video in onResume from this position
                if (player != null) {
                    if (player.isPlaying()) {
                        lastPositionOfVideoWhenGoInPause = player.getCurrentPosition();

                    }
                }
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }


        try {
            // Stop listening for changes to media routes.
            mMediaRouter.removeCallback(mMediaRouterCallback);

            // Pause rendering.
            mPaused = true;
            updateContents();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }


    }


    @Override
    protected void onStop() {
        // Be sure to call the super class.
        super.onStop();

        try {

            // Dismiss the presentation when the activity is not visible.
            if (mPresentation != null) {
                mPresentation.dismiss();
                mPresentation = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }
    }


    //Code to restrict mirroring
    private void updatePresentation() {

        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;


        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {

            mPresentation = new VideoPresentation(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                mPresentation = null;
                ErrorLog.SendErrorReport(ex);
            }
        }

        // Update the contents playing in this activity.
        updateContents();
    }

    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        if (mPresentation != null) {

            //In Secondary Device
            /*txtHello.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_remotely,
                    mPresentation.getDisplay().getName()));*/

            //This will display the text on TV
            if (mPaused) {
                //This txtHello is of Presentation class
                mPresentation.txtHello.setVisibility(View.VISIBLE);
            } else {
                mPresentation.txtHello.setVisibility(View.VISIBLE);
            }
        } else {

            //In Same Device
            /*txtHello.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_locally,
                    getWindowManager().getDefaultDisplay().getName()));*/

        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }
            };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        mPresentation = null;
                        updateContents();
                    }
                }
            };

    /**
     * The presentation to show on the secondary display.
     * <p>
     * Note that this display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own {@link Context} whenever we load resources.
     * </p>
     */
    private final static class VideoPresentation extends Presentation {
        private TextView txtHello;

        public VideoPresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);

            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.
            Resources r = getContext().getResources();

            // Inflate the layout.
            setContentView(R.layout.hidden_content);

            // Set up the  view for visual interest.
            txtHello = findViewById(R.id.txtHello);
            //videoView = (VideoView) findViewById(R.id.videoView);
        }

    }

    //IZISS

    /***
     * If Token process successfully then start the Video
     */
    private void setOnVideoPlayLicenseProcessListener(final String url) {

        this.OnVideoPlayLicenseListener = () -> {
            /*Intent i = new Intent(context, VideoPlayerScreenActivity.class);
            i.putExtra("VideoDetail", mVideoDetail);
            context.startActivity(i);*/
            playVideo(url);
        };
    }

    /***
     * Check Licencing information
     *
     * @param url
     */
    private void checkLicense(String url) {

        if (!LicenseUtil.isLicenseExpiry(getApplicationContext())) {
            playVideo(url);

        } else {
            AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.msg_license_expired));
            setOnVideoPlayLicenseProcessListener(url);
            LicenseUtil License = new LicenseUtil(this.OnVideoPlayLicenseListener);
            License.acquireLicence(this, mSharedPref.getLoginUser().getStudentID());

        }
    }

    /**
     * Add Event related Media
     */
    private void addMediaInfoEvent() {
        try {

            /*09th July 2019, Resolved Pyze library issue*/
            HashMap<String, Object> customAttributes = new HashMap<String, Object>();

            double Duration = ((double) totalVideoDuration / 60000);
            DecimalFormat df = new DecimalFormat("#.##");

            customAttributes.put("duration", df.format(Duration));
            String ContentName = VideoDetail.Title; //Video Title
            String Type = "Video"; //Media Type
            String CategoryName = ChapterVideoScreenActivity.ChapterName;
            String Percentage = String.valueOf(getVideoPlayedPercentage()); // Video Played
            String ContentID = String.valueOf(VideoDetail.VideoID); //Video ID
            PyzeEvents.PyzeMedia.postPlayedMedia(ContentName, Type, CategoryName, Percentage, ContentID, customAttributes);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Get Video Played percentage
     *
     * @return
     */
    private int getVideoPlayedPercentage() {
        int Percentage = 0;
        if (totalVideoPlayedPercentage == 100) {
            return totalVideoPlayedPercentage;
        } else {
            try {
                if (totalVideoDuration > 0)
                    Percentage = (int) (player.getCurrentPosition() * 100 / totalVideoDuration);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLog.SendErrorReport(e);
            } finally {
                return Percentage;
            }
        }
    }

    /***
     * Get contentPath on the bases of video
     * Check video is download or not if download then play from sd card else server
     */
    private void getContentPath() {

        //Check video is download or not if download then play from sd card else server
        if (mAppUtils.isVideoDownload(VideoDetail.DownloadURL)) {
            DebugLog.d("File Present", " -----------");
            contentType = ContentTypes.PDCF;
            contentPath = Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;
        } else if (mAppUtils.isVideoAvialableInMemoryCard(VideoDetail.DownloadURL)) { //From External Memory card
            DebugLog.d("File Present", " -----------");
            contentType = ContentTypes.PDCF;
            /*contentPath = System.getenv("SECONDARY_STORAGE") + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;*/
            contentPath = mAppUtils.getExternalSDCardPath() + "/"
                    + getResources().getString(R.string.app_name)
                    + "/" + VideoDetail.DownloadURL;
        } else {
            if (!mAppUtils.getConnectionState()) {
                //mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                DebugLog.d("File Not Present", " -----------");
                contentType = ContentTypes.DASH;
                //contentPath = "http://192.168.1.108/VisualPhysicsAdmin/trunk/uploads/"+VideoDetail.StreamingURL;

                //contentPath = "http://www.nlytn.in/app/test/2/stream.mpd";
                contentPath = mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + VideoDetail.StreamingURL;
                DebugLog.d("File Not Present", " ----------- " + contentPath);
            }
        }
    }


    /****************************************************************
     * IZISS VIDEO CHANGES
     *********************************************************************/

    private void setCustomVideoListeners() {

        //Undo Me
        //Initialize handlers to perform regularly
        mHandlerOptions = new Handler();
        mHandlerView = new Handler();
        mHandlerSeekBar = new Handler();

        //IZISS to handle rewind button visibility
        mHandlerRewind = new Handler();

        mHandlerVideoToast = new Handler();

        //This will show/hide player options
        mRunnableOptions = () -> {

            // slideUpDown();

        };
        mHandlerOptions.postDelayed(mRunnableOptions, HIDE_TIME_OUT);

        //Set listeners on Widgets
        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnFullScreen.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);

        btnShare.setOnClickListener(this);
        videoSpeedLayout.setOnClickListener(this);


        // This will detect LEFT single tap and double tap on layouts
        llLeft.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Log.i("Android Exomedia", "Single Click");

                changeOptionsVisibility();

                /*//*if (player != null) {

                    if (!player.getVideoControls().isVisible())
                        player.showControls();
                }*/

            }

            @Override
            public void onDoubleClick(View v) {
                Log.i("Android Exomedia", "Double Click");
                llOptions.setVisibility(View.GONE);
                llShareOptions.setVisibility(View.GONE);
                videoControlsLayout.setVisibility(View.GONE);
                seekBarLayout.setVisibility(View.GONE);
                btnFullScreen.setVisibility(View.INVISIBLE);

                //mHandlerOptions.removeCallbacks(mRunnableOptions);

                backwardVideo();

                btnBackward.setVisibility(View.VISIBLE);

                ObjectAnimator animator = ObjectAnimator.ofFloat(btnBackward, "translationX", 100, 0);
                animator.setInterpolator(new OvershootInterpolator());
                animator.setDuration(900);
                //animator.setRepeatCount(2);
                animator.start();

                hideView(fastRewindLayout);


            }
        });


        // This will detect RIGHT single tap and double tap on layouts
        llRight.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

                changeOptionsVisibility();

                /*//*if (player != null) {

                    if (!player.getVideoControls().isVisible())
                        player.showControls();
                }*/

            }

            @Override
            public void onDoubleClick(View v) {
                llOptions.setVisibility(View.GONE);
                llShareOptions.setVisibility(View.GONE);
                videoControlsLayout.setVisibility(View.GONE);
                seekBarLayout.setVisibility(View.GONE);
                btnFullScreen.setVisibility(View.INVISIBLE);

                //mHandlerOptions.removeCallbacks(mRunnableOptions);

                forwardVideo();
                btnForward.setVisibility(View.VISIBLE);

                ObjectAnimator animator = ObjectAnimator.ofFloat(btnForward, "translationX", -100, 0);
                animator.setInterpolator(new OvershootInterpolator());
                animator.setDuration(900);
                //animator.setRepeatCount(2);
                animator.start();
                hideView(fastForwardLayout);


            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (player != null) {
                    Log.i("Android Exomedia", "Seeking : " + seekBar.getProgress());
                    player.seekTo(seekBar.getProgress() * 1000);
                }


            }
        });

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnUp:

                //adjustPlaybackSpeed(true);

                //IZISS MPE
                //Play Next video
                sendToMixpanel(VideoDetail);

                if ((selectedVideoPosition + 1) <= (videoPlayList.size() - 1)) {
                    selectedVideoPosition++;
                    VideoDetail = videoPlayList.get(selectedVideoPosition);
                    contentPath = null;
                    progressBar.setVisibility(View.VISIBLE);
                    playVideo(null);
                }

                hideRewindButton(true);

                break;

            //Play previous video
            case R.id.btnDown:

                //IZISS MPE
                sendToMixpanel(VideoDetail);

                if (selectedVideoPosition > 0) {
                    selectedVideoPosition--;
                    VideoDetail = videoPlayList.get(selectedVideoPosition);
                    contentPath = null;
                    progressBar.setVisibility(View.VISIBLE);
                    playVideo(null);
                }

                hideRewindButton(true);

                //adjustPlaybackSpeed(false);

                break;

            case R.id.btnForward:
                forwardVideo();

                hideRewindButton(true);

                break;

            case R.id.btnBackward:
                backwardVideo();

                hideRewindButton(true);

                break;
            case R.id.video_speed_layout:
                PlaybackSpeedScreen playbackSpeedScreen = (PlaybackSpeedScreen) PlaybackSpeedScreen.createNewInstance(getVideoSpeeds(), this);
                playbackSpeedScreen.show(getSupportFragmentManager(), PlaybackSpeedScreen.class.getName());

                break;

            case R.id.btnFullScreen:

                changeScreenOrientation();

                break;

            case R.id.btnPlayPause:

                if (!AppUtil.isDebuggingModeEnabled(VideoPlayerScreenActivity.this)) {

                    if (player != null) {
                        if (player.isPlaying()) {
                            player.pause();
                            btnPlayPause.setImageResource(R.drawable.ic_video_play);

                        } else {
                            if (totalVideoPlayedPercentage == 100) {
                                totalVideoPlayedPercentage = 0;
                                playVideo(null);
                                btnPlayPause.setImageResource(R.drawable.ic_video_pause);
                            } else {
                                player.start();
                                btnPlayPause.setImageResource(R.drawable.ic_video_pause);
                            }
                        }
                    }

                }


                break;

            case R.id.share_btn:

                //Share a deep link with other users
                shareDeepLink("" + VideoDetail.ChapterID, "" + VideoDetail.VideoID);

                break;

        }

    }

    /*//***
     * To adjust the playback speed of video; isUp will increase the speed
     *
     * @param isUp
     */
    private void adjustPlaybackSpeed(boolean isUp) {
        if (isUp) {

            if (playBackSpeed < 2.0f) {

                if (playBackSpeed == 1.0 || playBackSpeed == 1.25f)
                    playBackSpeed += 0.25f;
                else
                    playBackSpeed += 0.5f;

            }

        } else {

            playBackSpeed = 1.0f;
        }

        if (player != null) {
            player.setPlaybackSpeed(playBackSpeed);
        }

        if (playBackSpeed == 1.0)
            txtSpeed.setText("1.0X");
        else
            txtSpeed.setText("" + playBackSpeed + "X");

    }

    /*
     * To change the screen orientation for full screen and not fullscreen
     * */

    private void changeScreenOrientation() {

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        // final int orientation = getRequestedOrientation();
        final int orientation = getResources().getConfiguration().orientation;
        // OR: orientation = display.getOrientation(); // outside an Activity

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    /**
     * To forward the video by 10 Seconds
     */

    private void forwardVideo() {
        if (player != null) {
            player.seekTo(player.getCurrentPosition() + 5000);
        }
    }

    /**
     * To backward the video by 10 Seconds
     */
    private void backwardVideo() {
        if (player != null) {
            player.seekTo(player.getCurrentPosition() - 5000);
        }
    }

    /*
     * Animation to show other  video controls from bottom
     *
     * @param showVisibility
     * */

    public void slideUpDown(boolean showVisibility) {

        if (showVisibility) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);

            llOptions.startAnimation(bottomUp);
            llOptions.setVisibility(View.VISIBLE);
            llShareOptions.setVisibility(View.VISIBLE);
            videoControlsLayout.setVisibility(View.VISIBLE);
            seekBarLayout.setVisibility(View.VISIBLE);
            btnFullScreen.setVisibility(View.VISIBLE);
        } else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_down);

            llOptions.startAnimation(bottomDown);

            llOptions.setVisibility(View.GONE);
            llShareOptions.setVisibility(View.GONE);
            videoControlsLayout.setVisibility(View.GONE);
            seekBarLayout.setVisibility(View.GONE);
            btnFullScreen.setVisibility(View.GONE);
        }
    }

    /*
     * Animation to show other  video controls from bottom
     *
     * @param
     * */

    public void changeOptionsVisibility() {
        Log.i("Android Exomedia", "Showing/Hiding Player Controls");
        if ((mHandlerOptions != null) && (mRunnableOptions != null)) {
            mHandlerOptions.removeCallbacks(mRunnableOptions);
        }
        mRunnableOptions = new Runnable() {

            @Override
            public void run() {

                changeOptionsVisibility();
            }
        };
        if (llOptions.getVisibility() != View.VISIBLE) {

            //Undo Me
            llOptions.setVisibility(View.VISIBLE);
            llShareOptions.setVisibility(View.VISIBLE);
            videoControlsLayout.setVisibility(View.VISIBLE);
            seekBarLayout.setVisibility(View.VISIBLE);
            btnFullScreen.setVisibility(View.VISIBLE);
            //btnPlayPause.setVisibility(View.VISIBLE);

            mHandlerOptions.postDelayed(mRunnableOptions, HIDE_TIME_OUT);
        } else {

            //Undo Me
            llOptions.setVisibility(View.GONE);
            llShareOptions.setVisibility(View.GONE);
            videoControlsLayout.setVisibility(View.GONE);
            seekBarLayout.setVisibility(View.GONE);
            btnFullScreen.setVisibility(View.INVISIBLE);
            //btnPlayPause.setVisibility(View.INVISIBLE);

            mHandlerOptions = new Handler();
        }
    }


    /**
     * This will hide the view after a particular time
     *
     * @param toHide
     */

    private void hideView(final View toHide) {

        //Undo Me
        toHide.setVisibility(View.VISIBLE);
        //llOptions.setVisibility(View.VISIBLE);

        mHandlerView = new Handler();
        mRunnableViews = () -> {

            if (toHide.getVisibility() == View.VISIBLE) {
                toHide.setVisibility(View.INVISIBLE);
            }

        };

        mHandlerView.postDelayed(mRunnableViews, 1500);
    }


    /**
     * This will update SeekBar with the video play/pause
     */
    private void updateSeekBar() {
        if (mHandlerOptions != null) {
            mHandlerOptions.removeCallbacks(mRunnableOptions);
        }
        if (mHandlerSeekBar != null) {
            mHandlerSeekBar.removeCallbacks(mRunnableSeekBar);
        }

        if (mHandlerRewind != null) {
            mHandlerRewind.removeCallbacks(mRunnableRewind);
        }

        /*Remove Handler which show toast*/
        if (mHandlerVideoToast != null) {
            mHandlerVideoToast.removeCallbacks(mRunnableVideoToast);
        }

        //Undo Me
        mHandlerSeekBar = new Handler();

        mRunnableSeekBar = () -> {

            mHandlerSeekBar.postDelayed(mRunnableSeekBar, 1000);
            seekBar.setProgress(((int) player.getCurrentPosition() / 1000));
            playedTimeTxt.setText(convertToVideoTimeFormat(((int) player.getCurrentPosition() / 1000) * 1000));
            Log.i("Player Maximum>>", "" + seekBar.getMax());
            Log.i("Current Position>>", "" + (((int) player.getCurrentPosition() / 1000)));

        };
        mHandlerSeekBar.postDelayed(mRunnableSeekBar, 1000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        float density = Resources.getSystem().getDisplayMetrics().density;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= 21) {
                seekBar.setProgressBackgroundTintList(new ColorStateList(states, new int[]{Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK}));
            }
            btnFullScreen.setImageResource(R.drawable.ic_fullscreen_exit);
            videoNameTxt.setPadding((int) (20 * density), (int) (20 * density), (int) (20 * density), 0);

            RelativeLayout.LayoutParams btnDownLayoutParams = (RelativeLayout.LayoutParams) btnDown.getLayoutParams();
            btnDownLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnDown.setLayoutParams(btnDownLayoutParams);

            RelativeLayout.LayoutParams btnUpLayoutParams = (RelativeLayout.LayoutParams) btnUp.getLayoutParams();
            btnUpLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            btnUp.setLayoutParams(btnUpLayoutParams);

            LinearLayout.LayoutParams shareLayoutParams = (LinearLayout.LayoutParams) btnShare.getLayoutParams();
            shareLayoutParams.setMargins(0, (int) (15 * density), (int) (20 * density), 0);
            btnShare.setLayoutParams(shareLayoutParams);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (Build.VERSION.SDK_INT >= 21) {
                seekBar.setProgressBackgroundTintList(new ColorStateList(states, new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE}));
            }
            btnFullScreen.setImageResource(R.drawable.ic_fullscreen);
            videoNameTxt.setPadding((int) (10 * density), (int) (70 * density), (int) (20 * density), 0);

            RelativeLayout.LayoutParams btnDownLayoutParams = (RelativeLayout.LayoutParams) btnDown.getLayoutParams();
            btnDownLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            btnDownLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            btnDown.setLayoutParams(btnDownLayoutParams);

            RelativeLayout.LayoutParams btnUpLayoutParams = (RelativeLayout.LayoutParams) btnUp.getLayoutParams();
            btnUpLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            btnUpLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            btnUp.setLayoutParams(btnUpLayoutParams);

            LinearLayout.LayoutParams shareLayoutParams = (LinearLayout.LayoutParams) btnShare.getLayoutParams();
            shareLayoutParams.setMargins(0, (int) (65 * density), (int) (20 * density), 0);
            btnShare.setLayoutParams(shareLayoutParams);
        }
    }

    /***
     * Send data to mixpanel
     *
     * @param mVideos
     */
    private void sendToMixpanel(Videos mVideos) {
        {
            //This object will accept key and its value to send with MixPanel data
            HashMap<String, String> hashMap = new HashMap<>();

            DataBase mDataBase = new DataBase(this);
            Chapters mChapters = mDataBase.getChapterDetail(mVideos.ChapterID);

            if (mChapters != null)
                hashMap.put("CHAPTER_NAME", "" + mChapters.ChapterName);

            //Key and their values which will be send to Mix Panel
            hashMap.put("CHAPTER_ID", "" + mVideos.ChapterID);
            hashMap.put("VIDEO_NAME", mVideos.Title);
            hashMap.put("VIDEO_ID", "" + mVideos.VideoID);
            hashMap.put("VIDEO_DURATION", "" + mVideos.Duration);
            hashMap.put("VIDEO_PLAYED_PERCENTAGE", "" + getVideoPlayedPercentage());

            float watchDuration = Integer.parseInt(mVideos.Duration) * getVideoPlayedPercentage();

            hashMap.put("WATCH_DURATION", "" + watchDuration);

            hashMap.put("TIME_TAKEN_IN_VIDEO_PLAY", "" + getDelayedTimeInVideoPlay());

            //IZISS VIDEO TIME to reset the video play time
            updateVideoOpenTime();

            //Initialize Mixpanel class and sent it
            MixPanelClass mixPanelClass = new MixPanelClass(this);

            //If first chapter after first login, this parameter is set to true on ReferalCodeScreenActivity
            if (mixPanelClass.getPref(MixPanelClass.IS_FIRST_VIDEO, false)) {

                //Send data to our function which will be further sent to Mix Panel
                mixPanelClass.sendData(MixPanelClass.MPE_FIRST_VIDEO_SEEN, hashMap);

                //this will set user identity and mix-panel identify user
                //Commented by IZISS on 04 June 2018 to register user on Registeration/Login
                //mixPanelClass.setUserIdentity();

                mixPanelClass.setPref(MixPanelClass.IS_FIRST_VIDEO, false);

                //Increment video play count for first video seen
                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_VIDEO_PLAY_COUNT, "1.0", true);

                //Increment Watch Duration for video watched
                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_TOTAL_WATCH_DURATION, "" + watchDuration, true);

            } else {

                mixPanelClass.sendData(MixPanelClass.MPE_VIDEO_SEEN, hashMap);

                //Increment video play count
                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_VIDEO_PLAY_COUNT, "1.0", true);

                //Increment Watch Duration for video watched
                mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_TOTAL_WATCH_DURATION, "" + watchDuration, true);

                //If this value is true it means user has view a video after app update and we have to send email and
                if (!mixPanelClass.getPref(MixPanelClass.IS_VIDEO_SEEN_AFTER_UPDATE, false)) {

                    //It means user has view his/her first video after app update
                    mixPanelClass.sendMixPanelPeopleAfterAppUpdate();

                    mixPanelClass.setPref(MixPanelClass.IS_VIDEO_SEEN_AFTER_UPDATE, true);
                }
            }
        }
    }

    /***
     * This will share the deep link
     *
     * @param chapterID,videoID
     */
    private void shareDeepLink(final String chapterID, final String videoID) {

        DataBase mDataBase = new DataBase(this);

        final Chapters mChapters = mDataBase.getChapterDetail(Integer.parseInt(chapterID));

        final String chapterName = mChapters.ChapterName;
        final String videoName = VideoDetail.Title;
        final String videoDescription = VideoDetail.Description;


        DeepLinkGenerator mDeepLinkGenerator = new DeepLinkGenerator(this);
        mDeepLinkGenerator.setCallBack(new OnLinkGenerateListener() {
            @Override
            public void onSuccess(String link) {

                String shareMessage = "Check-out this Visual Physics video of " + chapterName + ", it's awesome. " + videoDescription + " " + link;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Visual Physics"));

            }

            @Override
            public void onFail(String error) {


            }
        });
        mDeepLinkGenerator.getDeepLinkForVideo(chapterID, videoID);

        /*Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://www.visualphysics.nlytn.in/playvideo/" + chapterID + "/" + videoID))
                .setDynamicLinkDomain("n88nw.app.goo.gl")
                // Open links with this app on Android
                //.setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.visualphysics").build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {

                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");

                            String shareText = "Check-out this Visual Physics video of " + chapterName + ", it's awesome. " + videoDescription + " " + shortLink;

                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                            startActivity(Intent.createChooser(shareIntent, "Visual Physics"));
                        } else {

                            task.getException();
                            // Error
                            // ...
                        }
                    }
                });*/


    }


    /***
     * Return Video Position if come from Deep Link
     *
     * @param videoID
     * @return
     */
    private int getVideoPosition(int videoID) {

        if (videoPlayList != null) {
            if (videoPlayList.size() > 0) {
                for (int i = 0; i < videoPlayList.size(); i++) {
                    if (videoPlayList.get(i).VideoID == videoID)
                        return i;
                }
            }
        }

        return -1;
    }


    /***
     * This will update a local variable to update video open time
     */
    private void updateVideoOpenTime() {

        videoOpenTime = Calendar.getInstance().getTimeInMillis();

        isVideoPlayed = false;

        mHandlerVideoToast = new Handler();

        mRunnableVideoToast = () -> {
            {
                if (!isVideoPlayed && !isVideoDownloadedAndStored()) {
                    Toast.makeText(VideoPlayerScreenActivity.this, getResources().getString(R.string.taking_too_much_time), Toast.LENGTH_SHORT).show();
                }
            }
        };
        mHandlerVideoToast.postDelayed(mRunnableVideoToast, TIME_TO_SHOW_MESSAGE);//delay in milliseconds 5 seconds
    }

    /***
     * This will update a local variable when video played
     */
    private void updateVideoPlayedTime() {
        videoPlayedTime = Calendar.getInstance().getTimeInMillis();

        isVideoPlayed = true;
    }


    /***
     * This will return a string value with time difference in seconds
     */
    private String getDelayedTimeInVideoPlay() {

        long timeDifference = videoPlayedTime - videoOpenTime;

        String value = "0";

        if (timeDifference > 0) {

            value = "" + (timeDifference / 1000); //convert in seconds

        } else {

            //This will send the time delayed when loader is loading and user press back
            long timeDifferenceForBack = Calendar.getInstance().getTimeInMillis() - videoOpenTime;

            if (timeDifferenceForBack > 0) {

                value = "" + (timeDifferenceForBack / 1000); //convert in seconds

            }
        }

        return value;

    }

    /***
     * This will show rewind button and hide play pause button
     */

    private void showRewindButton() {

        btnRewind.setVisibility(View.VISIBLE);
        btnPlayPause.setVisibility(View.INVISIBLE);

        //To always show this button when showRewind button hides
        btnPlayPause.setImageResource(R.drawable.ic_video_play);

        //This will show the options layout again
        changeOptionsVisibility();

        mHandlerRewind = new Handler();

        mRunnableRewind = () -> {

            //hideRewindButton();

        };
        mHandlerRewind.postDelayed(mRunnableRewind, 5000);

    }

    /***
     * This will show hide button and show play pause button
     */
    private void hideRewindButton(boolean isShowPauseButton) {

        btnRewind.setVisibility(View.GONE);
        btnPlayPause.setVisibility(View.VISIBLE);

        if (!isShowPauseButton)
            btnPlayPause.setImageResource(R.drawable.ic_video_play);
        else
            btnPlayPause.setImageResource(R.drawable.ic_video_pause);

    }

    /***
     * IZISS 21 May 2018
     * This will detect whether a video is downloaded or not; To show toast message if video is taking too much time in playing..
     *
     * @return
     */
    private boolean isVideoDownloadedAndStored() {

        //Check video is download or not if download then play from sd card else server
        if (mAppUtils.isVideoDownload(VideoDetail.DownloadURL)) {

            return true;

            //From External Memory card
        } else if (mAppUtils.isVideoAvialableInMemoryCard(VideoDetail.DownloadURL)) {

            return true;
        } else {

            if (!mAppUtils.getConnectionState()) {

                return false;

            } else {

                return false;
            }
        }
    }

    /***
     * This will show chapters guide; if chapters are available
     */
    public void showGuide() {
        try {

            //Initialize Mixpanel
            MixPanelClass mixPanelClass = new MixPanelClass(this);

            //Check whether it is a first login for user then show him/her a Guide
            if (mixPanelClass.getPref(MixPanelClass.PREF_IS_FIRST_LOGIN, true)) {

                //Check whether a user has watched Chapter guide; if not then show
                if (!mixPanelClass.getPref(MixPanelClass.PREF_IS_VIDEO_GUIDE_DONE, false)) {

                    if (player != null) {
                        player.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_video_play);

                    }

                    /*This will show guide for chapter*/
                    GuideDialog mGuideDialog = new GuideDialog(this);
                    mGuideDialog.initDialog(GuideType.VIDEO);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This will generate the share link when video is being played
     */
    private void generateDeepLinkForVideoInBG() {

        /*Generate sharelink for first time*/
        DeepLinkGenerator mDeepLinkGenerator = new DeepLinkGenerator(this);
        mDeepLinkGenerator.getDeepLinkForVideo("" + VideoDetail.ChapterID, "" + VideoDetail.VideoID);
    }

    /**
     * @return
     */

    private boolean isMirroring() {

        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

        boolean screenMirror = false;

        if (mDisplayManager.getDisplays().length > 1) {
            screenMirror = true;

        }
        return screenMirror;
    }

    /**
     * This will pause the video
     */
    public void pauseVideo() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                btnPlayPause.setImageResource(R.drawable.ic_video_play);
            }
        }
    }

    /*This will play the video*/
    public void playVideo() {
        if (player != null) {
            if (player.isPlaying()) {
                player.start();
                btnPlayPause.setImageResource(R.drawable.ic_video_pause);
            }
        }
    }

}