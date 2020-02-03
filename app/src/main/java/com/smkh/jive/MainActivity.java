package com.smkh.jive;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String fragmentTag = null;
    Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
        setContentView(R.layout.activity_main);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.fragments_container).setBackgroundResource(R.mipmap.home_background_portrait);
        } else {
            findViewById(R.id.fragments_container).setBackgroundResource(R.mipmap.home_background_landscape);
        }

        //Window w = getWindow(); // in Activity's onCreate() for instance
        //w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("fragmentName")) {
                setTitle(savedInstanceState.getCharSequence("FragmentTitle"));
                String str = savedInstanceState.getString("fragmentName");
                Fragment fragment = getFragmentManager().getFragment(savedInstanceState, str);
                getFragmentManager().beginTransaction().replace(R.id.fragments_container, fragment).commit();
                this.currentFragment = fragment;
                this.fragmentTag = str;
            } else {
                onNavigationItemSelected(((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_home));
            }
        } else {
            onNavigationItemSelected(((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_home));
        }

        SharedPreferences preferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        Boolean isNoisy = preferences.getBoolean("isNoisy", true);
        if (isNoisy) {
            navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_notification_on);
        } else {
            navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_notification_off);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                String tag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2).getName();
                NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
                switch (tag) {
                    case "Home":
                        setTitle("JIVE: Home");
                        navView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        fragmentTag = "Home";
                        break;
                    case "Read":
                        setTitle("JIVE: Read");
                        navView.getMenu().findItem(R.id.nav_read).setChecked(true);
                        fragmentTag = "Read";
                        break;
                    case "Team":
                        setTitle("JIVE: Team");
                        navView.getMenu().findItem(R.id.nav_team).setChecked(true);
                        fragmentTag = "Team";
                        break;
                    case "About":
                        setTitle("JIVE: About");
                        navView.getMenu().findItem(R.id.nav_about).setChecked(true);
                        fragmentTag = "About";
                        break;
                    case "Feedback":
                        setTitle("JIVE: Feedback");
                        navView.getMenu().findItem(R.id.nav_feedback).setChecked(true);
                        fragmentTag = "Feedback";
                        break;
                    case "Contact":
                        setTitle("JIVE: Contact");
                        navView.getMenu().findItem(R.id.nav_contact).setChecked(true);
                        fragmentTag = "Contact";
                }
                currentFragment = getFragmentManager().findFragmentByTag(fragmentTag);
                getFragmentManager().popBackStack();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentFragment != null) {
            try{
                outState.putString("fragmentName", fragmentTag);
                outState.putCharSequence("FragmentTitle", getTitle());
                getFragmentManager().putFragment(outState, fragmentTag, currentFragment);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.getItemId() == R.id.nav_notification) {
            SharedPreferences preferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            Boolean isNoisy = preferences.getBoolean("isNoisy", true);
            if (isNoisy) {
                editor.putBoolean("isNoisy", false);
                item.setIcon(R.drawable.ic_notification_off);
                Toast.makeText(this, "Notifications are turned off", Toast.LENGTH_SHORT).show();
            } else {
                editor.putBoolean("isNoisy", true);
                item.setIcon(R.drawable.ic_notification_on);
                Toast.makeText(this, "Notifications are turned on", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        }
        if (!item.isChecked()) {
            int id = item.getItemId();
            Fragment fragment = null;
            if (id == R.id.nav_home) {
                MainActivity.this.setTitle("JIVE: Home");
                fragment = new HomeFragment();
                fragmentTag = "Home";
            } else if (id == R.id.nav_team) {
                MainActivity.this.setTitle("JIVE: Team");
                fragment = new TeamFragment();
                fragmentTag = "Team";
            } else if (id == R.id.nav_about) {
                MainActivity.this.setTitle("JIVE: About");
                fragment = new AboutFragment();
                fragmentTag = "About";
            } else if (id == R.id.nav_feedback) {
                MainActivity.this.setTitle("JIVE: Feedback");
                fragment = new FeedbackFragment();
                fragmentTag = "Feedback";
            } else if (id == R.id.nav_read) {
                MainActivity.this.setTitle("JIVE: Read");
                fragment = new ReadFragment();
                fragmentTag = "Read";
            } else if (id == R.id.nav_contact) {
                MainActivity.this.setTitle("JIVE: Contact");
                fragment = new ContactFragment();
                fragmentTag = "Contact";
            }
            if (fragment != null) {
                item.setChecked(true);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                this.currentFragment = fragment;
                drawer.closeDrawer(GravityCompat.START);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        while (drawer.isDrawerVisible(GravityCompat.START)) ;
                        handler.sendEmptyMessage(0);
                    }
                };
                thread.start();
            }
        }



        return true;
    }

    Handler handler = new Handler() {
        String str;
        @Override
        public void handleMessage(Message msg) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .addToBackStack(fragmentTag)
                    .replace(R.id.fragments_container, currentFragment, fragmentTag).commit();
        }
    };
}
