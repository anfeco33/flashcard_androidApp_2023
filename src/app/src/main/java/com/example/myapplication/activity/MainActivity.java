package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.view.View;

import com.example.myapplication.adapter.AddVocabularyAdapter;
import com.example.myapplication.entinies.VocabularyEntity;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.R;
import com.example.myapplication.fragment.SettingFragment;
import com.example.myapplication.viewModel.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private List<VocabularyEntity> dataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private AddVocabularyAdapter adapter;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ImageView profileImageView;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    AppDatabase appDatabase;
    ActivityMainBinding binding;
    private AlertDialog alertDialog;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.profile);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }



    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            return true;
        } else if (item.getItemId()==R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingFragment(), "SettingFragmentTag")
                    .commit();
            closeDrawer();
            return true;
        } else if (item.getItemId()==R.id.nav_logout) {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
    @Override
    public void onBackPressed() {
        // Kiểm tra xem SettingFragment có đang hiển thị hay không
        Fragment settingFragment = getSupportFragmentManager().findFragmentByTag("SettingFragmentTag");
        if (settingFragment != null && settingFragment.isVisible()) {
            // Nếu có, thay thế bằng HomeFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Nếu navigation drawer đang mở, thì đóng nó lại
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Nếu không, thực hiện hành động mặc định của nút back
            super.onBackPressed();
        }
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setNavItemChecked(int itemId) {
        navigationView.setCheckedItem(itemId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_folder) {
            Intent intent = new Intent(MainActivity.this, FolderMananager.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_topic) {
            Intent intent = new Intent(MainActivity.this, TopicManagement.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_privatedStudy) {
            Intent intent = new Intent(MainActivity.this, privateStudyList.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
