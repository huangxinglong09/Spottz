package com.spottz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spottz.R;
import com.spottz.activity.fragment.CategoryDetailFragment;
import com.spottz.activity.fragment.CategoryFragment;
import com.spottz.activity.fragment.QuestionFragment;
import com.spottz.activity.fragment.ResultViewFragment;
import com.spottz.activity.fragment.RouteFragment;
import com.spottz.activity.fragment.ScoreFragment;
import com.spottz.activity.fragment.ShareFragment;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.CategoryModel;
import com.spottz.net.NetClient;
import com.spottz.util.Constant;
import com.spottz.util.MessageDialog;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NetClient netClient;
    private TextView lblTitle;
    private FrameLayout fragmentMain;
    private Fragment frmtLast;
    private final ArrayList<View> mMenuItems = new ArrayList<>();
    boolean doubleBackToExitPressedOnce = false;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(getApplicationContext());
        String email = sessionManager.getEmailId();

        ///////////////////////////////////
        SpottzApplication.getInstance().loadCategory();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        netClient = new NetClient(this);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        lblTitle = (TextView) toolbar.findViewById(R.id.lblTitle);
        Utils.setExtraBold(lblTitle);

        fragmentMain = (FrameLayout) findViewById(R.id.flayout_fragment);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_left);
        final NavigationView navigationViewRight = (NavigationView) findViewById(R.id.nav_view_right);
        final Menu navMenu = navigationView.getMenu();
        // Install an OnGlobalLayoutListener and wait for the NavigationMenu to fully initialize
        navigationView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remember to remove the installed OnGlobalLayoutListener
                navigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Loop through and find each MenuItem View
                for (int i = 0; i < navMenu.size(); i++) {
                    final int id = (i == 0 ? R.id.nav_home : R.id.nav_help);
                    final MenuItem item = navMenu.findItem(id);
                    navigationView.findViewsWithText(mMenuItems, item.getTitle(), View.FIND_VIEWS_WITH_TEXT);
                }

                for (final View menuItem : mMenuItems) {
                    Utils.setBold((TextView) menuItem);
                }

            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationViewRight.setNavigationItemSelectedListener(this);
        showFragment(Constants.INT_FRMT_CATEGORY_LIST);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        }
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_right_bar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_user) {
            drawer.openDrawer(Gravity.RIGHT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(false);
        int id = item.getItemId();
        if (id == R.id.nav_help) {
            drawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, ContentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        } else if(id == R.id.nav_home){
            drawer.closeDrawer(GravityCompat.START);
            showFragment(Constants.INT_FRMT_CATEGORY_LIST);
        } else if(id == R.id.nav_myuser){
            drawer.closeDrawer(Gravity.RIGHT);
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

        } else if(id == R.id.nav_myroute){
            drawer.closeDrawer(Gravity.RIGHT);
            if(!sessionManager.getEmailId().isEmpty())
                showFragment(Constants.INT_FRMT_MYROUTE_LIST);
                //showFragment(Constants.INT_FRMT_CATEGORY_LIST);
            else
                Toast.makeText(getApplicationContext(), "U dient uw gegevens in te vullen onder 'mijn gegevens'", Toast.LENGTH_SHORT).show();
        } else if(id == R.id.nav_logout){
            sessionManager.logoutUser();
            finish();

        }

        return true;
    }

    public void showFragment(int subviewid) {
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = null;
        if (subviewid == Constants.INT_FRMT_CATEGORY_LIST) {
            // Category List Fragment
            SpottzApplication.getInstance().clearImage();
            ft.setCustomAnimations(R.anim.slide_re_in, R.anim.slide_re_out);
            fragment = new CategoryFragment();
            Bundle args = new Bundle();
            args.putInt("category_mode", 0);
            fragment.setArguments(args);
            lblTitle.setText("SPOTTZ");
        } else if( subviewid == Constants.INT_FRMT_MYROUTE_LIST) {
            // My route Fragment
            SpottzApplication.getInstance().clearImage();
            ft.setCustomAnimations(R.anim.slide_re_in, R.anim.slide_re_out);
            fragment = new CategoryFragment();
            Bundle args = new Bundle();
            args.putInt("category_mode", 1);
            fragment.setArguments(args);
            lblTitle.setText("SPOTTZ");
        } else if (subviewid == Constants.INT_FRMT_SCORE) {
            // ScoreFragment

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new ScoreFragment();
            String strCategory = SpottzApplication.getInstance().currentItem.strTitle;
            lblTitle.setText("Scores : " + strCategory);
        } else if (subviewid == Constants.INT_FRMT_CATEGORY_DETAIL) {
            // Category Details Fragment

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new CategoryDetailFragment();
            String strCategory = SpottzApplication.getInstance().currentItem.strTitle;
            //lblTitle.setText("Scores : " + strCategory);
            lblTitle.setText(strCategory);
        } else if (subviewid == Constants.INT_FRMT_ROUTE) {
            // Route Fragments

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new RouteFragment();
            lblTitle.setText("SPOTTZ");
            } else if (subviewid == Constants.INT_FRMT_RESULT) {
            // Route Fragments

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new ResultViewFragment();
            lblTitle.setText("SPOTTZ");
        } else if (subviewid == Constants.INT_FRMT_QUESTION) {
            // Question Fragment

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new QuestionFragment();
            lblTitle.setText("SPOTTZ");
        } else if (subviewid == Constants.INT_FRMT_SHARE) {
            // Share Fragment

            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            fragment = new ShareFragment();
            lblTitle.setText("SPOTTZ");
        }

        if (fragment != null) {
            if (frmtLast != null) {
                ft.remove(frmtLast);
            }
            // Replace current fragment by this new one
            ft.replace(R.id.flayout_fragment, fragment);
            ft.commit();
            frmtLast = fragment;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (frmtLast != null) {
            frmtLast.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void gotoRouteScreen() {
        if (SpottzApplication.getInstance().curSpots == null || SpottzApplication.getInstance().curSpots.arrQuestoins.size() < 1) {
            MessageDialog.showAlarmAlert(this, "No exist available question");
            return;
        }

        SpottzApplication.getInstance().handlerSpotsLoaded = null;
        SpottzApplication.getInstance().handlerLocationChanged = null;

        CategoryModel info = SpottzApplication.getInstance().currentItem;
        if(info == null)
            return;

        ////////////////////////////////////////////////////////
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if(!sessionManager.isLoggedIn()) {
            if (!info.type.equals("code")) {
                // show message
                showStartRouteAlertMessage(SpottzApplication.getInstance().startRouteAlertMessage);
                return;
            }
        }

        SpottzApplication.getInstance().startRoute(info.iID);
        showFragment(Constants.INT_FRMT_ROUTE);
    }

}
