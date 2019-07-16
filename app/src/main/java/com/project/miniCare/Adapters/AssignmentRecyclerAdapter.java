package com.project.miniCare.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
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
    private SparseBooleanArray mSparseBooleanArray;
    private Context context;

    public AssignmentRecyclerAdapter(ArrayList<String> mAssignment, OnClickListener onClickListener,Context context) {
        this.mAssignment = mAssignment;
        this.onClickListener = onClickListener;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // recycling the viewHolder and put them in the position they are supposed to be
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.assignment_list_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view,onClickListener,context);
        mSparseBooleanArray = new SparseBooleanArray();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called.");

        viewHolder.assignment.setText(mAssignment.get(i));
        if (mSparseBooleanArray.get(i,false)){
            viewHolder.cardView.setStrokeColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else{
            viewHolder.cardView.setStrokeColor(context.getResources().getColor(R.color.colorText));
        }
    }

    @Override
    public int getItemCount() {
        return mAssignment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MaterialCardView cardView;
        OnClickListener onClickListener;
        TextView assignment;
        Context context;
        public ViewHolder(@NonNull View itemView, OnClickListener onClickListener, Context context) {
            super(itemView);
            Log.d(TAG, "ViewHolder: Called");
            assignment = itemView.findViewById(R.id.assigment_list1);
            cardView = itemView.findViewById(R.id.assignment_parent);
            this.context = context;
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (mSparseBooleanArray.get(getAdapterPosition(),false)){
                mSparseBooleanArray.delete(getAdapterPosition());
                cardView.setStrokeColor(context.getResources().getColor(R.color.colorText));
            }
            else{
                mSparseBooleanArray.put(getAdapterPosition(),true);
                cardView.setStrokeColor(context.getResources().getColor(R.color.colorPrimary));
            }
            onClickListener.onClickListener(getAdapterPosition());
        }
    }
    public interface OnClickListener{
        void onClickListener(int position);
    }
}
