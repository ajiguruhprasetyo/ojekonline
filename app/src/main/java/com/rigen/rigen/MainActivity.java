package com.rigen.rigen;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.fragment.FragmentBooking;
import com.rigen.rigen.fragment.FragmentCS;
import com.rigen.rigen.fragment.FragmentHelp;
import com.rigen.rigen.fragment.FragmentHistory;
import com.rigen.rigen.fragment.FragmentOrder;
import com.rigen.rigen.fragment.FragmentProfile;
import com.rigen.rigen.service.BookingService;
import com.rigen.rigen.service.MessagesService;

public class MainActivity extends AppCompatActivity{
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private String  nama;
    private int pos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, BookingService.class));
        startService(new Intent(this, MessagesService.class));
        setContentView(R.layout.activity_main);
        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c= db.getAll();
        c.moveToFirst();
        nama    = c.getString(c.getColumnIndex("nama"));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pos==0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_main, new FragmentBooking()).commit();
            getSupportActionBar().setTitle("Booking");
        }
        if (pos==1) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_main, new FragmentOrder()).commit();
            getSupportActionBar().setTitle("Order");
        }
        if (pos==2) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_main, new FragmentHistory()).commit();
            getSupportActionBar().setTitle("History");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                Fragment fragment = null;
                switch (id) {
                    case R.id.nav_booking:
                        pos = 0;
                        fragment = new FragmentBooking();
                        getSupportActionBar().setTitle("Booking");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_help:
                        pos = 4;
                        fragment = new FragmentHelp();
                        getSupportActionBar().setTitle("Bantuan");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_cs:
                        pos = 5;
                        fragment = new FragmentCS();
                        getSupportActionBar().setTitle("Customer Service");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_profile:
                        pos = 3;
                        fragment = new FragmentProfile();
                        getSupportActionBar().setTitle("Profile");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_order:
                        pos = 1;
                        fragment = new FragmentOrder();
                        getSupportActionBar().setTitle("Order");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_history:
                        pos = 2;
                        fragment = new FragmentHistory();
                        getSupportActionBar().setTitle("History");
                        drawerLayout.closeDrawers();
                        break;

                }
                if (fragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();
                }

                return true;
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        View header = navigationView.getHeaderView(0);
        TextView txt_nama = (TextView) header.findViewById(R.id.txt_nama);
        txt_nama.setText(nama);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
}
