package com.unimate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unimate.view.DiscoverFragment;
import com.unimate.view.HomeFragment;
import com.unimate.view.PlannerFragment;
import com.unimate.view.ProfileFragment;
import com.unimate.view.TasksFragment;
import com.unimate.view.ui.UiKit;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIF_PERMISSION = 101;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiKit.applyEdgeToEdge(this);
        setContentView(R.layout.activity_main);

        // Content sits below the status bar; the bottom bar handles the gesture area itself.
        UiKit.padForSystemBars(findViewById(R.id.fragmentContainer), true, false);

        bottomNav = findViewById(R.id.bottomNavigationView);

        setupBottomNavigation();
        requestNotificationPermission();

        if (savedInstanceState == null) {
            // Home is already the checked item, so setSelectedItemId() would only count as a
            // "reselect" and never attach a fragment. Show it directly instead.
            bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
            showTab(R.id.nav_home, false);
        }
    }

    /** Lets screens switch tabs (e.g. Home → "See planner"). */
    public void navigateTo(@IdRes int navItemId) {
        if (bottomNav != null && bottomNav.getSelectedItemId() != navItemId) {
            bottomNav.setSelectedItemId(navItemId);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> showTab(item.getItemId(), true));
        // Re-tapping the current tab should not rebuild the screen.
        bottomNav.setOnItemReselectedListener(item -> { });
    }

    private boolean showTab(@IdRes int id, boolean animate) {
        Fragment selectedFragment = null;
        if (id == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (id == R.id.nav_planner) {
            selectedFragment = new PlannerFragment();
        } else if (id == R.id.nav_discover) {
            selectedFragment = new DiscoverFragment();
        } else if (id == R.id.nav_tasks) {
            selectedFragment = new TasksFragment();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }
        if (selectedFragment == null) return false;

        androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        if (animate) tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.fragmentContainer, selectedFragment).commit();
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
