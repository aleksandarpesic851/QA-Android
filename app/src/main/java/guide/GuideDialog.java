package guide;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.visualphysics.R;

import mixpanel.MixPanelClass;

/**
 * Created by Iziss on 14/6/18.
 */
public class GuideDialog {

    private Dialog dialog;
    private Activity activity;
    private Window window;
    private View dialogView;

    //Click listener callback
    private OnGotItClickListener mOnGotItClickListener;

    public GuideDialog(Activity activity) {
        this.activity = activity;
    }

    /***
     * Initialize the dialog
     */
    public void initDialog(final GuideType guideType) {

        int layoutID = R.layout.layout_chapter_overlay;
        String prefKey = MixPanelClass.PREF_IS_CHAPTER_GUIDE_DONE;

        //Decides screen which will be shown to user
        switch (guideType) {
            case CHAPTER:

                layoutID = R.layout.layout_chapter_overlay;
                prefKey = MixPanelClass.PREF_IS_CHAPTER_GUIDE_DONE;

                break;

            case VIDEO_LIST:

                layoutID = R.layout.layout_video_list_overlay;
                prefKey = MixPanelClass.PREF_IS_VIDEO_LIST_GUIDE_DONE;

                break;

            case VIDEO:

                layoutID = R.layout.layout_video_overlay;
                prefKey = MixPanelClass.PREF_IS_VIDEO_GUIDE_DONE;

                break;
        }

        //Initialize dialog with its theme
        dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);

        //Removes title bar from dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // This will holds the view for dialog
        dialogView = activity.getLayoutInflater().inflate(layoutID, null);

        //Set custom view on dialog
        dialog.setContentView(dialogView);

        //Get current window to show dialog
        window = dialog.getWindow();

        //Get window manager layout params's attributes
        WindowManager.LayoutParams windowManagerLayoutParams = window.getAttributes();

        //Set gravity of window manager
        windowManagerLayoutParams.gravity = Gravity.CENTER;

        //Set attributes to window
        window.setAttributes(windowManagerLayoutParams);

        //Set params for dialog to full screen
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        TextView mTextView = (TextView) dialogView.findViewById(R.id.txtGotIt);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeDialog();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                //Return call back
                if (mOnGotItClickListener != null) {
                    mOnGotItClickListener.onClick();
                }

            }
        });


        //Update preference
        updatePreference(prefKey);

    }

    /***
     * This will show the dialog
     */
    public void showDialog() {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    /***
     * This will hides the dialog
     */
    public void closeDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.cancel();
            }
        }
    }

    /***
     * This will update the preference that partucular screen guide is done
     *
     * @param prefKey
     */
    private void updatePreference(String prefKey) {

        try {

            //Initialize Mixpanel
            MixPanelClass mixPanelClass = new MixPanelClass(activity);

            //Comment me to only show dialog one time
            //mixPanelClass.setPref(prefKey, false);

            //Check whether it is a first login for user then show him/her a Guide
            if (mixPanelClass.getPref(MixPanelClass.PREF_IS_FIRST_LOGIN, true)) {

                //Check whether a user has watched Chapter guide; if not then show
                if (!mixPanelClass.getPref(prefKey, false)) {

                    //This will show the dialog
                    showDialog();

                    //set preference that dialog is seen once
                    mixPanelClass.setPref(prefKey, true);

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This will send call back to the screen from which it is open
     *
     * @param mOnGotItClickListener
     */
    public void setOnGotItClickListener(OnGotItClickListener mOnGotItClickListener) {

        this.mOnGotItClickListener = mOnGotItClickListener;
    }


    public interface OnGotItClickListener {
        void onClick();

    }


}
