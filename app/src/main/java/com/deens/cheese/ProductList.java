package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.ornach.nobobutton.NoboButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class ProductList extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    String selectedSO = "";
    UserClass userClass;
    CustomerClass selectedCustomer;

    ListView listView;

    TextView offerAmountTxt, subtotalAmountTxt, grandAmountTxt, totalTax;
    EditText taxAmountTxt;
    Double taxPercent = 0.0;
    NoboButton place;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    ArrayList<SOProductClass> products = new ArrayList<>();
    SOProductClass selectedProduct;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    LocationManager lm;
    String lat = "", lng = "", loc = "";

    int detailMethodCount = 0;
    String invoiceID = "";

    Boolean isLastEntry = false;

    // Search a customer
    EditText searchField;
    ImageView search, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_product_list);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        listView = findViewById(R.id.listView);
        offerAmountTxt = findViewById(R.id.offerAmount);
        taxAmountTxt = findViewById(R.id.taxAmount);
        totalTax = findViewById(R.id.totalTax);
        subtotalAmountTxt = findViewById(R.id.subtotalAmount);
        grandAmountTxt = findViewById(R.id.grandAmount);
        place = findViewById(R.id.place);
        searchField = findViewById(R.id.searchField);
        cancel = findViewById(R.id.cancel);
        search = findViewById(R.id.search);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        lm = (LocationManager) ProductList.this.getSystemService(Context.LOCATION_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);    // 10 seconds, in milliseconds
        mLocationRequest.setFastestInterval(1000);   // 1 second, in milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        loadPreferences();

        back = findViewById(R.id.back);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        back.setOnClickListener(view -> {
            ProductList.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        taxAmountTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    // Calculate bottom grand values of invoice
                    int grandOfferAmount = 0;
                    int grandSubtotal = 0;
                    int grandGrandAmount = 0;
                    int grandTaxAmount = 0;
                    int taxPercent = 0;

                    for (int j = 0 ; j < products.size(); j++){
                        int offAmt = products.get(j).getOfferAmount() * products.get(j).getQuantity();
                        int subAmt = products.get(j).getQuantity() * products.get(j).getProductSalaPrice().intValue();
                        grandOfferAmount = grandOfferAmount + offAmt;
                        grandSubtotal = grandSubtotal + subAmt;
                    }
                    // Tax Calculation
                    if (!taxAmountTxt.getText().toString().equals("")){
                        taxPercent = Integer.parseInt(taxAmountTxt.getText().toString().trim());
                        grandTaxAmount = (grandSubtotal - grandOfferAmount) * taxPercent/100;
                        totalTax.setText("Tax:"+ (grandTaxAmount));
                    }
                    // Grand Total Calculation
                    grandGrandAmount = grandSubtotal - grandOfferAmount + grandTaxAmount;
                    grandAmountTxt.setText(String.valueOf(grandGrandAmount));
                    subtotalAmountTxt.setText(String.valueOf(grandSubtotal - grandOfferAmount));
                    offerAmountTxt.setText(String.valueOf(grandOfferAmount));
                }catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
            }
        });

        place.setOnClickListener(view -> {
            if (lat.equals("") || lng.equals("")){
                SnackAlert.error(parentView, "Current location not received, Please wait!");
            }else {

                if (!grandAmountTxt.getText().toString().trim().equals("0")){
                    selectedProduct = products.get(detailMethodCount);
                    if (isNetworkAvailable()) {
                        SubmitMasterInvoice task = new SubmitMasterInvoice();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
            }
        });

        search.setOnClickListener(view -> {
            for (int i = 0 ; i < products.size() ; i++){
                if (products.get(i).getProductName().toUpperCase().contains(searchField.getText().toString().trim().toUpperCase())){
                    listView.smoothScrollToPosition(i);
                }
            }
        });

        cancel.setOnClickListener(view -> {
            searchField.setText("");
            // Re assign adapter to listview of all customers
            hideSoftKeyboard(ProductList.this);
        });
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
        selectedSO = getIntent().getExtras().getString("SO", "");
        String selectedCustomerString = getIntent().getExtras().getString("SelectedCustomer", "");
        selectedCustomer = gson.fromJson(selectedCustomerString, CustomerClass.class);
    }

    private class GetProducts extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetProductListByCustomer?";

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
                        .url(URL + methodName + "CustomerID=" + selectedCustomer.getCustomerID())
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Product");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!ProductList.this.isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        OrderProductAdapter adapter = new OrderProductAdapter(products, ProductList.this);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (view.getId() == R.id.add){
                                quantitySelectionDialog(i);
                            }
                            if (view.getId() == R.id.tradeOffer){
                                View listItem = getViewByPosition(i, listView);
                                EditText offerAmount = listItem.findViewById(R.id.offerAmount);
                                TextView offerLabel = listItem.findViewById(R.id.offerLabel);
                                TextView totalAmount = listItem.findViewById(R.id.totalAmount);
                                if (!offerAmount.getText().toString().trim().equals("")){
                                    try {
                                        if (products.get(i).getProductSalaPrice() >
                                                Double.parseDouble(offerAmount.getText().toString().trim()
                                                        .replace("-", ""))){
                                            products.get(i).setOfferAmount(Integer.parseInt(offerAmount.getText().toString().trim()
                                                    .replace("-", "")));
                                            offerLabel.setText("(Trade Offer: " + String.valueOf(products.get(i).getOfferAmount())
                                                    .replace("-", "") + ")");
                                            offerAmount.setText("");
                                            totalAmount.setText((products.get(i).getQuantity() * (products.get(i).getProductSalaPrice().intValue()
                                                    - products.get(i).getOfferAmount()))  + " PKR");
                                            // Calculate bottom grand values of invoice
                                            int grandOfferAmount = 0;
                                            int grandSubtotal = 0;
                                            int grandGrandAmount = 0;
                                            int grandTaxAmount = 0;
                                            int taxPercent = 0;

                                            for (int j = 0 ; j < products.size(); j++){
                                                int offAmt = products.get(j).getOfferAmount() * products.get(j).getQuantity();
                                                int subAmt = products.get(j).getQuantity() * products.get(j).getProductSalaPrice().intValue();
                                                grandOfferAmount = grandOfferAmount + offAmt;
                                                grandSubtotal = grandSubtotal + subAmt;
                                            }
                                            // Tax Calculation
                                            if (!taxAmountTxt.getText().toString().equals("")){
                                                taxPercent = Integer.parseInt(taxAmountTxt.getText().toString().trim());
                                                grandTaxAmount = (grandSubtotal - grandOfferAmount) * taxPercent/100;
                                                totalTax.setText("Tax:"+ (grandTaxAmount));
                                            }
                                            // Grand Total Calculation
                                            grandGrandAmount = grandSubtotal - grandOfferAmount + grandTaxAmount;
                                            grandAmountTxt.setText(String.valueOf(grandGrandAmount));
                                            //Todo minus offer amount here
                                            subtotalAmountTxt.setText(String.valueOf(grandSubtotal - grandOfferAmount));
                                            offerAmountTxt.setText(String.valueOf(grandOfferAmount));
                                        }
                                    }catch (NumberFormatException ex){
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class SubmitMasterInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/SubmitMstrInvoice?";
        String invoiceDate = "";
        String percentage = "";

        @Override
        protected void onPreExecute() {
            place.setEnabled(false);
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            invoiceDate = df.format(c);

            if (taxAmountTxt.getText().toString().trim().equals("")){
                percentage = "0";
            }else {
                percentage = taxAmountTxt.getText().toString().trim();
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
                                + "InvoiceDate=" + URLEncoder.encode(invoiceDate, "UTF-8")
                                + "&CustomerId=" + URLEncoder.encode(String.valueOf(selectedCustomer.getCustomerID()), "UTF-8")
                                + "&SubTotal=" + URLEncoder.encode(subtotalAmountTxt.getText().toString().trim(), "UTF-8")
                                + "&SaleTaxRate=" + URLEncoder.encode(percentage, "UTF-8")
                                + "&SaleTaxAmount=" + URLEncoder.encode(totalTax.getText().toString().trim().replace("Tax:", ""), "UTF-8")
                                + "&GrandTotal=" + URLEncoder.encode(grandAmountTxt.getText().toString().trim(), "UTF-8")
                                + "&MapLocation=" + URLEncoder.encode(loc, "UTF-8")
                                + "&Lat=" + URLEncoder.encode(lat, "UTF-8")
                                + "&Long=" + URLEncoder.encode(lng, "UTF-8")
                                + "&SOID=" + URLEncoder.encode(selectedSO, "UTF-8"))
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Master");

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
                if (message.equals("Deen's Cheese")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    // Call Invoice detail method recursively
                    invoiceID = message;

                    SnackAlert.success(parentView, "Invoice id: " + invoiceID);

                    if (isNetworkAvailable()) {
                        SubmitDetailInvoice task = new SubmitDetailInvoice();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
            }
        }
    }

    private class SubmitDetailInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/SubmitDetInvoice?";

        @Override
        protected void onPreExecute() {
            place.setEnabled(false);
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName + "InvoiceID=" + invoiceID
                                + "&ProductID=" + URLEncoder.encode(String.valueOf(selectedProduct.getProductID()), "UTF-8")
                                + "&CustomerId=" + URLEncoder.encode(String.valueOf(selectedCustomer.getCustomerID()), "UTF-8")
                                + "&SalePrice=" + URLEncoder.encode(String.valueOf(selectedProduct.getProductSalaPrice().intValue()), "UTF-8")
                                + "&Quantity=" + URLEncoder.encode(String.valueOf(selectedProduct.getQuantity()), "UTF-8")
                                + "&TradeOffer=" + URLEncoder.encode(String.valueOf(selectedProduct.getTradeOffer().intValue()), "UTF-8")
                                + "&Offer_Amt=" + URLEncoder.encode(String.valueOf(selectedProduct.getOfferAmount()), "UTF-8")
                                + "&TotalAmount=" + URLEncoder.encode(String.valueOf((selectedProduct.getQuantity() *
                                selectedProduct.getProductSalaPrice().intValue())-selectedProduct.getOfferAmount()*selectedProduct.getQuantity()), "UTF-8")
                                + "&IslastEntry=" + isLastEntry
                        )
                        .method("POST", body)
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
                loadingView.setVisibility(View.GONE);
                if (!message.contains("Submit")){
                    showDialog("Deen's Cheese", "Some error occurred! Please try later!", "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    detailMethodCount++;

                    if (detailMethodCount < products.size()){
                        selectedProduct = products.get(detailMethodCount);

                        if (detailMethodCount == products.size() - 1){
                            isLastEntry = true;
                        }

                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    }else {
                        showDialog("Deen's Cheese", "Order added successfully!", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Product")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0){
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            products.add(new SOProductClass(jsonObject.getInt("ProductID"),
                                    jsonObject.getString("CompanyName"),
                                    jsonObject.getString("ProductCode"),
                                    jsonObject.getString("ProductName"),
                                    jsonObject.getDouble("ProductSalaPrice"),
                                    jsonObject.getString("ProductType"),
                                    jsonObject.getString("ProductUnit"),
                                    jsonObject.getDouble("TradeOffer"),
                                    jsonObject.getDouble("Discount")
                            ));
                        }
                    }else {
                        message = "Deen's Cheese";
                        messageDetail = "No prodcut found!";
                    }
                }else {
                    message = "Deen's Cheese";
                    messageDetail = "No prodcut found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (methodName.equals("Master")){
            message = networkResp;

            if(!message.matches("\\d+(?:\\.\\d+)?")) {
                message = "Panda Fries";
                messageDetail = "Order couldn't be placed!";
            }
        }

        if (methodName.equals("Detail")){
            message = networkResp;
        }
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(ProductList.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(ProductList.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (message.contains("successfully")) {
                            ProductList.this.onBackPressed();
                            ProductList.this.overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(ProductList.this);
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    ///////////////////////////////////////
    ////// CURRENT LOCATION STARTS ////////

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (lat.equals("")) {
            new Handler().postDelayed(this::settingRequest, 1000);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Location Service Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // DO NOTHING
        if (location != null){
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());
        }
    }

    @SuppressLint("RestrictedApi")
    public void settingRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            final LocationSettingsStates state = result1.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can
                    // initialize location requests here.
                    getLocation();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(ProductList.this, 1000);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        if (mGoogleApiClient.isConnected()) {
                            getLocation();
                        } else {
                            mGoogleApiClient.connect();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog("DENIED PERMISSIONS", "Give required location permission to app \"Deen's Cheese\" in Settings & comeback", "Open Settings", "Cancel", true, "");
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lng = String.valueOf(mLastLocation.getLongitude());
                loc = returnLocation();
            } else {
                Boolean gps_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                    Log.i("EXCEPTION: ", ex.toString());
                }

                if (gps_enabled) {
                    if (!mGoogleApiClient.isConnected())
                        mGoogleApiClient.connect();

                    if (mGoogleApiClient.isConnected()) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                                ProductList.this);
                    } else {
                        settingRequest();
                    }
                } else {
                    settingRequest();
                }
            }
        }
    }

    private String returnLocation() {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(ProductList.this, Locale.getDefault());

        String userLocation = "";

        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);

            if (addresses.size() > 0) {
                if (addresses.get(0).getAddressLine(0) != null) {
                    String postalCode = "";

                    if (addresses.get(0).getPostalCode() != null) {
                        postalCode = " " + addresses.get(0).getPostalCode();
                    }

                    userLocation = addresses.get(0).getAddressLine(0).replace("Pakistan", "")
                            .replace(addresses.get(0).getAdminArea() + postalCode + ", ", "")
                            .replace(", " + addresses.get(0).getSubAdminArea() + ", ", "");
                } else {
                    userLocation = "NOT FOUND";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ProductList.this, "WRONG MAP DATA", Toast.LENGTH_LONG).show();
        }

        return userLocation;
    }

    private void showDialog(String title, String content, String positiveText, String negativeText,
                            Boolean isNegative, String methodType) {
        new BottomDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.drawable.logo)
                .setContent(content)
                .setPositiveText(positiveText)
                .setPositiveBackgroundColorResource(R.color.color_secondry)
                .setPositiveTextColorResource(android.R.color.white)
                .setNegativeText(negativeText)
                .onPositive(dialog -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    Log.d("Alert", "Dialog Dismissed");
                }).show();
    }

    ////// CURRENT LOCATION ENDS //////////
    ///////////////////////////////////////

    @Override
    protected void onRestart() {
        super.onRestart();
    }

//    private Double calculateDiscountPercent(){
//        Double grandAmount = Double.parseDouble(grandTotalTxt.getText().toString().trim());
//        Double subtotalAmount = Double.parseDouble(subtotalTxt.getText().toString().trim());
//        return 1.0 - (grandAmount/subtotalAmount);
//    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition
                + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void quantitySelectionDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductList.this);
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = in.inflate(R.layout.my_dialogue_box, null);
        EditText quantity = v.findViewById(R.id.quantity);
        builder.setTitle("Enter Quantity");
        builder.setView(v);

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        builder.setPositiveButton("Save Quantity", (dialog, which) -> {
            View listItem = getViewByPosition(index, listView);
            TextView quantityLabel = listItem.findViewById(R.id.quantityTitle);
            TextView totalAmount = listItem.findViewById(R.id.totalAmount);
            if (!quantity.getText().toString().trim().equals("")) {
                try {
                    int previousQuantity = products.get(index).getQuantity();

                    if ((previousQuantity + Integer.parseInt(quantity.getText().toString().trim())) > -1) {
                        products.get(index).setQuantity(previousQuantity +
                                Integer.parseInt(quantity.getText().toString().trim()));
                        quantityLabel.setText((products.get(index).getQuantity()) + "x");
                        quantity.setText("");
                        totalAmount.setText((products.get(index).getQuantity() *
                                (products.get(index).getProductSalaPrice().intValue()
                                        - products.get(index).getOfferAmount())) + " PKR");
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
            dialog.cancel();
        });

        builder.show();
    }

}