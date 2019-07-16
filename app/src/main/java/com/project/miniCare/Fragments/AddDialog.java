package com.project.miniCare.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.miniCare.R;
// Fragment for Assignment Fragment
public class AddDialog extends DialogFragment {
    private static final String TAG = "AddDialog";

    // widget
    private Button add;
    private ImageView cancel;
    private EditText input;
    public onInputSelected onInputSelected;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_dialog_add,container,false);
        getDialog().setCanceledOnTouchOutside(false);
        add = view.findViewById(R.id.add_ok);
        cancel = view.findViewById(R.id.add_cancel);
        input = view.findViewById(R.id.input_assignment);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!input.getText().toString().trim().isEmpty()){
                    onInputSelected.sendInput(input.getText().toString().trim());
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getActivity(),"The input is empty",Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.add_dialog_width);
        getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            onInputSelected = (onInputSelected) getTargetFragment();
        }
        catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException " + e.getMessage());
        }
    }

    public interface onInputSelected{
        void sendInput(String input);
    }

}
