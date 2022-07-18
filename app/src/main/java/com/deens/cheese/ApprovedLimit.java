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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.skydoves.elasticviews.ElasticButton;
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
import java.util.ArrayList;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class ApprovedLimit extends AppCompatActivity {

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    ElasticButton grantLimit;
    RelativeLayout loadingView;

    CustomerClass selectedCustomer;
    EditText limit;
    Spinner typeSpinner;
    RelativeLayout parentView;

    // Shared Preference
    SharedPreferences preferences;
    UserClass userClass;
    ArrayList<PaymentType> types = new ArrayList<>();
    String selectedType = "";

    // Products List
    NonScrollListView productList;
    // Total Product List
    ArrayList<ApproveProductClass> products = new ArrayList<>();
    // Customer Product Object with list of products price and discount values
    CustomerProductClass customerProducts;
    ArrayList<String> discounts = new ArrayList<>();
    ArrayList<String> prices = new ArrayList<>();
    int productIndexCount = 0;

    // Check if we are approving for 1st time or updating the already added products
    Boolean isNew = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_approved_limit);

        loadingView = findViewById(R.id.loadingView);
        grantLimit = findViewById(R.id.grantLimit);
        limit = findViewById(R.id.amount);
        typeSpinner = findViewById(R.id.typeSpinner);
        parentView = findViewById(R.id.parentView);
        productList = findViewById(R.id.productList);
        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        isNew = getIntent().getExtras().getBoolean("IsNew");

        loadPreferences();

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

//        if (Home.sideMenuItems.contains("Update Approve Customer")) {
            grantLimit.setVisibility(View.VISIBLE);
//        }else {
//            grantLimit.setVisibility(View.INVISIBLE);
//        }

        grantLimit.setOnClickListener(view -> {
            if (!getPricesAndDiscount()) {
                if (selectedType.equals("")) {
                    SnackAlert.error(parentView, "No payment type selected");
                } else {
                    if (limit.getText().toString().trim().equals("")) {
                        SnackAlert.error(parentView, "No limit selected");
                    } else {
                        try {
                            int limitValue = Integer.parseInt(limit.getText().toString().trim());
                            if (limitValue < 1) {
                                SnackAlert.error(parentView, "Insert right limit value!");
                            } else {
                                if (isNetworkAvailable()) {
                                    if (isNew) {
                                        ApproveLimitTask task = new ApproveLimitTask();
                                        task.execute();
                                    } else {
                                        UpdateCustomerProductRate task = new UpdateCustomerProductRate();
                                        task.execute();
                                    }
                                } else {
                                    SnackAlert.error(parentView, "Internet not available!");
                                }
                            }
                        } catch (NumberFormatException ex) {
                            SnackAlert.error(parentView, "Insert right limit value!");
                        }
                    }
                }
            } else {
                SnackAlert.error(parentView, "Wrong price or discount value entered");
            }
        });

        findViewById(R.id.back).setOnClickListener(view -> ApprovedLimit.super.onBackPressed());

        if (isNetworkAvailable()) {
            GetPaymentTypes task = new GetPaymentTypes();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        if (isNetworkAvailable()) {
            if (isNew) {
                GetProducts task = new GetProducts();
                task.execute();
            } else {
                GetCustomerProducts task = new GetCustomerProducts();
                task.execute();
            }
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        selectedCustomer = gson.fromJson(getIntent().getExtras().getString("SelectedUser"), CustomerClass.class);
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private class GetPaymentTypes extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/GetPaymentTypeList";

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
                parseJSONStringToJSONObject(networkResp, "Type");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    ArrayList<String> paymentTypeName = new ArrayList<>();

                    for (int i = 0; i < types.size(); i++) {
                        paymentTypeName.add(types.get(i).PaymentTypeName);
                    }

                    ArrayAdapter adapter = new ArrayAdapter(ApprovedLimit.this, R.layout.spinner_item, paymentTypeName);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    typeSpinner.setAdapter(adapter);

                    typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedType = types.get(i).getPaymentTypeID();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                }
            }
        }
    }

    private class ApproveLimitTask extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/ApproveCustomer?";

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

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "CustomerID=" + selectedCustomer.getCustomerID()
                                + "&PaymentTypeID=" + selectedType
                                + "&CreditLimit=" + URLEncoder.encode(limit.getText().toString().trim(), "UTF-8")
                                + "&LoginUserID=" + userClass.getUserID()
                                + "&IsApproved=true")
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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    if (messageDetail.contains("Updated") || messageDetail.contains("saved")
                            || messageDetail.contains("Saved")) {
                        if (isNetworkAvailable()) {
                            AddCustomerProductRate task = new AddCustomerProductRate();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        // Show some message from server
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class AddCustomerProductRate extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/AddCustomerProductRate?";

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

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "CustomerID=" + selectedCustomer.getCustomerID()
                                + "&ProductID=" + products.get(productIndexCount).getProductID()
                                + "&SalePrice=" + prices.get(productIndexCount)
                                + "&Discount=" + discounts.get(productIndexCount)
                                + "&LoginUserID=" + userClass.getUserID())
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "AddPrice");

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
                if (messageDetail.contains("Updated") || messageDetail.contains("Already")) {
                    // count track of products numbers incremented
                    productIndexCount++;
                    if (productIndexCount < products.size()) {
                        if (isNetworkAvailable()) {
                            AddCustomerProductRate task = new AddCustomerProductRate();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        showDialog("Deen's Cheese", "Record saved successfully", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                } else {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class UpdateCustomerProductRate extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/UpdateCustomerProductRate?";

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

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "CustomerID=" + selectedCustomer.getCustomerID()
                                + "&ProductID=" + customerProducts.getProductDetails().get(productIndexCount).getProductID()
                                + "&SalePrice=" + prices.get(productIndexCount)
                                + "&Discount=" + discounts.get(productIndexCount)
                                + "&PaymentTypeID=" + types.get(typeSpinner.getSelectedItemPosition()).getPaymentTypeID()
                                + "&CreditLimit=" + URLEncoder.encode(limit.getText().toString().trim(), "UTF-8")
                                + "&LoginUserID=" + userClass.getUserID())
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "UpdateProduct");

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
                if (messageDetail.contains("saved") || messageDetail.contains("Updated")) {
                    // count track of products numbers incremented
                    productIndexCount++;
                    if (productIndexCount < customerProducts.getProductDetails().size()) {
                        if (isNetworkAvailable()) {
                            UpdateCustomerProductRate task = new UpdateCustomerProductRate();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        showDialog("Deen's Cheese", "Record saved successfully", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                } else {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class GetProducts extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetProductList";

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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        ApproveProductAdapter adapter = new ApproveProductAdapter(products, ApprovedLimit.this);
                        productList.setAdapter(adapter);
                    }
                }
            }
        }
    }

    private class GetCustomerProducts extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/GetCustomerProductRate?";

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
                parseJSONStringToJSONObject(networkResp, "CustomerProduct");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (customerProducts.getProductDetails().size() > 0) {
                        ApproveProductAdapter adapter = new ApproveProductAdapter(customerProducts.getProductDetails(), ApprovedLimit.this);
                        productList.setAdapter(adapter);
                    }

                    selectedType = customerProducts.getPaymentTypeName();
                    ArrayList<String> paymentTypeName = new ArrayList<>();

                    for (int i = 0; i < types.size(); i++) {
                        paymentTypeName.add(types.get(i).getPaymentTypeName());
                    }

                    typeSpinner.setSelection(paymentTypeName.indexOf(customerProducts.getPaymentTypeName()));
                    limit.setText(String.valueOf(customerProducts.getCreditLimit().intValue()));
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Add") || methodName.equals("UpdateProduct")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

        if (methodName.equals("AddPrice")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

        if (methodName.equals("Type")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    types.add(new PaymentType(String.valueOf(jsonObject.getInt("PaymentTypeID")),
                            jsonObject.getString("PaymentTypeName")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Product")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            products.add(new ApproveProductClass(jsonObject.getInt("ProductID"),
                                    jsonObject.getString("CompanyName"),
                                    jsonObject.getString("ProductCode"),
                                    jsonObject.getString("ProductName"),
                                    jsonObject.getDouble("ProductSalaPrice"),
                                    jsonObject.getString("ProductType"),
                                    jsonObject.getString("ProductUnit"),
                                    jsonObject.getDouble("TradeOffer"),
                                    0.0));
                        }
                    } else {
                        message = "Deen's Cheese";
                        messageDetail = "No product found!";
                    }
                } else {
                    message = "Deen's Cheese";
                    messageDetail = "No product found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("CustomerProduct")) {
            try {
                // Whole Object of Customer Product
                JSONObject jsonObject = new JSONObject(networkResp);
                // Product Detail Array
                JSONArray jsonArray = jsonObject.getJSONArray("ProducDetails");
                // Extract Customer Product List
                ArrayList<ApproveProductClass> productDetails = new ArrayList<>();
                // Loop through each object of Customer Product Details
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    productDetails.add(new ApproveProductClass(
                            jsonObject1.getInt("ProductID"),
                            jsonObject1.getString("CompanyName"),
                            jsonObject1.getString("ProductCode"),
                            jsonObject1.getString("ProductName"),
                            jsonObject1.getDouble("ProductSalaPrice"),
                            jsonObject1.getString("ProductType"),
                            jsonObject1.getString("ProductUnit"),
                            jsonObject1.getDouble("TradeOffer"),
                            jsonObject1.getDouble("Discount")));
                }

                customerProducts = new CustomerProductClass(
                        jsonObject.getInt("CustomerID"),
                        jsonObject.getString("CustomerCode"),
                        jsonObject.getString("CustomerName"),
                        jsonObject.getInt("CityID"),
                        jsonObject.getString("CityName"),
                        jsonObject.getString("Area"),
                        jsonObject.getString("Address"),
                        jsonObject.getString("Mobile"),
                        jsonObject.getInt("SO_ID"),
                        jsonObject.getString("SO_Name"),
                        jsonObject.getString("CreditLimit").equals("null")? 0 : jsonObject.getDouble("CreditLimit"),
                        jsonObject.getString("PaymentTypeID").equals("null")? 0 : jsonObject.getInt("PaymentTypeID"),
                        jsonObject.getString("PaymentTypeName").equals("null")? "Cash": jsonObject.getString("PaymentTypeName"),
                        productDetails);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(ApprovedLimit.this);
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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
                        if (messageDetail.contains("saved")) {
                            ApprovedLimit.super.onBackPressed();
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    // We will call this method each time we call approve limit & products
    private boolean getPricesAndDiscount() {
        boolean wrongPrice = false;

        prices.clear();
        discounts.clear();

        try {
            ArrayList<ApproveProductClass> list;
            if (isNew) {
                list = products;
            } else {
                list = customerProducts.getProductDetails();
            }
            for (int i = 0; i < list.size(); i++) {
                View view = productList.getChildAt(i);
                EditText price = view.findViewById(R.id.price);
                EditText discount = view.findViewById(R.id.discount);

                int priceValue;
                int discountValue;
                priceValue = Integer.parseInt(price.getText().toString().trim());
                discountValue = Integer.parseInt(discount.getText().toString().trim());

                prices.add(String.valueOf(Integer.parseInt(price.getText().toString().trim()))
                        .replace("-", ""));
                discounts.add(String.valueOf(Integer.parseInt(discount.getText().toString().trim()))
                        .replace("-", ""));

                if (priceValue < 1) {
                    wrongPrice = true;
                }
                if (discountValue < 0) {
                    wrongPrice = true;
                }
            }

            if (prices.contains("") || prices.contains("0")
                    || discounts.contains("")) {
                wrongPrice = true;
            }


        } catch (NumberFormatException ex) {
            wrongPrice = true;
        }

        return wrongPrice;
    }
}