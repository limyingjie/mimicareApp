package com.project.miniCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.miniCare.MainActivity;
import com.project.miniCare.R;
import com.project.miniCare.Adapters.TabFragmentAdapter;

public class WalkFragment extends Fragment {
    private static final String TAG = "WalkFragment";

    private TabFragmentAdapter mtabFragmentAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walk,container,false);
    }
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mtabFragmentAdapter = new TabFragmentAdapter(getChildFragmentManager());
        viewPager = getView().findViewById(R.id.tabContainer);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        tabLayout = getView().findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        mtabFragmentAdapter.addFragment(new MainMenuFragment(),"Test");
        mtabFragmentAdapter.addFragment(new FragmentRecord(),"Record");
        mtabFragmentAdapter.addFragment(new DataTerminalFragment(),"Data");
        viewPager.setAdapter(mtabFragmentAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}
