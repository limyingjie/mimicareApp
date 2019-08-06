package com.project.mimiCare.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.mimiCare.Adapters.AssignmentRecyclerAdapter;
import com.project.mimiCare.Data.Assignment;
import com.project.mimiCare.LiveActivity;
import com.project.mimiCare.MainActivity;
import com.project.mimiCare.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class AssignmentFragment extends Fragment implements AssignmentRecyclerAdapter.OnClickListener,AddDialog.onInputSelected{
    private static final String TAG = "AssignmentFragment";
    Menu menu;

    //UI
    RecyclerView recyclerView;

    //var
    private ArrayList<Assignment> mAssignments;
    private AssignmentRecyclerAdapter mAssignmentRecyclerAdapter;
    private Random random;
    private Boolean needRefresh, isChanged;
    private View v;
    public AssignmentFragment() {
    }

    // save the fragment and thee bundle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Called");
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment,container,false);
        mAssignments = loadSharedPreferenceData();
        isChanged = false;
        // load from preference data
        if (mAssignments==null){
            initializeText();
            saveSharedPreferenceData();
        }
        else{
            Log.d(TAG, "onCreateView: Called");
            // check if there are expired assignment and remove it
            for (int pos = 0; pos < mAssignments.size();pos++){
                if (mAssignments.get(pos).getDate().compareTo(Calendar.getInstance())<0){
                    mAssignments.remove(pos);
                }
            }
        }
        // set that the Data has just been loaded and does not need refreshed
        needRefresh = false;
        initRecyclerView(view);
        v = view;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // load from preference data if it need refresh
        if (needRefresh){
            Log.d(TAG, "onResume: Called");
            mAssignments = loadSharedPreferenceData();
            // reload the recycleView
            initRecyclerView(v);
        }

        Log.d(TAG, "onStart: Called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Called");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: Called");
        ((MainActivity)getActivity()).changeTitle(R.string.assignment);
        
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.menu_assignment,menu);
    }



    @Override
    public void onPause() {
        // save the data when the fragment is paused
        long start = System.currentTimeMillis();
        // if there are no changes, then do not save the data
        if (isChanged){
            saveSharedPreferenceData();
            isChanged = false;
        }
        long stop = System.currentTimeMillis();
        Log.d(TAG, "onPause: Time Elapsed " + (stop-start));

        // the data needs refresh later
        needRefresh = true;
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Called");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_add:
                add();
                break;
        }
        return true;
    }

    @Override
    public void onClickListener(int position) {
        Log.d(TAG, "onClickListener: Called");
        Log.d(TAG, "onClickListener: " + mAssignments.get(position).getRemainingTime());
        Intent intent = new Intent(getActivity(), LiveActivity.class);
        intent.putExtra("assignment",mAssignments.get(position));
        intent.putExtra("position",position);
        startActivity(intent);
    }

    private void initializeText(){
        mAssignments = new ArrayList<>();
        Log.d(TAG, "initializeText: Called!");
        random = new Random();
        int max = 100;
        int min = 10;
        for (int i = 0; i < 5; i++){
            int num = random.nextInt(max-min)+min;
            mAssignments.add(new Assignment(
                    "Walk",
                    num,
                    random.nextInt(num),
                    randomizeFutureCalendar()
            ));
        }
    }

    private Calendar randomizeFutureCalendar(){
        Random newRandom = new Random();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, newRandom.nextInt(30));
        calendar.add(Calendar.MONTH, newRandom.nextInt(2));
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        return calendar;
    }
    private void insert(Assignment input){
        mAssignments.add(input);
        isChanged = true;
        mAssignmentRecyclerAdapter.notifyItemInserted(mAssignments.size()-1);
    }

    private void remove(int position){
        mAssignments.remove(position);
        isChanged = true;
        Log.d(TAG, "remove: Called");
        mAssignmentRecyclerAdapter.notifyItemRemoved(position);
    }

    private void add(){
        AddDialog addDialog = new AddDialog();
        addDialog.setTargetFragment(AssignmentFragment.this, 1);
        addDialog.show(getFragmentManager(),"AddDialog");
    }

    private void initRecyclerView(View view){
        Log.d(TAG, "initRecyclerView: Called");
        recyclerView = view.findViewById(R.id.recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAssignmentRecyclerAdapter = new AssignmentRecyclerAdapter(mAssignments,this,getActivity());

        // add a divider between items
        /*
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration); */

        // setup the layout and the recycleView
        recyclerView.setLayoutManager(layoutManager);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(mAssignmentRecyclerAdapter);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            remove(viewHolder.getAdapterPosition());
        }
    };

    @Override
    public void sendInput(String title, int target, Calendar day) {
        insert(new Assignment(title,target,0,day));
    }

    private void saveSharedPreferenceData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("mimiCare", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mAssignments);
        editor.putString("assignment",json);
        editor.apply();
    }

    private ArrayList<Assignment> loadSharedPreferenceData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("mimiCare", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("assignment",null);
        Type type = new TypeToken<ArrayList<Assignment>>() {}.getType();
        mAssignments = gson.fromJson(json,type);
        return mAssignments;
    }
}
