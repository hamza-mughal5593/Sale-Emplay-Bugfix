package com.deens.cheese.ui.report;

import static android.content.Context.MODE_PRIVATE;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.REPORT_URL;
import static com.deens.cheese.GlobalVariable.URL;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.StockItem;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentProfileBinding;
import com.deens.cheese.databinding.FragmentReportBinding;
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

public class ReportFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentReportBinding binding;
    RelativeLayout parentView;
    TextView dateFrom, dateTo;
    Spinner productSpinner, reportSpinner;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ArrayList<StockItem> products = new ArrayList<>();

    UserClass userClass;
    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    
    RelativeLayout loadingView;
    String productId = "";
    ArrayList<String> reportName = new ArrayList<>();
    ElasticButton getReport;
    TextView selectedDateField;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    String myFormat = "yyyy-MM-dd";
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

    public static ReportFragment newInstance(String param1) {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        dateFrom = root.findViewById(R.id.fromDate);
        dateTo = root.findViewById(R.id.dateTo);
        productSpinner = root.findViewById(R.id.productSpinner);
        reportSpinner = root.findViewById(R.id.reportName);
        loadingView = root.findViewById(R.id.loadingView);
        getReport = root.findViewById(R.id.bottomButton);

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

        dateFrom.setOnClickListener(view -> {
            selectedDateField = dateFrom;
            date = (view1, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        dateTo.setOnClickListener(view -> {
            selectedDateField = dateTo;
            date = (view1, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // set date for first time
        dateTo.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, -30);
        dateFrom.setText(sdf.format(myCalendar.getTime()));
        myCalendar.add(Calendar.DATE, +30);

        getReport.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                GetReportSheet task = new GetReportSheet();
                task.execute();
            } else {
                SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
            }
        });
        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (products.size() > 0){
            productId = String.valueOf(products.get(productSpinner.getSelectedItemPosition()).getProductID());
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
                        productSpinner.setOnItemSelectedListener(ReportFragment.this);

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

        String methodName = "Reports/GetStockReport?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                Request request = new Request.Builder()
                        .url(REPORT_URL + methodName
                        + "ProductID=" + productId
                        + "&DateFrom=" + dateFrom.getText().toString().trim()
                        + "&DateTo=" + dateTo.getText().toString().trim()
                        + "&ReportName=" + (URLEncoder.encode(String.valueOf(reportSpinner.getSelectedItem()), "UTF-8"))
                        + "&AuthKey=" + "8156sdcas1dcc1d8c4894Coiuj784C8941e856")
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.url().toString()));
                startActivity(browserIntent);

//                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
//                browserIntent.setDataAndType(Uri.parse(String.valueOf(request.url())), "application/pdf");
//
//                Intent chooser = Intent.createChooser(browserIntent, (CharSequence) reportSpinner.getSelectedItem());
//                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//                startActivity(chooser);

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
                    // Shoe report in webview
                }
            }
        }
    }

    private class GetReports extends AsyncTask<Void, Void, Void> {

        String methodName = "Stock/GetStockReportName";

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
                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
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
                        products.add(new StockItem(0, "Deen's Cheese", "Code", "All", 0.0, "", "", 0.0));
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

}