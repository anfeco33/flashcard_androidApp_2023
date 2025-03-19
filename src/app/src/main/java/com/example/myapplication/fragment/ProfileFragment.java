package com.example.myapplication.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.activity.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private TextView name, email;

    private RelativeLayout changePass, logout;

    private ImageView editAvatar, avatar;
    private StorageReference storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        logout = view.findViewById(R.id.signout);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("password", Context.MODE_PRIVATE);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("password");
                editor.apply();
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });
        name = view.findViewById(R.id.profile_name);
        email = view.findViewById(R.id.profile_email);
        editAvatar = view.findViewById(R.id.edit_avatar);
        avatar = view.findViewById(R.id.profile_avatar);
        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(avatar);
        name.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());
        changePass = view.findViewById(R.id.changePassword);
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.change_password);
                ImageView back = dialog.findViewById(R.id.back);
                EditText old_pass = dialog.findViewById(R.id.old_pass);
                EditText new_pass = dialog.findViewById(R.id.new_pass);
                EditText renew_pass = dialog.findViewById(R.id.renew_pass);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                TextView done = dialog.findViewById(R.id.done);
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String matkhaucu = String.valueOf(old_pass.getText());
                        String matkhaumoi = String.valueOf(new_pass.getText());
                        String nhaplaimatkhaumoi = String.valueOf(renew_pass.getText());
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("password", Context.MODE_PRIVATE);
                        String userPassword = sharedPreferences.getString("password", "");
                        System.out.println(userPassword);
                        if (TextUtils.isEmpty(matkhaucu) || TextUtils.isEmpty(matkhaumoi) || TextUtils.isEmpty(nhaplaimatkhaumoi)) {
                            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        } else if (!matkhaucu.equals(userPassword)) {
                            Toast.makeText(getContext(), "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                        } else if (!matkhaumoi.equals(nhaplaimatkhaumoi)) {
                            Toast.makeText(getContext(), "Mật khẩu mới và nhập lại mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                        } else {

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("password", matkhaumoi);
                            editor.apply();
                            currentUser.updatePassword(matkhaumoi)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("changePassword","successfully");

                                            } else {
                                                Log.d("changePassword","error");
                                            }
                                        }
                                    });

                            dialog.dismiss();

                            Toast.makeText(getContext(), "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);

            }
        });
        editAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        return view;
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(avatar);
            updateProfilePicture(selectedImageUri);
        }
    }
    private void updateProfilePicture(Uri selectedImageUri) {
        if (currentUser != null) {
            storageReference = FirebaseStorage.getInstance().getReference("images/"+currentUser.getUid());

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Cập nhật thành công
                                                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Có lỗi xảy ra
                                                Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                    });

        }
    }


}