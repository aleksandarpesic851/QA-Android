/*
package com.visualphysics;

*/
/**
 * Created by India on 7/6/2016.
 *//*


import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.intertrust.wasabi.Runtime;
import com.intertrust.wasabi.media.PlaylistProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TestActivity extends AppCompatActivity {

    private final String tokenFileName="token.xml";



    private final String contentPath= Environment.getExternalStorageDirectory().getPath() + "/video.mlv";



    VideoView player;
    AssetManager am;


    enum ContentTypes {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_chapternamescreen);

        am = this.getAssets();
        //player = (EMVideoView)findViewById(R.id.videoView);

        if(isFilePresent(contentPath))
        {
            Log.i("File Present" , " -----------");
        }
        else
        {
            Log.i("File Not Present" , " -----------");
        }
        // player = (VideoView)findViewById(R.id.videoView);

        //player.setMediaController(new MediaController(this));

        */
/*Button buttonLoad= (Button)findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                Log.i("BUTTON_TOKEN","******* Button Token Pressed");

                String tokenData = readFileFromAsset(tokenFileName);	// INSERT XML TOKEN DATA HERE
                Log.i("LOAD_TOKEN_BUTTON", "*** token loaded ");

                try {
                    new TokenHandler().execute(tokenData);
                } catch (Exception e) {
                    Log.e("Token Processing error:",e.getMessage());
                }

            }
        });


        Button buttonPlay= (Button)findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.i("BUTTON_PLAY", "******* Button Play Pressed");



                Log.i("BUTTON_PLAY- URL ", contentPath);
                playVideo(contentPath);

            }
        });*//*



        initMyApp();



    } //End OF On Create


    private void playVideo(String videoUrl) {

        try {

            ContentTypes contentType = ContentTypes.PDCF;

            PlaylistProxy.MediaSourceParams params = new PlaylistProxy.MediaSourceParams();
            params.sourceContentType = contentType
                    .getMediaSourceParamsContentType();

            PlaylistProxy playlistProxy = new PlaylistProxy();
            playlistProxy.start();

            // Url to protected content
            String contentTypeValue = contentType.toString();
            String playerUrl = playlistProxy.makeUrl(contentPath, PlaylistProxy.MediaSourceType.valueOf((contentTypeValue == "HLS" || contentTypeValue == "DASH") ? contentTypeValue : "SINGLE_FILE"), params);

            Log.e("LOCAL_PROXY_URL", playerUrl);

            player.setVisibility(View.VISIBLE);

            player.setVideoPath("http://127.0.0.1:48200/R/9flhCkvIpr2W9PdPYldYuW5Cffc.m3u8");
            player.setVideoURI(Uri.parse(playerUrl));
            player.requestFocus();

            player.start();

        } catch (Exception e) {
            Log.e("PLAY BACK ERROR", e.getLocalizedMessage());

        }
    }

    private boolean isFilePresent(String filePath) {
        try {

            File f = new File(filePath);

            Log.i("CHECK_FILE", filePath);


            if(f.exists()){
                Log.i("CHECK_FILE_PRESENT",filePath);

                return true;

            }else{
                Log.i("CHECK_FILE_NOT_PRESENT", filePath);
                return false;
            }


        } catch(Exception e) {

            Log.e("FILE_CHECK_FAILS", e.getLocalizedMessage());

        }

        return false;
    }


    private String readFileFromAsset(String filePath){

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
            Log.i("Token Not Loaded" , e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private class Personalizer extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress;

        protected void onPreExecute() {

            try {

                progress = ProgressDialog.show(TestActivity.this,"Please wait","Personalization in progress ...",  true);

            } catch (Exception e){
                Log.e("personalization:",e.getMessage());
            }
        }


        protected Void doInBackground(Void... params) {

            try {
                com.intertrust.wasabi.Runtime.personalize();

                Log.i("personalization- " , "in progress--------");

            } catch (Exception e){
                Log.e("personalization:",e.getMessage());
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            try {

                Log.i("Completed","Personalization");

                // DO SOMETHING
            } catch (Exception e) {
                Log.e("printing Nodes ID:",e.getMessage());
            }


            if (progress != null) {
                progress.dismiss();
            }
        }

    }


    private class TokenHandler extends AsyncTask<String , Void, Void> {
        ProgressDialog progress;
        boolean completedLicenseAcquisition = false;

        protected void onPreExecute() {

            try {

                //Looper.prepare();
                if(!completedLicenseAcquisition) progress = ProgressDialog.show(TestActivity.this,"Please wait","Aquiring Licenese ...",  true);

            } catch (Exception e){
                Log.e("TokenHandler:",e.getMessage());
            }
        }

        protected Void doInBackground(String... params) {

            try {

                // The action token is processed here. the method automatically stores the license to the local DB.
                Runtime.processServiceToken(params[0]);



            } catch (Exception  e){
                Log.e("TokenHandler:",e.getMessage());
            }

            return null;
        }

        protected void onPostExecute(Void unused) {


            if (progress != null) {
                progress.dismiss();
                completedLicenseAcquisition=true;

            }



        }

    }

    boolean initMyApp() {

        try {
            Runtime.initialize(getDir("wasabi", MODE_PRIVATE).getAbsolutePath());
            if(!Runtime.isPersonalized()) {

                Log.i("PERSONALIZATION", "*** device persoanlization in progress");

                new Personalizer().execute();
            }

        } catch (Exception e) {
            Log.e("INIT", e.getLocalizedMessage());
        }


        return true;

    }



} //EOF

*/
