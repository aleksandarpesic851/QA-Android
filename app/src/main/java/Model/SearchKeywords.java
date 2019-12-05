package Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import Utils.ErrorLog;

@Table(name = "SearchKeywords")
public class SearchKeywords extends Model implements Serializable {

    @Column(name = "SearchKeywordId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int searchKeywordId;

    @SerializedName("Keyword")
    @Column(name = "Keyword")
    private String searchKeyword = "";

    @SerializedName("BackedupKeyword")
    @Column(name = "BackedupKeyword")
    private String backedupKeyword = "";

    @SerializedName("HitCount")
    @Column(name = "HitCount")
    private int hitCount;

    @SerializedName("Status")
    @Column(name = "Status")
    private int status = 0;

    @SerializedName("ChapterId")
    @Column(name = "ChapterId")
    private int chapterId;

    @SerializedName("needToUpload")
    @Column(name = "needToUpload")
    private boolean needToUpload = false;

    public SearchKeywords() {
        super();
    }

    public SearchKeywords(int searchKeywordId, String searchKeyword, String backedupKeyword, int hitCount, int status, int chapterId, boolean needToUpload) {
        super();
        this.searchKeywordId = searchKeywordId;
        this.searchKeyword = searchKeyword;
        this.backedupKeyword = backedupKeyword;
        this.hitCount = hitCount;
        this.status = status;
        this.chapterId = chapterId;
        this.needToUpload = needToUpload;
    }

    public SearchKeywords(JSONObject object) {
        super();
        try {
            if (object.has("Id"))
                this.searchKeywordId = object.getInt("Id");
            if (object.has("Keyword"))
                this.searchKeyword = object.getString("Keyword");
            if (object.has("BackedupKeyword"))
                this.backedupKeyword = object.getString("BackedupKeyword");
            if (object.has("HitCount"))
                this.hitCount = object.getInt("HitCount");
            if (object.has("Status"))
                this.status = object.getInt("Status");
            if (object.has("ChapterId"))
                this.chapterId = object.getInt("ChapterId");
        } catch (JSONException e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);

    public static ArrayList<SearchKeywords> fromJson(JSONArray jsonObjects) {
        ArrayList<SearchKeywords> SearchKeywordArryList = new ArrayList<SearchKeywords>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                SearchKeywordArryList.add(new SearchKeywords(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }
        return SearchKeywordArryList;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getBackedupKeyword() {
        return backedupKeyword;
    }

    public void setBackedupKeyword(String backedupKeyword) {
        this.backedupKeyword = backedupKeyword;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public int getSearchKeywordId() {
        return searchKeywordId;
    }

    public void setSearchKeywordId(int searchKeywordId) {
        this.searchKeywordId = searchKeywordId;
    }

    public boolean isNeedToUpload() {
        return needToUpload;
    }

    public void setNeedToUpload(boolean needToUpload) {
        this.needToUpload = needToUpload;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
