package Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.visualphysics.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Model.AskToExpertDiscusssionList;
import UIControl.ExpandableTextView;
import Utils.AppUtil;

/**
 * Created by India on 7/4/2016.
 */
public class AskToExpertDiscussionList_ListAdpter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<AskToExpertDiscusssionList> mAskToExpertDiscussionArrayList;
    private Context context;
    private AppUtil mAppUtil;


    public AskToExpertDiscussionList_ListAdpter(ArrayList<AskToExpertDiscusssionList> AskToExpertDiscusssionList, Context context)
    {
        this.mAskToExpertDiscussionArrayList=AskToExpertDiscusssionList;
        this.context=context;
        mAppUtil = new AppUtil(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.ask_to_expert_discussion_list_listitem, parent,false);
        AskToExpertDiscussionViewHolder viewHolder = new  AskToExpertDiscussionViewHolder(itemview);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AskToExpertDiscussionViewHolder) {
            AskToExpertDiscussionViewHolder Holder = (AskToExpertDiscussionViewHolder) holder;
            Holder.mTxtStudentName.setText(mAskToExpertDiscussionArrayList.get(position).getFullName());
            Holder.mTxtDate.setText(mAskToExpertDiscussionArrayList.get(position).getResponseDate());
            Holder.mTxtComment.setText(mAskToExpertDiscussionArrayList.get(position).getResponse());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = mAppUtil.convertUTCToLocalTime(simpleDateFormat,
                    mAskToExpertDiscussionArrayList.get(position).getResponseDate());
            Holder.mTxtDate.setText(strDate);
            /*try{
                Date d = new Date(strDate);
                String mydate = simpleDateFormat.format(d);
                Holder.mTxtDate.setText(strDate);
            }
            catch(Exception e){
                e.printStackTrace();
            }*/
        }

    }

    @Override
    public int getItemCount() {
        return mAskToExpertDiscussionArrayList.size();
    }


    static class AskToExpertDiscussionViewHolder extends RecyclerView.ViewHolder
    {
        TextView mTxtStudentName,mTxtDate;
        ExpandableTextView mTxtComment;
        public AskToExpertDiscussionViewHolder(View itemView)
        {
            super(itemView);
            mTxtStudentName = (TextView) itemView.findViewById(R.id.txtStudentNameAskToExpertDiscussionListItem);
            mTxtDate = (TextView) itemView.findViewById(R.id.txtDateAskToExpertDiscussionListItem);
            mTxtComment = (ExpandableTextView) itemView.findViewById(R.id.txtCommentAskToExpertDiscussionListItem);

        }
    }




}

