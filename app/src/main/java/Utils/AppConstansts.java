package Utils;

import com.visualphysics.R;

/**
 * Created by India on 7/12/2016.
 */
public interface AppConstansts {

    int APP_OPEN_COUNT = 10; //Its a counter when the app open the counter will increase and check it reach to APP_OPEN_COUNT
    int RATE_APP_COUNT_1 = 5; //Its a counter when the user click on back btn from Video screen to show Rate us dialog
    int RATE_APP_COUNT_2 = 7;

    String ALGORITHM = "AES";
    String ENCRPYT_KEY = "NViLyPhSUtyAnSLi";

    boolean IS_ERROR_LOG_SAVE = false; // Save the Error Log file in SD card in Visual Physics folder
    boolean IS_DEBUG = false; // Print the log in console

    int APP_LAST_SYNC_DAYS = 5;

    /*With all menus*/
    /*public static final String DRAWER_TITLE[] = {"Home", "My Profile", "Buy", "Refer a Friend", "Rate us", "Help & Support", "Settings"};
    public static final int DRAWER_ITEM[] = {R.drawable.icon_home_inactive, R.drawable.icon_profile_inactive, R.drawable.icon_buy_inactive, R.drawable.icon_friends_inactive, R.drawable.icon_rate_inactive, R.drawable.icon_help_inactive, R.drawable.icon_settings_inactive};
    public static final int DRAWER_ITEM_SELECTED[] = {R.drawable.icon_home_active, R.drawable.icon_profile_active, R.drawable.icon_buy_active, R.drawable.icon_friends_active, R.drawable.icon_rate_active, R.drawable.icon_help_active, R.drawable.icon_settings_active};*/

    String[] DRAWER_TITLE = {"Home", "My Profile", "Buy", "Spread A Word", "Feedback", "Help & Support", "Settings"};
    int[] DRAWER_ITEM = {R.drawable.icon_home_inactive, R.drawable.icon_profile_inactive, R.drawable.icon_buy_inactive, R.drawable.icon_spread_a_word_inactive, R.drawable.icon_feedback_inactive, R.drawable.icon_help_inactive, R.drawable.icon_settings_inactive};
    int[] DRAWER_ITEM_SELECTED = {R.drawable.icon_home_active, R.drawable.icon_profile_active, R.drawable.icon_buy_active, R.drawable.icon_spread_a_word_active, R.drawable.icon_feedback_active, R.drawable.icon_help_active, R.drawable.icon_settings_active};

    String CATEGORY_ID = "CategoryID";
    String CHAPTER_NAME = "ChapterName";
    String CHAPTER_ID = "ChapterID";

}
