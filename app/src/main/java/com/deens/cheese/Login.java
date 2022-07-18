package com.deens.cheese;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import id.ionbit.ionalert.IonAlert;

public class Login extends AppCompatActivity {

    ScrollView parentView;
    EditText username, password;
    RelativeLayout loadingView;
    ElasticButton loginButton;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    UserClass loggedInUserClass;
    SharedPreferences  preferences;
    SharedPreferences.Editor editor;
    String savedDay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        parentView = findViewById(R.id.parent);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loadingView = findViewById(R.id.loadingView);
        loginButton = findViewById(R.id.login);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();


        loginButton.setOnClickListener(view -> {
            if (!checkPermission()){
                showSingleActionDialog(Login.this, "GIVE STORAGE PERMISSION", "Please give storage permission and re launch app.", "OK, I Understand!", "Settings" );
            }else {
                callLoginMethod();
            }
        });

        if (!checkPermission()){
            showSingleActionDialog(Login.this, "GIVE STORAGE PERMISSION", "Please give storage permission and re launch app.", "OK, I Understand!", "Settings" );
        }else {
            loadSavedPreferences();
        }
    }

    private void callLoginMethod(){
        hideSoftKeyboard(Login.this);

        if (username.getText().toString().trim().equals("") ||
                password.getText().toString().trim().equals("")){
            SnackAlert.error(parentView, "All fields are required!");
        }else {
            if (!isNetworkAvailable()){
                SnackAlert.error(parentView, "Internet not available!");
            }else {
                LoginTask task = new LoginTask();
                task.execute();
            }
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetLoginUserDetails?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName + "UserName=" + URLEncoder.encode(username.getText().toString().trim(), "UTF-8")
                                + "&Password=" + URLEncoder.encode(password.getText().toString().trim(), "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Login");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    // Unsuccessful Login
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    // Successful Login
                    savedSharedPreference();
                 }
            }
        }
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(Login.this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(Login.this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2296:
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                        // perform action when allow permission success
                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void showSingleActionDialog(Context context, String title, String content, String confirmText, String type){

        if (type.equals("Settings")){
            new IonAlert(context, IonAlert.NORMAL_TYPE)
                    .setTitleText(title)
                    .setContentText(content)
                    .setConfirmText(confirmText)
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation(false);
                        requestPermission();
                    })
                    .show();
        }else {
            new IonAlert(context, IonAlert.NORMAL_TYPE)
                    .setTitleText(title)
                    .setContentText(content)
                    .setConfirmText(confirmText)
                    .setConfirmClickListener(sDialog -> sDialog.dismissWithAnimation(false))
                    .show();
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        try{
            JSONArray jsonArray = new JSONArray(networkResp);
            for (int i = 0 ; i < jsonArray.length() ; i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getInt("UserID") == 0){
                    message = "Deen's Cheese";
                    messageDetail = "Username or Password is wrong";
                }else {
                    loggedInUserClass = new UserClass(
                            jsonObject.getInt("UserID"),
                            jsonObject.getString(("UserName")),
                            jsonObject.getString(("EmpName")),
                            jsonObject.getString(("EmpCode")),
                            jsonObject.getString(("PhoneNo")),
                            jsonObject.getString(("BranchName")),
                            jsonObject.getString(("DepartmentName")),
                            jsonObject.getString(("DesignationName")),
                            jsonObject.getInt(("UserTypeID")),
                            jsonObject.getString(("UserTypeName")),
                            jsonObject.getString(("GM")),
                            jsonObject.getString(("GMName")),
                            jsonObject.getString(("ST")),
                            jsonObject.getString(("STName")),
                            jsonObject.getString(("DM")),
                            jsonObject.getString(("DMName")),
                            jsonObject.getString(("SDM")),
                            jsonObject.getString(("SDMName")),
                            jsonObject.getString(("SO")),
                            jsonObject.getString(("SOName")));

                    Gson gson = new Gson();
                    String json = gson.toJson(loggedInUserClass);
                    editor.putString("User", json);
                    editor.putInt("UserType", loggedInUserClass.getUserTypeID());
                    editor.putInt("UserID", loggedInUserClass.getUserID());
                    editor.apply();
                }
            }
        }catch (Exception ex){
            message = "Error";
            messageDetail = "Some error occurred";
        }
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative){
        if (isNegative){
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(negativeTitle, negativeIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }else {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
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

    // We will save only when a successful login is made
    private void savedSharedPreference(){
        editor.putString("Day", (String) DateFormat.format("dd", new Date()));
        editor.apply();

        // Goto Home Activity now
        startActivity(new Intent(Login.this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
    }

    // We will check each time application is opened
    private void loadSavedPreferences(){
        // Check if shared preferences contains Day value
        if (preferences != null){
            if (preferences.contains("User") && preferences.contains("Day")){
                savedDay = preferences.getString("Day", "");

                // If True the saved day and current day is same
                if (((String) DateFormat.format("dd", new Date())).equals(savedDay)){
                    // Goto Home Activity now
                    startActivity(new Intent(Login.this, MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,
                            R.anim.slide_out_left);
                }
            }
        }
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(Login.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}