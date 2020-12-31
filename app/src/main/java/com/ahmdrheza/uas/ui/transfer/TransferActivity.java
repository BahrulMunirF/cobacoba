package com.ahmdrheza.uas.ui.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ahmdrheza.uas.R;
import com.ahmdrheza.uas.ui.settings.SettingsActivity;
import com.ahmdrheza.uas.transfer.TransferService;
import com.ahmdrheza.uas.ui.explorer.ExplorerActivity;
import com.ahmdrheza.uas.util.Permissions;
import com.ahmdrheza.uas.util.Settings;

public class TransferActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TransferActivity";
    private static final int INTRO_REQUEST = 1;

    private Settings mSettings;

    /**
     * Finish initializing the activity
     */
    private void finishInit() {
        Log.i(TAG, "finishing initialization of activity");

        // Setup the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Connect the action bar and navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.activity_transfer_navigation_drawer_open,
                R.string.activity_transfer_navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Process items in the navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // TODO: update this every time the activity is shown

        // Set the label for the subtitle in the navigation drawer
        String deviceName = mSettings.getString(Settings.Key.DEVICE_NAME);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.transfer_subtitle)).setText(
                getString(R.string.menu_transfer_subtitle, deviceName));

        // Setup the floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(TransferActivity.this, ExplorerActivity.class));
            }
        });

        // Launch the transfer service if it isn't already running
        TransferService.startStopService(this, mSettings.getBoolean(Settings.Key.BEHAVIOR_RECEIVE));

        // Add the transfer fragment
        TransferFragment mainFragment = new TransferFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.list_container, mainFragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = new Settings(this);
        setTheme(mSettings.getTheme(
                R.style.LightTheme_NoActionBar,
                R.style.DarkTheme_NoActionBar
        ));
        setContentView(R.layout.activity_transfer);

        if (!Permissions.haveStoragePermission(this)) {
            Permissions.requestStoragePermission(this);
        } else {
            finishInit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Determine if the dark theme is currently applied
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.themeName, typedValue, true);
        boolean currentThemeDark = "dark".equals(typedValue.string);

        // Recreate the activity if the theme has changed
        boolean shouldShowDark = mSettings.getBoolean(Settings.Key.UI_DARK);
        if (currentThemeDark != shouldShowDark) {
            setTheme(shouldShowDark ? R.style.DarkTheme : R.style.LightTheme);
            recreate();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                startActivity(new Intent(this, ExplorerActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_exit:
                TransferService.startStopService(this, false);
                finish();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (Permissions.obtainedStoragePermission(requestCode, grantResults)) {
            finishInit();
        } else {
            finish();
        }
    }
}
