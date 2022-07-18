package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class TargetList extends AppCompatActivity {

    String selectedSOID = "0";

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    ListView listView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    ArrayList<TargetClass> targets = new ArrayList<>();

    AppCompatButton addUpdate;
    RelativeLayout centreView;
    EditText amount;
    TextView startDate, endDate;
    Boolean isUpdate = false;
    ImageButton addTarget;
    TargetClass selectedTarget;
    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_target_list);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        listView = findViewById(R.id.listView);
        back = findViewById(R.id.back);
        addUpdate = findViewById(R.id.addUpdate);
        centreView = findViewById(R.id.centerView);
        amount = findViewById(R.id.amount);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        addTarget = findViewById(R.id.addTarget);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        selectedSOID = getIntent().getExtras().getString("SO");

        // Date variable initialized
        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        if (isNetworkAvailable()){
            GetTargetList task = new GetTargetList();
            task.execute();
        }else {
            SnackAlert.error(parentView, "Internet not available");
        }

        back.setOnClickListener(view -> {
            TargetList.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        addUpdate.setOnClickListener(view -> {
            try {
                if (amount.getText().toString().equals("") ||
                        startDate.getText().toString().trim().equals("") ||
                        endDate.getText().toString().trim().equals("")){
                    SnackAlert.error(parentView, "Please enter amount & dates");
                }else {
                    Integer.parseInt(amount.getText().toString().trim());

                    if (isNetworkAvailable()){
                        AddUpdateTarget task = new AddUpdateTarget();
                        task.execute();
                    }else {
                        SnackAlert.error(parentView, "Internet not available");
                    }
                }

            }catch (NumberFormatException ex){
                ex.printStackTrace();
                SnackAlert.error(parentView, "Amount is not correct");
            }
        });

        addTarget.setOnClickListener(view -> {
            centreView.setVisibility(View.VISIBLE);
            isUpdate = false;
            amount.setText("");
            startDate.setText("");
            endDate.setText("");
            addUpdate.setText("Add Target");
        });

        startDate.setOnClickListener(view -> {
            selectedDate = startDate;
            new DatePickerDialog(TargetList.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        endDate.setOnClickListener(view -> {
            selectedDate = endDate;
            new DatePickerDialog(TargetList.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

    }

    private class AddUpdateTarget extends AsyncTask<Void, Void, Void> {

        String methodName = "SaleTarget/UpdateSaleTarget?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            if (isUpdate){
                methodName = "SaleTarget/UpdateSaleTarget?" + "ID="+selectedTarget.getID() + "&";
            }else {
                methodName = "SaleTarget/AddSaleTarget?";
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "SO_ID=" + URLEncoder.encode(selectedSOID, "UTF-8")
                                + "&SaleTarget=" + URLEncoder.encode(amount.getText().toString().trim(), "UTF-8")
                                + "&DateFrom=" + URLEncoder.encode(startDate.getText().toString().trim(), "UTF-8")
                                + "&DateTo=" + URLEncoder.encode(endDate.getText().toString().trim(), "UTF-8")
                                + "&LoginUserID=" + userClass.getUserID()
                                )
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Add");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(TargetList.this);
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
                        if (messageDetail.contains("Update") || messageDetail.contains("Added")) {
                            centreView.setVisibility(View.GONE);
                            if (isNetworkAvailable()){
                                GetTargetList task = new GetTargetList();
                                task.execute();
                            }else {
                                SnackAlert.error(parentView, "Internet not available");
                            }
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

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("Target")){
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0){
                    if (jsonArray.getJSONObject(0).getInt("ID") != 0){
                        targets.clear();
                    }
                }
                for (int i = 0 ; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getInt("ID") != 0){
                        targets.add(new TargetClass(jsonObject.getInt("ID"),
                                jsonObject.getInt("SO_ID"),
                                jsonObject.getString("SOName"),
                                jsonObject.getString("DateFrom"),
                                jsonObject.getString("DateTo"),
                                jsonObject.getDouble("SaleTarget")));
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        if (methodName.equals("Add")){
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }
    }

    private class GetTargetList extends AsyncTask<Void, Void, Void> {

        String methodName = "SaleTarget/GetSaleTargetList?";

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
                        .url(URL + methodName + "SO_ID=" + selectedSOID)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Target");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    if (targets.size() > 0){
                        // Populate our ListView
                        TargetListAdapter adapter = new TargetListAdapter(targets, TargetList.this);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            isUpdate = true;
                            centreView.setVisibility(View.VISIBLE);
                            addUpdate.setText("Update Target");
                            selectedTarget = targets.get(i);
                            amount.setText(String.valueOf(selectedTarget.getSaleTarget().intValue()));
                            startDate.setText(selectedTarget.getDateFrom().replace("T00:00:00", ""));
                            endDate.setText(selectedTarget.getDateTo().replace("T00:00:00", ""));
                        });
                    }else {
                        showDialog("Deen's Cheese", "No target found", "OK",
                                "Cancel", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectedDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onBackPressed() {
        TargetList.super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}