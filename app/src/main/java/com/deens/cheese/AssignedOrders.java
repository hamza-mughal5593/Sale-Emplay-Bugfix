package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
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

public class AssignedOrders extends AppCompatActivity {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageButton addOrder;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;

    ListView listView;
    ImageView back, gotoMap;

    ArrayList<OrderClass> orders = new ArrayList<>();

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    Boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_orders);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        listView = findViewById(R.id.listView);
        addOrder = findViewById(R.id.addOrder);
        back = findViewById(R.id.back);
        gotoMap = findViewById(R.id.gotoMap);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        back.setOnClickListener(view -> {
            AssignedOrders.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        gotoMap.setOnClickListener(view -> {
            Gson gson = new Gson();
            String json = gson.toJson(orders);
            startActivity(new Intent(AssignedOrders.this, MapPins.class).putExtra("orders", json));
            overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        loadPreferences();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetOrderCount task = new GetOrderCount();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet Connectivity Problem!");
        }

    }

    private class GetOrderCount extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetInvoiceDetails?LoginUserID=" + userClass.getUserID() + "&IsAssign=true&Is_Post=true";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
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
                parseJSONStringToJSONObject(networkResp);

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
                    if (orders.size() > 0){
                        OrderAdapter adapter = new OrderAdapter(orders, AssignedOrders.this, false, false, false,AssignedOrders.this);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            Gson gson = new Gson();
                            String selectedOrder = gson.toJson(orders.get(i));
                            startActivity(new Intent(AssignedOrders.this, InvoiceDetail.class)
                                    .putExtra("SelectedOrder", selectedOrder)
                                    .putExtra("Done", true).putExtra("IsDeliver", true));
                            overridePendingTransition(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        });
                    }else {
                        showDialog("Deen's Cheese", "No assigned order found", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(networkResp);

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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }else {
            if (isNetworkAvailable()) {
                GetOrderCount task = new GetOrderCount();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        }
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(AssignedOrders.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(AssignedOrders.this)
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}