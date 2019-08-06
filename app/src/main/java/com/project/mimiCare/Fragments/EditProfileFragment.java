package com.project.mimiCare.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.project.mimiCare.Data.UserSetting;
import com.project.mimiCare.MainActivity;
import com.project.mimiCare.R;
import com.project.mimiCare.Utils.SharedPreferenceHelper;
import com.project.mimiCare.Utils.UniversalImageLoader;

import java.text.DateFormat;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private ImageView back,ok,profile_pic;
    private UserSetting userSetting;
    private EditText name,email,phone;
    private TextView dob,change_photo;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    private String profile_pic_uri;
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
        profile_pic = view.findViewById(R.id.profile_pic);
        name = view.findViewById(R.id.editText_name);
        email = view.findViewById(R.id.editText_email);
        phone = view.findViewById(R.id.editText_phone);
        dob = view.findViewById(R.id.textView_dob);
        change_photo = view.findViewById(R.id.change_photo_text);

        // load the data
        loadData();
        name.setText(userSetting.getName());
        email.setText(userSetting.getEmail());
        phone.setText(userSetting.getPhoneNumber());
        dob.setText(userSetting.getDob());
        setProfilePic();

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

        change_photo.setOnClickListener((View v)->{
            Intent pick_photo = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick_photo,1);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode== RESULT_OK){
                    Uri selected_image = data.getData();
                    profile_pic.setImageURI(selected_image);
                    profile_pic_uri = selected_image.toString();
                }
                break;
        }
    }

    private void setProfilePic(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
        Log.d(TAG, "setProfilePic: "+userSetting.getProfile_photo_uri());
        UniversalImageLoader.setImage(profile_pic_uri,profile_pic,null,"");
    }

    private void saveData(){
        // update userSetting
        userSetting.setName(name.getText().toString().trim());
        userSetting.setEmail(email.getText().toString().trim());
        userSetting.setPhoneNumber(phone.getText().toString().trim());
        userSetting.setDob(dob.getText().toString().trim());
        userSetting.setProfile_photo_uri(profile_pic_uri);
        SharedPreferenceHelper.savePreferenceData(getActivity(),"userSetting",userSetting);
    }

    private void loadData(){
        userSetting = (UserSetting) SharedPreferenceHelper.loadPreferenceData(getActivity(),"userSetting",new TypeToken<UserSetting>() {}.getType());
        if (userSetting==null){
            userSetting = new UserSetting("","","","",null);
        }
        profile_pic_uri = userSetting.getProfile_photo_uri();
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
