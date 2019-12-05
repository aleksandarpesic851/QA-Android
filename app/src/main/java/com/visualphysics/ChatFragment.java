/*
package com.visualphysics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import Model.LoginUser;
import Utils.SharedPrefrences;

@SuppressWarnings("All")
public class ChatFragment extends Fragment {

    private View mParent;

    private SharedPrefrences mSharedPrefrences;
    private LoginUser mLoginUser;

    //This is top portion of the script
    private String startHTML = "<html><head></head><body></body></html><script type=\"text/javascript\">\n" +
            "\twindow.$crisp=[];\n" +
            "\t$crisp.push([\"do\", \"chat:open\"]);";

    //This is used to set data in the script
    private String middleHTML = "";

    //This is bottom portion of the script
    private String endHTML = "window.CRISP_WEBSITE_ID=\"" + MyApplication.CRISP_WEBSITE_ID + "\";\n" +
            "\t(function(){\n" +
            "\t\td=document;\n" +
            "\t\ts=d.createElement(\"script\");\n" +
            "\t\ts.src=\"https://client.crisp.chat/l.js\";\n" +
            "\t\ts.async=1;\n" +
            "\t\td.getElementsByTagName(\"head\")[0].appendChild(s);\n" +
            "\t})();\n" +
            "</script>";

    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mParent = inflater.inflate(
                R.layout.fragment_chat, container, false);

        mSharedPrefrences = new SharedPrefrences(getActivity());
        mLoginUser = mSharedPrefrences.getLoginUser();

       */
/* //Set user data
        Crisp.User.setEmail(mLoginUser.getEmail());
        Crisp.User.setNickname(mLoginUser.getFullName());
        Crisp.User.setPhone(mLoginUser.getCellPhone());

        //Custom attributes
        Crisp.Session.setData("SED", mLoginUser.getSubscriptionEndDate());
        Crisp.Session.setData("STUDENT_ID", mLoginUser.getStudentID());*//*


        // Set custom properties on script
        middleHTML =
                "$crisp.push([\"set\", \"session:data\",[[[\"SED\", \"" + mLoginUser.getSubscriptionEndDate() + "\"]]]]);" +
                        "$crisp.push([\"set\", \"session:data\",[[[\"STUDENT_ID\", \"" + mLoginUser.getStudentID() + "\"]]]]);" +
                        "$crisp.push([\"set\", \"user:email\",[\"" + mLoginUser.getEmail() + "\"]]);" +
                        "$crisp.push([\"set\", \"user:nickname\",[\"" + mLoginUser.getFullName() + "\"]]);";

        webView = (WebView) mParent.findViewById(R.id.webView);

        //Set properties
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);

        //Load this data in webview
        webView.loadDataWithBaseURL("file:///android_asset", startHTML + middleHTML + endHTML, "text/html", "UTF-8", null);

        return mParent;
    }

    */
/*@Override
    public void onDestroyView() {
        super.onDestroyView();

        CrispFragment f = (CrispFragment) getActivity().getFragmentManager().findFragmentByTag("crisp_fragment");

        if (f != null)
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();

    }*//*

}
*/
