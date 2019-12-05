package zoho;

import android.content.Context;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zoho.commons.ChatComponent;
import com.zoho.livechat.android.MbedableComponent;
import com.zoho.salesiqembed.ZohoSalesIQ;

import Model.LoginUser;
import Utils.SharedPrefrences;
import mixpanel.MixPanelClass;

/**
 * Created by iziss on 28/6/18.
 */
public class ZohoUtils {

    /**
     * This will hide ZOHO chat icon appears at bottom
     */
    public static void hideZohoChat() {
        try {
            ZohoSalesIQ.Chat.setVisibility(MbedableComponent.CHAT, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This will show ZOHO chat icon appears at bottom
     */
    public static void showZohoChat() {

        try {
            ZohoSalesIQ.Chat.setVisibility(MbedableComponent.CHAT, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Set properties
        setZohoProperties();
    }

    /***
     * This will set user information to ZOHO
     *
     * @param mContext
     */
    public static void setVisitorsInfo(Context mContext) {

        SharedPrefrences mSharedPrefrences = new SharedPrefrences(mContext);
        LoginUser mLoginUser = mSharedPrefrences.getLoginUser();

        //Set user data
        ZohoSalesIQ.Visitor.setEmail(mLoginUser.getEmail());
        ZohoSalesIQ.Visitor.setName(mLoginUser.getFullName());
        ZohoSalesIQ.Visitor.setContactNumber(mLoginUser.getCellPhone());

        //Custom attributes
        ZohoSalesIQ.Visitor.addInfo("SED", mLoginUser.getSubscriptionEndDate());
        ZohoSalesIQ.Visitor.addInfo("REGISTRATION_DATE", mLoginUser.getCreatedDate());
        ZohoSalesIQ.Visitor.addInfo("STUDENT_ID", mLoginUser.getStudentID());

        MixPanelClass mixPanelClass = new MixPanelClass(mContext);

        //Set questionnaire data to Zoho
        ZohoSalesIQ.Visitor.addInfo(MixPanelClass.MPA_USE, mixPanelClass.getQuestionPreference("Q1").getOtherOptionAnswer());
        ZohoSalesIQ.Visitor.addInfo(MixPanelClass.MPA_EXAM, mixPanelClass.getQuestionPreference("Q2").getOtherOptionAnswer());
        ZohoSalesIQ.Visitor.addInfo(MixPanelClass.MPA_YEAR, mixPanelClass.getQuestionPreference("Q3").getOtherOptionAnswer());

    }

    /**
     * This will set push token to ZOHO server
     */
    public static void setPushToken() {

    /*Set Firebase credentials*/
        if (FirebaseInstanceId.getInstance() != null) {
            String firebaseDeviceToken = FirebaseInstanceId.getInstance().getToken();

            if (firebaseDeviceToken != null) {

                ZohoSalesIQ.Notification.enablePush(firebaseDeviceToken, true); //true-> for test devices

            }
        }

        //Set properties
        setZohoProperties();
    }

    /**
     * This will set different properties for Zoho
     */
    public static void setZohoProperties() {

        try {
        /*Enable FAQ for Zoho*/
            ZohoSalesIQ.FAQ.setVisibility(true);

        /*Disable Feedback from Zoho*/
            ZohoSalesIQ.Chat.setVisibility(ChatComponent.Feedback, false);

        /*Disable Rating from Zoho*/
            ZohoSalesIQ.Chat.setVisibility(ChatComponent.Rating, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
