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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
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
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class UpdateStock extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    RelativeLayout parentView;
    RelativeLayout loadingView;

    ArrayList<StockItem> products = new ArrayList<>();

    Spinner productSpinner;
    TextView startDate, endDate;
    AppCompatButton getData;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    TextView selectedDateField;

    ListView stockList;
    ArrayList<StockListItem> stocks = new ArrayList<>();
    String productId = "";

    RelativeLayout bottomView;
    AppCompatButton increase, decrease, update;
    TextView count;
    int countNumber = 0;
    StockListItem selectedItemToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_stock);
        parentView = findViewById(R.id.parent);
        loadingView = findViewById(R.id.loadingView);
        productSpinner = findViewById(R.id.productSpinner);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        getData = findViewById(R.id.getData);
        stockList = findViewById(R.id.productList);
        bottomView = findViewById(R.id.bottomView);
        count = findViewById(R.id.count);
        increase = findViewById(R.id.increase);
        decrease = findViewById(R.id.decrease);
        update = findViewById(R.id.update);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        loadPreferences();

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(UpdateStock.this.findViewById(android.R.id.content), "Internet not available!");
        }

        findViewById(R.id.back).setOnClickListener(view -> {
            UpdateStock.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        startDate.setOnClickListener(view -> {
            selectedDateField = startDate;
            // Date variable initialized
            date = (view1, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            new DatePickerDialog(UpdateStock.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        endDate.setOnClickListener(view -> {
            selectedDateField = endDate;
            // Date variable initialized
            date = (view1, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            new DatePickerDialog(UpdateStock.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        getData.setOnClickListener(view -> {
            if (startDate.getText().toString().trim().equals("") || endDate.getText().toString().trim().equals("")){
                SnackAlert.error(parentView, "Dates not selected");
            }else {
                if (isNetworkAvailable()) {
                    GetStockList task = new GetStockList();
                    task.execute();
                } else {
                    SnackAlert.error(UpdateStock.this.findViewById(android.R.id.content), "Internet not available!");
                }
            }
        });

        // Set current date to start & end date label
        startDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
        endDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        increase.setOnClickListener(view -> {
            countNumber++;
            count.setText(String.valueOf(countNumber));
        });

        decrease.setOnClickListener(view -> {
            if (Integer.parseInt(count.getText().toString())> 0){
                countNumber--;
                count.setText(String.valueOf(countNumber));
            }
        });

        update.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                UpdateStockItem task = new UpdateStockItem();
                task.execute();
            } else {
                SnackAlert.error(UpdateStock.this.findViewById(android.R.id.content), "Internet not available!");
            }
        });
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(UpdateStock.this);
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        productId = (String.valueOf(products.get(productSpinner.getSelectedItemPosition()).getProductID()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
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
                parseJSONStringToJSONObject(networkResp, "Products");

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
                        ArrayList<String> productNames = new ArrayList<>();
                        for (int i = 0 ; i < products.size(); i++){
                            productNames.add(products.get(i).getProductName());
                        }
                        ArrayAdapter adapter = new ArrayAdapter(UpdateStock.this, R.layout.spinner_item, productNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        productSpinner.setAdapter(adapter);

                        // Set Listener
                        productSpinner.setOnItemSelectedListener(UpdateStock.this);

                        // Call for first time stock list
                        if (products.size() > 0){
                            productId = String.valueOf(products.get(0).getProductID());
                        }
                        if (startDate.getText().toString().trim().equals("") || endDate.getText().toString().trim().equals("")){
                            SnackAlert.error(parentView, "Dates not selected");
                        }else {
                            if (isNetworkAvailable()) {
                                GetStockList task = new GetStockList();
                                task.execute();
                            } else {
                                SnackAlert.error(UpdateStock.this.findViewById(android.R.id.content), "Internet not available!");
                            }
                        }

                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class GetStockList extends AsyncTask<Void, Void, Void> {

        String methodName = "Stock/GetStockList?";

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
                                + "ProductId=" + productId
                                + "&StockDateFrom=" + startDate.getText().toString().trim()
                                + "&StockDateTo=" + endDate.getText().toString().trim()
                        )
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Stock");

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
                    if (stocks.size() > 0) {
                        StockListAdapter adapter = new StockListAdapter(stocks, UpdateStock.this);
                        stockList.setAdapter(adapter);

                        stockList.setOnItemClickListener((adapterView, view, i, l) -> {
                            bottomView.setVisibility(View.VISIBLE);
                            selectedItemToUpdate = stocks.get(i);
                            countNumber = stocks.get(i).getQuantity();
                            count.setText(String.valueOf(countNumber));
                        });

                    } else {
                        showDialog("Deen's Cheese", "No stock found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class UpdateStockItem extends AsyncTask<Void, Void, Void> {

        String methodName = "Stock/UpdateStock?";

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
                                + "StockId=" + URLEncoder.encode(String.valueOf(selectedItemToUpdate.getStockId()), "UTF-8")
                                + "&ProductId=" + URLEncoder.encode(String.valueOf(selectedItemToUpdate.getProductId()), "UTF-8")
                                + "&ProductCost=" + URLEncoder.encode(String.valueOf(selectedItemToUpdate.getProductCost()), "UTF-8")
                                + "&SalePrice=" + URLEncoder.encode(String.valueOf(selectedItemToUpdate.getSalePrice()), "UTF-8")
                                + "&Quantity=" + URLEncoder.encode(String.valueOf(countNumber), "UTF-8")
                                + "&StockDate=" + URLEncoder.encode(String.valueOf(selectedItemToUpdate.getStockDate()), "UTF-8")
                        )
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Update");

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

                showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);

            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Products")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            products.add(new StockItem(jsonObject.getInt("ProductID"),
                                    jsonObject.getString("CompanyName"),
                                    jsonObject.getString("ProductCode"),
                                    jsonObject.getString("ProductName"),
                                    jsonObject.getDouble("ProductSalaPrice"),
                                    jsonObject.getString("ProductType"),
                                    jsonObject.getString("ProductUnit"),
                                    jsonObject.getDouble("TradeOffer")));
                        }
                    } else {
                        message = "Deen's Cheese";
                        messageDetail = "No prodcut found!";
                    }
                } else {
                    message = "Deen's Cheese";
                    messageDetail = "No prodcut found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Stock")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("StockId").equals("0")) {
                        if (jsonArray.length() > 0){
                            stocks.clear();
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            stocks.add(new StockListItem(jsonObject.getInt("StockId"),
                                    jsonObject.getString("ProductName"),
                                    jsonObject.getInt("ProductId"),
                                    jsonObject.getInt("ProductCost"),
                                    jsonObject.getInt("SalePrice"),
                                    jsonObject.getInt("Quantity"),
                                    jsonObject.getString("StockDate"),
                                    jsonObject.getString("StockType")));
                        }
                    } else {
                        message = "Deen's Cheese";
                        messageDetail = "No Stock found!";
                    }
                } else {
                    message = "Deen's Cheese";
                    messageDetail = "No Stock found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Update")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(UpdateStock.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(UpdateStock.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (messageDetail.contains("Update")) {
                            // Do something refresh view
                            bottomView.setVisibility(View.GONE);
                            if (isNetworkAvailable()) {
                                GetStockList task = new GetStockList();
                                task.execute();
                            } else {
                                SnackAlert.error(UpdateStock.this.findViewById(android.R.id.content), "Internet not available!");
                            }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UpdateStock.super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectedDateField.setText(sdf.format(myCalendar.getTime()));
    }

}