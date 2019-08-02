package com.project.miniCare.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.project.miniCare.Data.UserSetting;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;
import com.project.miniCare.Utils.SharedPreferenceHelper;

import java.text.DateFormat;
import java.util.Calendar;

public class EditProfileFragment extends Fragment {

    private ImageView back,ok;
    private UserSetting userSetting;
    private EditText name,email,phone;
    private TextView dob;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile,container,false);

        // initialize
        back = view.findViewById(R.id.edit_profile_back);
        ok = view.findViewById(R.id.edit_profile_ok);
        name = view.findViewById(R.id.editText_name);
        email = view.findViewById(R.id.editText_email);
        phone = view.findViewById(R.id.editText_phone);
        dob = view.findViewById(R.id.textView_dob);

        // load the data
        loadData();
        name.setText(userSetting.getName());
        email.setText(userSetting.getEmail());
        phone.setText(userSetting.getPhoneNumber());
        dob.setText(userSetting.getDob());

        // set onClickListener
        back.setOnClickListener((View v)->{
            getActivity().onBackPressed();
        });
        ok.setOnClickListener((View v)->{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Confirmation")
                    .setMessage("Are you sure?")
                    .setPositiveButton("ok",(DialogInterface dialog,int i)->{
                        saveData();
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    });
            Dialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        });
        dob.setOnClickListener((View v)->{
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    mDataSetListener,
                    year,month,day
            );
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        // initialize the listener
        mDataSetListener = (DatePicker datePicker, int y, int m, int d)->{
            Calendar cal = Calendar.getInstance();
            cal.set(y,m,d);
            String newDob = DateFormat.getDateInstance().format(cal.getTime());
            dob.setText(newDob);
        };
        return view;
    }

    private void saveData(){
        // update userSetting
        userSetting.setName(name.getText().toString().trim());
        userSetting.setEmail(email.getText().toString().trim());
        userSetting.setPhoneNumber(phone.getText().toString().trim());
        userSetting.setDob(dob.getText().toString().trim());
        SharedPreferenceHelper.savePreferenceData(getActivity(),"userSetting",userSetting);
    }

    private void loadData(){
        userSetting = (UserSetting) SharedPreferenceHelper.loadPreferenceData(getActivity(),"userSetting",new TypeToken<UserSetting>() {}.getType());
        if (userSetting==null){
            userSetting = new UserSetting("","","","");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().hide();
        ((MainActivity)getActivity()).bottomNav.setVisibility(View.GONE);

    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).getSupportActionBar().show();
        ((MainActivity)getActivity()).bottomNav.setVisibility(View.VISIBLE);
    }

}
