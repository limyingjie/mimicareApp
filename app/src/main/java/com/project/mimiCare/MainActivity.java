package com.project.mimiCare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.project.mimiCare.Fragments.AssignmentFragment;
import com.project.mimiCare.Fragments.DevicesFragment;
import com.project.mimiCare.Fragments.MainMenuFragment;
import com.project.mimiCare.Fragments.UserFragment;
import com.project.mimiCare.Fragments.WalkFragment;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private static final String TAG = "MainActivity";
    public String device;
    public Toolbar toolbar;
    public BottomNavigationViewEx bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize toolbar so that it can be called in other fragment and remove the Actionbar title
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNav =findViewById(R.id.bottom_navigation);
        initBottomNavigationViewEx();
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // code from Simple BLE
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new MainMenuFragment()).commit();
        else
            onBackStackChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Called");
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    switch (menuItem.getItemId()){
                        case R.id.item_walk:
                            selectedFragment = new MainMenuFragment();
                            break;
                        case R.id.item_user:
                            selectedFragment = new UserFragment();
                            break;
                        case R.id.item_goal:
                            selectedFragment = new AssignmentFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment,selectedFragment).commit();
                    return true;
                }
            };
    public void changeTitle(int title){
        TextView textView = findViewById(R.id.toolbarTitle);
        switch (title){
            case R.string.user:
                textView.setGravity(Gravity.START);
                textView.setText(R.string.user);
                break;
            case R.string.app_name:
                textView.setGravity(Gravity.START);
                textView.setText(R.string.app_name);
                break;
            case R.string.assignment:
                textView.setGravity(Gravity.CENTER);
                textView.setText(R.string.assignment);
                break;
        }

    }

    private void initBottomNavigationViewEx(){
        bottomNav.enableAnimation(false);
        bottomNav.enableItemShiftingMode(false);
        bottomNav.enableShiftingMode(false);
        bottomNav.setTextVisibility(true);
    }
}