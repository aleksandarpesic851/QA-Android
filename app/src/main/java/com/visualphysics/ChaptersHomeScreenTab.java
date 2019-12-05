package com.visualphysics;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import Adapter.ChapterTabAapter;
import Database.DataBase;
import Model.Chapters;
import Utils.ErrorLog;

/**
 * Created by admin on 5/19/2016.
 */
public class ChaptersHomeScreenTab extends Fragment {

    private View rootView;
    private AssetManager am;
    private DataBase database;
    private ArrayList<Chapters> mChapterArrayList;
    private ChapterTabAapter mChapterAdapter;
    private RecyclerView mChaptersRecyclerView;
    private int REQUEST_WRITE_STORAGE;

    private final String tokenFileName="bb_lic_token_bigs_bug_bunny.xml";

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
    private void mDeclaration()
    {

        int position = this.getArguments().getInt("position") + 1;
        REQUEST_WRITE_STORAGE = this.getArguments().getInt("REQUEST_WRITE_STORAGE");

        database = new DataBase(getActivity());
        mChapterArrayList = (ArrayList<Chapters>) database.doGetCategoryChapterList(position);
        //am = getActivity().getAssets();

        mChaptersRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_tab1Frag);
        mChaptersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(mChapterArrayList!=null){
            mChapterAdapter = new ChapterTabAapter(mChapterArrayList,getActivity(), position,REQUEST_WRITE_STORAGE);

            // 4. set adapter
            mChaptersRecyclerView.setAdapter(mChapterAdapter);
        }else{
            Toast.makeText(getActivity(), "No chapter available", Toast.LENGTH_SHORT).show();
        }

        // Getting Token and Processing
        Log.i("BUTTON_TOKEN","******* Button Token Pressed");

        //String tokenData = readFileFromAsset(tokenFileName);	// INSERT XML TOKEN DATA HERE
        Log.i("LOAD_TOKEN_BUTTON", "*** token loaded ");

        try {
            //new TokenHandler().execute(tokenData);
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            Log.e("Token Processing error:",e.getMessage());
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

    private class TokenHandler extends AsyncTask<String, Void, Void> {
        ProgressDialog progress;
        boolean completedLicenseAcquisition = false;

        protected void onPreExecute() {

            try {

                //Looper.prepare();
                if (!completedLicenseAcquisition)
                    progress = ProgressDialog.show(getActivity(), "Please wait", "Aquiring Licenese ...", true);

            } catch (Exception e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
            }
        }

        protected Void doInBackground(String... params) {

            try {

                // The action token is processed here. the method automatically stores the license to the local DB.
                Runtime.processServiceToken(params[0]);


            } catch (ErrorCodeException e){
                ErrorLog.SendErrorReport(e);
            }catch (Exception e) {
                ErrorLog.SaveErrorLog(e);
                e.printStackTrace();
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

}
