package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.myapplication.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class LoginActivity extends AppCompatActivity {
    private EditText eT_Email, eT_Password;
    private TextView  tV_forgotPass;
    private Button btn_Login, tV_Register;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    SharedPreferences sharedPreferences;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        eT_Email = findViewById(R.id.eT_Email);
        eT_Password = findViewById(R.id.eT_Password);
        progressBar = findViewById(R.id.progressBar);
        tV_Register = findViewById(R.id.btn_Register);
        sharedPreferences = getSharedPreferences("password", Context.MODE_PRIVATE);

        tV_Register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        tV_forgotPass = findViewById(R.id.tV_forgotPass);
        tV_forgotPass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_Login = findViewById(R.id.btn_Login);
        btn_Login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String email = String.valueOf(eT_Email.getText());
                String password = String.valueOf(eT_Password.getText());
                boolean allValid = true;

                if(TextUtils.isEmpty(email)){
                    eT_Email.setError(getResources().getString(R.string.error_message));
                    allValid = false;
                }
                if(TextUtils.isEmpty(password)){
                    eT_Password.setError(getResources().getString(R.string.error_message));
                    allValid = false;
                }
                if(!allValid)
                    return;
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", password);
                    editor.apply();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {

                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_successfully),Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_fail),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

}