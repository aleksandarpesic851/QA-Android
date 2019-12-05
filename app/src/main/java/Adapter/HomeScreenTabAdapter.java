package Adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.visualphysics.ChaptersHomeScreenTab;

/**
 * Created by admin on 5/19/2016.
 */
public class HomeScreenTabAdapter extends FragmentStatePagerAdapter {

    int noOfTabs;
    Context mContext;

    public HomeScreenTabAdapter(FragmentManager fm, int noOfTabs, Context context) {
        super(fm);
        this.noOfTabs = noOfTabs;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args;
        switch (position) {
            case 0:
                ChaptersHomeScreenTab chapterTab1 = new ChaptersHomeScreenTab();
                args = new Bundle();
                args.putInt("position", position);
                args.putInt("REQUEST_WRITE_STORAGE", 203);
                chapterTab1.setArguments(args);

                return chapterTab1;
            case 1:
                ChaptersHomeScreenTab chapterTab2 = new ChaptersHomeScreenTab();
                args = new Bundle();
                args.putInt("position", position);
                args.putInt("REQUEST_WRITE_STORAGE", 204);
                chapterTab2.setArguments(args);

                return chapterTab2;
            case 2:
                ChaptersHomeScreenTab chapterTab3 = new ChaptersHomeScreenTab();
                args = new Bundle();
                args.putInt("position", position);
                args.putInt("REQUEST_WRITE_STORAGE", 205);
                chapterTab3.setArguments(args);

                return chapterTab3;
            case 3:
                ChaptersHomeScreenTab chapterTab4 = new ChaptersHomeScreenTab();
                args = new Bundle();
                args.putInt("position", position);
                args.putInt("REQUEST_WRITE_STORAGE", 206);
                chapterTab4.setArguments(args);

                return chapterTab4;
            default:
                return null;
        }

    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        // Generate title based on item position
//        // return tabTitles[position];
//
//        // getDrawable(int i) is deprecated, use getDrawable(int i, Theme theme) for min SDK >=21
//        // or ContextCompat.getDrawable(Context context, int id) if you want support for older versions.
//        // Drawable image = context.getResources().getDrawable(iconIds[position], context.getTheme());
//        // Drawable image = context.getResources().getDrawable(imageResId[position]);
//
//        Drawable image = ContextCompat.getDrawable(mContext, R.drawable.tab1_black);
//        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
//        SpannableString sb = new SpannableString(" ");
//        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
//        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return sb;
//    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}
