package com.visualphysics;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by saisasank on 17/12/17.
 */
public class PlaybackSpeedScreen extends BottomSheetDialogFragment implements RecyclerViewClickListener {

    private BottomSheetBehavior mBottomSheetBehavior;

    private List<VideoSpeed> mVideoSpeeds;
    private RecyclerViewClickListener mRecyclerViewClickListener;
    private RecyclerView speedList;

    public static Fragment createNewInstance(List<VideoSpeed> videoSpeeds, RecyclerViewClickListener recyclerViewClickListener) {
        PlaybackSpeedScreen playbackSpeedScreen = new PlaybackSpeedScreen();
        playbackSpeedScreen.mVideoSpeeds = videoSpeeds;
        playbackSpeedScreen.mRecyclerViewClickListener = recyclerViewClickListener;
        return playbackSpeedScreen;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);

                mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                mBottomSheetBehavior.setPeekHeight((int)(300 * (Resources.getSystem().getDisplayMetrics().density)));
            }
        });

        return dialog;

    }


    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.layout_playback_speed, null);
        speedList = (RecyclerView) view.findViewById(R.id.speed_list);

        dialog.setContentView(view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        VideoSpeedAdapter videoSpeedAdapter = new VideoSpeedAdapter(mVideoSpeeds, this);
        speedList.setLayoutManager(linearLayoutManager);
        speedList.setAdapter(videoSpeedAdapter);
    }

    @Override
    public void onItemClick(Object o) {
        if(mRecyclerViewClickListener != null) {
            mRecyclerViewClickListener.onItemClick(null);
        }
        dismiss();
    }

    public static class VideoSpeed {
        private float playbackSpeed;
        private boolean isSelected;

        public VideoSpeed(float playbackSpeed, boolean isSelected) {
            this.playbackSpeed = playbackSpeed;
            this.isSelected = isSelected;
        }

        public float getPlaybackSpeed() {
            return playbackSpeed;
        }

        public void setPlaybackSpeed(float playbackSpeed) {
            this.playbackSpeed = playbackSpeed;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

}
