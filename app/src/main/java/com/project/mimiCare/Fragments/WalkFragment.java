package com.project.mimiCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mimiCare.MainActivity;
import com.project.mimiCare.R;
import com.project.mimiCare.Adapters.TabFragmentAdapter;

public class WalkFragment extends Fragment{
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
        ((MainActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mtabFragmentAdapter = new TabFragmentAdapter(getChildFragmentManager());
        viewPager = getView().findViewById(R.id.tabContainer);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                Log.d(TAG, "onPageSelected: " + i);
                // 0 is Test, 1 is Terminal
                if (i == 0){
                    // start the thread
                }
                else if(i==1){
                    // stop the thread
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setupViewPager(viewPager);

        tabLayout = getView().findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        mtabFragmentAdapter.addFragment(new MainMenuFragment(),"Test");
        mtabFragmentAdapter.addFragment(new DataTerminalFragment(),"Data");
        viewPager.setAdapter(mtabFragmentAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).getSupportActionBar().show();
    }
}
