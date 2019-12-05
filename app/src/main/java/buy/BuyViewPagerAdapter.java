package buy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.visualphysics.ChapterVideosScreenTabFragment;

import java.util.ArrayList;

import Model.Packages;

/**
 * Created by admin on 5/20/2016.
 */
public class BuyViewPagerAdapter extends FragmentStatePagerAdapter {

    //INR and USD
    private int noOfTabs = 2;
    private Context mContext;

    public static String KEY_CURRENCY_TYPE = "CurrencyType";
    public static String CURRENCY_TYPE_INR = "INR";
    public static String CURRENCY_TYPE_USD = "USD";

    private ArrayList<Packages> mPackageArrayList;
    private OnPackageSelectionListener mOnPackageSelectionListener;

    public BuyViewPagerAdapter(FragmentManager fm, Context context, ArrayList<Packages> mPackageArrayList, OnPackageSelectionListener mOnPackageSelectionListener) {
        super(fm);
        this.mContext = context;
        this.mPackageArrayList = mPackageArrayList;
        this.mOnPackageSelectionListener = mOnPackageSelectionListener;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle args = new Bundle();

        switch (position) {
            case 0:

                BuyTabFragment inrFragment = new BuyTabFragment(mPackageArrayList, CURRENCY_TYPE_INR, mOnPackageSelectionListener);
                inrFragment.setArguments(args);
                return inrFragment;

            case 1:

                BuyTabFragment usdFragment = new BuyTabFragment(mPackageArrayList, CURRENCY_TYPE_USD, mOnPackageSelectionListener);
                usdFragment.setArguments(args);
                return usdFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}

