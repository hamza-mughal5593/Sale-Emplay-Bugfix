package com.deens.cheese;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.deens.cheese.databinding.ActivityHomeBinding;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    UserClass userClass;
    String message = "";
    String messageDetail = "";
    public static ArrayList<String> sideMenuItems = new ArrayList<>();

    DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        loadPreferences();

        setSupportActionBar(binding.appBarHome.toolbar);
        binding.appBarHome.fab.setVisibility(View.GONE);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.nav_attendence, R.id.nav_attendence_history, R.id.nav_employees, R.id.nav_customer,
                R.id.nav_approve, R.id.nav_orders, R.id.nav_pickOrder,
                R.id.nav_stock, R.id.nav_stock_record,
                R.id.nav_target, R.id.nav_report, R.id.nav_salereport,R.id.nav_cash, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (isNetworkAvailable()) {
            GetMenuItems task = new GetMenuItems();
            task.execute();
        }else {
            Toast.makeText(Home.this, "Internet not available", Toast.LENGTH_LONG).show();
        }

        View headerView = navigationView.getHeaderView(0);
        TextView name = headerView.findViewById(R.id.name);
        TextView designation = headerView.findViewById(R.id.designation);
        AppCompatButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(view -> {
            // Clear all Shared Preference Values
            editor.clear();
            editor.apply();
            sideMenuItems.clear();

            Intent intent = new Intent(this, Login.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        name.setText(userClass.getUserName().toUpperCase());
        designation.setText(userClass.getDesignationName());

        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) { }

            @Override
            public void onDrawerOpened(View view) {hideSoftKeyboard(Home.this);}

            @Override
            public void onDrawerClosed(View view) {
                // your refresh code can be called from here
            }

            @Override
            public void onDrawerStateChanged(int i) {}
        });
    }

    private void hideSideMenuItems() {
        Menu nav_Menu = navigationView.getMenu();
        if (sideMenuItems.contains("Employee Management")){
            nav_Menu.findItem(R.id.nav_employees).setVisible(true);
        }
        if (sideMenuItems.contains("MarkAttence")){
            nav_Menu.findItem(R.id.nav_attendence).setVisible(true);
        }
        if (sideMenuItems.contains("AttdanceReport")){
            nav_Menu.findItem(R.id.nav_attendence_history).setVisible(true);
        }
        if (sideMenuItems.contains("Customer Management")){
            nav_Menu.findItem(R.id.nav_customer).setVisible(true);
        }
        if (sideMenuItems.contains("Approve Customer")){
            nav_Menu.findItem(R.id.nav_approve).setVisible(true);
        }
        if (sideMenuItems.contains("Order Management")){
            nav_Menu.findItem(R.id.nav_orders).setVisible(true);
        }
        if (sideMenuItems.contains("Pick Order")){
            nav_Menu.findItem(R.id.nav_pickOrder).setVisible(true);
        }
        if (sideMenuItems.contains("Profile")){
            nav_Menu.findItem(R.id.nav_profile).setVisible(true);
        }
        if (sideMenuItems.contains("StoreKeeper")){
            nav_Menu.findItem(R.id.nav_stock).setVisible(true);
            nav_Menu.findItem(R.id.nav_stock_record).setVisible(true);
        }
        if (sideMenuItems.contains("Sale Target")){
            nav_Menu.findItem(R.id.nav_target).setVisible(true);
        }
        if (sideMenuItems.contains("Stock Report")){
            nav_Menu.findItem(R.id.nav_report).setVisible(true);
        }
        if (sideMenuItems.contains("Sale Report")){
            nav_Menu.findItem(R.id.nav_salereport).setVisible(true);
        }
        if (sideMenuItems.contains("Cash Received")){
            nav_Menu.findItem(R.id.nav_cash).setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(Home.this, SaleTargetView.class)
                        .putExtra("ID", userClass.getUserID()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
            drawer.setEnabled(false);
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
                    Toast.makeText(Home.this, "Rights not found", Toast.LENGTH_SHORT).show();
                } else {
                    drawer.setEnabled(true);
                    hideSideMenuItems();
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

}