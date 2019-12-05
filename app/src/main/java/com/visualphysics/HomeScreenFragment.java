package com.visualphysics;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import Adapter.HomeScreenTabAdapter;

/**
 * Created by admin on 5/19/2016.
 */
public class HomeScreenFragment extends Fragment {

    View mParent;
    View divider;
    TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mParent = inflater.inflate(
                R.layout.listfrag, container, false);


        mDeclaration();

        return mParent;
    }

    void mDeclaration() {

        tabLayout = (TabLayout) mParent.findViewById(R.id.listfragTabLayout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());


        // tabLayout.getTabAt(0).setIcon(R.drawable.tab1_selector);
        View child = getActivity().getLayoutInflater().inflate(R.layout.tab_imageview, null);

        ImageView imgTabImage = (ImageView) child.findViewById(R.id.imgTabImage);
        imgTabImage.setImageResource(R.drawable.tab1_selector);
        tabLayout.getTabAt(0).setCustomView(child);

        tabLayout.getTabAt(0).setTag("Catergory1");
        tabLayout.getTabAt(0).getCustomView().setSelected(true);

        child = getActivity().getLayoutInflater().inflate(R.layout.tab_imageview, null);
        imgTabImage = (ImageView) child.findViewById(R.id.imgTabImage);

        imgTabImage.setImageResource(R.drawable.tab2_selector);
        tabLayout.getTabAt(1).setCustomView(child);
        tabLayout.getTabAt(1).setTag("Catergory2");

        child = getActivity().getLayoutInflater().inflate(R.layout.tab_imageview, null);
        imgTabImage = (ImageView) child.findViewById(R.id.imgTabImage);
        imgTabImage.setImageResource(R.drawable.tab3_selector);
        tabLayout.getTabAt(2).setCustomView(child);
        tabLayout.getTabAt(2).setTag("Catergory2");

        child = getActivity().getLayoutInflater().inflate(R.layout.last_tab_imageview, null);
        imgTabImage = (ImageView) child.findViewById(R.id.imgTabImage);
       
        imgTabImage.setImageResource(R.drawable.tab4_selector);
        tabLayout.getTabAt(3).setCustomView(child);
        tabLayout.getTabAt(3).setTag("Catergory3");


        final ViewPager viewPager = (ViewPager) mParent.findViewById(R.id.ListDragViewPager);

        HomeScreenTabAdapter homePagerAdapter = new HomeScreenTabAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount(), getActivity());
        viewPager.setAdapter(homePagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
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
