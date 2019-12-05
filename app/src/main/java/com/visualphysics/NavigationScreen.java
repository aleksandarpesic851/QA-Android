package com.visualphysics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.Runtime;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.pyze.android.PyzeIdentity;
import com.pyze.android.PyzeManager;
import com.pyze.android.push.IPyzePushListener;
import com.pyze.android.push.dto.PyzePushMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Adapter.ChapterVideoListSearchAdapter;
import Adapter.CustomSearchAdapter;
import Adapter.DrawerAdapter;
import Database.DataBase;
import Model.AppMenu;
import Model.ChapterVideoDetail;
import Model.Chapters;
import Model.LoginUser;
import Model.SearchKeywords;
import Model.Videos;
import Model.VideosWithChapter;
import UIControl.CustomSearchViewAutoComplete;
import Utils.AppUtil;
import Utils.DebugLog;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import deeplink.DeepLinkGenerator;
import firebase.FirebaseClass;
import guide.GuideDialog;
import guide.GuideType;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

import static Utils.AppConstansts.CATEGORY_ID;
import static Utils.AppConstansts.CHAPTER_ID;
import static Utils.AppConstansts.CHAPTER_NAME;
import static Utils.AppUtil.isTagUpdatedForVideos;

public class NavigationScreen extends AppCompatActivity implements OnTaskCompleted.CallBackListener, SearchView.OnQueryTextListener {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Fragment fragment;
    TextView toolbartext;
    public static boolean isMainActivityShown = false;

    private ApiCall mApiCall;
    private AppUtil mAppUtils;
    private DataBase db;
    private SharedPrefrences mSharedPref;

    private ListView list_menu_items;
    private DrawerAdapter mDrawerAdapter;

    //IZISS
    private LicenseListener OnVideoPlayLicenseListener;

    private String COUPON_CODE = null;
    private static boolean isNavigationScreenActive = true;

    public static final String DEEP_LINK_PREFIX = "www.visualphysics.nlytn.in";
    public static final String DEEP_LINK_PREFIX_GOOGLE = "n88nw.app.goo.gl";

    private final String DEEP_LINK_VIDEO = "playvideo";
    private final String DEEP_LINK_VIEW_PROFILE = "viewprofile";
    private final String DEEP_LINK_BUY_PACKAGE = "buyPackage";
    private final String DEEP_LINK_REFER_FRIEND = "referFriend";
    private final String DEEP_LINK_SETTINGS = "settings";
    private final String DEEP_LINK_CHAPTER = "chapter";
    private final String DEEP_LINK_SURVEY = "survey";

    private final String DEEP_LINK_PROMO = "promo";
    private final String DEEP_LINK_TEST = "test";
    private FrameLayout frameLayout;

    private ArrayList<Chapters> mChapterArrayListFromServer;
    private ArrayList<Videos> mVideosArrayListFromServer;
    private ArrayList<String> searchTextList;
    private CustomSearchViewAutoComplete mSearchView;
    private LinearLayout mLinearLayoutSearch;
    private RecyclerView mRecyclerViewSearchVideo;
    private ImageButton mImgBtnBack;
    private ProgressDialog mProgressDialog;
    private TextView tvNoResultFound;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isMainActivityShown) {
            if (frameLayout.getVisibility() == View.GONE) {
                mImgBtnBack.performClick();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("Are you sure you want to Quit?")
                    .setPositiveButton("Yes", (dialog, whichButton) -> finish()).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }
            ).show();
        } else if (!isMainActivityShown) {

            isMainActivityShown = true;

            onNavigationItemSelected(0);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the wallet; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_wishlist, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search:
                getSupportActionBar().hide();
                frameLayout.setVisibility(View.GONE);
                mLinearLayoutSearch.setVisibility(View.VISIBLE);
                mSearchView.setIconified(false);
                mSearchView.requestFocus();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_screen_activity);

        if (isLogin()) {

            mDeclaration();

            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            if (SplashScreenActivity.referralCodeDeepLink != null) {
                data = SplashScreenActivity.referralCodeDeepLink;
            }

            //Always add this screen to resolve deep link and white screen issue, the flow will navigate after this fragment
            onNavigationItemSelected(0);

            if (data != null) {

                Log.i("Data>>", "" + data);

                if (data.toString().contains(DEEP_LINK_VIDEO)) {

                    playVideoSetup(data.toString());

                } else if (data.toString().contains(DEEP_LINK_VIEW_PROFILE)) {
                    try {
                        COUPON_CODE = getIds(data.toString())[4];
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ErrorLog.SendErrorReport(ex);
                    }

                    onNavigationItemSelected(1);

                } else if (data.toString().contains(DEEP_LINK_BUY_PACKAGE)) {

                    //To handle buy package id and its end date
                    String[] values = getIds(data.toString());

                    if (values != null) {

                        if (values.length > 5) {

                            try {
                                String packageCategoryID = values[4];
                                int packageValidationDays = Integer.parseInt(values[5]);

                                SharedPrefrences mSharedPrefrences = new SharedPrefrences(this);
                                mSharedPrefrences.setPreferences(BuyFrag.KEY_PACKAGE_CATEGORY_ID, packageCategoryID);
                                mSharedPrefrences.setPreferences(BuyFrag.KEY_EXPIRY_DATE, "" + AppUtil.getCurrentTimeInMillisPlusDays(packageValidationDays));

                            } catch (Exception ex) {
                                ex.printStackTrace();

                            }

                        } else {

                            /*Added to show actual buy screen when some one click on buy now on subscription popup
                             * We will check if the expiry time of offer is expired or not*/
                            SharedPrefrences mSharedPrefrences = new SharedPrefrences(this);
                            if (Long.parseLong(mSharedPrefrences.getPreferences(BuyFrag.KEY_EXPIRY_DATE, "0")) > AppUtil.getCurrentTimeInMillis()) {

                            } else {
                                mSharedPrefrences.setPreferences(BuyFrag.KEY_PACKAGE_CATEGORY_ID, "");
                                mSharedPrefrences.setPreferences(BuyFrag.KEY_EXPIRY_DATE, "" + 0);
                            }


                        }
                    }

                    onNavigationItemSelected(2);

                } else if (data.toString().contains(DEEP_LINK_REFER_FRIEND)) {

                    /*Added by IZISS to remove refer a friend functionality*/
                    //onNavigationItemSelected(3);

                } else if (data.toString().contains(DEEP_LINK_SETTINGS)) {

                    onNavigationItemSelected(6);

                } else if (data.toString().contains(DEEP_LINK_CHAPTER)) {

                    String[] values = getIds(data.toString());

                    Chapters mChapters = db.getChapterDetailForDeepLink(Integer.parseInt(values[4]), Integer.parseInt(values[5]));
                    if (mChapters != null) {
                        Intent mIntent = new Intent(this, ChapterVideoScreenActivity.class);
                        mIntent.putExtra(CATEGORY_ID, Integer.parseInt(values[4]));
                        mIntent.putExtra(CHAPTER_NAME, mChapters.ChapterName);
                        mIntent.putExtra(CHAPTER_ID, Integer.parseInt(values[5]));
                        startActivity(mIntent);
                    } else {
                        Toast.makeText(NavigationScreen.this, "Chapter is not available.", Toast.LENGTH_SHORT).show();
                    }
                } else if (data.toString().contains(DEEP_LINK_SURVEY) || data.toString().contains(DEEP_LINK_PROMO) || data.toString().contains(DEEP_LINK_TEST)) {

                    String pageNameToLoad = getIds(data.toString())[3];
                    String pageToLoad = getIds(data.toString())[4];

                    Intent mIntent = new Intent(this, WebViewActivity.class);
                    mIntent.putExtra(WebViewActivity.PAGE_NAME_TO_LOAD, pageNameToLoad);
                    mIntent.putExtra(WebViewActivity.PAGE_TO_LOAD, pageToLoad);
                    startActivity(mIntent);
                }
            }

            //mDeclaration();
            checkVideoDetailToBeUpload();

            if (isNavigationScreenActive) {
                Pyze.showInAppNotificationUI(this, null);
                registerForPyzePush();
            }

            SplashScreenActivity.referralCodeDeepLink = null;

            if (mAppUtils.getConnectionState()) {

                //To update SED date and then send to Mixpanel every time and put a check on the response sot that profile will not created
                getStudentSubscriptionEndDate();

            }

            //Show mixpanel in app notification; commented to resolve mess up with Guide
            showMixpanelInAppMessage(false);

            //This will set visitors profile/info to zoho
            ZohoUtils.setVisitorsInfo(this);

            //Show Zoho chat icon
            ZohoUtils.showZohoChat();

        } else {

            this.finish();
            Intent mIntent = new Intent(this, SplashScreenActivity.class);
            startActivity(mIntent);

        }
    }

    private void checkDataBaseAndDownloadLatestVideoContent() {
        if (db.isChapterAvailable() && db.isChapterVideoAvailable()) {

            List<Chapters> mChapterArrayList = db.getAllChapterList();

            if (isTagIDUpdated(mChapterArrayList)) {

                /*Code to check whether Tag column is available or not*/
                List<Videos> mVideosList = db.getAllVideosForChapter(mChapterArrayList.get(0).ChapterID);

                if (!isTagUpdatedForVideos(mVideosList)) {

                    //Get All Chapter and Videos with Tag in video table
                    getAllChapters();
                }
            } else {
                //Get All Chapter and Videos with TagId in video table
                getAllChapters();
            }
        } else {
            //Get All Chapter and Videos with TagId in video table
            getAllChapters();
        }
    }

    /***
     * Initialize the variable
     */
    private void mDeclaration() {
        db = new DataBase(getApplicationContext());
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mApiCall = new ApiCall();
        mAppUtils = new AppUtil(NavigationScreen.this);
        //mAppUtils.getKeyHash();
        mSharedPref = new SharedPrefrences(getApplicationContext());
        checkDataBaseAndDownloadLatestVideoContent();

        Toolbar toolbar = findViewById(R.id.toolbar_HomeScreen);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbartext = findViewById(R.id.toolbartext);

        toolbar.setNavigationIcon(R.drawable.ic_home);
        drawerLayout = findViewById(R.id.drawer_layout);

        frameLayout = findViewById(R.id.frameLayout_HomeScreen);

        setupSearchView();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close) {

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Do whatever you want here
            }

            //Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Do whatever you want here
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);

                //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        //navigationView.setVerticalScrollBarEnabled(false);
        //navigationView.setItemIconTintList(null);

        list_menu_items = navigationView.findViewById(R.id.list_menu_items);

        mDrawerAdapter = new DrawerAdapter(this);

        list_menu_items.setAdapter(mDrawerAdapter);

        list_menu_items.setOnItemClickListener((adapterView, view, i, l) -> onNavigationItemSelected(i));

        isMainActivityShown = true;

        /*Comment by IZISS to handle double calling of fragment*/
       /* //fragment = new HomeScreenFragment();
        fragment = new NewHomeScreenFragment();

        //IZISS 17 Oct
        //getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_HomeScreen, fragment)
        //        .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_HomeScreen, fragment)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitAllowingStateLoss();*/

        /*Comment by IZISS to handle double calling of fragment END*/

//        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + "Visual Physics" + "</font>"));
//        toolbar.setTitle("Visual Physics");

        toolbartext.setText(getResources().getString(R.string.visual_physics));

        //getUserInfo();

        initExpressPlay();
        // getAppMenuItem();

        //Add the Last open date
        //mAppUtils.lastOpenApp(getApplicationContext());

        addEvent();

    }

    private void setupSearchView() {
        mImgBtnBack = findViewById(R.id.imgBtnBackSearchViewChapterVideoTabScreen);
        mImgBtnBack.setOnClickListener(v -> {
            getSupportActionBar().show();
            frameLayout.setVisibility(View.VISIBLE);
            mLinearLayoutSearch.setVisibility(View.GONE);
            mSearchView.setIconified(true);
            mSearchView.clearFocus();
            if (mVideoAdapter != null) {
                mVideosListSparsedArray = new SparseArray<>();
                mVideoAdapter = new ChapterVideoListSearchAdapter(mVideosListSparsedArray,
                        this);
                mRecyclerViewSearchVideo.setAdapter(mVideoAdapter);
                tvNoResultFound.setVisibility(View.GONE);
            }
        });
        mLinearLayoutSearch = findViewById(R.id.lLayoutSearchViewChapterVideoTabScreen);
        mSearchView = findViewById(R.id.searchViewChapterVideoTabScreen);
        mSearchView.setOnQueryTextListener(this);
        tvNoResultFound = findViewById(R.id.tvNoResultFound);
        tvNoResultFound.setVisibility(View.GONE);
        TextView searchText = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});

        CustomSearchAdapter adapter = new CustomSearchAdapter
                (this, android.R.layout.select_dialog_item, db.getSearchKeywords());
        mSearchView.setAutoCompleSuggestionsAdapter(adapter);

        mSearchView.setOnItemClickListener((parent, view, position, id) -> {
            SearchKeywords searchKeywords = (SearchKeywords) view.getTag();
            mSearchView.setText(searchKeywords.getSearchKeyword());
            CharSequence searchToString = searchText.getText();
            Selection.setSelection(searchText.getEditableText(), searchToString.length());
            if (searchKeywords.getStatus() == 0) {
                tvNoResultFound.setVisibility(View.VISIBLE);
                tvNoResultFound.setText(R.string.upcoming_video);
            } else {
                tvNoResultFound.setText(R.string.no_result_found);
                tvNoResultFound.setVisibility(View.GONE);
                if (TextUtils.isEmpty(searchKeywords.getBackedupKeyword().trim())) {
                    ArrayList<String> queryList = new ArrayList<>();
                    queryList.add(searchKeywords.getSearchKeyword());
                    getVideoList(queryList);
                } else {
                    String[] querySplit = searchKeywords.getBackedupKeyword().split(",");
                    ArrayList<String> queryList = new ArrayList<>(Arrays.asList(querySplit));
                    queryList.add(0, searchKeywords.getSearchKeyword());
                    getVideoList(queryList);
                }
            }

            AppUtil.hideKeyBoard(this);
        });

        mRecyclerViewSearchVideo = findViewById(R.id.recyclerViewSearchVideoListChapterVideoTabScreen);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewSearchVideo.setLayoutManager(layoutManager);
    }

    /***
     * Get the Login user infor
     */
    private void getUserInfo() {
        mSharedPref = new SharedPrefrences(getApplicationContext());
        View headerView = navigationView.getHeaderView(0);
        TextView mTxtUserName = headerView.findViewById(R.id.txtUserNameNavigationScreen);
        TextView mTxtUserCode = headerView.findViewById(R.id.txtUserCodeNavigationScreen);
        mTxtUserName.setText(mSharedPref.getLoginUser().getFullName());
        mTxtUserCode.setText(mSharedPref.getLoginUser().getReferralCode());
    }

    public boolean onNavigationItemSelected(int position) {
        AppUtil.SELECTED_POSITION = position;

        /*Please remove this code if we need to perform action for refer menu as well
         * We have increment the current position by 1 so that we don't need to change the code written for position in switch case
         * Added by IZISS to remove refer a friend functionality on 13 Feb 2019*/

        if (position >= 3) {
            position = position + 1;
        }

        switch (position) {

            //For Home
            case 0:
                isMainActivityShown = true;
                //changeFragment(new HomeScreenFragment(), "Visual Physics");
                changeFragment(new NewHomeScreenFragment(), "Visual Physics");
                break;

            //For My Profile
            case 1:
                isMainActivityShown = false;

                if (COUPON_CODE != null) {

                    Bundle bundle = new Bundle();
                    bundle.putString("COUPON_CODE", COUPON_CODE);

                    MyProfileFragment myProfileFragment = new MyProfileFragment();
                    myProfileFragment.setArguments(bundle);

                    changeFragment(myProfileFragment, "Visual Physics");

                } else {
                    changeFragment(new MyProfileFragment(), "Visual Physics");

                }

                new FirebaseClass(this).sendAnalyticsData(0, "Open My Profile", AppUtil.EVENT_GENERAL_FEATURE);

                break;

            //For Buy
            case 2:
                isMainActivityShown = false;
                changeFragment(new BuyFrag(), "Visual Physics");

                new FirebaseClass(this).sendAnalyticsData(0, "Open Buy", AppUtil.EVENT_GENERAL_FEATURE);

                break;

            //For Refer A Friend
            case 3:
                isMainActivityShown = false;
                changeFragment(new ReferAllScreenFragment(), "Visual Physics");

                new FirebaseClass(this).sendAnalyticsData(0, "Referral", AppUtil.EVENT_REFERRAL);
                break;

            //For Spread a word
            case 4:
                isMainActivityShown = false;
                changeFragment(new SpreadAWordFragment(), "Visual Physics");
                new FirebaseClass(this).sendAnalyticsData(0, "Open Spread a Word", AppUtil.EVENT_SPREAD_A_WORD);
                break;

            // For Feedback
            case 5:
                String pageNameToLoad = "feedback";
                String pageToLoad = "";

                new FirebaseClass(this).sendAnalyticsData(0, "Open Feedback", AppUtil.EVENT_FEEDBACK);

                Intent mIntent = new Intent(this, WebViewActivity.class);
                mIntent.putExtra(WebViewActivity.PAGE_NAME_TO_LOAD, pageNameToLoad);
                mIntent.putExtra(WebViewActivity.PAGE_TO_LOAD, pageToLoad);
                startActivity(mIntent);
                break;
            //For Help & Support
            case 6:
                Intent browserIntent;
                LoginUser User = mSharedPref.getLoginUser();
                if (User != null)
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nlytn.in/contact-us?name="
                            + User.getFullName() + "&email=" + User.getEmail()));
                else
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nlytn.in/contact-us"));
                startActivity(browserIntent);

                new FirebaseClass(this).sendAnalyticsData(0, "Help", AppUtil.EVENT_CONVERSION);
                //navigationView.getMenu().getItem(PreviousItemSelectedIndex).setChecked(true);
                break;
            //For Settings
            case 7:
                isMainActivityShown = false;
                changeFragment(new SettingFrag(), "Visual Physics");
                break;
            case 8:
                isMainActivityShown = false;
                changeFragment(new AskToExpertScreenFragment(), "Visual Physics");
                break;
            case 9:
                Intent intent = new Intent(NavigationScreen.this, FreeSubscriptionScreen.class);
                startActivity(intent);
                break;
            default:
                isMainActivityShown = false;

        }

        drawerLayout.closeDrawer(GravityCompat.START);

        if (mDrawerAdapter != null)
            mDrawerAdapter.notifyDataSetChanged();

        /*This will update referral link*/
        updateReferralLink();

        return true;
    }

    public void changeFragment(Fragment fragment, String title) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id
                .frameLayout_HomeScreen, fragment);

        //IZISS 17 Oct; Undo this change on 15 Nov to resolve a crash
        fragmentTransaction.commit();
        //fragmentTransaction.commitAllowingStateLoss();

        if (toolbartext != null) {
            toolbartext.setText(title);
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /***
     * @param fragment
     * @param title
     * @param backStack
     */
    public void changeFragment(Fragment fragment, String title, boolean backStack) {

        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout_HomeScreen);

        if (mFragment != null) {
            if (mFragment.getClass().getSimpleName().equalsIgnoreCase(fragment.getClass().getSimpleName())) {
                return;
            } else {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                if (backStack) {
                    ft.addToBackStack(null);
                }
                ft.replace(R.id.frameLayout_HomeScreen, fragment);
                ft.commitAllowingStateLoss();
            }
        } else {

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            if (backStack) {
                ft.addToBackStack(null);
            }
            ft.replace(R.id.frameLayout_HomeScreen, fragment);
            ft.commitAllowingStateLoss();
        }

        if (toolbartext != null) {
            toolbartext.setText(title);
            drawerLayout.closeDrawer(GravityCompat.START);
        }

    }

    /***
     * Apply Font to App Menu item
     *
     * @param mi
     */
    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "Lato_Regular.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    /***
     * Get the AppMenu Items and Hide that Menu from the Menu screen
     */
    private void getAppMenuItem() {
        try {
            ArrayList<AppMenu> mAppMenuArrayList = (ArrayList<AppMenu>) db.doGetAppMenuList();
            if (mAppMenuArrayList != null && mAppMenuArrayList.size() > 0) {
                for (int i = 0; i < navigationView.getMenu().size(); i++) {
                    SpannableString MenuTitle = (SpannableString) navigationView.getMenu().getItem(i).getTitle();
                    for (AppMenu appMenu : mAppMenuArrayList) {
                        if (appMenu.getAppMenuName().equalsIgnoreCase(MenuTitle.toString())) {
                            navigationView.getMenu().getItem(i).setVisible(false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }


//


//            LayoutInflater factory = LayoutInflater.from(NavigationScreen.this);
//            final View deleteDialogView = factory.inflate(
//                    R.layout.custom_dialog, null);
//            final AlertDialog deleteDialog = new AlertDialog.Builder(NavigationScreen.this).create();
//            deleteDialog.setView(deleteDialogView);
//
//            deleteDialogView.findViewById(R.id.text_dialog)
//
//
//            deleteDialogView.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    //your business logic
//                    deleteDialog.dismiss();
//                }
//            });
//            deleteDialogView.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    deleteDialog.dismiss();
//
//                }
//            });
//
//            deleteDialog.show();


    /***
     * If Internet is avilable then check if any pending detail are reamining to be upload then
     * upload on the server
     */
    private void checkVideoDetailToBeUpload() {

        if (isLogin()) {


            if (!mAppUtils.getConnectionState()) {

            } else {
                List<ChapterVideoDetail> VideoDetailList = db.getPendingVideoDetail(mSharedPref.getLoginUser()
                        .getStudentID());
                if (VideoDetailList != null && VideoDetailList.size() > 0) {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (ChapterVideoDetail VideoDetail : VideoDetailList) {
                            JSONObject VideoObj = new JSONObject();
                            VideoObj.put("VideoID", VideoDetail.getVideoID());

                            /*Commented on 26 Nov 2018 to not send below details to server*/
                            /*if (VideoDetail.isLikeStatus()) // If Like Status is true
                                VideoObj.put("LikeCount", 1);
                            if (VideoDetail.getVideoPlayCount() > 0) // If video play is greater then 0
                                VideoObj.put("PlayCount", VideoDetail.getVideoPlayCount());
                            if (VideoDetail.getVideoDownloadCount() > 0) // If Video Download count greater then 0
                                VideoObj.put("Downloads", VideoDetail.getVideoDownloadCount());*/

                            jsonArray.put(VideoObj);
                        }
                        mApiCall.doUpdatePendingVideoDetail(mSharedPref.getLoginUser().getStudentID(),
                                mAppUtils.getDeviceID(), jsonArray,
                                new OnTaskCompleted(this), mApiCall.AddPendingVideoDetail);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorLog.SaveErrorLog(e);
                        ErrorLog.SendErrorReport(e);
                    }


                }

            }
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        if (Method.equals(mApiCall.GetAllChapterAndVideos)) {
            parseResponseForAllChaptersData(result);
        } else if (Method.equals(mApiCall.AddPendingVideoDetail)) {
            parseResponseForAddPendingVideoDetailData(result);
        } else if (Method.equals(mApiCall.GetUserSubscription)) {
            parseResponseForGetSubscriptionEndDateData(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {

    }

    /***
     * Parse the response of Uploading the pending data of video
     *
     * @param response
     */
    private void parseResponseForAddPendingVideoDetailData(JSONObject response) {
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.AddPendingVideoDetail);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 0) {
                new UpdateVideoDetailAsyncTask().execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("NavigationScreen", "http://www.visualphysics.nlytn.in/chapter");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());

        isNavigationScreenActive = true;

    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();

        isNavigationScreenActive = false;

    }

    @Override
    public boolean onQueryTextSubmit(String searchText) {
        tvNoResultFound.setText(R.string.no_result_found);
        if (searchText.isEmpty()) {
            tvNoResultFound.setVisibility(View.VISIBLE);
            return false;
        }
        searchTextList = new ArrayList<>();
        searchTextList.add(searchText);
        getVideoList(searchTextList);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {
        if (searchText.isEmpty()) {
            tvNoResultFound.setText(R.string.no_result_found);
            if (mVideoAdapter != null) {
                mVideosListSparsedArray = new SparseArray<>();
                mVideoAdapter = new ChapterVideoListSearchAdapter(mVideosListSparsedArray,
                        this);
                mRecyclerViewSearchVideo.setAdapter(mVideoAdapter);
                tvNoResultFound.setVisibility(View.VISIBLE);
            }
        }
        return false;
    }

    private void getVideoList(ArrayList<String> query) {
        if (query.size() > 0) {
            getChapterVideos(query);
            addSearchVideoEvent(query.get(0));
        }
    }

    private ChapterVideoListSearchAdapter mVideoAdapter;
    private SparseArray<ArrayList<VideosWithChapter>> mVideosListSparsedArray;

    /***
     * Get Chapter realted videos
     *
     * @param searchQuery
     */
    private void getChapterVideos(ArrayList<String> searchQuery) {
        if (searchQuery.get(0).length() > 2) {
            if (mVideoAdapter != null) {
                mVideosListSparsedArray = new SparseArray<>();
                mVideoAdapter.notifyDataSetChanged();
            }

            if (db.isChapterAvailable() && db.isChapterVideoAvailable()) {

                //This will set the data after sorting on the RecyclerView
                mVideosListSparsedArray = db.doGetVideosListSearch(searchQuery);
                if (mVideosListSparsedArray.size() > 0) {
                    tvNoResultFound.setVisibility(View.GONE);
                } else {
                    tvNoResultFound.setText(R.string.no_result_found);
                    tvNoResultFound.setVisibility(View.VISIBLE);
                }

                mVideoAdapter = new ChapterVideoListSearchAdapter(mVideosListSparsedArray,
                        this);

                // 4. set adapter
                mRecyclerViewSearchVideo.setAdapter(mVideoAdapter);
            } else {
                //Get All Chapter and Videos with TagId in video table
                getAllChapters();
            }
        }
    }

    /***
     * Get All Chapters of All Category
     */
    private void getAllChapters() {
        if (!mAppUtils.getConnectionState()) {

            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();

        } else {
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getAllChaptersAndAllVideos(mAppUtils.getDeviceID(),
                    new OnTaskCompleted(this), mApiCall.GetAllChapterAndVideos);
        }
    }

    /***
     * Parse the response of Chapters
     *
     * @param response
     */
    private void parseResponseForAllChaptersData(JSONObject response) {
        try {
            JSONObject mJsonObj;
            //If there is encryption error
            if (response.has("encryption_error")) {
                mJsonObj = response.getJSONObject("encryption_error");
            } else
                mJsonObj = response.getJSONObject(mApiCall.GetAllChapterAndVideos);
            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {

                Toast.makeText(this, getResources().getString(R.string.Error_Msg_Try_Later), Toast.LENGTH_SHORT).show();
                dismissDialog();

            } else if (ErrorCode == 2) {

                Toast.makeText(this, mJsonObj.getString("Message"), Toast.LENGTH_SHORT).show();
                dismissDialog();

            } else if (ErrorCode == 0) {

                mJsonObj = mJsonObj.getJSONObject("data");

                //Get Chapters
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("chapter"));
                mChapterArrayListFromServer = Chapters.fromJson(jsonArray);

                //Get Videos
                jsonArray = new JSONArray(mJsonObj.getString("video"));
                mVideosArrayListFromServer = Videos.fromJson(jsonArray);

                if (mChapterArrayListFromServer != null && mVideosArrayListFromServer != null) {
                    //Insert the Category Data into Database
                    new AsyncTaskRunner().execute();
                } else {
                    dismissDialog();
                }
            }

        } catch (Exception e) {

            dismissDialog();

            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    /***
     * Store Chapters and Videos
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return db.doAddChapters(mChapterArrayListFromServer) && db.doAddChapterVideos(mVideosArrayListFromServer);
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);

            dismissDialog();

            if (status) {
                Toast.makeText(NavigationScreen.this, "DataDownlaoded", Toast.LENGTH_SHORT).show();
                //Set updated data on list
//                setDataOnList();
                getVideoList(searchTextList);
            }
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

    /***
     * Check if the TagID for chapters are updated or not else we need to fetch from server
     *
     * @param mChapterArrayList
     * @return
     */
    private boolean isTagIDUpdated(List<Chapters> mChapterArrayList) {

        for (Chapters mChapters : mChapterArrayList) {

            if (mChapters.TagID != null) {

                if (mChapters.TagID.length() > 0) {

                    return true;

                }
            }
        }

        return false;
    }

    /***
     * Async task to update the pending video status like (LikeUploadStatus/VideoDownloadCount/PlayCount)
     */
    private class UpdateVideoDetailAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            db.updatePendingUploadVideoStatus(mSharedPref.getLoginUser().getStudentID());
            return null;
        }
    }

    private class Personalizer extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress;
        boolean status = false;

        protected void onPreExecute() {

            try {

                progress = ProgressDialog.show(NavigationScreen.this, "Please wait", "Personalization in progress " +
                        "...", true);

            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }

        protected Void doInBackground(Void... params) {

            try {
                Runtime.personalize();
                status = true;
                Log.i("personalization- ", "in progress--------");
            } catch (ErrorCodeException e) {
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReportPersonalization(e);
            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            if (progress != null) {
                progress.dismiss();
            }
            try {

                LicenseUtil mLicenseUtil = new LicenseUtil();
                mLicenseUtil.getPersonalizedDetails();

                if (Runtime.isPersonalized()) {
                    Log.i("Completed", "Personalization");

                    LicenseUtil License = new LicenseUtil();
                    License.acquireLicence(NavigationScreen.this, mSharedPref.getLoginUser().getStudentID());
                } else {

                    AppUtil.displaySnackBarWithMessage(((Activity) NavigationScreen.this).findViewById(android.R.id.content),
                            getResources().getString(R.string.msg_personalization_unable));

                }
                // DO SOMETHING
            } catch (Exception e) {
                Log.e("printing Nodes ID:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }

        }

    }

    /***
     * Initialize the Exprees play resources
     *
     * @return
     */
    private boolean initExpressPlay() {
        try {

            Runtime.initialize(getDir("wasabi", MODE_PRIVATE).getAbsolutePath());

            if (!Runtime.isPersonalized()) {

                Log.i("PERSONALIZATION", "*** device persoanlization in progress");

                new Personalizer().execute();
            }


        } catch (Exception e) {
            Log.e("INIT", e.getLocalizedMessage());
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }


        return true;

    }

    private class AcquireTokenTask extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            try {

            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
            }
        }

        protected Void doInBackground(Void... params) {

            try {
                LicenseUtil.downloadExpressPlayLicense(getApplicationContext(), "http://www.nlytn.in/app/test/test" +
                        ".xml");
            } catch (Exception e) {
                Log.e("personalization:", e.getMessage());
                ErrorLog.SaveErrorLog(e);
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
        }

    }

    /***
     * @param requestCode
     * @param permissions
     * @param grantResults
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent("onRequestPermissionsResult");
            // You can also include some extra data.
            intent.putExtra("RequestCode", requestCode);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //navigationView.getMenu().getItem(PreviousItemSelectedIndex).setChecked(true);

        mDrawerAdapter.notifyDataSetChanged();
    }

    //IZISS
    private String[] getIds(String url) {

        try {

            String[] values = url.split("/");

            for (int i = 0; i < values.length; i++) {
                Log.i("Value>", "" + values[i]);
            }

            return values;
        } catch (Exception ex) {
            return new String[]{"", "", "", "", "", "", "", "", ""};
        }
    }


    private void playVideoScreen(String url) {
        try {
            String[] values = getIds(url);
            Videos mVideoDetail = db.getVideoDetail(Integer.parseInt(values[4]), Integer.parseInt(values[5]));

            //Videos mVideoDetail = db.getVideoDetail(33, 2242);

            if (isLogin()) {
                if (mVideoDetail != null) {
                    Intent i = new Intent(this, VideoPlayerScreenActivity.class);
                    i.putExtra("VideoDetail", mVideoDetail);
                    i.putExtra("directPlay", true);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Video is not available.", Toast.LENGTH_SHORT).show();
                }
            } else {
                this.finish();
                Intent mIntent = new Intent(this, SplashScreenActivity.class);
                startActivity(mIntent);
            }
        } catch (Exception ex) {
            ErrorLog.SendErrorReport(ex);
        }
    }

    private void playVideoSetup(String url) {
        try {

            playVideoScreen(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorLog.SendErrorReport(ex);
        }

    }

    /***
     * If Token process successfully then start the Video
     */
    private void setOnVideoPlayLicenseProcessListener(final String url) {

        this.OnVideoPlayLicenseListener = new LicenseListener() {
            @Override
            public void onProcessToken() {
                /*Intent i = new Intent(context, VideoPlayerScreenActivity.class);
                i.putExtra("VideoDetail", mVideoDetail);
                context.startActivity(i);*/
                playVideoScreen(url);
            }
        };
    }

    private boolean isLogin() {

        mSharedPref = new SharedPrefrences(this);
        if (mSharedPref.getLoginUser() != null) {
            if (mSharedPref.getLoginUser().getStudentID() != null) {
                return mSharedPref.getLoginUser().getStudentID().length() > 0;
            }
        }
        return false;
    }

    private void addEvent() {

        //Pyze.initializeEvents(getApplication());
        mSharedPref = new SharedPrefrences(this);
        LoginUser loginUser = mSharedPref.getLoginUser();

        if (loginUser != null && isNavigationScreenActive) {

            PyzeIdentity.setUserIdentifer(loginUser.getStudentID());

            /*09th July 2019, Resolved Pyze library issue*/
            HashMap<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("Name", loginUser.getFullName());
            attributes.put("Email", loginUser.getEmail());
            attributes.put("StudentID", loginUser.getStudentID());
            attributes.put("SubscriptionEndDate", loginUser.getSubscriptionEndDate().split(" ")[0]);
            //PyzeIdentity.postTraits(attributes);
            PyzeEvents.postCustomEventWithAttributes("UserInfo", attributes);

           /* HashMap<String, String> newattributes = new HashMap<String, String>();
            newattributes.put("duration", "10");
            PyzeEvents.PyzeMedia.postPlayedMedia("Test2","Video2","CategoryName2","50","11",newattributes);*/
            //PyzeEvents.postCustomEventWithAttributes("UserInfo",attributes);
        }
    }

    /***
     * Add Custom event when user click on RateUS
     */
    private void addRateUsEvent() {
        /*09th July 2019, Resolved Pyze library issue*/
        HashMap<String, Object> customattributes = new HashMap<String, Object>();
        customattributes.put("RateUsMenu", "Clicked");

        if (isNavigationScreenActive)
            PyzeEvents.postCustomEventWithAttributes("Rated_Users", customattributes);
    }

    private void registerForPyzePush() {

        Pyze.registerForPushNotification(new IPyzePushListener() {
            @Override
            public void onPushNotificationReceived(PyzePushMessage pyzePushMessage) {
                //Perform some operation and then ask Pyze to display the notification
                Pyze.showPushNotification(pyzePushMessage);
            }
        });
        Log.i("Pyze Push>>", "" + PyzeManager.fcmToken);

        if (PyzeManager.fcmToken == null) {

            String var1 = FirebaseInstanceId.getInstance().getToken();

            if (PyzeManager.getContext() != null) {

                /*09th July 2019, Resolved Pyze library issue*/
                //PyzeManager.sendFCMRegisteration(var1);
                PyzeManager.sendFCMRegistration(var1);

            } else {
                com.pyze.android.utils.Log.d("Stored the FCM token to use after initialization!");
                PyzeManager.fcmToken = var1;
            }
        }
    }

    /***
     * Get Student subscription end date after Success payment
     */
    private void getStudentSubscriptionEndDate() {

        SharedPrefrences mSharePref = new SharedPrefrences(getApplicationContext());

        String DeviceID = mAppUtils.getDeviceID();
        LoginUser User = mSharePref.getLoginUser();
        if (!mAppUtils.getConnectionState()) {
            mAppUtils.displayNoInternetSnackBar(findViewById(android.R.id.content));
        } else if (DeviceID == null)
            AppUtil.displaySnackBarWithMessage(findViewById(android.R.id.content),
                    getResources().getString(R.string.error_device_id_not_found));
        else {

            DebugLog.v("Device", "Student ID " + User.getStudentID());
            mApiCall.getUserSubscription(User.getStudentID(), DeviceID,
                    new OnTaskCompleted(this), mApiCall.GetUserSubscription);

        }
    }

    /***
     * Parse the response of Getting Subscription end date data
     *
     * @param response
     */
    private void parseResponseForGetSubscriptionEndDateData(JSONObject response) {

        SharedPrefrences mSharePref = new SharedPrefrences(getApplicationContext());

        Intent intent = new Intent();
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetUserSubscription);

            int ErrorCode = mJsonObj.getInt("Error");
            if (ErrorCode == 1) {

            } else if (ErrorCode == 2) {

            } else if (ErrorCode == 0) {

                mJsonObj = mJsonObj.getJSONObject("data");
                LoginUser User = mSharePref.getLoginUser();
                User.setSubscriptionPeriod(mJsonObj.getString("SubscriptionPeriod"));
                User.setSubscriptionEndDate(mJsonObj.getString("SubscriptionEndDate"));

                //This will set Selected TagId to user preference
                User.setTagID(mJsonObj.optString("TagID"));

                Gson gson = new Gson();
                String jsonString = gson.toJson(User);

                //Update User Detail
                mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

                if (User != null) {

                    //Initialize Mixpanel class and sent it
                    MixPanelClass mixPanelClass = new MixPanelClass(this);

                    //Code to check whether profile is created or not
                    if (!mixPanelClass.getPref(MixPanelClass.IS_FIRST_VIDEO, false)) {
                        mixPanelClass.updateMixPanelPeopleAttribute(MixPanelClass.MPA_SUBSCRIPTION_END_DATE, User.getSubscriptionEndDate(), false);
                    }
                }

            }


            //Commented on 27 August 2018 to resolve white screen issue
            //Only redirects when current item is shown
            /*if (isFragmentAlreadyShown(new NewHomeScreenFragment())) {
                onNavigationItemSelected(0);
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /***
     * Set fragment on container
     *
     * @param fragment
     */
    public boolean isFragmentAlreadyShown(Fragment fragment) {

        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout_HomeScreen);

        if (mFragment != null) {
            return mFragment.getClass().getSimpleName().equalsIgnoreCase(fragment.getClass().getSimpleName());
        } else {
            return true;
        }

    }

    /***
     * This will show a mixpanel notification if available
     * Reference: https://mixpanel.com/help/reference/android-inapp-messages
     */
    public void showMixpanelInAppMessage(boolean isFromDialog) {

        MixPanelClass mixPanelClass = new MixPanelClass(this);

        //Added for firebase push notifications on 18th June 2019
        mixPanelClass.initializeMixpanelPush();

        //Check whether a user has watched Chapter guide; if not then the in-app message will only open after viewing chpater guide
        if (mixPanelClass.getPref(MixPanelClass.PREF_IS_CHAPTER_GUIDE_DONE, false)) {

            if (mixPanelClass.getCurrentInstance().getPeople().getNotificationIfAvailable() != null) {

                mixPanelClass.getCurrentInstance().getPeople().showNotificationIfAvailable(this);
            }

        }

    }

    /***
     * This will show chapters guide; if chapters are available
     */
    public void showGuide() {
        try {

            /*This will show guide for chapter*/
            GuideDialog mGuideDialog = new GuideDialog(this);
            mGuideDialog.initDialog(GuideType.CHAPTER);
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
     * Unused
     * This will change the color of TaskBar
     */
    private void changeTaskBarColor(int colorID) {
        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= 21)
            window.setStatusBarColor(ContextCompat.getColor(this, colorID));
    }

    /**
     * This will generate the list
     */
    private void updateReferralLink() {
        DeepLinkGenerator mDeepLinkGenerator = new DeepLinkGenerator(this);
        mDeepLinkGenerator.getDeepLinkForReferralCode();

    }

    /***
     * Add Event for Analytics when video is search
     *
     * @param QueryString
     */
    private void addSearchVideoEvent(String QueryString) {
        HashMap<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("searchType", "Global");
        PyzeEvents.PyzeMedia.postSearched(QueryString, customAttributes);
    }

}

