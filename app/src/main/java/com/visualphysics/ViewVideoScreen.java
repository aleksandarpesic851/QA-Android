package com.visualphysics;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import Model.HomeScreenFrag_Model;

public class ViewVideoScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video_screen);

        Bundle bundle = getIntent().getExtras();


        String stuff = bundle.getString("Key");
        getSupportActionBar().setTitle(stuff);
    }

}
