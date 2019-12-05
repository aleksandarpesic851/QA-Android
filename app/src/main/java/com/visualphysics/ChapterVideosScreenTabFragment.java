package com.visualphysics;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import Adapter.ChapterVideoListAdapter;
import Database.DataBase;
import Model.Chapters;
import Model.LoginUser;
import Model.Videos;
import Utils.ErrorLog;
import Utils.SharedPrefrences;
import mixpanel.MixPanelClass;

/**
 * Created by admin on 5/20/2016.
 */
public class ChapterVideosScreenTabFragment extends Fragment {

    private View parentView;
    private int CategoryID, ChapterID;
    private DataBase database;
    private ArrayList<Videos> mChapterVideosArrayList;
    private RecyclerView mChapterVideosRecyclerView;
    private ChapterVideoListAdapter mVideoAdapter;

    private String ChapterType;

    private final String tokenFileName = "token.xml";
    private final String contentPath = Environment.getExternalStorageDirectory().getPath() + "/video.mlv";
    AssetManager am;
    public int REQUEST_WRITE_STORAGE;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        parentView = inflater.inflate(
                R.layout.recyclerview_chapternamescreen, container, false);
        mDeclaration();
        return parentView;

    }

    /***
     * Declare member variable
     */
    private void mDeclaration() {

        if (this.getArguments().containsKey("ChapterID")) {
            this.ChapterID = this.getArguments().getInt("ChapterID");
            this.CategoryID = this.getArguments().getInt("CategoryID");
            this.ChapterType = this.getArguments().getString("VideoType");//Theory or Solved Problems
            if (ChapterType.equals("Theory"))
                REQUEST_WRITE_STORAGE = 101;
            else
                REQUEST_WRITE_STORAGE = 202;
        }

        database = new DataBase(getActivity());

        // mChapterVideosArrayList = (ArrayList<Videos>) database.doGetVideosList(ChapterType, ChapterID);

        mChapterVideosArrayList = getPreferredChapters((ArrayList<Videos>) database.doGetVideosList(ChapterType, ChapterID));

        mChapterVideosRecyclerView = (RecyclerView) parentView.findViewById(R.id.recyclerView_chapternamescreen);
        mChapterVideosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChapterVideosRecyclerView.setHasFixedSize(true);
        mChapterVideosRecyclerView.setItemViewCacheSize(50);
        mChapterVideosRecyclerView.setDrawingCacheEnabled(true);

        // 3. create an adapter
        mVideoAdapter = new ChapterVideoListAdapter(mChapterVideosArrayList,
                getActivity(), REQUEST_WRITE_STORAGE);
        // 4. set adapter
        mChapterVideosRecyclerView.setAdapter(mVideoAdapter);

        am = getActivity().getAssets();

        //initMyApp();

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
            ErrorLog.SaveErrorLog(e);
            Log.e("FILE_CHECK_FAILS", e.getLocalizedMessage());

        }

        return false;
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
            Log.i("Token Not Loaded", e.getMessage());
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
        } finally {
            if (br != null) {
                try {
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    ErrorLog.SaveErrorLog(e);
                }
            }
        }

        return sb.toString();

    }

    private class Personalizer extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress;

        protected void onPreExecute() {

            try {

                progress = ProgressDialog.show(getActivity(), "Please wait", "Personalization in progress ...", true);

            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }


        protected Void doInBackground(Void... params) {

            try {
                com.intertrust.wasabi.Runtime.personalize();

                Log.i("personalization- ", "in progress--------");

            } catch (ErrorCodeException e) {
                ErrorLog.SendErrorReport(e);
            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            try {

                Log.i("Completed", "Personalization");

                // DO SOMETHING
            } catch (Exception e) {
                Log.e("printing Nodes ID:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }


            if (progress != null) {
                progress.dismiss();
            }
        }

    }


    private class TokenHandler extends AsyncTask<String, Void, Void> {
        ProgressDialog progress;
        boolean completedLicenseAcquisition = false;

        protected void onPreExecute() {

            try {

                //Looper.prepare();
                if (!completedLicenseAcquisition)
                    progress = ProgressDialog.show(getActivity(), "Please wait", "Aquiring Licenese ...", true);

            } catch (Exception e) {
                Log.e("TokenHandler:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
            }
        }

        protected Void doInBackground(String... params) {

            try {

                // The action token is processed here. the method automatically stores the license to the local DB.
                Runtime.processServiceToken(params[0]);


            } catch (ErrorCodeException e) {
                ErrorLog.SendErrorReport(e);
            } catch (Exception e) {
                Log.e("TokenHandler:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {


            if (progress != null) {
                progress.dismiss();
                completedLicenseAcquisition = true;

            }


        }

    }

    boolean initMyApp() {

        try {
            Runtime.initialize(getActivity().getDir("wasabi", getActivity().MODE_PRIVATE).getAbsolutePath());
            if (!Runtime.isPersonalized()) {

                Log.i("PERSONALIZATION", "*** device persoanlization in progress");

                new Personalizer().execute();
            }

        } catch (ErrorCodeException e) {
            ErrorLog.SendErrorReport(e);
        } catch (Exception e) {
            Log.e("INIT", e.getLocalizedMessage());
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }


        return true;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoAdapter != null)
            mVideoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mVideoAdapter != null) {
            mVideoAdapter.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /***
     * This will return chapter list; if user has a valid tagId then it will return according to it other wise all chapters list
     *
     * @param mVideosArrayList
     * @return
     */
    private ArrayList<Videos> getPreferredChapters(ArrayList<Videos> mVideosArrayList) {

        MixPanelClass mixPanelClass = new MixPanelClass(getActivity());

        LoginUser mUser = new SharedPrefrences(getActivity()).getLoginUser();

        if (mixPanelClass.isValidSavedTagID()) {

            ArrayList<Videos> preferredList = new ArrayList<>();

            for (Videos mVideos : mVideosArrayList) {

                boolean isPreferredTag = false;

                if (mVideos.TagID != null) {

                    if (mVideos.TagID.length() > 0) {

                        String splitArray[] = mVideos.TagID.split(",");

                        for (int i = 0; i < splitArray.length; i++) {

                            if (splitArray[i].equalsIgnoreCase(mUser.getTagID())) {
                                isPreferredTag = true;
                                break;
                            }
                        }
                    }
                    else {
                        isPreferredTag = true;
                    }
                }
                else {
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

