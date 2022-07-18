package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.deens.cheese.databinding.FragmentPreinvoiceBinding;
import com.deens.cheese.ui.preinvoice.PreInvoiceFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.ornach.nobobutton.NoboButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class MappedCustomerView extends AppCompatActivity {

    private FragmentPreinvoiceBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    ArrayList<CustomerClass> customers = new ArrayList<>();
    String selectedCustomerID = "";
    Spinner customerSpinner;
    NoboButton preInvoice;
    ImageView back;
    String whatToGenerate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapped_customer_view);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        customerSpinner = findViewById(R.id.customerSpinner);
        preInvoice = findViewById(R.id.preInvoice);
        back = findViewById(R.id.back);

        whatToGenerate = getIntent().getExtras().getString("From", whatToGenerate);
        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetMappedCustomers task = new GetMappedCustomers();
            task.execute();
        } else {
            SnackAlert.error(findViewById(android.R.id.content), "Internet not available!");
        }

        preInvoice.setOnClickListener(view -> {
            if (whatToGenerate.contains("Purchase Order")){
                startActivity(new Intent(MappedCustomerView.this, ProductListPO.class)
                        .putExtra("CID", selectedCustomerID));
            }else {
                startActivity(new Intent(MappedCustomerView.this, ProductListDailySale.class)
                        .putExtra("CID", selectedCustomerID));
            }
        });

        preInvoice.setText(whatToGenerate);

        back.setOnClickListener(view -> MappedCustomerView.super.onBackPressed());
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private class GetMappedCustomers extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/GetMapedCustomer?UserID=" + userClass.getUserID();

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
                        .url(URL + methodName)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Customer");

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
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    Toast.makeText(MappedCustomerView.this, "Customers not found", Toast.LENGTH_SHORT).show();
                } else {
                    if (customers.size() > 0){
                        selectedCustomerID = String.valueOf(customers.get(0).getCustomerID());
                        ArrayList<String> names = new ArrayList<>();
                        for (int i = 0; i < customers.size(); i++) {
                            names.add(customers.get(i).getCustomerName());
                        }
                        ArrayAdapter adapter = new ArrayAdapter(MappedCustomerView.this, R.layout.spinner_item, names.toArray());
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        customerSpinner.setAdapter(adapter);
                        customerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedCustomerID = String.valueOf(customers.get(i).getCustomerID());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {}
                        });
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("Customer")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0){
                    if (!jsonArray.getJSONObject(0).getString("CustomerCode").equals("null")){
                        customers.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            customers.add(new CustomerClass(jsonObject.getInt("CustomerID"),
                                    jsonObject.getString("CustomerCode"),
                                    jsonObject.getString("CustomerName"),
                                    jsonObject.getInt("CityID"),
                                    jsonObject.getString("CityName"),
                                    jsonObject.getString("Area"),
                                    jsonObject.getString("Address"),
                                    jsonObject.getString("Mobile"),
                                    jsonObject.getString("Mobile1")
                            ));
                        }
                    }else {
                        message = "Deen's Cheese";
                        messageDetail = "No customer found!";
                    }
                }else {
                    message = "Deen's Cheese";
                    messageDetail = "No customer found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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
}