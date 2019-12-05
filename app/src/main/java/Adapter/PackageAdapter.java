package Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.visualphysics.R;

import java.util.ArrayList;

import Model.Packages;

/**
 * Created by India on 7/13/2016.
 */
public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> implements View.OnClickListener {

    private ArrayList<Packages> mPackageArrayList;
    private Context mContext;
    private RadioButton mPreviousRadioBtn;
    private int previousPos, packageID = 0;

    public PackageAdapter(ArrayList<Packages> PackageArrayList, Context Context) {
        this.mPackageArrayList = PackageArrayList;
        this.mContext = Context;
    }


    @Override
    public PackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.package_list_item, parent, false);
        PackageViewHolder viewHolder = new PackageViewHolder(itemview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PackageViewHolder holder, int position) {
        Packages PackageObj = mPackageArrayList.get(position);

        //holder.mTxtOrignalPrice.setText("Orignal Price : "+PackageObj.getOriginalPrice());
        // holder.mTxtOrignalPrice.setPaintFlags(holder.mTxtOrignalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        // holder.mTxtPeriod.setText("Period : "+PackageObj.getPeriod() + " days");

        //Commentedf by Iziss for New designs
        /*holder.mTxtOfferPrice.setText("Price : INR " + PackageObj.getOfferPrice() + " / USD " + PackageObj.getUSDOfferPrice());
        holder.mRadioBtn.setText(PackageObj.getPackageName() + " ( " + PackageObj.getPeriod() + " Days )");
        holder.mRadioBtn.setChecked(PackageObj.isChecked());
        holder.mRadioBtn.setOnClickListener(this);
        holder.mRadioBtn.setTag(position);*/

        holder.mTxtOfferPrice.setText("Price : INR " + PackageObj.getOfferPrice() + " / USD " + PackageObj.getUSDOfferPrice());
        holder.txtPackageName.setText(PackageObj.getPackageName());
        holder.txtOriginalDays.setText("" + PackageObj.getPeriod());

        if (PackageObj.getOfferPeriod() != null) {

            if (!PackageObj.getPeriod().equalsIgnoreCase(PackageObj.getOfferPeriod())) {

                holder.txtOriginalDays.setVisibility(View.VISIBLE);
                holder.txtOriginalDays.setPaintFlags(holder.txtOriginalDays.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                holder.txtOfferDays.setText("" + PackageObj.getOfferPeriod());
                holder.txtOffer.setVisibility(View.VISIBLE);

            } else {
                holder.txtOriginalDays.setVisibility(View.GONE);
                holder.txtOffer.setVisibility(View.GONE);
                holder.txtOfferDays.setText("" + PackageObj.getPeriod());
            }
        } else {
            holder.txtOriginalDays.setVisibility(View.GONE);
            holder.txtOffer.setVisibility(View.GONE);
            holder.txtOfferDays.setText("" + PackageObj.getPeriod());
        }

        holder.mRadioBtn.setChecked(PackageObj.isChecked());
        holder.mRadioBtn.setOnClickListener(this);
        holder.mRadioBtn.setTag(position);


    }

    @Override
    public int getItemCount() {
        return mPackageArrayList.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rdPackageNamePackageListItem:

                int position = (Integer) v.getTag();

                if (mPreviousRadioBtn != null && mPreviousRadioBtn != v) {
                    mPreviousRadioBtn.setChecked(false);
                    mPackageArrayList.get(previousPos).setChecked(false);
                }
                previousPos = position;
                mPreviousRadioBtn = (RadioButton) v;
                mPackageArrayList.get(position).setChecked(true);
                packageID = mPackageArrayList.get(position).getPackageID();

                break;
        }
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {
        RadioButton mRadioBtn;
        TextView mTxtOrignalPrice, mTxtOfferPrice, mTxtPeriod;

        //Newly added
        TextView txtPackageName, txtOffer, txtOriginalDays, txtOfferDays;

        public PackageViewHolder(View itemView) {
            super(itemView);
            mRadioBtn = (RadioButton) itemView.findViewById(R.id.rdPackageNamePackageListItem);
            mTxtOrignalPrice = (TextView) itemView.findViewById(R.id.txtOriginalPricePackageListItem);
            mTxtOfferPrice = (TextView) itemView.findViewById(R.id.txtOfferPricePackageListItem);
            mTxtPeriod = (TextView) itemView.findViewById(R.id.txtPeriodPackageListItem);

            //New added
            txtPackageName = (TextView) itemView.findViewById(R.id.txtPackageName);
            txtOffer = (TextView) itemView.findViewById(R.id.txtOffer);
            txtOriginalDays = (TextView) itemView.findViewById(R.id.txtOriginalDays);
            txtOfferDays = (TextView) itemView.findViewById(R.id.txtOfferDays);

        }
    }

    /***
     *
     */
    public int getPackageID() {
        return packageID;
    }
}
