package com.project.miniCare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.project.miniCare.Fragments.AssignmentFragment;
import com.project.miniCare.Fragments.DevicesFragment;
import com.project.miniCare.Fragments.LiveFragment;
import com.project.miniCare.Fragments.UserFragment;
import com.project.miniCare.Fragments.WalkFragment;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    public String device;
    public Toolbar toolbar;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize toolbar and disable toolbar title
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNav =findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


        //possibly use https://github.com/ittianyu/BottomNavigationViewEx instead
        //remove padding in bottom navigation
        removePaddingFromNavigation();


        // code from Simple BLE
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment,new DevicesFragment()).commit();
        else
            onBackStackChanged();
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

    public void goLive(View view){
        Bundle args = new Bundle();
        args.putString("device", device);
        Fragment fragment = new LiveFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "live").addToBackStack(null).commit();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    switch (menuItem.getItemId()){
                        case R.id.item_scan:
                            selectedFragment = new DevicesFragment();
                            break;
                        case R.id.item_walk:
                            selectedFragment = new WalkFragment();
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

    private void removePaddingFromNavigation(){
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNav.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
            View activeLabel = item.findViewById(R.id.largeLabel);
            if (activeLabel instanceof TextView) {
                activeLabel.setPadding(0, 0, 0, 0);
            }
        }
    }
}
