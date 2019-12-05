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
 * Created by admin on 6/10/2016.
 */
@Table(name = "Video")
public class Videos extends Model implements Serializable {

    @Column(name = "VideoID", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int VideoID;

    @Column(name = "ChapterID")
    public int ChapterID;

    @Column(name = "Title")
    public String Title;

    @Column(name = "Type")
    public String Type;

    @Column(name = "VideoURL")
    public String VideoURL;

    @Column(name = "StreamingURL")
    public String StreamingURL;

    @Column(name = "DownloadURL")
    public String DownloadURL;

    @Column(name = "Image")
    public String Image;

    @Column(name = "Description")
    public String Description;

    @Column(name = "Duration")
    public String Duration;

    @Column(name = "Size")
    public String Size;

    @Column(name = "SearchTags")
    public String SearchTags;

    @Column(name = "Status")
    public int Status = 1;

    @Column(name = "Rno")
    public String Rno;

    @Column(name = "VideoOrder")
    public int video_Order = 1;

    @Column(name = "TagID")
    public String TagID = "";

    @Column(name = "Tag")
    public String Tag = "";

    public Videos() {
        super();
    }

    public Videos(JSONObject object) {
        super();
        try {
            if (object.has("VideoID"))
                this.VideoID = object.getInt("VideoID");
            if (object.has("ChapterID"))
                this.ChapterID = object.getInt("ChapterID");
            if (object.has("Title"))
                this.Title = object.getString("Title");
            if (object.has("Type"))
                this.Type = object.getString("Type");
            if (object.has("Image"))
                this.Image = object.getString("Image");
            if (object.has("VideoURL"))
                this.VideoURL = object.getString("VideoURL");
            if (object.has("StreamingURL"))
                this.StreamingURL = object.getString("StreamingURL");
            if (object.has("DownloadURL"))
                this.DownloadURL = object.getString("DownloadURL");
            if (object.has("Image"))
                this.Image = object.getString("Image");
            if (object.has("Description"))
                this.Description = object.getString("Description");
            if (object.has("Duration"))
                this.Duration = object.getString("Duration");
            if (object.has("Size"))
                this.Size = object.getString("Size");
            if (object.has("Rno"))
                this.Rno = object.getString("Rno");
            if (object.has("SearchTags"))
                this.SearchTags = object.getString("SearchTags");
            if (object.has("video_Order"))
                this.video_Order = object.getInt("video_Order");
            if (object.has("Status"))
                this.Status = object.getInt("Status");
            if (object.has("TagID"))
                this.TagID = object.getString("TagID");
            if (object.has("Tag"))
                this.Tag = object.getString("Tag");

        } catch (JSONException e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // Videos.fromJson(jsonArray);

    public static ArrayList<Videos> fromJson(JSONArray jsonObjects) {
        ArrayList<Videos> VideosArryList = new ArrayList<Videos>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                VideosArryList.add(new Videos(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }
        return VideosArryList;
    }
}