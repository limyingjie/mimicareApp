package com.project.miniCare.Fragments;

import android.content.Intent;
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

import com.project.miniCare.Adapters.AssignmentRecyclerAdapter;
import com.project.miniCare.Data.Assignment;
import com.project.miniCare.LiveActivity;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;

import java.util.ArrayList;
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
        mAssignments = new ArrayList<>();
        initializeText();
        initRecyclerView(view);
        return view;
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
        Intent intent = new Intent(getActivity(), LiveActivity.class);
        intent.putExtra("assignment",mAssignments.get(position));
        startActivity(intent);
    }

    private void initializeText(){
        Log.d(TAG, "initializeText: Called!");
        random = new Random();
        int max = 100;
        int min = 10;
        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        for (int i = 0; i < 5; i++){
            int num = random.nextInt(max-min)+min;
            int num_day = random.nextInt(6);
            mAssignments.add(new Assignment(
                    "Walk",
                    num,
                    random.nextInt(num),
                    days[num_day]
            ));
        }
    }

    private void insert(Assignment input){
        mAssignments.add(input);
        mAssignmentRecyclerAdapter.notifyItemInserted(mAssignments.size()-1);
    }

    private void remove(int position){
        mAssignments.remove(position);
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
    public void sendInput(String title, int target, String day) {
        insert(new Assignment(title,target,0,day));
    }
}
