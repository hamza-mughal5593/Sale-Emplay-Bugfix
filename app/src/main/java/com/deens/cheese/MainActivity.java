package com.deens.cheese;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deens.cheese.Adapter.CustomExpandableListAdapter;
import com.deens.cheese.Helper.FragmentNavigationManager;
import com.deens.cheese.Interface.NavigationManager;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private String[] items;
    private String activityTitle;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitles;
    private LinkedHashMap<String, List<String>> listChild;
    private NavigationManager navigationManager;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    UserClass userClass;
    String message = "";
    String messageDetail = "";
    public static ArrayList<String> sideMenuItems = new ArrayList<>();
    public static ArrayList<String> sideMenuGroups = new ArrayList<>();
    public static ArrayList<Integer> sortNumber = new ArrayList<>();

    Bundle savedInstanceStateGlobal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedInstanceStateGlobal = savedInstanceState;
        drawerLayout = findViewById(R.id.drawerLayout);
        expandableListView = findViewById(R.id.navList);
        activityTitle = getTitle().toString();
        navigationManager = FragmentNavigationManager.getMInstance(this);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        loadPreferences();

        View navHeaderView = getLayoutInflater().inflate(R.layout.nav_header, null, false);
        expandableListView.addHeaderView(navHeaderView);

        TextView name = findViewById(R.id.name);
        TextView designation = findViewById(R.id.designation);
        name.setText(userClass.getUserName().toUpperCase());
        designation.setText(userClass.getDesignationName());

        if (isNetworkAvailable()) {
            GetMenuItems task = new GetMenuItems();
            task.execute();
        }else {
            Toast.makeText(MainActivity.this, "Internet not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private void selectFirstItem() {
        if (navigationManager != null) {
            String firstItem = "Dashboard";
            navigationManager.showFragment("Dashboard");
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>" + firstItem + "</font>"));
        }
    }

    private void setupDrawer(){
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
//        drawerToggle.setHomeAsUpIndicator(R.color.transparent);
        drawerToggle.setToolbarNavigationClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        drawerLayout.setDrawerListener(drawerToggle);

        drawerToggle.syncState();
    }

    private void addDrawerItems(){
        adapter = new CustomExpandableListAdapter(this, listTitles, listChild);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupExpandListener(i -> {
//            getSupportActionBar().setTitle(listTitles.get(i));
        });

        expandableListView.setOnChildClickListener((expandableListView, view, i, i1, l) -> {
            // TODO: Change fragment here
            String selectedItem = ((List) listChild.get(listTitles.get(i))).get(i1).toString();
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>"+selectedItem+"</font>"));
            drawerLayout.close();
            // TODO: Here we will call respective fragment class
            if (selectedItem.equals("Logout")){
                // Clear all Shared Preference Values
                editor.clear();
                editor.apply();
                sideMenuItems.clear();
                sideMenuGroups.clear();

                Intent intent = new Intent(this, Login.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);

            }else {
                navigationManager.showFragment(selectedItem);
            }
            return false;
        });
    }

    private void genData() {
        List<String> titles = new ArrayList<>();

        // Get titles of all menu groups
        for (int i = 0 ; i < sideMenuGroups.size(); i++){
            if (!titles.contains(sideMenuGroups.get(i))){
                titles.add(sideMenuGroups.get(i));
            }
        }

        listChild = new LinkedHashMap<>();

        // Get titles of all menu group items
        for (int i = 0 ; i < titles.size(); i++){
            ArrayList<String> list = new ArrayList<>();
            for (int j = 0 ; j < sideMenuItems.size(); j++) {
                if (!sideMenuItems.get(j).equals("Update Approve Customer") ){
                    if (titles.get(i).equals(sideMenuGroups.get(j))) {
                        list.add(sideMenuItems.get(j));
                    }
                }
            }
            listChild.put(titles.get(i), list);
        }

        // Sort groups with sorting number
        listChild.put("Settings", Arrays.asList("Logout"));
        listTitles = new ArrayList<>(listChild.keySet());
    }

    private void initItems() {
        items = new String[]{"Profile", "Employee Management", "Customer Management", "Reports", "Order Management", "Stock"};
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private class GetMenuItems extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetMenuList?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            drawerLayout.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... arg) {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName + "UserID=" + URLEncoder.encode(String.valueOf(userClass.getUserID()), "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp);

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()) {
                if (!message.equals("")) {
                    // Show some message from server
                    Toast.makeText(MainActivity.this, "Rights not found", Toast.LENGTH_SHORT).show();
                } else {
                    drawerLayout.setEnabled(true);

                    initItems();

                    genData();
                    addDrawerItems();
                    setupDrawer();

                    if (savedInstanceStateGlobal == null)
                        selectFirstItem();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Dashboard</font>"));
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(networkResp);

            for (int i = 0 ; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sideMenuItems.add(jsonObject.getString("MenuName"));
                sideMenuGroups.add(jsonObject.getString("GroupName"));
                sortNumber.add(jsonObject.getInt("SortNo"));
            }

            if (sideMenuItems.size() == 0){
                message = "Deen's Cheese";
                messageDetail = "Some error occurred";
            }

        } catch (JSONException e) {
            e.printStackTrace();
            message = "Deen's Cheese";
            messageDetail = "Some error occurred";
        }
    }

    private void loadPreferences(){
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}