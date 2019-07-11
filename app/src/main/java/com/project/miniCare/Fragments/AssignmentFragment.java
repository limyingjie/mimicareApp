package com.project.miniCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.miniCare.Adapters.AssignmentRecyclerAdapter;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;

import java.util.ArrayList;

public class AssignmentFragment extends Fragment implements AssignmentRecyclerAdapter.OnClickListener {
    private static final String TAG = "AssignmentFragment";
    Menu menu;

    //UI
    RecyclerView recyclerView;

    //var
    private ArrayList<String> mAssignment = new ArrayList<>();
    private AssignmentRecyclerAdapter mAssignmentRecyclerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment,container,false);
        insertText();
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).changeTitle(R.string.assignment);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.menu_assignment,menu);
    }

    @Override
    public void onClickListener(int position) {
        Log.d(TAG, "onClickListener: Called");
    }

    private void insertText(){
        Log.d(TAG, "insertText: Called!");
        for (int i=0; i < 20; i++){
            mAssignment.add("Assignment " + i);
        }
    }

    private void initRecyclerView(View view){
        Log.d(TAG, "initRecyclerView: Called");
        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAssignmentRecyclerAdapter = new AssignmentRecyclerAdapter(mAssignment,this);

        // add a divider between items
        /*
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration); */

        // setup the layout and the recycleView
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAssignmentRecyclerAdapter);

    }
    
}
