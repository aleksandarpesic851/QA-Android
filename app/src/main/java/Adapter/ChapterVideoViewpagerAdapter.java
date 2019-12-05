package Adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.visualphysics.ChapterVideosScreenTabFragment;

/**
 * Created by admin on 5/20/2016.
 */
public class ChapterVideoViewpagerAdapter extends FragmentStatePagerAdapter {

    private int noOfTabs,CategoryID,ChapterID;
    private Context mContext;

    public ChapterVideoViewpagerAdapter(FragmentManager fm, int noOfTabs,
                                        int CategoryID,int ChapterID,Context context) {
        super(fm);
        this.noOfTabs = noOfTabs;
        this.CategoryID = CategoryID;
        this.ChapterID = ChapterID;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args;
        switch (position) {
            case 0:
                ChapterVideosScreenTabFragment theoryTab = new ChapterVideosScreenTabFragment();
                args = new Bundle();
                args.putInt("CategoryID", CategoryID);
                args.putInt("ChapterID", ChapterID);
                args.putString("VideoType", "Theory");
                theoryTab.setArguments(args);
                return theoryTab;
            case 1:
                ChapterVideosScreenTabFragment solvedProblemTab = new ChapterVideosScreenTabFragment();
                args = new Bundle();
                args.putInt("CategoryID", CategoryID);
                args.putInt("ChapterID", ChapterID);
                args.putString("VideoType", "SolvedProblems");
                solvedProblemTab.setArguments(args);
                return solvedProblemTab;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}

