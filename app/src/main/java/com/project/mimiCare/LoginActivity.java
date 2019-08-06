package com.project.mimiCare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private Button signIn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = findViewById(R.id.button_register);
        signIn.setOnClickListener((View view)->{
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
        });

        // username is focused and the keyboard will be shown
        EditText username = findViewById(R.id.editText_username);
        if(username.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        TextView register = findViewById(R.id.textView_create_account);
        register.setOnClickListener((View v)->{
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        });
    }
}
