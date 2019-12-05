package Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Utils.ErrorLog;

/**
 * Created by India on 6/25/2016.
 */

@Table(name = "AppMenu")
public class AppMenu extends Model {

    @Column(name = "AppMenuId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int AppMenuId;

    @Column(name = "AppMenuName")
    private String AppMenuName;

    public AppMenu() {
        super();
    }

    public AppMenu(int AppMenuId, String AppMenuName) {
        super();
        this.AppMenuId = AppMenuId;
        this.AppMenuName = AppMenuName;
    }

    public AppMenu(JSONObject object) {
        super();
        try {
            if (object.has("AppMenuId"))
                this.AppMenuId = object.getInt("AppMenuId");
            if (object.has("AppMenuName"))
                this.AppMenuName = object.getString("AppMenuName");
        } catch (JSONException e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);

    public static ArrayList<AppMenu> fromJson(JSONArray jsonObjects) {
        ArrayList<AppMenu> AppMenuArryList = new ArrayList<AppMenu>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                AppMenuArryList.add(new AppMenu(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                ErrorLog.SaveErrorLog(e);
                ErrorLog.SendErrorReport(e);
            }
        }
        return AppMenuArryList;
    }

    public String getAppMenuName() {
        return AppMenuName;
    }

    public void setAppMenuName(String appMenuName) {
        AppMenuName = appMenuName;
    }

    public int getAppMenuId() {
        return AppMenuId;
    }

    public void setAppMenuId(int appMenuId) {
        AppMenuId = appMenuId;
    }
}
