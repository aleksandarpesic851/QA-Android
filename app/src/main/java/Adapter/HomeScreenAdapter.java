package Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.visualphysics.R;
import com.visualphysics.ViewVideoScreen;

import java.util.ArrayList;

import Model.HomeScreenFrag_Model;

/**
 * Created by admin on 5/19/2016.
 */
public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.HomeScreenViewHolder>  {

    HomeScreenFrag_Model homeScreenFrag_model;
    ArrayList<HomeScreenFrag_Model> mCategories;
    Context mContext;
    public HomeScreenAdapter(Context context,ArrayList<HomeScreenFrag_Model> categories)
    {
        mContext=context;
        mCategories=categories;
    }
    @Override
    public HomeScreenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.homescreenfrag, null);
        HomeScreenViewHolder viewHolder = new  HomeScreenViewHolder(itemview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HomeScreenViewHolder holder, final int position) {

        holder.mCategoryText.setText(mCategories.get(position).getCategoryName());
        holder.mHomeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCategories.get(position).getCategoryName();

                Intent intent = new Intent(mContext, ViewVideoScreen.class);
                intent.putExtra("Key",mCategories.get(position).getCategoryName());
                mContext.startActivity(intent);


            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    static class HomeScreenViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mHomeScreen;
        TextView mCategoryText;
        public HomeScreenViewHolder(View itemView) {
            super(itemView);

      mHomeScreen=(LinearLayout)itemView.findViewById(R.id.layout_homescreenfrag);

            mCategoryText=(TextView)itemView.findViewById(R.id.categoryTxt_HomeScreenFrag);
        }
    }
}
