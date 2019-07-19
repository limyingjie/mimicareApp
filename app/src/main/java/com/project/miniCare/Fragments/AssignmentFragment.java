package com.project.miniCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.project.miniCare.Adapters.AssignmentRecyclerAdapter;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;

import java.util.ArrayList;

public class AssignmentFragment extends Fragment implements AssignmentRecyclerAdapter.OnClickListener, AddDialog.onInputSelected {
    private static final String TAG = "AssignmentFragment";
    Menu menu;

    //UI
    RecyclerView recyclerView;

    //var
    private ArrayList<String> mAssignment = new ArrayList<>();
    private AssignmentRecyclerAdapter mAssignmentRecyclerAdapter;

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
            case R.id.item_delete:
                remove(mAssignmentRecyclerAdapter.getSelectedPosition());
        }
        return true;
    }

    @Override
    public void onClickListener(int position) {
        Log.d(TAG, "onClickListener: Called");
    }

    private void initializeText(){
        Log.d(TAG, "initializeText: Called!");
        if (mAssignment.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                mAssignment.add("Assignment " + i);
            }
        }
    }

    private void insert(String input){
        mAssignment.add(input);
        mAssignmentRecyclerAdapter.notifyItemInserted(mAssignment.size()-1);
    }

    private void remove(int position){
        mAssignment.remove(position);
        SparseBooleanArray sparseBooleanArray = mAssignmentRecyclerAdapter.getSelectedPosition();
        sparseBooleanArray.delete(position);
        Log.d("AssignmentRecyclerAdapt", "get "+sparseBooleanArray);
        // empty sparseBooleanArray
        SparseBooleanArray outputArray = new SparseBooleanArray();
        // rearrange the array
        for (int i = 0; i < sparseBooleanArray.size(); i++){
            int key = sparseBooleanArray.keyAt(i);
            // -1 position to all key > position
            if (key > position){
                outputArray.put(key-1,true);
            }
            else{
                outputArray.put(key,true);
            }
        }
        Log.d("AssignmentRecyclerAdapt", "set "+outputArray);
        mAssignmentRecyclerAdapter.setSelectedPosition(outputArray);
        mAssignmentRecyclerAdapter.notifyItemRemoved(position);
    }

    private void remove(SparseBooleanArray sparseBooleanArray){
        if (sparseBooleanArray.size() == 0) {
            Toast.makeText(getActivity(),"No item is selected",Toast.LENGTH_SHORT).show();
        }
        else {
            // remove from the back so that the array does not get error
            for (int i = sparseBooleanArray.size()-1; i >= 0; i--) {
                int key = sparseBooleanArray.keyAt(i);
                Log.d("AssignmentRecyclerAdapt", "remove: "+key);
                mAssignment.remove(key);
            }
            sparseBooleanArray.clear();
            mAssignmentRecyclerAdapter.setSelectedPosition(sparseBooleanArray);
            mAssignmentRecyclerAdapter.notifyDataSetChanged();
        }
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
        mAssignmentRecyclerAdapter = new AssignmentRecyclerAdapter(mAssignment,this,getActivity());

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
    public void sendInput(String input) {
        insert(input);
    }
}
