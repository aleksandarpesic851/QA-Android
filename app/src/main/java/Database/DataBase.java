package Database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.SparseArray;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import Model.AppMenu;
import Model.Category;
import Model.ChapterVideoDetail;
import Model.Chapters;
import Model.Countries;
import Model.SearchKeywords;
import Model.States;
import Model.Videos;
import Model.VideosWithChapter;
import Utils.ErrorLog;
import Utils.SharedPrefrences;


public class DataBase extends SQLiteOpenHelper implements DBConstants {

    private static final String DB_NAME = "VisualPhysics";
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;
    private static final int DB_VERSION = 4; //Will be incremented by 1 after each single alteration in DB structure

    public DataBase(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";

    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        if (dbExist) {


        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (Exception e) {
                // throw new Error("Error copying database");
                e.printStackTrace();
                ErrorLog.SendErrorReport(e);
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        try {
            InputStream myInput = context.getAssets().open(DB_NAME);
            //String outFileName = DB_PATH + DB_NAME;
            File outFileName = context.getDatabasePath(DB_NAME);
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }

    }

    public void getData() {

        String myPath = DB_PATH + DB_NAME;

        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        // SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        // return c;
    }

    public void open() {
        // String myPath = DB_PATH + DB_NAME;
        File outFileName = context.getDatabasePath(DB_NAME);
        String myPath = outFileName.toString();
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public void close() {

        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

    }

    // //get the country list
    @SuppressWarnings("null")
    public ArrayList<Countries> getCountryList() {
        ArrayList<Countries> countryArrayList = null;
        Cursor cursor = null;
        try {
            open();

            String query = "SELECT * FROM " + COUNTRY_TABLE_NAME;

            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                countryArrayList = new ArrayList<Countries>();
                while (cursor.moveToNext()) {

                    Countries CountryObj = new Countries();
                    CountryObj.setCountryID(cursor.getString(cursor.getColumnIndex(COUNTRY_COUNTRY_ID)));
                    CountryObj.setCountryName(cursor.getString(cursor.getColumnIndex(COUNTRY_COUNTRY_NAME)));
                    countryArrayList.add(CountryObj);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {
            close();
        }

        return countryArrayList;
    }


    // //get the State list
    @SuppressWarnings("null")
    public ArrayList<States> getStateList(String CountryID) {
        ArrayList<States> stateArrayList = null;
        Cursor cursor = null;
        try {
            open();

            String query = "SELECT * FROM " + STATE_TABLE_NAME + " WHERE " + COUNTRY_COUNTRY_ID + " =?";
            String[] args = new String[]{CountryID};
            cursor = db.rawQuery(query, args);

            if (cursor.getCount() > 0) {
                stateArrayList = new ArrayList<States>();
                while (cursor.moveToNext()) {

                    States StatesObj = new States();
                    StatesObj.setStateID(cursor.getString(cursor.getColumnIndex(STATE_STATE_ID)));
                    StatesObj.setCountryID(cursor.getString(cursor.getColumnIndex(STATE_COUNTRY_ID)));
                    StatesObj.setStateName(cursor.getString(cursor.getColumnIndex(STATE_STATE_NAME)));
                    stateArrayList.add(StatesObj);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {
            close();
        }

        return stateArrayList;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }


    /**************************Category***********************/
    /***
     * Add Cateogry HomeScreenFragment into the database
     *
     * @param CategoryArrayList
     * @return
     */
    public boolean doAddCategory(ArrayList<Category> CategoryArrayList) {
        boolean status = false;
        ActiveAndroid.beginTransaction();
        try {
            for (Category CategoryObj : CategoryArrayList) {
                CategoryObj.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
            return status;
        }
    }


    /***
     * Get the Category HomeScreenFragment from the database
     *
     * @return HomeScreenFragment <Category>
     */
    public List<Category> doGetCategoryList() {
        List<Category> CategoryList = null;
        try {
            CategoryList = new Select().from(Category.class).execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return CategoryList;
    }


    /***
     * Check is Category record available in the database or not
     *
     * @return boolean
     */
    public boolean isCategoryAvailable() {
        boolean status = false;
        List<Category> CategoryList = null;
        try {
            CategoryList = new Select().from(Category.class).execute();
            if (CategoryList != null && CategoryList.size() > 0)
                status = true;

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /*****
     * @param ChapterID
     * @return
     */
    public boolean isChapterVideoAvailable(int ChapterID) {
        boolean status = false;
        List<Videos> VideoList = null;
        try {
            //VideoList = new Select().from(Videos.class).execute();
            VideoList = new Select()
                    .from(Videos.class)
                    .where("ChapterID='" + ChapterID + "'")
                    .execute();
            if (VideoList != null && VideoList.size() > 0)
                status = true;

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;

    }

    /***
     * @return
     */
    public boolean isChapterVideoAvailable() {
        boolean status = false;
        List<Videos> VideoList = null;
        try {
            //VideoList = new Select().from(Videos.class).execute();
            VideoList = new Select()
                    .from(Videos.class)
                    //.where("ChapterID='"+ ChapterID + "'")
                    .execute();
            if (VideoList != null && VideoList.size() > 0)
                status = true;

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;

    }

    /***
     * @return
     */
    public boolean isSearchKeywordDataAvailable() {
        boolean status = false;
        List<SearchKeywords> searchKeywordList = null;
        try {
            searchKeywordList = new Select()
                    .from(SearchKeywords.class)
                    .execute();
            if (searchKeywordList != null && searchKeywordList.size() > 0)
                status = true;

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * @param VideosArrayList
     * @return
     */
    public boolean doAddChapterVideos(ArrayList<Videos> VideosArrayList) {
        boolean status = false;
        ActiveAndroid.beginTransaction();
        try {
            for (Videos VideoObj : VideosArrayList) {
                VideoObj.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } finally {
            ActiveAndroid.endTransaction();
            return status;
        }
    }


    /***
     * Get Videos HomeScreenFragment
     *
     * @return
     */
    public List<Videos> doGetVideosList(String Type, int ChapterID) {
        List<Videos> VideosList = null;
        try {
            VideosList = new Select()
                    .from(Videos.class)
                    .where("Type='" + Type + "'" + " and ChapterID='" + ChapterID + "'" + " and Status = 1")
                    //.where("ChapterID" + " = ?", ChapterID)
                    .orderBy("VideoOrder ASC,VideoID ASC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideosList;
    }

    /***
     * Get Videos HomeScreenFragment
     *
     * @return
     */
    public List<Videos> getAllVideosForChapter(int ChapterID) {
        List<Videos> VideosList = null;
        try {
            VideosList = new Select()
                    .from(Videos.class)
                    .where("ChapterID='" + ChapterID + "'" + " and Status = 1")
                    //.where("ChapterID" + " = ?", ChapterID)
                    .orderBy("VideoOrder ASC,VideoID ASC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideosList;
    }


    /***
     * Get All Videos of Chapter
     *
     * @param ChapterID
     * @return
     */
    public List<Videos> doGetVideosList(int ChapterID) {
        List<Videos> VideosList = null;
        try {
            VideosList = new Select()
                    .from(Videos.class)
                    .where("ChapterID='" + ChapterID + "'")
                    //.where("ChapterID" + " = ?", ChapterID)
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideosList;
    }


    /***
     * Delete all records from Category table
     *
     * @return
     */
    public boolean deleteAllCategoryRecords() {
        boolean status = false;
        try {
            new Delete().from(Category.class).executeSingle();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * Get Videos search by video Name
     *
     * @return
     */
    public List<Videos> doGetVideosListSearchByName(int ChapterID, String VideoTitle) {
        List<Videos> VideosList = null;
        try {
            VideosList = new Select()
                    .from(Videos.class)
                    //.where("Title LIKE '" +VideoTitle +"'"+ "and ChapterID='" +ChapterID +"'")
                    .where("(Title LIKE '%" + VideoTitle + "%' or Description Like '%" + VideoTitle + "%') and ChapterID='" + ChapterID + "'"
                            + " and Status = 1")
                    //.where("ChapterID" + " = ?", ChapterID)
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideosList;
    }


    /***
     * Get Videos search by hash tag
     *
     * @return
     */
    @SuppressLint("Recycle")
    public SparseArray<ArrayList<VideosWithChapter>> doGetVideosListSearch(ArrayList<String> searchText) {
        SparseArray<ArrayList<VideosWithChapter>> videosWithChaptersDetailMap = new SparseArray<>(50);
        ArrayList<VideosWithChapter> videosWithChaptersDetailList = null;

        List<SearchKeywords> searchKeywords1 = getSearchKeywords();
        for (SearchKeywords item : searchKeywords1) {
            if (searchText.get(0).contains(item.getSearchKeyword())) {
                searchText.add(item.getSearchKeyword());
            }
        }

        Cursor cursor = null;
        try {
            open();

            String[] searchColumnCriteria = {"vid.SearchTags", "vid.Description", "vid.Title", "chap.ChapterName"};
            String searchCriteria = getLikeQueryString(searchText, searchColumnCriteria);

            if (TextUtils.isEmpty(searchCriteria)) {
                return videosWithChaptersDetailMap;
            }

            String query = "SELECT vid.*, ChapterName, ChapterOrder, VideoCount, CreatedDate, CategoryID, TotalSize, ChapterOrder, chap.TagID AS ChapterTagID, TotalDuration, chap.Image AS ChapterImage, chap.Status AS ChapterStatus FROM Video AS vid INNER JOIN Chapter AS chap ON chap.ChapterID=vid.ChapterID WHERE " + searchCriteria + " and vid.Status = 1 ORDER BY vid.ChapterID ASC";

            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    VideosWithChapter videosWithChapter = new VideosWithChapter();
                    int chapterID = cursor.getInt(cursor.getColumnIndex(CHAPTER_ID));
                    videosWithChapter.ChapterID = chapterID;
                    videosWithChapter.ChapterName = cursor.getString(cursor.getColumnIndex(CHAPTER_NAME));
                    videosWithChapter.CategoryID = cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
                    videosWithChapter.Image = cursor.getString(cursor.getColumnIndex(CHAPTER_IMAGE));
                    videosWithChapter.VideoCount = cursor.getString(cursor.getColumnIndex(VIDEO_COUNT));
                    videosWithChapter.CreatedDate = cursor.getString(cursor.getColumnIndex(CREATED_DATE));
                    videosWithChapter.TotalDuration = cursor.getString(cursor.getColumnIndex(TOTAL_DURATION));
                    videosWithChapter.TotalSize = cursor.getString(cursor.getColumnIndex(TOTAL_SIZE));
                    videosWithChapter.ChapterOrder = cursor.getInt(cursor.getColumnIndex(CHAPTER_ORDER));
                    videosWithChapter.Status = cursor.getInt(cursor.getColumnIndex(CHAPTER_STATUS));
                    videosWithChapter.TagID = cursor.getString(cursor.getColumnIndex(CHAPTER_TAG_ID));

                    videosWithChapter.VideoID = cursor.getInt(cursor.getColumnIndex(VIDEO_ID));
                    videosWithChapter.Title = cursor.getString(cursor.getColumnIndex(TITLE));
                    videosWithChapter.Type = cursor.getString(cursor.getColumnIndex(TYPE));
                    videosWithChapter.VideoURL = cursor.getString(cursor.getColumnIndex(VIDEO_URL));
                    videosWithChapter.StreamingURL = cursor.getString(cursor.getColumnIndex(STREAMING_URL));
                    videosWithChapter.DownloadURL = cursor.getString(cursor.getColumnIndex(DOWNLOAD_URL));
                    videosWithChapter.Image = cursor.getString(cursor.getColumnIndex(IMAGE));
                    videosWithChapter.Description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
                    videosWithChapter.Duration = cursor.getString(cursor.getColumnIndex(DURATION));
                    videosWithChapter.Size = cursor.getString(cursor.getColumnIndex(SIZE));
                    videosWithChapter.SearchTags = cursor.getString(cursor.getColumnIndex(SEARCH_TAGS));
                    videosWithChapter.Status = cursor.getInt(cursor.getColumnIndex(STATUS));
                    videosWithChapter.Rno = cursor.getString(cursor.getColumnIndex(RNO));
                    videosWithChapter.video_Order = cursor.getInt(cursor.getColumnIndex(VIDEO_ORDER));
                    videosWithChapter.TagID = cursor.getString(cursor.getColumnIndex(TAG_ID));
                    videosWithChapter.Tag = cursor.getString(cursor.getColumnIndex(TAG));

                    if (videosWithChaptersDetailMap.indexOfKey(chapterID) >= 0) {
                        videosWithChaptersDetailMap.get(chapterID).add(videosWithChapter);
                    } else {
                        videosWithChaptersDetailList = new ArrayList<>();
                        videosWithChaptersDetailList.add(videosWithChapter);
                        videosWithChaptersDetailMap.put(chapterID, videosWithChaptersDetailList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {
            close();
        }

        List<SearchKeywords> searchKeywords = getAllSearchKeywords(searchText.get(0));
        if (!searchKeywords.isEmpty()) {
            SearchKeywords searchKeyword = searchKeywords.get(0);
            increaseSearchKeywordsHitCount(searchKeyword.getSearchKeyword());
        } else {
            SearchKeywords searchKeyword = new SearchKeywords();
            searchKeyword.setSearchKeyword(searchText.get(0));
            searchKeyword.setHitCount(1);
            searchKeyword.setStatus(2);
            insertSearchKeyword(searchKeyword);
        }

        return videosWithChaptersDetailMap;
    }

    private String getLikeQueryString(ArrayList<String> searchText, String[] searchColumnCriteria) {
        StringBuilder searchQuery = new StringBuilder();
        if (searchColumnCriteria.length <= 0) return "";

        for (int i = 0; i < searchColumnCriteria.length; i++) {
            for (String searchItem : searchText) {
                searchItem = searchItem.replaceAll("'", "''").replaceAll("\"", "\"\"");
                searchQuery.append(" ").append(searchColumnCriteria[i].trim()).append(" LIKE '%").append(searchItem.trim()).append("%' or ");
            }
        }
        return searchQuery.toString().substring(0, searchQuery.toString().lastIndexOf("or"));
    }

    /***
     * Get Videos search by hash tag
     *
     * @return
     */
    public List<Videos> doGetVideosListSearchByHashTag(String HashTag) {
        List<Videos> VideosList = null;
        try {
            VideosList = new Select()
                    .from(Videos.class)
                    //.where("Title LIKE '" +VideoTitle +"'"+ "and ChapterID='" +ChapterID +"'")
                    //.where("Title LIKE '%"+VideoTitle+"%' and ChapterID='" +ChapterID +"'")
                    .where("SearchTags LIKE '%" + HashTag + "%'" + " and Status = 1")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideosList;
    }


    /**************************Chapters***********************/
    /***
     * Add Chapters HomeScreenFragment into the database
     *
     * @param ChapterArrayList
     * @return
     */
    public boolean doAddChapters(ArrayList<Chapters> ChapterArrayList) {
        boolean status = false;
        ActiveAndroid.beginTransaction();
        try {
            for (Chapters ChapterObj : ChapterArrayList) {
                ChapterObj.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } finally {
            ActiveAndroid.endTransaction();
            return status;
        }
    }

    /***
     * Get the Chapter HomeScreenFragment from the database
     *
     * @return HomeScreenFragment <Chapter>
     */
    public List<Chapters> doGetChapterList() {
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select().from(Chapters.class).execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return ChapterList;
    }


    /***
     * Check is Chapter record available in the database or not
     *
     * @return boolean
     */
    public boolean isChapterAvailable() {
        boolean status = false;
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select().from(Chapters.class).execute();
            if (ChapterList != null && ChapterList.size() > 0)
                status = true;


        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * Delete all records from Chapters table
     *
     * @return
     */
    public boolean deleteAllChapterRecords() {
        boolean status = false;
        try {
            new Delete().from(Chapters.class).execute();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * Get the Chapter HomeScreenFragment according to catergory
     *
     * @return HomeScreenFragment <Chapter>
     */
    public List<Chapters> doGetCategoryChapterList(int CatergoryID) {
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select()
                    .from(Chapters.class)
                    .where("CategoryID='" + CatergoryID + "'" + " and Status = 1")
                    .orderBy("ChapterOrder ASC,CategoryID ASC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return ChapterList;
    }


    /***
     * Get All Chapters NewHomeScreenFragment
     *
     * @return NewHomeScreenFragment <Chapter>
     */
    public List<Chapters> getAllChapterList() {
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select()
                    .from(Chapters.class)
                    .where("Status = 1")
                    .orderBy("VideoCount DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return ChapterList;
    }

    /***
     * Get All SearchKeywords
     *
     * @return NavigationScreen <SearchKeywords>
     */
    public List<SearchKeywords> getSearchKeywords() {
        List<SearchKeywords> searchKeywordsList = null;
        try {
            searchKeywordsList = new Select()
                    .from(SearchKeywords.class)
                    .orderBy("HitCount DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return searchKeywordsList;
    }

    /***
     * Get SearchKeywords
     *
     * @return NavigationScreen <SearchKeywords>
     */
    private List<SearchKeywords> getAllSearchKeywords(String searchKeyword) {
        searchKeyword = searchKeyword.replaceAll("'", "''").replaceAll("\"", "\"\"");
        List<SearchKeywords> searchKeywordsList = null;
        try {
            searchKeywordsList = new Select()
                    .from(SearchKeywords.class)
                    .where("Keyword = '" + searchKeyword + "'")
                    .orderBy("Id DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return searchKeywordsList;
    }

    /***
     * Get SearchKeywords
     *
     * @return NavigationScreen <SearchKeywords>
     */
    private List<SearchKeywords> getSearchKeywords(String searchKeyword) {
        searchKeyword = searchKeyword.replaceAll("'", "''").replaceAll("\"", "\"\"");
        List<SearchKeywords> searchKeywordsList = null;
        try {
            searchKeywordsList = new Select()
                    .from(SearchKeywords.class)
                    .where("Status = 1 AND Keyword = '" + searchKeyword + "'")
                    .orderBy("Id DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return searchKeywordsList;
    }

    /***
     * Get SearchKeywords
     *
     * @return NavigationScreen <SearchKeywords>
     */
    private boolean insertSearchKeyword(SearchKeywords searchKeyword) {
        boolean status = false;
        try {
            searchKeyword.setSearchKeywordId(getMaxSearchKeywordId()+1);
            searchKeyword.setNeedToUpload(true);
            searchKeyword.save();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * Get SearchKeywords Max Id
     *
     * @return NavigationScreen <SearchKeywords>
     */
    private int getMaxSearchKeywordId() {
        String query = "Select Max(SearchKeywordId) as MaxId from SearchKeywords";
        int maxId = 0;

        try {
            open();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                maxId = cursor.getInt(cursor.getColumnIndex("MaxId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);

        } finally {
            close();
        }

        return maxId;
    }

    /***
     * Get SearchKeywords
     *
     * @return NavigationScreen <SearchKeywords>
     */
    private List<SearchKeywords> getSearchKeywords(int searchKeywordId) {
        List<SearchKeywords> searchKeywordsList = null;
        try {
            searchKeywordsList = new Select()
                    .from(SearchKeywords.class)
                    .where("Status = 1 AND SearchKeywordId = " + searchKeywordId)
                    .orderBy("Id DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return searchKeywordsList;
    }

    /***
     * Increase SearchKeywords HitCount
     *
     * @return NavigationScreen <SearchKeywords>
     */
    public boolean increaseSearchKeywordsHitCount(String searchKeyword) {
        searchKeyword = searchKeyword.replaceAll("'", "''").replaceAll("\"", "\"\"");
        boolean status = false;
        try {

            // Check the download count and add count
            List<SearchKeywords> searchKeywords = getAllSearchKeywords(searchKeyword);
            if (!searchKeywords.isEmpty()) {
                int hitCount = searchKeywords.get(0).getHitCount() + 1;

                //Update the record
                new Update(SearchKeywords.class)
                        .set("HitCount=?,needToUpload=?", hitCount, 1)
                        .where("Keyword = '" + searchKeyword + "'")
                        .execute();
                status = true;
            } else {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        return status;
    }


    /**************************App Menu***********************/
    /***
     * Add AppMenu that are not to be display in Left Menu screen
     *
     * @param AppMenuArrayList
     * @return
     */
    public boolean doAddAppMenu(ArrayList<AppMenu> AppMenuArrayList) {
        boolean status = false;
        ActiveAndroid.beginTransaction();
        try {
            boolean deleteStatus = false;
            try {
                new Delete().from(AppMenu.class).execute();
            } catch (Exception e) {
                ErrorLog.SendErrorReport(e);
            }


            for (AppMenu AppMenuObj : AppMenuArrayList) {
                AppMenuObj.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } finally {
            ActiveAndroid.endTransaction();
            return status;
        }
    }

    /**************************App Menu***********************/
    /***
     * Add SearchKeyword
     *
     * @param searchKeywordsArrayList
     * @return
     */
    public boolean doAddSearchKeywords(ArrayList<SearchKeywords> searchKeywordsArrayList) {
        boolean status = false;
        ActiveAndroid.beginTransaction();
        try {
            boolean deleteStatus = false;
            try {
                new Delete().from(SearchKeywords.class).execute();
            } catch (Exception e) {
                ErrorLog.SendErrorReport(e);
            }


            for (SearchKeywords searchKeywords : searchKeywordsArrayList) {
                searchKeywords.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        } finally {
            ActiveAndroid.endTransaction();
            return status;
        }
    }


    /***
     * Get the APP menu
     *
     * @return HomeScreenFragment <Chapter>
     */
    public List<AppMenu> doGetAppMenuList() {
        List<AppMenu> AppMenuList = null;
        try {
            AppMenuList = new Select()
                    .from(AppMenu.class)
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return AppMenuList;
    }

    /**********************ADD Chapter video detail (Like/Play/Download)***********************************/

    /***
     * @param VideoDetail
     * @return
     */
    public boolean doAddVideoLike(ChapterVideoDetail VideoDetail) {
        boolean status = false;
        try {
            if (checkVideoDetailAvailable(VideoDetail.getVideoID())) {
                //Update the record
                new Update(ChapterVideoDetail.class)
                        .set("LikeStatus=?", 1)
                        .where("VideoID='" + VideoDetail.getVideoID() + "'" + " and UserID='" + VideoDetail.getUserID() + "'")
                        .execute();
                status = true;
            } else {
                //insert the record
                VideoDetail.setUploadStatus(false);
                VideoDetail.save();
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        return status;
    }

    /***
     * Add the Download count of Video
     *
     * @param VideoDetail
     * @return
     */
    public boolean doAddVideoDownloadCount(ChapterVideoDetail VideoDetail) {
        boolean status = false;
        try {
            if (checkVideoDetailAvailable(VideoDetail.getVideoID())) {
                // Check the download count and add count
                ChapterVideoDetail chapterVideoDetailLocal = getVideoDetail(VideoDetail.getVideoID());
                int VideoDownloadCount = chapterVideoDetailLocal.getVideoDownloadCount() + 1;

                //Update the record
                new Update(ChapterVideoDetail.class)
                        .set("VideoDownloadCount=?", VideoDownloadCount)
                        .where("VideoID='" + VideoDetail.getVideoID() + "'" + " and UserID='" + VideoDetail.getUserID() + "'")
                        .execute();
                status = true;
            } else {
                //insert the record
                VideoDetail.setUploadLikeStatus(false);
                VideoDetail.save();
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        return status;
    }


    /***
     * Add the Play count of Video
     *
     * @param VideoDetail
     * @return
     */
    public boolean doAddVideoPlayCount(ChapterVideoDetail VideoDetail) {
        boolean status = false;
        try {
            if (checkVideoDetailAvailable(VideoDetail.getVideoID())) {
                // Check the download count and add count
                ChapterVideoDetail chapterVideoDetailLocal = getVideoDetail(VideoDetail.getVideoID());
                int VideoPlayCount = chapterVideoDetailLocal.getVideoPlayCount() + 1;

                //Update the record
                new Update(ChapterVideoDetail.class)
                        .set("VideoPlayCount=?", VideoPlayCount)
                        .where("VideoID='" + VideoDetail.getVideoID() + "'" + " and UserID='" + VideoDetail.getUserID() + "'")
                        .execute();
                status = true;
            } else {
                //insert the record
                VideoDetail.setUploadLikeStatus(false);
                VideoDetail.save();
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        return status;
    }


    /***
     * Check Video Detail is already avilable in the database or not
     *
     * @param VideoID
     * @return
     */
    public boolean checkVideoDetailAvailable(int VideoID) {
        List<ChapterVideoDetail> VideoDetailList = null;
        SharedPrefrences mSharePref = new SharedPrefrences(context);
        String UserID = mSharePref.getLoginUser().getStudentID();
        boolean status = false;
        try {
            VideoDetailList = new Select()
                    .from(ChapterVideoDetail.class)
                    .where("VideoID='" + VideoID + "'" + " and UserID='" + UserID + "'")
                    .execute();
            if (VideoDetailList != null && VideoDetailList.size() > 0)
                status = true;

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return status;
    }

    /***
     * Update the status of video
     *
     * @param VideoID
     */
    public void updateUploadVideoStatus(int VideoID, String ColoumName) {
        SharedPrefrences mSharePref = new SharedPrefrences(context);
        String UserID = mSharePref.getLoginUser().getStudentID();
        try {
            new Update(ChapterVideoDetail.class)
                    .set(ColoumName + "=?", 1)
                    .where("VideoID='" + VideoID + "'" + " and UserID='" + UserID + "'")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

    }

    /***
     * Update the video status (Like Video Upload status/VideoDownloadCount/VideoPlayCount)
     */
    public void updatePendingUploadVideoStatus(String UserID) {

        try {
            //Update Video Like Upload Status
            new Update(ChapterVideoDetail.class)
                    .set("UploadLikeStatus" + "=?", 1)
                    .where("UserID='" + UserID + "'" + " and (UploadLikeStatus=0 and LikeStatus=1)")
                    .execute();

            //Update Video Download count
            new Update(ChapterVideoDetail.class)
                    .set("VideoDownloadCount" + "=?", 0)
                    .where("UserID='" + UserID + "'" + " and (VideoDownloadCount > 0)")
                    .execute();

            //Update Video Download count
            new Update(ChapterVideoDetail.class)
                    .set("VideoPlayCount" + "=?", 0)
                    .where("UserID='" + UserID + "'" + " and (VideoPlayCount > 0)")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

    }

    /***
     * Get the Video Detail
     *
     * @param VideoID
     * @return
     */
    public ChapterVideoDetail getVideoDetail(int VideoID) {
        List<ChapterVideoDetail> VideoDetailList = null;
        ChapterVideoDetail VideoDetail = null;
        SharedPrefrences mSharePref = new SharedPrefrences(context);
        String UserID = mSharePref.getLoginUser().getStudentID();
        boolean status = false;
        try {
            VideoDetailList = new Select()
                    .from(ChapterVideoDetail.class)
                    .where("VideoID='" + VideoID + "'" + " and UserID='" + UserID + "'")
                    .execute();
            if (VideoDetailList != null && VideoDetailList.size() > 0) {
                VideoDetail = VideoDetailList.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideoDetail;
    }


    public List<ChapterVideoDetail> getPendingVideoDetail(String UserID) {
        List<ChapterVideoDetail> VideoDetailList = null;
        boolean status = false;
        try {
            VideoDetailList = new Select()
                    .from(ChapterVideoDetail.class)
                    //.where("UserID='"+ UserID + "'"+ " and UploadLikeStatus=false")
                    .where("UserID='" + UserID + "'" + " and ((UploadLikeStatus=0 and LikeStatus=1) OR (VideoDownloadCount > 0) OR (VideoPlayCount>0)  )") //Where Like status true and upload status false
                    .execute();


        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return VideoDetailList;
    }

    //IZISS
    public Videos getVideoDetail(int ChapterID, int VideoID) {

        List<Videos> VideosList = new ArrayList<>();
        Videos VideoDetail = null;

        try {
            VideosList = new Select()
                    .from(Videos.class)
                    .where("ChapterID='" + ChapterID + "'" + " and VideoID='" + VideoID + "'")
                    .execute();

            if (VideosList != null && VideosList.size() > 0) {
                VideoDetail = VideosList.get(0);
            }


        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        return VideoDetail;

    }


    /***
     * Get the Chapter HomeScreenFragment according to catergory
     *
     * @return HomeScreenFragment <Chapter>
     */
    public Chapters getChapterDetail(int chapterID) {
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select()
                    .from(Chapters.class)
                    .where("ChapterID='" + chapterID + "'")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        if (ChapterList != null) {
            if (ChapterList.size() > 0) {
                return ChapterList.get(0);
            }
        }


        return null;
    }


    /***
     * Get the Chapter HomeScreenFragment according to catergory
     *
     * @return HomeScreenFragment <Chapter>
     */
    public Chapters getChapterDetailForDeepLink(int categoryID, int chapterID) {
        List<Chapters> ChapterList = null;
        try {
            ChapterList = new Select()
                    .from(Chapters.class)
                    .where("ChapterID='" + chapterID + "'" + " and CategoryID='" + categoryID + "'")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }

        if (ChapterList != null) {
            if (ChapterList.size() > 0) {
                return ChapterList.get(0);
            }
        }


        return null;
    }

    /***
     * Get All SearchKeywords with filter needToUpload = true
     *
     * @return NavigationScreen <SearchKeywords>
     */
    public List<SearchKeywords> checkAndGetSearchKeywordsToUpload() {
        List<SearchKeywords> searchKeywordsList = null;
        try {
            searchKeywordsList = new Select()
                    .from(SearchKeywords.class)
                    .where("needToUpload = 1")
                    .orderBy("Id DESC")
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SendErrorReport(e);
        }
        return searchKeywordsList;
    }
}

