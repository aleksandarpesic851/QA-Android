/*
package com.visualphysics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaDrm;
import android.media.MediaRouter;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.media.MediaAdapter;
import com.intertrust.wasabi.media.PlaylistProxy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import Database.DataBase;
import Model.Videos;
import Utils.AppConstansts;
import Utils.AppUtil;
import Utils.ApplicationConfiguration;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.SharedPrefrences;
import exoplayer.ContentTypes;
import exoplayer.SimpleExoPlayerWithDrm;
import firebase.FirebaseClass;
import zoho.ZohoUtils;

*/
/**
 * Created by India on 7/6/2016.
 *//*

public class NewVideoPlayerScreenActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewClickListener {

    //IZISS Playback
    private VideoView player;
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

    private ProgressBar progressBar;

    //Time in millis to show toast message 5 Secs
    private long TIME_TO_SHOW_MESSAGE = 5000;


    int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}, // enabled
            new int[]{-android.R.attr.state_enabled}, // disabled
            new int[]{-android.R.attr.state_checked}, // unchecked
            new int[]{android.R.attr.state_pressed}  // pressed
    };

    */
/*
     * create a simpleexoplayer view to render the content, set its surfaceView to be "secure"
     *//*


    SimpleExoPlayerView simpleExoPlayerView;

    @Override
    public void onItemClick(Object o) {

    }


    public enum ContentTypes {
        DASH("application/dash+xml"), HLS("application/vnd.apple.mpegurl"), PDCF(
                "video/mp4"), M4F("video/mp4"), DCF("application/vnd.oma.drm.dcf"), BBTS(
                "video/mp2t"), CFF("video/mp4");
        String mediaSourceParamsContentType = null;

        private ContentTypes(String mediaSourceParamsContentType) {
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

        setContentView(R.layout.video_player_screen_activity_new);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        mAppUtils = new AppUtil(getApplicationContext());

        //Check the last open app UTC date time with current UTC date TIme
        if (checkLastOpenAppDateTime()) {
            mDeclaration();

            Bundle bundle = new Bundle();
            bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, VideoDetail.VideoID);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, VideoDetail.Title);
        }

        //Show Zoho chat icon
        ZohoUtils.hideZohoChat();


    }

    private void mDeclaration() {

        player = (VideoView) findViewById(R.id.videoView);

        //IZISS Video Player

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();

        //Check Video Detail come from Deep Link
        if (intent.hasExtra("VideoDetail")) {

            VideoDetail = (Videos) intent.getSerializableExtra("VideoDetail");

            DataBase database = new DataBase(this);

            videoPlayList = (ArrayList<Videos>) database.doGetVideosList(VideoDetail.Type, VideoDetail.ChapterID);


        } else if (intent.hasExtra("videos")) {
            videoPlayList = (ArrayList<Videos>) intent.getSerializableExtra("videos");
            selectedVideoPosition = intent.getIntExtra("selectedVideoPosition", 0);

            VideoDetail = videoPlayList.get(selectedVideoPosition);
        } else {
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            finish();
        }

        progressBar.setVisibility(View.VISIBLE);

        //IZISS EDIT 01 Oct 2018
       */
/* if (intent.hasExtra("directPlay")) {
            checkLicense(contentPath);
        } else {
            playVideo(contentPath);
        }*//*



        int color = Color.parseColor("#55000000");

        simpleExoPlayerView = findViewById(R.id.exoPlayerView);
        final SurfaceView surfaceView = (SurfaceView) simpleExoPlayerView.getVideoSurfaceView();
        surfaceView.setSecure(true);


        //Check if content path is null then get the content path again
        if (TextUtils.isEmpty(contentPath))
            getContentPath();

        playDRMVideo("https://ut.test.expressplay.com/hms/ut/rights/?ExpressPlayToken=CwAAAxAOaX4AAAIQ_F6HFxcvLUGKc4_wwcmRN3XUlg2DzSvIBiG8iEkp_dGpr590WmqqAE9WaOFyG64qY9IgDvMqYbiMPlhbcETBDjftU9i3afzWDADe-Gdmqz9unZhsx_ilgfthfTEp34nQin57ZjPL5z1Y7BS9K5LPTu8Lremyfr-HuYyplMsPKRVejho9GzUsIMawqpROjWUPoppnaY4W5L0xRBUM_k10JiJeENDLiSAxpE9Hk9VGYSVP-IHpqtL7lnCEOtB-U8wvpv13C1ad61J2exuwMF5wYy8wVzqpoE2lFALAvDXdPaV1NMmPkYm_D5IxoMWOGsJ-H5ISR4cV3tkjP0kKlJLIw8jE61X6QH6Ak8ycTei21NDilOomP75ckxecnAOxRby9N58VNkw9yb9Wfi5OpJMMIebu0rp3SL-QQkY7C1z2T0JSpHPH6U5YeCQiFzP4fZxGbnUe3FahYrViZCL9h5jg3U1SdR4qE1kFLqlT-JaUbrIN5oCbpnKHLHRS1QrY-FHgTQx-AjKpmZoxKNl09qipo5QJtv8_PHAS5NFY76FXW3tsQvwMIriNQGuE9CZ7ZhKcA-B732B48pt7G5PW1YzedqkSLEV_cWj9haERJRbHLfPww4Ue_CPC8SiB6o9H8gnp2HeYcTwGgKDCPJCo7rUj4tUvjqvoplrUx7aSumMhubcAjKxT-etlN2aqxnr0y93EZA7OnjaxL_sNIB2C4BYtvhPANyE", contentPath);


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


    private void playVideo(String videoUrl) {

        try {

            //Check if content path is null then get the content path again
            if (TextUtils.isEmpty(contentPath))
                getContentPath();


            PlaylistProxy.MediaSourceParams params = new PlaylistProxy.MediaSourceParams();
            params.sourceContentType = contentType
                    .getMediaSourceParamsContentType();

            //For Subtitle we need a url of a .vtt file
            params.subtitleUrl = "http://www.nlytn.in/sub/subt.vtt";
            //params.subtitleUrl = "http://content-access.intertrust-dev.com/content/dash/frozen_dash_list_sub/subtitles/eng/subtitles_en.vtt";

            //Language Code reference
            params.subtitleLang = "eng";

            //Language name
            params.subtitleName = "English";

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

            mFirebaseClass.sendAnalyticsData(VideoDetail.VideoID, VideoDetail.Title, AppUtil.EVENT_PLAY_VIDEO);
            new FirebaseClass(NewVideoPlayerScreenActivity.this).sendCustomAnalyticsData("custom_video_play", "Video Played");

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

    */
/***
     * Set Video player Listeners
     *//*

    private void setListeners() {

        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {

                progressBar.setVisibility(View.GONE);

                addVideoCount();

            }
        });

        player.setOnErrorListener(new OnErrorListener() {

            //IZISS Playback
            @Override
            public boolean onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                        "Unable to play video please try later");

                ErrorLog.SendErrorReportUnableToPlayVideo(e);

                return true;
            }

        });


        if (!isVideoCompletionAttached) {
            isVideoCompletionAttached = true;
            player.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion() {
                    if (!(player.isPlaying()) && (player.getDuration() > 0)) {
                        Log.v("Android ExoMedia", "Played Video No : " + selectedVideoPosition);
                        if ((selectedVideoPosition) < (videoPlayList.size() - 1)) {

                        } else if (selectedVideoPosition == (videoPlayList.size() - 1)) {

                            player.restart();
                            player.pause();

                            progressBar.setVisibility(View.GONE);

                        }
                    }
                }
            });

        }
    }

    */
/***
     * Add Video play count
     *//*

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

                */
/*Calendar sDate = getDatePart(LastAppOpenDate);
                Calendar eDate = getDatePart(CurrentDate);
*//*


                if (CurrentDate.getTime() < LastAppOpenDate.getTime()) {
                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(NewVideoPlayerScreenActivity.this,
                            R.style.AppCompatAlertDialogStyle);
                    myAlertDialog.setTitle(R.string.app_name);
                    myAlertDialog.setMessage(R.string.msg_device_time_change);
                    myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // do something when the OK button is clicked
                            finish();
                        }
                    });

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

        if ((mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 0) == AppConstansts.RATE_APP_COUNT_1
                || mSharedPref.getPreferences(mSharedPref.APP_OPEN_COUNT, 0) == AppConstansts.RATE_APP_COUNT_2)
                && ApplicationConfiguration.isRATE_POPUP_SHOWN == false) {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                }
            }

            //Commented by IZISS on 22 Aug to not show the rate us popup.
           */
/* //check if User has already click on Yes button when the Rate us dialog appears then don't display the Dialog
            if (mSharedPref.getPreferences(mSharedPref.IS_RATE_APP, false) == false)
                showCustomeDialog();
            else
                finish();*//*


        } else
            finish();

    }

    */
/***
     * Display Rate us dialog
     *//*

    public void showCustomeDialog() {
        ApplicationConfiguration.isRATE_POPUP_SHOWN = true;
        Dialog dialog = new Dialog(NewVideoPlayerScreenActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.rateus_screen);

        Button mBtnYes = (Button) dialog.findViewById(R.id.btnYesRateUsScreen);
        Button mBtnNo = (Button) dialog.findViewById(R.id.btnNoRateUsScreen);

        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPref.setPreferences(mSharedPref.IS_RATE_APP, true);
                startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())), 100);
                //finish();
            }
        });
        mBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                }
            }

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
            */
/*txtHello.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_remotely,
                    mPresentation.getDisplay().getName()));*//*


            //This will display the text on TV
            if (mPaused) {
                //This txtHello is of Presentation class
                mPresentation.txtHello.setVisibility(View.VISIBLE);
            } else {
                mPresentation.txtHello.setVisibility(View.VISIBLE);
            }
        } else {

            //In Same Device
            */
/*txtHello.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_locally,
                    getWindowManager().getDefaultDisplay().getName()));*//*


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

    */
/**
     * Listens for when presentations are dismissed.
     *//*

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

    */
/**
     * The presentation to show on the secondary display.
     * <p>
     * Note that this display may have different metrics from the display on which
     * the main activity is showing so we must be careful to use the presentation's
     * own {@link Context} whenever we load resources.
     * </p>
     *//*

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
            txtHello = (TextView) findViewById(R.id.txtHello);
            //videoView = (VideoView) findViewById(R.id.videoView);
        }


    }

    //IZISS

    */
/***
     * If Token process successfully then start the Video
     *//*

    private void setOnVideoPlayLicenseProcessListener(final String url) {

        this.OnVideoPlayLicenseListener = new LicenseListener() {
            @Override
            public void onProcessToken() {
                */
/*Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                i.putExtra("VideoDetail", mVideoDetail);
                context.startActivity(i);*//*

                playVideo(url);
            }
        };
    }

    */
/***
     * Check Licencing information
     *
     * @param url
     *//*

    private void checkLicense(String url) {

        if (!LicenseUtil.isLicenseExpiry(getApplicationContext())) {
            playVideo(url);

        } else {
            mAppUtils.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.msg_license_expired));
            setOnVideoPlayLicenseProcessListener(url);
            LicenseUtil License = new LicenseUtil(this.OnVideoPlayLicenseListener);
            License.acquireLicence(this, mSharedPref.getLoginUser().getStudentID());

        }
    }

    */
/***
     * Get contentPath on the bases of video
     * Check video is download or not if download then play from sd card else server
     *//*

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
            */
/*contentPath = System.getenv("SECONDARY_STORAGE") + "/"
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
        }
    }


    @Override
    public void onClick(View v) {

    }

    */
/*
     * To change the screen orientation for full screen and not fullscreen
     * *//*


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

    */
/**
     * To forward the video by 10 Seconds
     *//*


    private void forwardVideo() {
        if (player != null) {
            player.seekTo(player.getCurrentPosition() + 5000);
        }
    }

    private void playDRMVideo(String universalToken, String dashInputMedia) {

        MediaAdapter mediaAdapter;
        final String WV_UUID_STRING = "edef8ba9-79d6-4ace-a3c8-27dcd51d21ed";
        final String TAG = "SamplePlayer";

        //identify and log device DRM capability
        Log.i("PLAY DRM VIDEO", "OS Version: " + android.os.Build.VERSION.RELEASE);
        UUID uuid = UUID.fromString(WV_UUID_STRING);
        MediaDrm mediaDrm = null;
        String nativeDrmDescription = null;
        try {
            mediaDrm = new MediaDrm(uuid);
            nativeDrmDescription = mediaDrm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION);
            Log.i("PLAY DRM VIDEO", "Media DRM properties: " + nativeDrmDescription);
        } catch (UnsupportedSchemeException e) {
            e.printStackTrace();
        }


        try {

            // these media source and license values to be replaced
            //String universalToken = "https://ut.test.expressplay.com/hms/ut/rights/?ExpressPlayToken=CgAAAw-9aYIAAAFAPqkch5jzQpQsTV1xYPhM3Ckb7EniG3IDplDZge5k5tObUm0biJQa6tK7Yphv17M7iCN1_xpgYJ54tRPbso6c03nHiwV_AiqyW3Ttrlso8nY9nj-M7oa3vK5QpbCO-AHo6b6sgozzb5XRX_-QjCyRGFVrp_a613BjgIH157nucSEEhfz8opPuBuOLnM-emiqr1uFmFqcOWUFvIWgI0X4voZ9fgICEsUs4w6prNiDNW4n-ayWLQ8Qcq41UttPyTbni94mgKqmRZJ6DtsW0yabI90tpxXQCUzPn7rYc_yFcEviXWH8NxaIcfDUteRbEMmhtnMJIxgl1d473mszKbYojOSKSvoOPBo_1Q9d2-F9lxiRdx6YlsrQ16_frOpsAlb5w5see9plNgPLijDR6njpxupk1A2iPA3e76O_MKzfmsxyY0x8LX9ZJU637wgIz7nKYcYoMLQ";
            //final String dashInputMedia = "https://ms3-test.intertrust.com:8443/dash/OnDemand-Marlin+WV/stream.mpd";

            */
/*
             * Create a MediaAdapter object with simple parameters
             *//*

            PlaylistProxy.MediaSourceParams mediaSourceParams = new PlaylistProxy.MediaSourceParams();

            MediaAdapter.Config config = new MediaAdapter.Config();

            //choose the desired DRM type
            config.drmType = MediaAdapter.DrmType.PREFER_NATIVE_DRM;
            config.licenseType = MediaAdapter.LicenseType.STREAMING_LICENSE;
            mediaAdapter = new MediaAdapter(config);

            MediaAdapter.Params params = new MediaAdapter.Params();
            params.mediaSourceParams = mediaSourceParams;
            params.mediaSource = dashInputMedia;
            params.tokenUrl = universalToken;
            params.mediaSourceType = PlaylistProxy.MediaSourceType.DASH;
            params.flags = EnumSet.noneOf(PlaylistProxy.Flags.class);
            params.flags.add(PlaylistProxy.Flags.ALLOW_EXTERNAL_CLIENT);

//            PlaybackInfo playbackInfo;
            MediaAdapter.PlaybackInfo playbackInfo = mediaAdapter.adapt(params);
            PlaylistProxy playlistProxy = playbackInfo.playlistProxy;

            final String mediaUrl = playbackInfo.contentUrl;
            Log.i(TAG, "Content URL: " + mediaUrl);
            final String tokenUrl = playbackInfo.tokenUrl;
            Log.i(TAG, "Token URL: " + tokenUrl);

            boolean marlinAdaptation = playlistProxy != null && tokenUrl == null;

            if (marlinAdaptation) {

                //playback content using Marlin
                SimpleExoPlayer player;
                try {
                    player = SimpleExoPlayerWithDrm.getPlayer(getBaseContext(), exoplayer.ContentTypes.HLS, mediaUrl, tokenUrl,
                            null, true);
                    simpleExoPlayerView.setPlayer(player);
                    Toast.makeText(getBaseContext(), "Playing back clear HLS with Marlin license " +
                            "through Playlist Proxy", Toast.LENGTH_LONG).show();
                } catch (UnsupportedDrmException e) {
                    e.printStackTrace();
                    return;
                }

            } else {

                //playback content using Marlin
                SimpleExoPlayer player;
                try {
                    player = SimpleExoPlayerWithDrm.getPlayer(getBaseContext(), exoplayer.ContentTypes.DASH, mediaUrl, tokenUrl,
                            null, true);
                    simpleExoPlayerView.setPlayer(player);
                    Toast.makeText(getBaseContext(), "Playing back clear DASH with Marlin license " +
                            "through Playlist Proxy", Toast.LENGTH_LONG).show();
                } catch (UnsupportedDrmException e) {
                    e.printStackTrace();
                    return;
                }

            }


        } catch (ErrorCodeException e) {
            // Consult WasabiErrors.txt for resolution of the error codes
            Log.e(TAG, "playback error: " + e.getLocalizedMessage());
            return;
        }


    }


}*/
