package com.project.miniCare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.project.miniCare.Utils.SimpleToast;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button register = findViewById(R.id.button_register);
        register.setOnClickListener((View v) ->{
            SimpleToast.show(RegisterActivity.this,"Register Successful", Toast.LENGTH_SHORT);
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        });
    }
}
