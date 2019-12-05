package Model;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import Utils.ErrorLog;

/**
 * Chapters for Category
 */
@Table(name = "Chapter")
public class Chapters extends Model implements Serializable {

    @Column(name = "ChapterID", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int ChapterID;

    @Column(name = "ChapterName")
    public String ChapterName;

    @Column(name = "CategoryID")
    public String CategoryID;

    @Column(name = "Image")
    public String Image;

    @Column(name = "VideoCount")
    public String VideoCount;

    @Column(name = "CreatedDate")
    public String CreatedDate;

    @Column(name = "TotalDuration")
    public String TotalDuration;

    @Column(name = "TotalSize")
    public String TotalSize;

    @Column(name = "ChapterOrder")
    public int ChapterOrder;

    @Column(name = "Status")
    public int Status = 1;

    @Column(name = "TagID")
    public String TagID = "";


    public Chapters() {
        super();
    }

    public Chapters(JSONObject object) {
        super();
        try {
            if (object.has("ChapterID"))
                this.ChapterID = object.getInt("ChapterID");
            if (object.has("ChapterName"))
                this.ChapterName = object.getString("ChapterName");
            if (object.has("CategoryID"))
                this.CategoryID = object.getString("CategoryID");
            if (object.has("Image"))
                this.Image = object.getString("Image");
            if (object.has("VideoCount"))
                this.VideoCount = object.getString("VideoCount");
            if (object.has("CreatedDate"))
                this.CreatedDate = object.getString("CreatedDate");
            if (object.has("TotalDuration"))
                this.TotalDuration = object.getString("TotalDuration");
            if (object.has("TotalSize"))
                this.TotalSize = object.getString("TotalSize");
            if (object.has("ChapterOrder"))
                this.ChapterOrder = object.getInt("ChapterOrder");
            if (object.has("Status"))
                this.Status = object.getInt("Status");
            if (object.has("TagID"))
                this.TagID = object.getString("TagID");
        } catch (JSONException e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // Chapters.fromJson(jsonArray);

    public static ArrayList<Chapters> fromJson(JSONArray jsonObjects) {
        ArrayList<Chapters> ChaptersArryList = new ArrayList<Chapters>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                ChaptersArryList.add(new Chapters(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }
        return ChaptersArryList;
    }

}