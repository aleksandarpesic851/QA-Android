package buy;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.visualphysics.R;

import java.util.ArrayList;

import Model.Packages;

/**
 * Created by admin on 5/19/2016.
 */
@SuppressWarnings("All")
public class BuyFragmentWithTabs extends Fragment {

    private View parentView;
    private TabLayout tabLayoutCurrency;
    private ViewPager viewPagerCurrency;

    private BuyViewPagerAdapter mBuyViewPagerAdapter;
    private ArrayList<Packages> mPackageArrayList;
    private OnPackageSelectionListener mOnPackageSelectionListener;

    public BuyFragmentWithTabs(ArrayList<Packages> mPackageArrayList, OnPackageSelectionListener mOnPackageSelectionListener) {

        this.mPackageArrayList = mPackageArrayList;
        this.mOnPackageSelectionListener = mOnPackageSelectionListener;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.buy_fragment, container, false);

        mDeclaration();

        return parentView;

    }

    /***
     * Initialize the resources
     */
    private void mDeclaration() {

        //To show toast
        //mApiCall = new ApiCall(getActivity());

        tabLayoutCurrency = parentView.findViewById(R.id.tabLayoutCurrency);
        viewPagerCurrency = parentView.findViewById(R.id.viewPagerCurrency);

        //This will create 2 new tabs
        tabLayoutCurrency.addTab(tabLayoutCurrency.newTab());
        tabLayoutCurrency.addTab(tabLayoutCurrency.newTab());

        View child = getActivity().getLayoutInflater().inflate(R.layout.chaptername_customtab_layout, null);
        ((TextView) child.findViewById(R.id.txt_ChapternameCustomTab)).setText(BuyViewPagerAdapter.CURRENCY_TYPE_INR);
        tabLayoutCurrency.getTabAt(0).setCustomView(child);
        tabLayoutCurrency.getTabAt(0).getCustomView().setSelected(true);

        child = getActivity().getLayoutInflater().inflate(R.layout.chaptername_customtab_layout2, null);
        ((TextView) child.findViewById(R.id.txt_ChapternameCustomTab)).setText(BuyViewPagerAdapter.CURRENCY_TYPE_USD);
        tabLayoutCurrency.getTabAt(1).setCustomView(child);

        //set pager adapter
        mBuyViewPagerAdapter = new BuyViewPagerAdapter(getActivity().getSupportFragmentManager(), getActivity(), mPackageArrayList, mOnPackageSelectionListener);
        viewPagerCurrency.setAdapter(mBuyViewPagerAdapter);
        viewPagerCurrency.setOffscreenPageLimit(1);

        //add listener when tab changes
        viewPagerCurrency.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutCurrency));

        tabLayoutCurrency.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPagerCurrency.setCurrentItem(tab.getPosition());

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

}
