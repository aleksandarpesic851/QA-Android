package com.visualphysics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import Model.LoginUser;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.SharedPrefrences;
import deeplink.DeepLinkGenerator;
import deeplink.OnLinkGenerateListener;


public class ReferAllScreenFragment extends Fragment implements OnClickListener {

    private View parentView;
    private TextView mtxtRefrelCode;
    private Button mBtnBack, mBtnFacebookShare, mBtnWhatsAppShare, mBtnSendSMS_Share, mBtnMailShare, mBtnCoypToClipBoard,
            mBtnGooglePlus, mBtnContacts;

    private Typeface typeFaceProximaNovaRegular;

    private String strReferCode;
    private String strShareMessage = "Visual Physics has really nice Physics videos. Try full course free for 3 days. Use the code to get 3 more days free. Referral Code : ";

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    private String appLinkTinyUrl = "";

    private ProgressDialog Dialog;
    private SharedPrefrences mSharedPref;
    private LoginUser User;

    
	/*@Override
    protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.referall_screen_activity);

		mDeclaration();
	}*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.activity_free_subscription_screen, container, false);
        //setUpViews();
        mDeclaration();

        return parentView;
    }

    private void mDeclaration() {

        mSharedPref = new SharedPrefrences(getActivity());
        Dialog = new ProgressDialog(getActivity());
        ((Button) parentView.findViewById(R.id.btnReferfriendFreeSubscriptionScreen)).setOnClickListener(this);

/*        mBtnFacebookShare = (Button) parentView.findViewById(R.id.btnFacebookShareReferAllScreen);
        mBtnFacebookShare.setTypeface(typeFaceProximaNovaRegular);
        mBtnFacebookShare.setOnClickListener(this);

        mBtnWhatsAppShare = (Button) parentView.findViewById(R.id.btnWhatsAppShareReferAllScreen);
        mBtnWhatsAppShare.setTypeface(typeFaceProximaNovaRegular);
        mBtnWhatsAppShare.setOnClickListener(this);

        mBtnSendSMS_Share = (Button) parentView.findViewById(R.id.btnSendSMSShareReferAllScreen);
        mBtnSendSMS_Share.setTypeface(typeFaceProximaNovaRegular);
        mBtnSendSMS_Share.setOnClickListener(this);

        mBtnMailShare = (Button) parentView.findViewById(R.id.btnMailShareReferAllScreen);
        mBtnMailShare.setTypeface(typeFaceProximaNovaRegular);
        mBtnMailShare.setOnClickListener(this);

        mBtnGooglePlus = (Button) parentView.findViewById(R.id.btnGooglePlusShareReferAllScreen);
        mBtnGooglePlus.setTypeface(typeFaceProximaNovaRegular);
        mBtnGooglePlus.setOnClickListener(this);


        String AndroidId = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);

       *//* TextView deviceID = (TextView) parentView.findViewById(R.id.deviceid_ReferFrdFrag);
        deviceID.setText("Device ID - " + AndroidId);*//*

        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);*/

        User = mSharedPref.getLoginUser();
        if (User != null) {
            ((TextView) parentView.findViewById(R.id.txtReferCodeFreeSubscriptionScreen))
                    .setText(User.getReferralCode());

        }

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

			/*case R.id.btnBackReferAllScreen:
                finish();
				break;*/
            case R.id.btnFacebookShareReferAllScreen:

                Dialog.setMessage("Please wait...");
                Dialog.show();
                shareTutorialonFB();
                break;

            case R.id.btnWhatsAppShareReferAllScreen:
                if (!appInstalledOrNot("com.whatsapp")) {
                    AppUtil.displaySnackBarWithMessage(parentView.findViewById(android.R.id.content),
                            "Whatsapp application is not install in the device.");
                } else {
                    Dialog.setMessage("Please wait...");
                    Dialog.show();
                    Intent whatsAppIntent = new Intent();
                    whatsAppIntent.setAction(Intent.ACTION_SEND);
                    whatsAppIntent.putExtra(Intent.EXTRA_TEXT, strShareMessage);
                    whatsAppIntent.setType("text/plain");

                    // Do not forget to add this to open whatsApp App specifically
                    whatsAppIntent.setPackage("com.whatsapp");
                    startActivityForResult(whatsAppIntent, 10001);
                }

                break;

            case R.id.btnSendSMSShareReferAllScreen:
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body", strShareMessage);
                startActivity(smsIntent);
                break;

            case R.id.btnMailShareReferAllScreen:
                Intent emailintent = new Intent();
                emailintent.setAction(Intent.ACTION_SEND);
                emailintent.setType("message/rfc822");
                emailintent.putExtra(Intent.EXTRA_SUBJECT, "Visual Physics");
                emailintent.putExtra(Intent.EXTRA_TEXT, strShareMessage);
                startActivity(emailintent);
                break;


            case R.id.btnGooglePlusShareReferAllScreen:
                sendInvitation();
                break;
            case R.id.btnReferfriendFreeSubscriptionScreen:

                /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, "Visual Physics has really nice Physics videos. Try full course free for 3 days. Use the code to get 3 more days free. Referral Code : "+
                //mSharedPref.getLoginUser().getReferralCode() + "\nhttps://play.google.com/store/apps/details?id="+getActivity().getPackageName());
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Visual Physics has really nice Physics videos. Try full course free for 3 days. Use the code to get 3 more days free. Referral Code : " +
                        mSharedPref.getLoginUser().getReferralCode() + "\nhttps://goo.gl/XVuXws");

                startActivity(Intent.createChooser(shareIntent, "Visual Physics"));*/

                //shareDeepLink(mSharedPref.getLoginUser().getReferralCode());

                /*Changed to use new methodology for DeepLink*/
                shareDeepLink();

                break;

            default:
                break;
        }
    }

    private void sendInvitation() {
        Intent shareIntent = new PlusShare.Builder(getActivity())
                .setType("text/plain")
                .setText(strShareMessage)
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }

    //Facebook
    public void shareTutorialonFB() {
        try {

			/* Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                     R.drawable.ic_launcher);
			 SharePhoto photo = new SharePhoto.Builder()
		        .setBitmap(icon)
		        .build();
			 SharePhotoContent content = new SharePhotoContent.Builder()
		        .addPhoto(photo)
		        .build();
			 
			 ShareDialog.show(ReferAllScreenActivity.this, content);*/

            if (ShareDialog.canShow(ShareLinkContent.class)) {
                Spanned sharedText = Html.fromHtml("Visual Physics");
                ShareLinkContent linkContent = new ShareLinkContent.Builder()

                        //.setContentTitle("Download App get 100 points FREE!")
//				    		.setContentTitle("Refer a friend and get 3 days Subscription.")
                        .setContentDescription("Refer a friend and get 3 days Subscription.")
//				            .setContentUrl(Uri.parse(""))
//				            .setImageUrl(Uri.parse(""))
                        .build();

                shareDialog.show(linkContent);
            }
            /* SendButton sendButton = (SendButton)findViewById(R.id.fb_share_button);
             sendButton.setShareContent(content);
			 ShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
				
				@Override
				public void onSuccess(Result result) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onError(FacebookException error) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
			});*/
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (Dialog != null) {
            if (Dialog.isShowing()) {
                Dialog.dismiss();
            }
        }

    }

    /***
     * Check if app install or not
     *
     * @param uri
     * @return
     */
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
            ErrorLog.SendErrorReport(e);
        }
        return app_installed;
    }

/*    *//***
     * This will share the deep link
     *
     * @param referralCode
     *//*
    private void shareDeepLink(final String referralCode) {

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://www.visualphysics.nlytn.in/referralCode/" + referralCode))
                .setDynamicLinkDomain("n88nw.app.goo.gl")
                // Open links with this app on Android
                //.setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.visualphysics").build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {

                            try {

                                // Short link created
                                Uri shortLink = task.getResult().getShortLink();
                                Uri flowchartLink = task.getResult().getPreviewLink();

                                String shareMessage = "Hey !! Visual Physics has best videos to learn physics concepts. Check it out. Download and install the app with this link only to get 10 days free trial.\n";

                                //Added to resolve crash by IZISS on 31 July 2018
                                if (isAdded()) {

                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage + shortLink);
                                    startActivity(Intent.createChooser(shareIntent, "Visual Physics"));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } else {
                            task.getException();
                        }
                    }
                });

        *//*DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://www.visualphysics.nlytn.in/referralCode/" + referralCode))
                .setDynamicLinkDomain("https://n88nw.app.goo.gl/")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildDynamicLink();*//*

    }*/

    /***
     * This will share the deep link
     *
     * @param
     */
    private void shareDeepLink() {

        //Added to resolve crash by IZISS on 31 July 2018
        if (isAdded()) {

            DeepLinkGenerator mDeepLinkGenerator = new DeepLinkGenerator(getActivity());
            mDeepLinkGenerator.setCallBack(new OnLinkGenerateListener() {
                @Override
                public void onSuccess(String link) {

                    String shareMessage = "Hey !! Visual Physics has best videos to learn physics concepts. Check it out. Download and install the app with this link only to get 10 days free trial.\n";

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage + link);
                    startActivity(Intent.createChooser(shareIntent, "Visual Physics"));

                }

                @Override
                public void onFail(String error) {


                }
            });
            mDeepLinkGenerator.getDeepLinkForReferralCode();


        }

    }

}
