package com.visualphysics;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


/**
 * Created by saisasank on 17/12/17.
 */
public class VideoSpeedAdapter extends RecyclerView.Adapter<VideoSpeedAdapter.VideoSpeedViewHolder> {

    private List<PlaybackSpeedScreen.VideoSpeed> videoSpeeds;
    private RecyclerViewClickListener mRecyclerViewClickListener;

    public VideoSpeedAdapter(List<PlaybackSpeedScreen.VideoSpeed> videoSpeeds, RecyclerViewClickListener recyclerViewClickListener) {
        this.videoSpeeds = videoSpeeds;
        mRecyclerViewClickListener = recyclerViewClickListener;
    }

    @Override
    public VideoSpeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playback_speed, parent, false);
        return new VideoSpeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoSpeedViewHolder holder, final int position) {
        final PlaybackSpeedScreen.VideoSpeed videoSpeed = videoSpeeds.get(position);
        if(videoSpeed.isSelected()) {
            holder.doneImage.setVisibility(View.VISIBLE);
        } else {
            holder.doneImage.setVisibility(View.INVISIBLE);
        }

        holder.speedText.setText(videoSpeed.getPlaybackSpeed() + "x");

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!videoSpeed.isSelected()) {
                    videoSpeeds.get(position).setSelected(true);
                }
                for(int i = 0; i < (videoSpeeds.size()); i++) {
                    if(i != position) {
                        videoSpeeds.get(i).setSelected(false);
                    }
                }
                mRecyclerViewClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoSpeeds.size();
    }

    public static class VideoSpeedViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout parentLayout;
        public ImageView doneImage;
        public TextView speedText;

        public VideoSpeedViewHolder(View itemView) {
            super(itemView);

            parentLayout = (RelativeLayout) itemView.findViewById(R.id.parent);
            doneImage = (ImageView) itemView.findViewById(R.id.done_image);
            speedText = (TextView) itemView.findViewById(R.id.speed_text);
        }
    }
}
