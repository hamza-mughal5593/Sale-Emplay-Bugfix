package com.deens.cheese.ui.dashboard;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.approve.ApproveFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;
import com.hadiidbouk.charts.BarData;
import com.hadiidbouk.charts.ChartProgressBar;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.AssignedOrders;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.GraphClass;
import com.deens.cheese.OrderClass;
import com.deens.cheese.ProductDetailClass;
import com.deens.cheese.R;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentDashboardBinding;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import im.dacer.androidcharts.LineView;

import net.cr0wd.snackalert.SnackAlert;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    RelativeLayout parentView, loadingView;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    ArrayList<GraphClass> monthlyData = new ArrayList<>();
    ArrayList<GraphClass> weeklyData = new ArrayList<>();

    RelativeLayout orderView;
    TextView orderCount;
    SpinKitView loader;

    UserClass userClass;
    SharedPreferences preferences;
    ArrayList<OrderClass> orders = new ArrayList<>();

    HorizontalScrollView monthlyView;
    LineView monthlyGraph;
    ChartProgressBar barProgressBar;
    Boolean isFirstTime = true;

    public static DashboardFragment newInstance(String param1) {
        DashboardFragment fragment = new DashboardFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        orderView = root.findViewById(R.id.orderView);
        orderCount = root.findViewById(R.id.orderCount);
        loader = root.findViewById(R.id.loader);
        monthlyView = root.findViewById(R.id.monthlyView);
        monthlyGraph = root.findViewById(R.id.monthlyGraph);
        barProgressBar = root.findViewById(R.id.weeklyProgressBar);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);

        loadPreferences();

        if (userClass.getUserTypeID() == 7) {
            if (isNetworkAvailable()) {
                GetOrderCount task = new GetOrderCount();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        } else {
            orderView.setVisibility(View.GONE);
        }

        if (isNetworkAvailable()) {
            GetMonthlyData task = new GetMonthlyData();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet Connectivity Problem!");
        }

        orderView.setOnClickListener(view -> {
            Gson gson = new Gson();
            String json = gson.toJson(orders);
            startActivity(new Intent(getContext(), AssignedOrders.class).putExtra("orders", json));
            getActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
            getActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        return root;
    }

    private class GetOrderCount extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetInvoiceDetails?LoginUserID=" + userClass.getUserID() + "&IsAssign=true&Is_Post=true";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loader.setVisibility(View.VISIBLE);
            orders.clear();
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
                parseJSONStringToJSONObject(networkResp, "");

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
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    orderCount.setText(String.valueOf(orders.size()));
                    orderView.setVisibility(View.VISIBLE);
                    loader.setVisibility(View.GONE);
                }
            }
        }
    }

    private class GetWeeklyData extends AsyncTask<Void, Void, Void> {

        String methodName;

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            methodName = "Dashboard/GetWeeklyGraph?LoginUserID=" + userClass.getUserID();
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
                parseJSONStringToJSONObject(networkResp, "Week");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {

                    ArrayList<String> xAxisList = new ArrayList<>();
                    ArrayList<Integer> yAxisList = new ArrayList<>();

                    for (int i = 0; i < weeklyData.size(); i++) {
                        xAxisList.add(weeklyData.get(i).getName());
                        yAxisList.add(Integer.valueOf(weeklyData.get(i).getAmount()));
                    }

                    loadBarChart(xAxisList, yAxisList);
                }
            }
        }
    }

    private class GetMonthlyData extends AsyncTask<Void, Void, Void> {

        String methodName;

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            methodName = "Dashboard/GetMonthlyGraph?LoginUserID=" + userClass.getUserID();
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
                parseJSONStringToJSONObject(networkResp, "Month");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {

                    ArrayList<String> xAxisList = new ArrayList<>();
                    ArrayList<Integer> yAxisList = new ArrayList<>();

                    for (int i = 0; i < monthlyData.size(); i++) {
                        xAxisList.add(monthlyData.get(i).getName());
                        yAxisList.add(Integer.valueOf(monthlyData.get(i).getAmount()));
                    }

                    loadLineChart(xAxisList, yAxisList);

                    if (isNetworkAvailable()) {
                        GetWeeklyData task = new GetWeeklyData();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet Connectivity Problem!");
                    }
                }
            }
        }
    }

    private void loadLineChart(ArrayList<String> xAxisData, ArrayList<Integer> yAxisData) {
        monthlyGraph.setDrawDotLine(true);
        monthlyGraph.setShowPopup(LineView.SHOW_POPUPS_All);
        monthlyGraph.setBottomTextList(xAxisData);
        monthlyGraph.setColorArray(new int[]{Color.BLACK, Color.GREEN, Color.GRAY, Color.CYAN});
        ArrayList<ArrayList<Integer>> dataLists = new ArrayList<>();
        dataLists.add(yAxisData);
        monthlyGraph.setDataList(dataLists);
    }

    private void loadBarChart(ArrayList<String> xAxisData, ArrayList<Integer> yAxisData) {
        ArrayList<BarData> dataList = new ArrayList<>();

        for (int i = 0; i < xAxisData.size(); i++) {
            dataList.add(new BarData(xAxisData.get(i), yAxisData.get(i).floatValue(), String.valueOf(yAxisData.get(i))));
        }

        barProgressBar.setDataList(dataList);
        barProgressBar.setMaxValue(Collections.max(yAxisData).floatValue() + 20000f);
        barProgressBar.build();
        barProgressBar.animate();
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        try {

            if (methodName.equals("Month")) {
                JSONArray jsonArray = new JSONArray(networkResp);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        monthlyData.add(new GraphClass(jsonObject.getString("MonthName").substring(0, 3).toUpperCase(Locale.ROOT),
                                jsonObject.getString("SaleAmount").replace(".0", "")));
                    }
                    if (monthlyData.size() == 0) {
                        message = "Deen's Cheese";
                        messageDetail = "No monthly data received";
                    }
            } else if (methodName.equals("Week")){
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    weeklyData.add(new GraphClass(jsonObject.getString("DayName").substring(0, 3).toUpperCase(Locale.ROOT),
                            jsonObject.getString("SaleAmount").replace(".0", "")));
                }
                if (weeklyData.size() == 0) {
                    message = "Deen's Cheese";
                    messageDetail = "No weekly data received";
                }
            } else
            {
                JSONArray jsonArray = new JSONArray(networkResp);
                orders.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    JSONArray jsonArray1 = jsonObject.getJSONArray("ProducDetails");

                    ArrayList<ProductDetailClass> products = new ArrayList<>();

                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject1 = jsonArray1.getJSONObject(j);

                        products.add(new ProductDetailClass(jsonObject1.getInt("Id"),
                                jsonObject1.getInt("ProductID"),
                                jsonObject1.getString("ProductName"),
                                jsonObject1.getInt("SalePrice"),
                                jsonObject1.getInt("Quantity"),
                                jsonObject1.getInt("TradeOffer"),
                                jsonObject1.getInt("Offer_Amt"),
                                jsonObject1.getInt("TotalAmount"),
                                jsonObject1.getInt("DiscountAmount"),
                                jsonObject1.getInt("TotalDiscount")));
                    }

                    if (jsonObject.getInt("INV_InvoiceID") != 0) {
                        orders.add(new OrderClass(jsonObject.getInt("INV_InvoiceID"),
                                jsonObject.getString("INV_InvoiceNo"),
                                jsonObject.getString("INV_InvoiceDate"),
                                jsonObject.getInt("INV_SubTotal"),
                                Integer.parseInt(String.valueOf(jsonObject.getDouble("INV_TradeOfferAmount")).replace(".0", "")),
                                jsonObject.getInt("INV_DiscountAmount"),
                                jsonObject.getInt("INV_GrandTotal"),
                                jsonObject.getInt("INV_PaymentReceived"),
                                jsonObject.getInt("INV_PaymentDue"),
                                jsonObject.getString("INV_MapLocation"),
                                jsonObject.getString("INV_Lat"),
                                jsonObject.getString("INV_Long"),
                                jsonObject.getInt("CusCustomerID"),
                                jsonObject.getString("CusCustomerCode"),
                                jsonObject.getString("CusCustomerName"),
                                jsonObject.getInt("CusCityID"),
                                jsonObject.getString("CusCityName"),
                                jsonObject.getString("CusArea"),
                                jsonObject.getString("CusAddress"),
                                jsonObject.getString("CusMobile"),
                                jsonObject.getString("CusMobile1"),
                                jsonObject.getString("CusRegistrationDate"),
                                jsonObject.getBoolean("CusIsActive"),
                                jsonObject.getString("CusVisit_Day"),
                                jsonObject.getString("CusCreditLimit"),
                                jsonObject.getString("CusPaymentTypeName"),
                                jsonObject.getBoolean("CusIsApproved"),
                                jsonObject.getString("CusApprovedOn"),
                                jsonObject.getString("CusMapLocation"),
                                jsonObject.getString("CusLat"),
                                jsonObject.getString("CusLong"),
                                jsonObject.getInt("SO_GM"),
                                jsonObject.getString("SO_GMName"),
                                jsonObject.getInt("SO_ST"),
                                jsonObject.getString("SO_STName"),
                                jsonObject.getInt("SO_DM"),
                                jsonObject.getString("SO_DMName"),
                                jsonObject.getInt("SO_SDM"),
                                jsonObject.getString("SO_SDMName"),
                                jsonObject.getInt("SO_ID"),
                                jsonObject.getString("SO_Name"),
                                jsonObject.getDouble("INV_TaxRate"),
                                jsonObject.getDouble("INV_TaxAmount"),
                                products));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                        if (message.contains("saved")) {
                            getActivity().onBackPressed();
                            getActivity().overridePendingTransition(R.anim.slide_in_left,
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
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }else {
            if (userClass.getUserTypeID() == 7) {
                if (isNetworkAvailable()) {
                    GetOrderCount task = new GetOrderCount();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                }
            } else {
                orderView.setVisibility(View.GONE);
            }
        }
    }
}