package com.visualphysics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by admin on 5/23/2016.
 */
public class Tab4 extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.tab1fragment_recyclerview, container, false);


/*        ArrayList<String> strings = new ArrayList<>();
        strings.add("HI");
        strings.add("DJDJD");
        strings.add("KLSD");
        strings.add("KDKSLD");
        strings.add("sldkfdf");
        strings.add("dfdfdf");

        java.util.List<Chapters> chapters ;
        DataBase database= new DataBase(getActivity());
        chapters=database.doGetCategoryChapterList("4");


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_tab1Frag);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 3. create an adapter
        ChapterTabAapter mAdapter = new ChapterTabAapter(chapters,
                getActivity(),4);
        // 4. set adapter
        recyclerView.setAdapter(mAdapter);
*/

        return rootView;
    }
}