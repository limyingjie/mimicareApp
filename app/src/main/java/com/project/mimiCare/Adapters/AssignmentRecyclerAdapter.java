package com.project.mimiCare.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.mimiCare.Data.Assignment;
import com.project.mimiCare.R;

import java.util.ArrayList;
// https://proandroiddev.com/recyclerview-pro-tips-part-1-8a291594bafc tips
public class AssignmentRecyclerAdapter extends RecyclerView.Adapter<AssignmentRecyclerAdapter.ViewHolder>{
    private static final String TAG = "AssignmentRecyclerAdapt";
    private ArrayList<Assignment> mAssignment;
    private OnClickListener onClickListener;
    private Context context;

    public AssignmentRecyclerAdapter(ArrayList<Assignment> mAssignment, OnClickListener onClickListener,Context context) {
        Log.d(TAG, "AssignmentRecyclerAdapter: Called");
        this.mAssignment = mAssignment;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreateViewHolder: Called");
        // recycling the viewHolder and put them in the position they are supposed to be
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.assignment_list_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view,onClickListener,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called.");
        Assignment assignment_data = mAssignment.get(i);
        viewHolder.assignment_title.setText(assignment_data.getName());
        viewHolder.assignment_progress.setMax(assignment_data.getTarget());
        viewHolder.assignment_progress.setProgress(assignment_data.getCurrent());
        if (assignment_data.isDone()){
            viewHolder.assignment_date.setText("Done");
            viewHolder.cardView.setBackgroundColor(context.getResources().getColor(R.color.colorLightGrey));
        }
        else{
            viewHolder.assignment_date.setText(String.format(context.getResources().getString(R.string.date),
                    assignment_data.getRemainingTime()));
            if (assignment_data.isLate()){
                viewHolder.cardView.setBackgroundColor(context.getResources().getColor(R.color.colorLightGrey));
            }
        }
        viewHolder.assignment_score.setText(String.format("Score: %d",assignment_data.getScore()));
        String step = context.getString(R.string.step);
        step = String.format(step,assignment_data.getCurrent(),assignment_data.getTarget());
        viewHolder.assignment_step.setText(step);
    }
    @Override
    public int getItemCount() {
        return mAssignment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MaterialCardView cardView;
        OnClickListener onClickListener;
        TextView assignment_title, assignment_date, assignment_step, assignment_score;
        ProgressBar assignment_progress;
        Context context;
        LinearLayout layout;
        public ViewHolder(@NonNull View itemView, OnClickListener onClickListener, Context context) {
            super(itemView);
            Log.d(TAG, "ViewHolder: Called");
            assignment_title = itemView.findViewById(R.id.assigment_title);
            assignment_date = itemView.findViewById(R.id.assignment_date);
            assignment_progress = itemView.findViewById(R.id.assignment_progress);
            assignment_step = itemView.findViewById(R.id.assignment_step);
            assignment_score = itemView.findViewById(R.id.assignment_score);
            cardView = itemView.findViewById(R.id.assignment_parent);
            this.context = context;
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: "+ getAdapterPosition());
            onClickListener.onClickListener(getAdapterPosition());
        }
    }
    public interface OnClickListener{
        void onClickListener(int position);
    }
}
