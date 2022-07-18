package com.deens.cheese.ui.salereport;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.REPORT_URL;
import static com.deens.cheese.GlobalVariable.URL;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.CustomerClass;
import com.deens.cheese.EmployeeClass;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.StockItem;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentSalereportBinding;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
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

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class SaleReportFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentSalereportBinding binding;
    RelativeLayout parentView;
    TextView dateFrom, dateTo, lastDateFrom, lastDateTo;
    Spinner productSpinner, reportSpinner, salemanSpinner, customerSpinner;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ArrayList<StockItem> products = new ArrayList<>();
    ArrayList<EmployeeClass> salemans = new ArrayList<>();
    ArrayList<CustomerClass> customers = new ArrayList<>();

    UserClass userClass;
    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    
    RelativeLayout loadingView;
    String productId = "", so_id = "", salemanID = "", customerID = "";
    ArrayList<String> reportName = new ArrayList<>();
    ElasticButton getReport;
    TextView selectedDateField;
    EditText rateValue;
    LinearLayout comparisonView;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    String myFormat = "yyyy-MM-dd";
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

    public static SaleReportFragment newInstance(String param1) {
        SaleReportFragment fragment = new SaleReportFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSalereportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        dateFrom = root.findViewById(R.id.fromDate);
        dateTo = root.findViewById(R.id.dateTo);
        productSpinner = root.findViewById(R.id.productSpinner);
        reportSpinner = root.findViewById(R.id.reportName);
        loadingView = root.findViewById(R.id.loadingView);
        getReport = root.findViewById(R.id.bottomButton);
        salemanSpinner = root.findViewById(R.id.salemanSpinner);
        customerSpinner = root.findViewById(R.id.customerSpinner);
        lastDateFrom = root.findViewById(R.id.lastFromDate);
        lastDateTo = root.findViewById(R.id.lastDateTo);
        rateValue = root.findViewById(R.id.rateValue);
        comparisonView = root.findViewById(R.id.comparisonView);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        loadPreferences();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        if (isNetworkAvailable()) {
            GetReports task = new GetReports();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        if (isNetworkAvailable()) {
            GetEmployeesList task = new GetEmployeesList();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        dateFrom.setOnClickListener(view -> {
            selectedDateField = dateFrom;
            initializeDateValue();
        });

        dateTo.setOnClickListener(view -> {
            selectedDateField = dateTo;
            initializeDateValue();
        });

        lastDateFrom.setOnClickListener(view -> {
            selectedDateField = lastDateFrom;
            initializeDateValue();
        });

        lastDateTo.setOnClickListener(view -> {
            selectedDateField = lastDateTo;
            initializeDateValue();
        });

        // set date for first time
        dateTo.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, -30);
        dateFrom.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, -1);
        lastDateTo.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, -29);
        lastDateFrom.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, +60);

        getReport.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                try {
                    if (!rateValue.getText().toString().trim().equals("")){
                        Integer.parseInt(rateValue.getText().toString().trim());
                    }
                    GetReportSheet task = new GetReportSheet();
                    task.execute();
                }catch (NumberFormatException ex){
                    SnackAlert.error(parentView, "Please insert valid rate value");
                }
            } else {
                SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
            }
        });

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.productSpinner){
            if (products.size() > 0){
                productId = String.valueOf(products.get(productSpinner.getSelectedItemPosition()).getProductID());
            }
        }
        if (adapterView.getId() == R.id.salemanSpinner){
            if (salemans.size() > 0){
                so_id = String.valueOf(salemans.get(salemanSpinner.getSelectedItemPosition()).getSO());
                salemanID = String.valueOf(salemans.get(salemanSpinner.getSelectedItemPosition()).getUserID());
            }
            if (so_id.equals("0") && salemanID.equals("0")){
                customers.clear();
                customers.add(new CustomerClass(0, "ALL"));
                ArrayList<String> temp = new ArrayList<>();
                temp.add("ALL");

                // Load Customer Spinner
                ArrayAdapter adapter1 = new ArrayAdapter(getContext(), R.layout.spinner_item, temp);
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                customerSpinner.setAdapter(adapter1);
                customerID = "0";
            }else {
                if (isNetworkAvailable()) {
                    GetCustomerList task = new GetCustomerList();
                    task.execute();
                } else {
                    SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                }
            }
        }
        if (adapterView.getId() == R.id.customerSpinner){
            if (customers.size() > 0){
                customerID = String.valueOf(customers.get(customerSpinner.getSelectedItemPosition()).getCustomerID());
            }
        }
        if (adapterView.getId() == R.id.reportName){
            if (reportName.size() > 0){
                if (reportSpinner.getSelectedItem().equals("Sale Comparision Summary")){
                    comparisonView.setVisibility(View.VISIBLE);
                }else {
                    comparisonView.setVisibility(View.GONE);
                }
            }
        }
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
            if (getContext() != null) {
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
                        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, productNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        productSpinner.setAdapter(adapter);

                        // Set Listener
                        productSpinner.setOnItemSelectedListener(SaleReportFragment.this);

                        // Call for first time stock list
                        if (products.size() > 0){
                            productId = String.valueOf(products.get(0).getProductID());
                        }

                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class GetReportSheet extends AsyncTask<Void, Void, Void> {

        String methodName = "Reports/GetSaleReport?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                String parameters = "SaleManID=" + salemanID
                        + "&CustomerID=" + customerID
                        + "&ProductID=" + productId
                        + "&DateFrom=" + URLEncoder.encode(dateFrom.getText().toString().trim(), "UTF-8")
                        + "&DateTo=" + URLEncoder.encode(dateTo.getText().toString().trim(), "UTF-8")
                        + "&ReportName=" + (URLEncoder.encode(String.valueOf(reportSpinner.getSelectedItem()), "UTF-8"))
                        + "&AuthKey=" + "8156sdcas1dcc1d8c4894Coiuj784C8941e856";

                if (reportSpinner.getSelectedItem().equals("Sale Comparision Summary")){
                    parameters = parameters
                            + "&LastDateForm=" + URLEncoder.encode(lastDateFrom.getText().toString().trim(), "UTF-8")
                            + "&LastDateTo=" + URLEncoder.encode(lastDateTo.getText().toString().trim(), "UTF-8");
                }

                parameters = parameters + "&RateValue=" + URLEncoder.encode(rateValue.getText().toString().trim(), "UTF-8");

                Request request = new Request.Builder()
                        .url(REPORT_URL + methodName + parameters)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.url().toString()));
                startActivity(browserIntent);

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (getContext() != null) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class GetReports extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetSaleReportName";

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
            if (getContext() != null) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (reportName.size() > 0) {
                        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, reportName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        reportSpinner.setAdapter(adapter);

                        reportSpinner.setOnItemSelectedListener(SaleReportFragment.this);

                    } else {
                        showDialog("Deen's Cheese", "No report found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class GetCustomerList extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/GetCustomerList?IsActive=true&";

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
                        .url(URL + methodName + "SO_ID=" + so_id)
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
            if (getContext() != null){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                    customers.clear();
                    customerID = "";
                } else {
                    if (customers.size() > 0) {
                        ArrayList<String> employeeName = new ArrayList<>();

                        for (int i = 0 ; i < customers.size(); i++) {
                            employeeName.add(customers.get(i).getCustomerName());
                        }

                        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, employeeName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        customerSpinner.setAdapter(adapter);

                        // Set Listener
                        customerSpinner.setOnItemSelectedListener(SaleReportFragment.this);

                        if (customers.size() > 0){
                            customerID = String.valueOf(customers.get(0).getCustomerID());
                        }
                    } else {
                        showDialog("Deen's Cheese", "No customer found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class GetEmployeesList extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetEmployeeList?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
            salemans.clear();
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName + "EmpID=0&UserTypeID=7&UserID=" + URLEncoder.encode(String.valueOf(userClass.getUserID()), "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Employees");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (getContext() != null){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    if (salemans.size() > 0) {
                        ArrayList<String> employeeName = new ArrayList<>();

                        for (int i = 0 ; i < salemans.size(); i++) {
                            employeeName.add(salemans.get(i).getEmpName());
                        }

                        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, employeeName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        salemanSpinner.setAdapter(adapter);

                        // Set Listener
                        salemanSpinner.setOnItemSelectedListener(SaleReportFragment.this);

                        if (salemans.size() > 0){
                            so_id = String.valueOf(salemans.get(0).getSO());
                            salemanID = String.valueOf(salemans.get(0).getUserID());
                        }

                        customers.add(new CustomerClass(0, "ALL"));
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add("ALL");

                        // Load Customer Spinner
                        ArrayAdapter adapter1 = new ArrayAdapter(getContext(), R.layout.spinner_item, temp);
                        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        customerSpinner.setAdapter(adapter1);

                        customerID = "0";
                    } else {
                        showDialog("Deen's Cheese", "No Saleman found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Products")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")) {
                        products.add(new StockItem(0, "Deen's Cheese", "Code", "ALL", 0.0, "", "", 0.0));
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
                for (int i = 0 ; i < jsonArray.length() ; i++){
                    reportName.add((String) jsonArray.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (methodName.equals("Employees")) {
            try {
                salemans.add(new EmployeeClass(0, "ALL", 0));
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (userClass.getUserTypeID() < jsonObject.getInt("UserTypeID")){
                        salemans.add(new EmployeeClass(jsonObject.getInt("UserID"),
                                jsonObject.getString("EmpName"),
                                jsonObject.getInt("SO")));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                message = "Deen's Cheese";
                messageDetail = "No Saleman found";
            }
        }
        if (methodName.equals("Customer")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0){
                    customers.clear();
                    customerID = "";
                    if (!jsonArray.getJSONObject(0).getString("CustomerCode").equals("null")){
                        customers.clear();
                        customers.add(new CustomerClass(0, "ALL"));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            customers.add(new CustomerClass(jsonObject.getInt("CustomerID"),
                                    jsonObject.getString("CustomerName")));
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(getActivity())
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (messageDetail.contains("Update")) {
                            // Do something refresh view
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
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Update label for Date fields
    private void updateLabel() {
        selectedDateField.setText(sdf.format(myCalendar.getTime()));
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

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
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

    private void initializeDateValue(){
        date = (view1, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}