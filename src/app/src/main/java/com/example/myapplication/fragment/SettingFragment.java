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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.activity.EditProfileActivity;
import com.example.myapplication.activity.LoginActivity;
import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingFragment extends Fragment {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private TextView name, email;

    private RelativeLayout changePass, logout;

    private ImageView editAvatar, avatar;
    private StorageReference storageReference;
    private SwitchCompat switchNightMode;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ImageView imgvBack;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // night mode
        SharedPreferences prefs = getActivity().getSharedPreferences("AppSettingsPrefs", 0);
        boolean isNightModeOn = prefs.getBoolean("NightMode", false);
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        logout = view.findViewById(R.id.signout);
        ImageView imgvBack = view.findViewById(R.id.back);
        switchNightMode = view.findViewById(R.id.switchNightMode);

        // night mode setup
        SharedPreferences prefs = getActivity().getSharedPreferences("AppSettingsPrefs", 0);
        boolean isNightModeOn = prefs.getBoolean("NightMode", false);
        switchNightMode.setChecked(isNightModeOn);


        imgvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                mainActivity.closeDrawer();
                mainActivity.setNavItemChecked(R.id.nav_home);
            }
        });

        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                if (isChecked) {
                    // enable night mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("NightMode", true);
                } else {
                    // disable night mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("NightMode", false);
                }
                editor.apply();

                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("password", Context.MODE_PRIVATE);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("password");
                editor.apply();
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
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
                final Dialog dialog = new Dialog(getActivity());
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
                            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        } else if (!matkhaucu.equals(userPassword)) {
                            Toast.makeText(getActivity(), "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                        } else if (!matkhaumoi.equals(nhaplaimatkhaumoi)) {
                            Toast.makeText(getActivity(), "Mật khẩu mới và nhập lại mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
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

                            Toast.makeText(getActivity(), "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.setting_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
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

                                            } else {
                                                Log.d("imageUp","error");
                                            }
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.d("imageUp","error");
                    });

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        SharedPreferences prefs = getActivity().getSharedPreferences("AppSettingsPrefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("NightMode", switchNightMode.isChecked());
        editor.apply();
    }
}