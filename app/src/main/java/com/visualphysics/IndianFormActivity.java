package com.visualphysics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import java.nio.charset.Charset;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import UIControl.CustomTextViewRegular;
import fragment.Form1Fragment;
import fragment.Questionnaire1Fragment;
import mixpanel.MixPanelClass;
import zoho.ZohoUtils;

public class IndianFormActivity extends AppCompatActivity {


    public static boolean isReferralCodeApplied = false;
    public static String strReferralCode = "";
    public CustomTextViewRegular mActionBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_indian_form);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        android.support.v7.app.ActionBar.LayoutParams layoutParams = new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT,
                android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        View view = LayoutInflater.from(this).inflate(R.layout.actionbar_title, null);
        mActionBarTitle = (CustomTextViewRegular) view.findViewById(R.id.txtActionBarHeader);

        mActionBarTitle.setGravity(Gravity.CENTER);
        mActionBarTitle.setTypeface(mActionBarTitle.getTypeface(), Typeface.BOLD);
        getSupportActionBar().setCustomView(view, layoutParams);

        mDeclaration();

        getSupportFragmentManager().beginTransaction().replace(R.id.llQuestionnaire, new Questionnaire1Fragment()).commit();

        //Hide Zoho Chat
        ZohoUtils.hideZohoChat();

        //Check if the screen is appeared once and the app is closed
        MixPanelClass mixPanelClass = new MixPanelClass(this);
        mixPanelClass.setPref(MixPanelClass.PREF_IS_QUESTIONNAIRE_COMPLETED, false);
    }

    /**
     * Declaration
     */
    private void mDeclaration() {

        //Check whether a referral code is applied on previous screen
        if (getIntent().hasExtra("IS_REFERRAL_CODE_APPLIED")) {

            isReferralCodeApplied = getIntent().getBooleanExtra("IS_REFERRAL_CODE_APPLIED", false);
            strReferralCode = getIntent().getStringExtra("REFERRAL_CODE");
        }

    }
}
