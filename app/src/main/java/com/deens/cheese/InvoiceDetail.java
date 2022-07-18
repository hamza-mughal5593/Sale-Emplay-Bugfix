package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;
import com.paulrybitskyi.valuepicker.ValuePickerView;
import com.paulrybitskyi.valuepicker.model.Item;
import com.paulrybitskyi.valuepicker.model.PickerItem;
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

public class InvoiceDetail extends AppCompatActivity {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;
    OrderClass selectedOrder;

    ElasticButton bottomButton;
    AppCompatButton updateInvocie;
    TextView invoiceNumber;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    TextView customerName, contact, date, invoiceCoordinate, invoiceMap,
            customerCoordinate, areaAddress, customerMap, cityName;

    TextView subtotal, discountAmount, offerAmount, grandTotal, received, due, taxAmount;

    SupportMapFragment mapFragment;
//    private GoogleMap mMap;

    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    NonScrollListView invoiceDetailListView;
    ElasticButton cancelButton, assignButton, deliverButton;

    // Sale Man List
    ArrayList<EmployeeClass> smList = new ArrayList<>();

    ValuePickerView salemanPicker;
    RelativeLayout salesmanSelectionView;
    int LAUNCH_UPDATE_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_invoice_detail);

        subtotal = findViewById(R.id.subtotal);
        discountAmount = findViewById(R.id.discountAmount);
        taxAmount = findViewById(R.id.taxAmount);
        grandTotal = findViewById(R.id.grandTotal);
        offerAmount = findViewById(R.id.offerAmount);
        received = findViewById(R.id.received);
        cancelButton = findViewById(R.id.cancelButton);
        assignButton = findViewById(R.id.assignButton);
        due = findViewById(R.id.due);
        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        invoiceNumber = findViewById(R.id.invoiceNumber);
        customerName = findViewById(R.id.customerName);
        contact = findViewById(R.id.contact);
        date = findViewById(R.id.date);
        invoiceCoordinate = findViewById(R.id.invoiceCoordinate);
        invoiceMap = findViewById(R.id.invoiceMap);
        customerCoordinate = findViewById(R.id.customerCoordinate);
        areaAddress = findViewById(R.id.areaAddress);
        customerMap = findViewById(R.id.customerMap);
        cityName = findViewById(R.id.cityName);
        invoiceDetailListView = findViewById(R.id.listView);
        salemanPicker = findViewById(R.id.salemanPicker);
        salesmanSelectionView = findViewById(R.id.salesmanSelectionView);
        updateInvocie = findViewById(R.id.updateInvoice);
        deliverButton = findViewById(R.id.deliverOrder);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        bottomButton = findViewById(R.id.assignPost);

          // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        loadPreferences();

        invoiceNumber.setText(selectedOrder.getINV_InvoiceNo());
        customerName.setText(selectedOrder.getCusCustomerName());
        contact.setText(selectedOrder.getCusMobile());
        date.setText(selectedOrder.getINV_InvoiceDate().replace("T00:00:00", ""));
        invoiceCoordinate.setText(selectedOrder.getINV_Lat() + ", " + selectedOrder.getINV_Long());
        invoiceMap.setText(selectedOrder.getINV_MapLocation());
        customerCoordinate.setText(selectedOrder.getCusLat() + ", " + selectedOrder.getCusLong());
        areaAddress.setText(selectedOrder.getCusArea() + ", " + selectedOrder.getCusAddress());
        customerMap.setText(selectedOrder.getCusMapLocation());
        cityName.setText(selectedOrder.getCusCityName());

        // Pre Updates total values
        loadDetailListView();

        back = findViewById(R.id.back);

//        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(InvoiceDetail.this);

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        if (loggedInUserType.equals("6") || loggedInUserType.equals("7")){
            bottomButton.setText("Post Order");
            if (getIntent().getExtras().getBoolean("Done")){
                bottomButton.setVisibility(View.GONE);
                updateInvocie.setVisibility(View.GONE);
            }
        }else {
            if (isNetworkAvailable()) {
                GetSalesmanList task = new GetSalesmanList();
                task.execute();
            }else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
            bottomButton.setText("Assign Order");
        }

        if (loggedInUserType.equals("6")){
            updateInvocie.setVisibility(View.VISIBLE);
        }else {
            updateInvocie.setVisibility(View.GONE);
        }

        bottomButton.setOnClickListener(view -> {
            if (getIntent().getExtras().getBoolean("Done") ||
                    loggedInUserType.equals("6") || loggedInUserType.equals("7")){
                if (isNetworkAvailable()) {
                    AssignPostInvoice task = new AssignPostInvoice();
                    task.execute();
                }else {
                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                }
            }else {
                salesmanSelectionView.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(view -> {
            salesmanSelectionView.setVisibility(View.GONE);
        });

        assignButton.setOnClickListener(view -> {
            salesmanSelectionView.setVisibility(View.GONE);
            if (isNetworkAvailable()) {
                AssignPostInvoice task = new AssignPostInvoice();
                task.execute();
            }else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        });

        updateInvocie.setOnClickListener(view -> {
            Gson gson = new Gson();
            String order = gson.toJson(selectedOrder);
            startActivityForResult(new Intent(InvoiceDetail.this, SOInvoiceUpdate.class)
                    .putExtra("SelectedOrder", order), LAUNCH_UPDATE_ACTIVITY);
            overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        if (getIntent().getExtras().getBoolean("IsDeliver")){
            bottomButton.setVisibility(View.GONE);
            deliverButton.setVisibility(View.VISIBLE);
            updateInvocie.setVisibility(View.GONE);
        }

        deliverButton.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                DeliverInvoice task = new DeliverInvoice();
                task.execute();
            }else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        });
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.clear();
//
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//
////        LatLng invoiceLatLng = new LatLng(Double.parseDouble(selectedOrder.getINV_Lat()),
////                Double.parseDouble(selectedOrder.getINV_Long()));
////        mMap.addMarker(new MarkerOptions().position(invoiceLatLng).
////                icon(BitmapDescriptorFactory.fromBitmap(resizeGoogleMapIcons("invoice",
////                        screenHeight / 23, screenHeight / 23))).title(selectedOrder.getINV_InvoiceNo()));
//        LatLng customerLatLng = new LatLng(Double.parseDouble(selectedOrder.getCusLat()),
//                Double.parseDouble(selectedOrder.getCusLong()));
//        mMap.addMarker(new MarkerOptions().position(customerLatLng).
//                icon(BitmapDescriptorFactory.fromBitmap(resizeGoogleMapIcons("customer",
//                        screenHeight / 23, screenHeight / 23))).title(selectedOrder.getCusCustomerName()));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 14));
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_UPDATE_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("order");
                Gson gson = new Gson();
                selectedOrder = gson.fromJson(result, OrderClass.class);
                // Update total values when returning from update screen
                loadDetailListView();
            }
        }

    }

    private class AssignPostInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/PostInvoice?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            if (loggedInUserType.equals("6")){
                methodName = "Invoice/PostInvoice?InvoiceID="+selectedOrder.getINV_InvoiceID()
                        +"&SOID=" + selectedOrder.getSO_ID();
            }else {
                methodName = "Invoice/AssignInvoice?LoginUserID="+loggedInUserId+"&InvoiceID="
                        +selectedOrder.getINV_InvoiceID()+"&SOID="+selectedOrder.getSO_ID()+"&SaleManID=" +
                        smList.get(salemanPicker.getSelectedItem().getId()).getUserID()+"&SortNo=0";
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName)
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Post");

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

    private class DeliverInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            methodName = "Invoice/DeliverInvoice?InvoiceID="+selectedOrder.getINV_InvoiceID()
                        +"&LoginUserID=" + loggedInUserId;
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName)
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Deliver");

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

    private class GetSalesmanList extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetEmployeeList?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
            bottomButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName + "EmpID=0&UserTypeID=7&UserID=" + URLEncoder.encode(loggedInUserId, "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Saleman");

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

                    if (!getIntent().getExtras().getBoolean("Done")){
                        bottomButton.setEnabled(true);
                    }else {
                        bottomButton.setVisibility(View.GONE);
                    }

                    ArrayList<Item> salemanPickerItems = new ArrayList<>();

                    for (int i = 0 ; i < smList.size() ; i++){
                        salemanPickerItems.add(new PickerItem(i, smList.get(i).getEmpName()));
                    }

                    if (salemanPickerItems.size() > 0){
                        salemanPicker.setItems(salemanPickerItems);
                        salemanPicker.setSelectedItem(salemanPickerItems.get(0));
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Post")) {
            if (networkResp.contains("Submit")){
                message = "Deen's Cheese";
                if (loggedInUserType.equals("6")){
                    messageDetail = "Invoice posted successfully";
                }else {
                    messageDetail = "Invoice assigned successfully";
                }
            }else {
                message = "Deen's Cheese";
                if (loggedInUserType.equals("6")){
                    messageDetail = "Invoice couldn't be posted";
                }else {
                    messageDetail = "Invoice couldn't be assigned";
                }
            }
        }

        if (methodName.equals("Deliver")) {
            if (networkResp.contains("Submit")){
                message = "Deen's Cheese";
                messageDetail = "Invoice delivered successfully";
            }else {
                message = "Deen's Cheese";
                messageDetail = networkResp;
            }
        }

        if (methodName.equals("Saleman")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    smList.add(new EmployeeClass(jsonObject.getInt("UserID"),
                            jsonObject.getString("UserName"),
                            jsonObject.getString("Password"),
                            jsonObject.getInt("UserTypeID"),
                            jsonObject.getString("UserTypeName"),
                            jsonObject.getInt("EmpID"),
                            jsonObject.getString("EmpCode"),
                            jsonObject.getString("EmpName"),
                            jsonObject.getString("EmpFatherName"),
                            jsonObject.getString("CNIC"),
                            jsonObject.getString("DOB"),
                            jsonObject.getString("Gender"),
                            jsonObject.getInt("EmpCityID"),
                            jsonObject.getString("EmpCityName"),
                            jsonObject.getString("EmpAddress"),
                            jsonObject.getString("ContactNo"),
                            jsonObject.getString("ContactNo2nd"),
                            jsonObject.getInt("BranchID"),
                            jsonObject.getString("BranchName"),
                            jsonObject.getInt("DepartmentID"),
                            jsonObject.getString("DepartmentName"),
                            jsonObject.getInt("DesignationID"),
                            jsonObject.getString("DesignationName"),
                            jsonObject.getString("DOJ"),
                            jsonObject.getInt("EmpStatusID"),
                            jsonObject.getString("EmpStatusName"),
                            jsonObject.getString("DOR"),
                            jsonObject.getString("Remarks"),
                            jsonObject.getInt("GM"),
                            jsonObject.getString("GMName"),
                            jsonObject.getInt("ST"),
                            jsonObject.getString("STName"),
                            jsonObject.getInt("DM"),
                            jsonObject.getString("DMName"),
                            jsonObject.getInt("SDM"),
                            jsonObject.getString("SDMName"),
                            jsonObject.getInt("SO"),
                            jsonObject.getString("SOName")));
                }

                if (smList.size() == 0){
                    message = "Deen's Cheese";
                    messageDetail = "No Salesman/driver under you found";
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
        selectedOrder = gson.fromJson(getIntent().getExtras().getString("SelectedOrder"), OrderClass.class);
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(InvoiceDetail.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(InvoiceDetail.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (messageDetail.contains("successfully")) {
                            InvoiceDetail.this.onBackPressed();
                            InvoiceDetail.this.overridePendingTransition(R.anim.slide_in_left,
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

    public Bitmap resizeGoogleMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap;
        imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        if (imageBitmap == null) {
            imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("brown0", "drawable", getPackageName()));
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void loadDetailListView(){
        if (selectedOrder.getProductsDetails().size() > 0){
            InvoiceDetailAdapter adapter = new InvoiceDetailAdapter(selectedOrder.getProductsDetails(),
                    InvoiceDetail.this);
            invoiceDetailListView.setAdapter(adapter);
            Double offerAmt = 0.0;
            for (int i = 0 ; i < selectedOrder.getProductsDetails().size() ; i++){
                offerAmt = offerAmt + selectedOrder.getProductsDetails().get(i).getOffer_Amt();
            }

            offerAmount.setText(String.valueOf(offerAmt).replace(".0", " PKR"));
            subtotal.setText(String.valueOf(selectedOrder.getINV_SubTotal()).replace(".0", "") + " PKR");
            discountAmount.setText(String.valueOf(selectedOrder.getINV_DiscountAmount()).replace(".0", "") + " PKR");
            grandTotal.setText(String.valueOf(selectedOrder.getINV_GrandTotal()).replace(".0", "") + " PKR");
            received.setText(String.valueOf(selectedOrder.getINV_PaymentReceived()).replace(".0", "") + " PKR");
            due.setText(String.valueOf(selectedOrder.getINV_PaymentDue()).replace(".0", "") + " PKR");
            taxAmount.setText(String.valueOf(selectedOrder.getINV_TaxAmount()).replace(".0", " PKR"));
        }else {
            invoiceDetailListView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // When returning from Updating invoice update total amounts here
    }
}