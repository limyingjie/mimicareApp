package com.project.mimiCare.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.mimiCare.R;
import com.project.mimiCare.Utils.SimpleToast;

import java.text.DateFormat;
import java.util.Calendar;

// Fragment for Assignment Fragment
public class AddDialog extends DialogFragment {
    private static final String TAG = "AddDialog";

    // widget
    private Button add;
    private ImageView cancel;
    private EditText input_title;
    private EditText input_target;
    private TextView input_day;
    public onInputSelected onInputSelected;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    private Calendar setCal;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_dialog_add,container,false);
        getDialog().setCanceledOnTouchOutside(false);
        add = view.findViewById(R.id.add_ok);
        cancel = view.findViewById(R.id.add_cancel);
        input_title = view.findViewById(R.id.input_assignment);
        input_target = view.findViewById(R.id.input_target);
        input_day = view.findViewById(R.id.input_day);
        setCal = Calendar.getInstance();

        // adjust the Date input
        input_day.setText(getOneDayAfterCurrentDate());
        input_day.setOnClickListener((View v)->{
            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.DATE,1);
            int year = minDate.get(Calendar.YEAR);
            int month = minDate.get(Calendar.MONTH);
            int day = minDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    mDataSetListener,
                    year, month, day);

            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.show();
        });

        // listener after picking the date
        mDataSetListener = (DatePicker datePicker, int year, int month, int day)->{
            setCal.set(year,month,day,23,59,59);
            input_day.setText(DateFormat.getDateInstance().format(setCal.getTime()));
        };

        add.setOnClickListener((View v)->{
                if (!isEmpty(input_target)|input_day.getText().toString().matches("")|!isEmpty(input_title)){
                    if (Integer.parseInt(input_target.getText().toString())<=0){
                        SimpleToast.show(getActivity(),"the target cannot be negative or zero",Toast.LENGTH_SHORT);
                        return;
                    }
                    onInputSelected.sendInput(
                            input_title.getText().toString().trim(),
                            Integer.parseInt(input_target.getText().toString()),
                            setCal
                    );
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getActivity(),"The input is empty",Toast.LENGTH_SHORT).show();
                }
        });
        cancel.setOnClickListener((View v)->getDialog().dismiss());
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
        void sendInput(String title,int target, Calendar day);
    }

    private boolean isEmpty(EditText editText){
        return editText.getText().toString().trim().isEmpty();
    }

    private String getOneDayAfterCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        return DateFormat.getDateInstance().format(calendar.getTime());
    }

}
