package com.project.miniCare.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.miniCare.R;

import java.util.ArrayList;
// https://proandroiddev.com/recyclerview-pro-tips-part-1-8a291594bafc tips
public class AssignmentRecyclerAdapter extends RecyclerView.Adapter<AssignmentRecyclerAdapter.ViewHolder>{
    private static final String TAG = "AssignmentRecyclerAdapt";
    private ArrayList<String> mAssignment;
    private OnClickListener onClickListener;

    public AssignmentRecyclerAdapter(ArrayList<String> mAssignment, OnClickListener onClickListener ) {
        this.mAssignment = mAssignment;
        this.onClickListener = onClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // recycling the viewHolder and put them in the position they are supposed to be
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.assignment_list_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view,onClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called.");

        viewHolder.assignment.setText(mAssignment.get(i));
    }

    @Override
    public int getItemCount() {
        return mAssignment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        OnClickListener onClickListener;
        TextView assignment;
        public ViewHolder(@NonNull View itemView, OnClickListener onClickListener) {
            super(itemView);
            Log.d(TAG, "ViewHolder: Called");
            assignment = itemView.findViewById(R.id.assigment_list1);
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            onClickListener.onClickListener(getAdapterPosition());
        }
    }
    public interface OnClickListener{
        void onClickListener(int position);
    }
}
