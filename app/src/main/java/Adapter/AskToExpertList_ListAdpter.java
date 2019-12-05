package Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.visualphysics.AskToExpertDiscussionListScreenActivity;
import com.visualphysics.R;

import java.util.ArrayList;

import Model.AskToExpertList;
import UIControl.ExpandableTextView;
import Utils.OnLoadMoreListener;

/**
 * Created by India on 7/4/2016.
 */
public class AskToExpertList_ListAdpter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<AskToExpertList> mAskToExpertArrayList;
    private Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;


    private OnLoadMoreListener mOnLoadMoreListener;
    private RecyclerView recyclerView;


    public AskToExpertList_ListAdpter(ArrayList<AskToExpertList> AskToExpertArrayList, Context context,
                                      RecyclerView recyclerView)
    {
        this.mAskToExpertArrayList=AskToExpertArrayList;
        this.context=context;
        this.recyclerView = recyclerView;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.ask_to_expert_list_listitem, parent,false);
            AskToExpertViewHolder viewHolder = new  AskToExpertViewHolder(itemview);
            return viewHolder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_more_data_footer, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        if (holder instanceof AskToExpertViewHolder) {
            AskToExpertViewHolder Holder = (AskToExpertViewHolder) holder;
            Holder.mTxtComment.setText(mAskToExpertArrayList.get(position).getMessage());
            Holder.mTxtTitle.setText(mAskToExpertArrayList.get(position).getTitle());
            Holder.mTxtStudentName.setText(mAskToExpertArrayList.get(position).getFullName());
            Holder.mTxtDate.setText(mAskToExpertArrayList.get(position).getDate());
            Holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, AskToExpertDiscussionListScreenActivity.class);
                    i.putExtra("AskToExpertID", mAskToExpertArrayList.get(position).getAskToExpertID());
                    i.putExtra("TitleName", mAskToExpertArrayList.get(position).getTitle());
                    context.startActivity(i);
                }
            });
        }
        else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {

        return mAskToExpertArrayList == null ? 0 : mAskToExpertArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mAskToExpertArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    static class AskToExpertViewHolder extends RecyclerView.ViewHolder
    {
        TextView mTxtTitle,mTxtStudentName,mTxtDate;
        ExpandableTextView mTxtComment;
        LinearLayout mLinearLayout;
        public AskToExpertViewHolder(View itemView)
        {
            super(itemView);
            mTxtComment = (ExpandableTextView) itemView.findViewById(R.id.txtCommentAskToExpertListItem);
            mTxtTitle = (TextView) itemView.findViewById(R.id.txtTitleAskToExpertListItem);
            mTxtStudentName = (TextView) itemView.findViewById(R.id.txtStudentNameAskToExpertListItem);
            mTxtDate = (TextView) itemView.findViewById(R.id.txtDateAskToExpertListItem);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.lLayoutAskToExpertListItem);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }
    public void setLoaded() {
        isLoading = false;
    }

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = recyclerView.indexOfChild(v);
            Intent i = new Intent(context, AskToExpertDiscussionListScreenActivity.class);
            i.putExtra("AskToExpertID", mAskToExpertArrayList.get(position).getAskToExpertID());
            i.putExtra("TitleName", mAskToExpertArrayList.get(position).getTitle());
            context.startActivity(i);
        }
    }
}

