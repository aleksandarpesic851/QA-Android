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
 * Created by India on 6/9/2016.
 */
@Table(name = "Category")
public class Category extends Model implements Serializable {

    @Column(name = "CategoryID", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int CategoryID;

    @Column(name = "CategoryName")
    public String CategoryName;

    @Column(name = "Description")
    public String Description;

    public Category() {
        super();
    }

    public Category(JSONObject object) {
        super();
        try {
            if (object.has("CategoryID"))
                this.CategoryID = object.getInt("CategoryID");
            if (object.has("CategoryName"))
                this.CategoryName = object.getString("CategoryName");
            if (object.has("Description"))
                this.Description = object.getString("Description");
        } catch (JSONException e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);

    public static ArrayList<Category> fromJson(JSONArray jsonObjects) {
        ArrayList<Category> CategoryArrayList = new ArrayList<Category>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                CategoryArrayList.add(new Category(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }
        return CategoryArrayList;
    }

}
