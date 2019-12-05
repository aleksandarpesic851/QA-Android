package com.visualphysics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by admin on 5/20/2016.
 */
public class SolvedProblemsFrag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.recyclerview_chapternamescreen, container, false);

       /* ArrayList<String> strings = new ArrayList<>();
        strings.add("HI");
        strings.add("DJDJD");
        strings.add("KLSD");
        strings.add("KDKSLD");
        strings.add("sldkfdf");
        strings.add("dfdfdf");

        java.util.List<Videos> videos;
        DataBase database = new DataBase(getActivity());
        videos = database.doGetVideosList("SolvedProblems");




        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_chapternamescreen);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 3. create an adapter
        ChapterVideoListAdapter mAdapter = new ChapterVideoListAdapter(videos,
                getActivity());
        // 4. set adapter
        recyclerView.setAdapter(mAdapter);*/


        return rootView;
    }

}

