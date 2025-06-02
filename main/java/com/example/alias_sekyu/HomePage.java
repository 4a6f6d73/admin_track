package com.example.alias_sekyu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.alias_sekyu.databinding.ActivityHomepgBinding;
import com.example.alias_sekyu.fragments.admin_event;
import com.example.alias_sekyu.fragments.event_summary;
import com.example.alias_sekyu.fragments.event_task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomePage extends AppCompatActivity {

    private ActivityHomepgBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepg);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_activity);
        bottomNav.setOnItemSelectedListener(navListen);

        Fragment selectedFragment = new event_task();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_only, selectedFragment).commit();
    }

    private final NavigationBarView.OnItemSelectedListener navListen = item -> {
        int itemId = item.getItemId();

        Fragment selectedFragment = null;

        if(itemId == R.id.nav_activity){
            selectedFragment = new event_task();
        } else if (itemId == R.id.nav_records){
            selectedFragment = new event_summary();
        } else if (itemId == R.id.nav_user) {
            selectedFragment = new admin_event();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_only, selectedFragment).commit();
        return true;
    };
}