package com.project.mimiCare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.mimiCare.Utils.SimpleToast;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.editText_full_name);
        // username is focused and the keyboard will be shown
        if(fullName.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        Button register = findViewById(R.id.button_register);
        register.setOnClickListener((View v) ->{
            SimpleToast.show(RegisterActivity.this,"Register Successful", Toast.LENGTH_SHORT);
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        });
    }
}
