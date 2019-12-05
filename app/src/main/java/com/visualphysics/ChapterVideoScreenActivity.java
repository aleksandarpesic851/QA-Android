package com.visualphysics;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pyze.android.PyzeEvents;

import java.util.ArrayList;
import java.util.HashMap;

import Adapter.ChapterVideoListAdapter;
import Adapter.ChapterVideoViewpagerAdapter;
import Database.DataBase;
import Model.Videos;
import guide.GuideDialog;
import guide.GuideType;
import mixpanel.MixPanelClass;

public class ChapterVideoScreenActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    private TabLayout tabLayout;
    private ImageView mBack;
    private TextView mTxtChapterName;
    private int mIntCategoryID, mIntChapterID;
    public static String ChapterName = "";

    private ImageView mImgSearch;
    private SearchView mSearchView;
    private LinearLayout mLinearLayoutTab, mLinearLayoutSearch;
    private ImageButton mImgBtnBack;

    private DataBase database;
    private ArrayList<Videos> mChapterVideosArrayList;
    private RecyclerView mRecyclerViewSearchVideo;
    private ChapterVideoListAdapter mVideoAdapter;
    private int REQUEST_WRITE_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_name_screen);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.chapter_actionbarlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDeclaration();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the wallet; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_wishlist, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    void mDeclaration() {

        Intent intent = getIntent();

        if (intent.hasExtra("ChapterID")) {

            mIntChapterID = intent.getIntExtra("ChapterID", -1);
            mIntCategoryID = intent.getIntExtra("CategoryID", -1);
            ChapterName = intent.getStringExtra("ChapterName");
        }

        database = new DataBase(getApplicationContext());

        mTxtChapterName = findViewById(R.id.txtChapterNameVideoScreen);
        mTxtChapterName.setText(ChapterName);

        mImgSearch = findViewById(R.id.imgSearch);
        mImgSearch.setOnClickListener(this);

        mImgBtnBack = findViewById(R.id.imgBtnBackSearchViewChapterVideoTabScreen);
        mImgBtnBack.setOnClickListener(this);

        mLinearLayoutTab = findViewById(R.id.lLayoutChapterVideoTabScreen);
        mLinearLayoutSearch = findViewById(R.id.lLayoutSearchViewChapterVideoTabScreen);
        mSearchView = findViewById(R.id.searchViewChapterVideoTabScreen);
        mSearchView.setOnQueryTextListener(this);
        TextView searchText = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        mRecyclerViewSearchVideo = findViewById(R.id.recyclerViewSearchVideoListChapterVideoTabScreen);
        mRecyclerViewSearchVideo.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mBack = findViewById(R.id.back_Arrow);
        mBack.setOnClickListener(this);


        tabLayout = findViewById(R.id.chapterName_TabLayout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());


//        tabLayout.getTabAt(0).setText("Theory");
//        tabLayout.getTabAt(1).setText("Solved Problems");


        View child = getLayoutInflater().inflate(R.layout.chaptername_customtab_layout, null);
        TextView txt = child.findViewById(R.id.txt_ChapternameCustomTab);
        tabLayout.getTabAt(0).setCustomView(child);
        tabLayout.getTabAt(0).getCustomView().setSelected(true);

        child = getLayoutInflater().inflate(R.layout.chaptername_customtab_layout2, null);
        txt = child.findViewById(R.id.txt_ChapternameCustomTab);
        tabLayout.getTabAt(1).setCustomView(child);


        final ViewPager viewPager = findViewById(R.id.chapterName_ViewPager);

        ChapterVideoViewpagerAdapter chapterVideoViewPagerAdapter = new
                ChapterVideoViewpagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount(),
                mIntCategoryID, mIntChapterID, this);

        viewPager.setAdapter(chapterVideoViewPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        //This will show guide; if already not seen
        //showGuide();

        //Show mixpanel in app notification;
        showMixpanelInAppMessage(false);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back_Arrow:
                finish();
                break;
            case R.id.imgSearch:
                getSupportActionBar().hide();
                mLinearLayoutTab.setVisibility(View.GONE);
                mLinearLayoutSearch.setVisibility(View.VISIBLE);
                mSearchView.setIconified(false);
                mSearchView.requestFocus();
                break;
            case R.id.imgBtnBackSearchViewChapterVideoTabScreen:
                getSupportActionBar().show();
                mLinearLayoutTab.setVisibility(View.VISIBLE);
                mLinearLayoutSearch.setVisibility(View.GONE);
                mSearchView.setIconified(true);
                mSearchView.clearFocus();
                if (mVideoAdapter != null) {
                    mChapterVideosArrayList.clear();
                    mVideoAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.action_search:
                getSupportActionBar().hide();
                mLinearLayoutTab.setVisibility(View.GONE);
                mLinearLayoutSearch.setVisibility(View.VISIBLE);
                mSearchView.setIconified(false);
                mSearchView.requestFocus();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getVideoList(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getVideoList(newText);
        return false;
    }

    //Check to get the video list from chapter or through #tag
    private void getVideoList(String query) {
        if (query.length() > 0) {
            /*if(query.substring(0,1).equals("#")){ //check hash tag
                if(query.length() > 3) {
                    getHasTagVideos(query);
                }
            }else*/
            getChapterVideos(query);


        }
    }

    /***
     * Get Chapter realted videos
     *
     * @param VideoTitle
     */
    private void getChapterVideos(String VideoTitle) {
        if (VideoTitle.length() > 2) {
            if (mVideoAdapter != null) {
                mChapterVideosArrayList.clear();
                mVideoAdapter.notifyDataSetChanged();
            }
            mChapterVideosArrayList = (ArrayList<Videos>)
                    database.doGetVideosListSearchByName(mIntChapterID, VideoTitle);
            //Toast.makeText(getApplicationContext(), "Size == " + mChapterVideosArrayList.size(), Toast.LENGTH_LONG).show();

            // 3. create an adapter
            mVideoAdapter = new ChapterVideoListAdapter(mChapterVideosArrayList,
                    this, REQUEST_WRITE_STORAGE);

            // 4. set adapter
            mRecyclerViewSearchVideo.setAdapter(mVideoAdapter);

            addSearchVideoEvent(VideoTitle); //Add Event for Analytics
        }
    }

    /***
     * get hast tag videos
     *
     * @param HashTag
     */
    private void getHasTagVideos(String HashTag) {
        if (HashTag.length() > 2) {
            if (mVideoAdapter != null) {
                mChapterVideosArrayList.clear();
                mVideoAdapter.notifyDataSetChanged();
            }
            mChapterVideosArrayList = (ArrayList<Videos>)
                    database.doGetVideosListSearchByHashTag(HashTag);
            //Toast.makeText(getApplicationContext(), "Size == " + mChapterVideosArrayList.size(), Toast.LENGTH_LONG).show();

            // 3. create an adapter
            mVideoAdapter = new ChapterVideoListAdapter(mChapterVideosArrayList,
                    this, REQUEST_WRITE_STORAGE);
            // 4. set adapter
            mRecyclerViewSearchVideo.setAdapter(mVideoAdapter);
        }

    }

   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChapterName = null;
        System.gc();
    }

    /***
     * Add Event for Analytics when video is search
     *
     * @param QueryString
     */
    private void addSearchVideoEvent(String QueryString) {
        /*09th July 2019, Resolved Pyze library issue*/
        HashMap<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("chapterName", ChapterName);
        customAttributes.put("chapterID", String.valueOf(mIntChapterID));
        PyzeEvents.PyzeMedia.postSearched(QueryString, customAttributes);
    }


    /***
     * This will show guide
     *//*
    public void showGuide() {
        try {

            *//*This will show guide for chapter*//*
            GuideDialog mGuideDialog = new GuideDialog(this);
            mGuideDialog.initDialog(GuideType.VIDEO_LIST);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }*/

    /***
     * This will show chapters guide; if chapters are available
     */
    public void showGuide() {
        try {

            /*This will show guide for chapter*/
            GuideDialog mGuideDialog = new GuideDialog(this);
            mGuideDialog.initDialog(GuideType.VIDEO_LIST);
            mGuideDialog.setOnGotItClickListener(new GuideDialog.OnGotItClickListener() {
                @Override
                public void onClick() {

                    showMixpanelInAppMessage(true);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /***
     * This will show a mixpanel notification if available
     * Reference: https://mixpanel.com/help/reference/android-inapp-messages
     */
    public void showMixpanelInAppMessage(boolean isFromDialog) {

        MixPanelClass mixPanelClass = new MixPanelClass(this);

        //Check whether a user has watched Chapter guide; if not then the in-app message will only open after viewing chpater guide
        if (mixPanelClass.getPref(MixPanelClass.PREF_IS_VIDEO_LIST_GUIDE_DONE, false)) {

            if (mixPanelClass.getCurrentInstance().getPeople().getNotificationIfAvailable() != null) {

                mixPanelClass.getCurrentInstance().getPeople().showNotificationIfAvailable(this);
            }
        } else {

            //This will show guide; if already not seen
            showGuide();
        }
    }
}



