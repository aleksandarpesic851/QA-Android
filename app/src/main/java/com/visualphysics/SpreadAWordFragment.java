package com.visualphysics;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.LikeView;
import com.facebook.share.widget.ShareDialog;

import java.util.Objects;

import Utils.AppUtil;
import Utils.ErrorLog;

public class SpreadAWordFragment extends Fragment implements View.OnClickListener {
    CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private ProgressDialog progressDialog;
    private String strShareMessage = "Hey !! Visual Physics has best videos to learn physics concepts.\nCheck it out ";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.spread_a_word, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ivShareFacebook).setOnClickListener(this);
        view.findViewById(R.id.ivShareTwitter).setOnClickListener(this);
        view.findViewById(R.id.ivShareWhatsapp).setOnClickListener(this);
        view.findViewById(R.id.ivShare).setOnClickListener(this);
        view.findViewById(R.id.ivConnectFacebook).setOnClickListener(this);
        view.findViewById(R.id.ivConnectTwitter).setOnClickListener(this);
        view.findViewById(R.id.ivConnectYoutube).setOnClickListener(this);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        progressDialog = new ProgressDialog(getActivity());
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                ErrorLog.saveErrorLog("Successful Post on Facebook"+ result.getPostId());
            }

            @Override
            public void onCancel() {
                ErrorLog.saveErrorLog("User canceled Post on Facebook");
            }

            @Override
            public void onError(FacebookException error) {
                ErrorLog.SaveErrorLog(error);
            }
        });
    }

    //Facebook
    public void shareFB() {
        try {
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setQuote(strShareMessage)
                        .setContentUrl(Uri.parse("https://www.nlytn.in"))
                        .build();
                shareDialog.show(linkContent, ShareDialog.Mode.WEB);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivShareFacebook:
                shareFB();
                break;
            case R.id.ivShareTwitter:
                Intent twitterShareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=Hey%20!!%20Check-out%20this%20course.&tw_p=tweetbutton&url=https%3A%2F%2Fwww.nlytn.in&via=visual_physics"));
                startActivity(twitterShareIntent);
                break;
            case R.id.ivShareWhatsapp:
                progressDialog.setMessage(getContext().getString(R.string.progress_bar_message));
                progressDialog.show();
                if (!AppUtil.appInstalledOrNot("com.whatsapp", Objects.requireNonNull(getContext()))) {
                    cancelProgressBar();
                    AppUtil.displaySnackBarWithMessage(v,
                            getContext().getString(R.string.whatsapp_not_install));
                } else {
                    Intent whatsAppIntent = new Intent();
                    whatsAppIntent.setAction(Intent.ACTION_SEND);
                    whatsAppIntent.putExtra(android.content.Intent.EXTRA_TEXT, strShareMessage);
                    whatsAppIntent.setType("text/plain");
                    // Do not forget to add this to open whatsApp App specifically
                    whatsAppIntent.setPackage("com.whatsapp");
                    startActivityForResult(whatsAppIntent, 10001);
                }
                break;
            case R.id.ivShare:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, strShareMessage);
                shareIntent.setType("text/plain");
                startActivityForResult(Intent.createChooser(shareIntent, "Spread A Word about Visual Physics"), 10002);
                break;
            case R.id.ivConnectFacebook:
                Intent facebookConnectIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/plugins/error/confirm/page?iframe_referer&kid_directed_site=false&secure=true&plugin=page&return_params={\"href\":\"https://www.facebook.com/VisualPhysics/\",\"tabs\":\"timeline\",\"width\":\"340\",\"height\":\"500\",\"small_header\":\"false\",\"adapt_container_width\":\"true\",\"hide_cover\":\"false\",\"show_facepile\":\"true\",\"appId\":null,\"ret\":\"sentry\",\"act\":null}"));
                try {
                    startActivity(facebookConnectIntent);
                } catch (ActivityNotFoundException ex) {
                    cancelProgressBar();
                    throw ex;
                }
                break;
            case R.id.ivConnectTwitter:
                Intent twitterConnectIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/follow?screen_name=visual_physics&tw_p=followbutton"));
                try {
                    startActivity(twitterConnectIntent);
                } catch (ActivityNotFoundException ex) {
                    cancelProgressBar();
                    AppUtil.displaySnackBarWithMessage(v,
                            getContext().getString(R.string.twitter_follow_error));
                    throw ex;
                }
                break;
            case R.id.ivConnectYoutube:
                Intent youtubeConnectIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/user/Visueaz/featured"));
                try {
                    startActivity(youtubeConnectIntent);
                } catch (ActivityNotFoundException ex) {
                    cancelProgressBar();
                    AppUtil.displaySnackBarWithMessage(v,
                            getContext().getString(R.string.youtube_post_error));
                }
                break;
        }
    }

    private void cancelProgressBar() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cancelProgressBar();
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
