package com.example.remindo;

import static com.example.remindo.R.id.navProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.example.remindo.adapter.DataUser;
import com.example.remindo.adapter.adapterViewPager;
import com.example.remindo.databinding.ActivityMainBinding;
import com.example.remindo.fragment.CalendarFragment;
import com.example.remindo.fragment.HomeFragment;
import com.example.remindo.fragment.IdeaFragment;
import com.example.remindo.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class CustomBottomNav extends AppCompatActivity {

    //private DrawerLayout drawerLayout;

    ViewPager2 pagerMain;
    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    BottomNavigationView bottomNav;

    private FirebaseAuth auth;
    public static final int navHome = 0;
    public static final int navIdea = 1;
    public static final int navCalendar = 2;
    public static final int navProfile = 3;


    String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_bottom_nav);

        pagerMain = findViewById(R.id.pagerMain);
        bottomNav = findViewById(R.id.bottomNav);

        auth = FirebaseAuth.getInstance();

        userId = auth.getCurrentUser().getUid();



        DataUser.setUpdateData(1);

        if (userId == null) {
            Intent i = new Intent(CustomBottomNav.this, SignIn.class);
            startActivity(i);

        }


        fragmentArrayList.add(new HomeFragment());
        fragmentArrayList.add(new IdeaFragment());
        fragmentArrayList.add(new CalendarFragment());
        fragmentArrayList.add(new ProfileFragment());

        adapterViewPager adapterviewpager = new adapterViewPager(this, fragmentArrayList);

        pagerMain.setAdapter(adapterviewpager);
        pagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNav.setSelectedItemId(R.id.navHome);
                        break;
                    case 1:
                        bottomNav.setSelectedItemId(R.id.navIdea);
                        break;
                    case 2:
                        bottomNav.setSelectedItemId(R.id.navCalendar);
                        break;
                    case 3:
                        bottomNav.setSelectedItemId(R.id.navProfile);
                        break;
                }

                super.onPageSelected(position);
            }
        });
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.navHome) {
                    pagerMain.setCurrentItem(0);
                } else if (itemId == R.id.navIdea) {
                    pagerMain.setCurrentItem(1);
                } else if (itemId == R.id.navCalendar) {
                    pagerMain.setCurrentItem(2);
                } else if (itemId == R.id.navProfile) {
                    pagerMain.setCurrentItem(3);
                }
                return true;

            }
        });

        Intent intent = getIntent();

        if (intent.getIntExtra("updated", 0) == 1) {
            pagerMain.setCurrentItem(3);
            bottomNav.setSelectedItemId(R.id.navProfile);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


}