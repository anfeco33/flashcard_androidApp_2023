package com.example.myapplication.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.EmailUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private EditText eT_Name,eT_Email, eT_Password, eT_confirmPassword;
    private Button btn_Register, button_back;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private String otpCode;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();

        eT_Name = findViewById(R.id.eT_Name);
        eT_Email = findViewById(R.id.eT_Email);
        eT_Password = findViewById(R.id.eT_Password);
        eT_confirmPassword = findViewById(R.id.eT_confirmPassword);
        progressBar = findViewById(R.id.progressBar);
        button_back = findViewById(R.id.btn_back);
        button_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
        btn_Register = findViewById(R.id.btn_Register);
        btn_Register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                progressBar.setVisibility(View.VISIBLE);
                String name = String.valueOf(eT_Name.getText());
                String email = String.valueOf(eT_Email.getText());
                String password = String.valueOf(eT_Password.getText());
                String repassword = String.valueOf(eT_confirmPassword.getText());
                if(TextUtils.isEmpty(name)) {
                    eT_Name.setError(getResources().getString(R.string.error_message));
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    eT_Email.setError(getResources().getString(R.string.error_message));
                    return;
                } else if (!isValidEmail(email)) {
                    eT_Email.setError(getResources().getString(R.string.incorrect_email));
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    eT_Password.setError(getResources().getString(R.string.error_message));
                    return;
                }
                if(TextUtils.isEmpty(repassword)){
                    eT_confirmPassword.setError(getResources().getString(R.string.error_message));
                    return;
                }
                if(!password.equals(repassword)){
                    eT_confirmPassword.setError(getResources().getString(R.string.do_not_match));
                    return;
                }

                otpCode = generateOtp();
                EmailUtil.sendVerificationEmail(email, name, otpCode);

                final Dialog dialog = new Dialog(RegisterActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.otp);
                EditText input_otp = dialog.findViewById(R.id.eT_otp);
                TextView textView = dialog.findViewById(R.id.backOtp);
                Button submit = dialog.findViewById(R.id.submit);
                TextView alert = dialog.findViewById(R.id.alert);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String otp = input_otp.getText().toString();
                        if(!otp.equals(otpCode)){
                            alert.setVisibility(View.VISIBLE);
                        }else {
                            alert.setVisibility(View.GONE);
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                Uri photoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/studentmanager-72b6a.appspot.com/o/images%2FScreenshot%202023-04-10%20170802.png?alt=media&token=bbdef93b-4f74-4d24-a075-5e31b55a7662");

                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setPhotoUri(photoUri)
                                                        .setDisplayName(name)
                                                        .build();

                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("success", "thành công");
                                                                } else {
                                                                    Log.d("success", "thất bại");
                                                                }
                                                            }
                                                        });


                                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_successfully),Toast.LENGTH_SHORT).show();

//                                                HashMap<String, Object> hashMap = new HashMap<>();
//                                                hashMap.put("id",user.getUid());
//                                                hashMap.put("email",email);
//                                                hashMap.put("password",password);
//                                                hashMap.put("image","https://firebasestorage.googleapis.com/v0/b/studentmanager-72b6a.appspot.com/o/images%2FScreenshot%202023-04-10%20170802.png?alt=media&token=bbdef93b-4f74-4d24-a075-5e31b55a7662");
//                                                hashMap.put("name", name);

                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialog.dismiss();
                        }
                    }
                });
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        otpCode = generateOtp();
                        EmailUtil.sendVerificationEmail(email, name, otpCode);
                    }
                });

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);


            }
        });

    }
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        return email.matches(emailPattern);
    }
    private String generateOtp() {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);

        while (output.length() < 6) {
            output = "0" + output;
        }
        return output;
    }


}