package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class SaleTargetView extends AppCompatActivity {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    String userid = "";
    SaleTargetClass detail;

    TextView month, fromDate, toDate, totalTarget, totalDays, achieveTarget,
    achieveDays, remainingTarget, remainingDays, pendingTarget, currentRatio,
            requiredRatio, remarks;

    UserClass userClass;
    // Shared Preference
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_sale_target_view);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);

        month = findViewById(R.id.month);
        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);
        totalDays = findViewById(R.id.totalDays);
        achieveDays = findViewById(R.id.achieveDays);
        totalTarget = findViewById(R.id.totalTarget);
        achieveTarget = findViewById(R.id.achieveTarget);
        remainingTarget = findViewById(R.id.remainingTarget);
        remainingDays = findViewById(R.id.remainingDays);
        pendingTarget = findViewById(R.id.pendingTarget);
        currentRatio = findViewById(R.id.currentRatio);
        requiredRatio = findViewById(R.id.requiredRatio);
        remarks = findViewById(R.id.remarks);

        loadPreferences();

        back = findViewById(R.id.back);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        back.setOnClickListener(view -> {
            SaleTargetView.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        if (isNetworkAvailable()){
            GetSaleTargetDetail task = new GetSaleTargetDetail();
            task.execute();
        }else {
            SnackAlert.error(parentView, "Internet not available");
        }
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(SaleTargetView.this);
                return false;
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

    private class GetSaleTargetDetail extends AsyncTask<Void, Void, Void> {

        String methodName = "Dashboard/GetSaleTargetDetails?";

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
                        .url(URL + methodName
                                + "UserID=" + userid)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Detail");

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
                loadingView.setVisibility(View.INVISIBLE);
                if (!message.equals("")){
                    loadingView.setVisibility(View.GONE);
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    if (detail.getResult().equals("null")){
                        // Show some message from server
                        showDialog("Deen's Cheese", "No detail received", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }else {
                        month.setText(detail.getMonthName());
                        fromDate.setText(detail.getDateFrom());
                        toDate.setText(detail.getDateTo());
                        totalTarget.setText(detail.getSaleTarget());
                        totalDays.setText(detail.getTotalDays().replace(" Days", ""));
                        achieveTarget.setText(detail.getAchieveTarget());
                        achieveDays.setText(detail.getPassedDays().replace(" Days", ""));
                        remainingTarget.setText(detail.getRemainingTarget());
                        remainingDays.setText(detail.getRemainingDays().replace(" Days", ""));
                        pendingTarget.setText(detail.getRemainingTarget());
                        currentRatio.setText(detail.getCurrentTargetRatio());
                        requiredRatio.setText(detail.getRequiredTargetPerday());
                        remarks.setText(detail.getResult());

                        if (detail.getResultColour().toUpperCase().equals("RED")){
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_red));
                        }else if (detail.getResultColour().toUpperCase().equals("GREEN")){
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_green));
                        } else {
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_yellow));
                        }
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Detail")) {
            try {
                JSONObject jsonObject = new JSONObject(networkResp);
                detail = new SaleTargetClass(jsonObject.getString("Result"),
                        jsonObject.getString("ResultColour"),
                        jsonObject.getString("DateFrom"),
                        jsonObject.getString("DateTo"),
                        jsonObject.getString("MonthName"),
                        jsonObject.getString("TotalDays"),
                        jsonObject.getString("PassedDays"),
                        jsonObject.getString("RemainingDays"),
                        jsonObject.getString("SaleTarget"),
                        jsonObject.getString("AchieveTarget"),
                        jsonObject.getString("RemainingTarget"),
                        jsonObject.getString("PerDayTarget"),
                        jsonObject.getString("CurrentTargetRatio"),
                        jsonObject.getString("RequiredTargetPerday"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
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
        } else {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (messageDetail.contains("Successfully Update")) {
                            super.onBackPressed();
                        }
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    private void loadPreferences(){
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
        userid = String.valueOf(userClass.getUserID());
    }
}