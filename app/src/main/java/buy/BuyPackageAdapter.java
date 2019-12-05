package buy;

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
public class BuyPackageAdapter extends RecyclerView.Adapter<BuyPackageAdapter.PackageViewHolder> implements View.OnClickListener {

    private ArrayList<Packages> mPackageArrayList;
    private Context mContext;
    private RadioButton mPreviousRadioBtn;
    private int previousPos, packageID = 0;

    private String currencyType;
    private OnPackageSelectionListener mOnPackageSelectionListener;
    private OnPackageSelectionListener mOnPackageSelectionListenerFragment;

    public BuyPackageAdapter(ArrayList<Packages> PackageArrayList, Context Context, String currencyType, OnPackageSelectionListener mOnPackageSelectionListener, OnPackageSelectionListener mOnPackageSelectionListenerFragment) {

        this.mPackageArrayList = PackageArrayList;
        this.mContext = Context;
        this.currencyType = currencyType;
        this.mOnPackageSelectionListener = mOnPackageSelectionListener;
        this.mOnPackageSelectionListenerFragment = mOnPackageSelectionListenerFragment;
    }


    @Override
    public PackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_buy_package, parent, false);
        PackageViewHolder viewHolder = new PackageViewHolder(itemview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PackageViewHolder holder, int position) {

        Packages PackageObj = mPackageArrayList.get(position);

        holder.txtCurrency.setText(currencyType);

        if (currencyType.equalsIgnoreCase(BuyViewPagerAdapter.CURRENCY_TYPE_INR)) {
            holder.mTxtOrignalPrice.setText(PackageObj.getOriginalPrice());
            holder.mTxtOfferPrice.setText(PackageObj.getOfferPrice());
            holder.txtPercentage.setText(getPercentageDiscount(PackageObj, true));

        } else {

            if (!PackageObj.getOriginalUSDPrice().equalsIgnoreCase("0")) {
                holder.mTxtOrignalPrice.setText(PackageObj.getOriginalUSDPrice());
            } else {
                holder.mTxtOrignalPrice.setText(PackageObj.getUSDOfferPrice());
            }

            holder.mTxtOfferPrice.setText(PackageObj.getUSDOfferPrice());
            holder.txtPercentage.setText(getPercentageDiscount(PackageObj, false));
        }


        holder.mTxtOrignalPrice.setPaintFlags(holder.txtOriginalDays.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        //holder.mTxtOfferPrice.setText("Price : INR " + PackageObj.getOfferPrice() + " / USD " + PackageObj.getUSDOfferPrice());
        holder.txtPackageName.setText(PackageObj.getPackageName());
        holder.txtOriginalDays.setText("" + PackageObj.getPeriod());

        if (PackageObj.getOfferPeriod() != null) {

            if (!PackageObj.getPeriod().equalsIgnoreCase(PackageObj.getOfferPeriod())) {

                holder.txtOriginalDays.setVisibility(View.VISIBLE);
                holder.txtOriginalDays.setPaintFlags(holder.txtOriginalDays.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                holder.txtOfferDays.setText("" + PackageObj.getOfferPeriod());
                // holder.txtOffer.setVisibility(View.VISIBLE);

            } else {
                holder.txtOriginalDays.setVisibility(View.GONE);
                // holder.txtOffer.setVisibility(View.GONE);
                holder.txtOfferDays.setText("" + PackageObj.getPeriod());
            }
        } else {
            holder.txtOriginalDays.setVisibility(View.GONE);
            //holder.txtOffer.setVisibility(View.GONE);
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

                mOnPackageSelectionListener.onPackageSelected(mPackageArrayList.get(position), position, currencyType);
                mOnPackageSelectionListenerFragment.onPackageSelected(mPackageArrayList.get(position), position, currencyType);

                break;
        }
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {

        RadioButton mRadioBtn;
        TextView mTxtOrignalPrice, mTxtOfferPrice, mTxtPeriod;

        //Newly added
        TextView txtPackageName, txtOffer, txtOriginalDays, txtOfferDays, txtCurrency, txtPercentage;

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
            txtCurrency = (TextView) itemView.findViewById(R.id.txtCurrency);
            txtPercentage = (TextView) itemView.findViewById(R.id.txtPercentage);

        }
    }

    /**
     */
    public int getPackageID() {
        return packageID;
    }


    /***
     * This will return discount percentage
     *
     * @param mPackages
     * @return
     */
    private String getPercentageDiscount(Packages mPackages, boolean isINR) {

        float actualPrice = 0.0f;
        float offerPrice = 0.0f;

        if (isINR) {

            actualPrice = Integer.parseInt(mPackages.getOriginalPrice());
            offerPrice = Integer.parseInt(mPackages.getOfferPrice());

        } else {

            actualPrice = Integer.parseInt(mPackages.getOriginalUSDPrice());
            offerPrice = Integer.parseInt(mPackages.getUSDOfferPrice());

        }

        if (actualPrice > offerPrice) {

            float totalDiscountPercentage = ((actualPrice - offerPrice) / actualPrice) * 100;

            return "" + ((int) totalDiscountPercentage) + "% OFF";

        } else {

            return "0% OFF";

        }

    }

    /**
     * This will refresh the list data
     */
    public void refreshList() {

        if (mPackageArrayList != null) {

            for (int i = 0; i < mPackageArrayList.size(); i++) {
                mPackageArrayList.get(i).setChecked(false);
            }
        }

        notifyDataSetChanged();

    }
}
