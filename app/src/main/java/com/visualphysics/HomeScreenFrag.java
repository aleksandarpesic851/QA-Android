package com.visualphysics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import Adapter.HomeScreenAdapter;
import Model.HomeScreenFrag_Model;

/**
 * Created by admin on 5/19/2016.
 */
public class HomeScreenFrag extends Fragment {

    public static ArrayList<HomeScreenFrag_Model.FrictionCategory> frictionCategories;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.recyclerview_homescreenfrag, container, false);


        ArrayList<HomeScreenFrag_Model> category = new ArrayList<HomeScreenFrag_Model>();
        category.add(new HomeScreenFrag_Model(1,"Friction"));
        category.add(new HomeScreenFrag_Model(2,"Fluids"));
        category.add(new HomeScreenFrag_Model(3,"Sound"));
        category.add(new HomeScreenFrag_Model(4,"Elasticity"));
        category.add(new HomeScreenFrag_Model(5,"Conductors"));
        category.add(new HomeScreenFrag_Model(6,"Gravitational"));

        frictionCategories = new ArrayList<HomeScreenFrag_Model.FrictionCategory>();
        frictionCategories.add(new HomeScreenFrag_Model.FrictionCategory(1,"Friction1"));
        frictionCategories.add(new HomeScreenFrag_Model.FrictionCategory(2,"Friction2"));
        frictionCategories.add(new HomeScreenFrag_Model.FrictionCategory(3,"Friction3"));

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_homescreenfrag);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        // 3. create an adapter
        HomeScreenAdapter mAdapter = new HomeScreenAdapter(getActivity(),category);
        // 4. set adapter
        recyclerView.setAdapter(mAdapter);
        // 5. set item animator to DefaultAnimator

        return rootView;
    }


}
