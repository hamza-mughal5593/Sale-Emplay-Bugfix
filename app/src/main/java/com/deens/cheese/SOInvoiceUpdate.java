package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class SOInvoiceUpdate extends AppCompatActivity {

    NonScrollListView productList, invoiceProductsList;

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;
    OrderClass selectedOrder;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    SOInvoiceDetailAdapter invoiceAdapter;

    ArrayList<SOProductClass> products = new ArrayList<>();

    TextView subtotalAmount, grandAmount, taxAmounttxt, offerAmount;

    NoboButton update;

    int detailMethodCount = 0;

    Boolean isLastEntry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soinvoice_update);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        back = findViewById(R.id.back);
        invoiceProductsList = findViewById(R.id.invoiceProductList);
        productList = findViewById(R.id.productList);
        taxAmounttxt = findViewById(R.id.discountAmount);
        subtotalAmount = findViewById(R.id.subtotalAmount);
        grandAmount = findViewById(R.id.grandAmount);
        offerAmount = findViewById(R.id.offerAmount);
        update = findViewById(R.id.update);

        loadPreferences();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(5000).// long
                setExitAnimDuration(5000).// long
                start();

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        loadDetailListView();
        calculateAmounts();

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        update.setOnClickListener(view -> {
            if (selectedOrder.getProductsDetails().size() > 0){
                if (isNetworkAvailable()) {
                    UpdateMasterInvoice task = new UpdateMasterInvoice();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet not available!");
                }
            }
        });
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
                        .url(URL + methodName + "CustomerID=" + selectedOrder.getCusCustomerID())
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
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        productList.setVisibility(View.VISIBLE);

                        SOProductAdapter productsAdapter = new SOProductAdapter(products, SOInvoiceUpdate.this);
                        productList.setAdapter(productsAdapter);

                        productList.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (view.getId() == R.id.add){
                                hideSoftKeyboard(SOInvoiceUpdate.this);
                                // Create a new Invoice Product List
                                ArrayList<ProductDetailClass> invoiceNewList = selectedOrder.getProductsDetails();

                                if (products.get(i).getQuantity() != 0){
                                    int id = 1;

                                    if (selectedOrder.getProductsDetails().size() > 0){
                                        id = selectedOrder.getProductsDetails().get(selectedOrder.getProductsDetails().size() - 1).getId() + 1;
                                    }

                                    ProductDetailClass productDetail = new ProductDetailClass(id
                                            , products.get(i).getProductID()
                                            , products.get(i).getProductName()
                                            , products.get(i).getProductSalaPrice().intValue()
                                            , products.get(i).getQuantity()
                                            , 0, 0
                                            , (int)(products.get(i).getQuantity() * products.get(i).getProductSalaPrice())
                                            , products.get(i).getDiscount().intValue(),0);
                                    invoiceNewList.add(productDetail);
                                    selectedOrder.setProductsDetails(invoiceNewList);
                                    invoiceAdapter = new SOInvoiceDetailAdapter(selectedOrder.getProductsDetails(),
                                            SOInvoiceUpdate.this);
                                    invoiceProductsList.setAdapter(invoiceAdapter);

                                    calculateAmounts();

                                    // Reload Total Product ListView
                                    if (isNetworkAvailable()) {
                                        GetProducts task = new GetProducts();
                                        task.execute();
                                    } else {
                                        SnackAlert.error(parentView, "Internet not available!");
                                    }

                                }else {
                                    SnackAlert.error(parentView, "Enter quantity!");
                                }
                            }
                        });
                    } else {
                        productList.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private class UpdateMasterInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/UpdateMstrInvoice?";
        int taxAmount = 0;

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            String[] temp = taxAmounttxt.getText().toString().trim().split("@");
            taxAmount = Integer.parseInt(temp[0].replace(" ", ""));
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName + "InvoiceID="+ selectedOrder.getINV_InvoiceID()
                                +"&InvoiceDate="+ URLEncoder.encode(selectedOrder.getINV_InvoiceDate())
                                +"&CustomerId=" + selectedOrder.getCusCustomerID()
                                +"&SubTotal="+ URLEncoder.encode(subtotalAmount.getText().toString().trim(), "UTF-8")
                                +"&SaleTaxRate="+ selectedOrder.getINV_TaxRate()
                                +"&SaleTaxAmount="+ URLEncoder.encode(String.valueOf(taxAmount), "UTF-8")
                                +"&GrandTotal="+ URLEncoder.encode(grandAmount.getText().toString().trim(), "UTF-8")
                                +"&MapLocation="+ URLEncoder.encode(selectedOrder.getCusMapLocation(), "UTF-8")
                                +"&Lat="+ URLEncoder.encode(selectedOrder.getCusLat(), "UTF-8")
                                +"&Long="+ URLEncoder.encode(selectedOrder.getCusLong(), "UTF-8")
                                +"&SOID=" + selectedOrder.getSO_ID())
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
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    if (messageDetail.contains("Update")){
                        // Step 2. Call in loop invoice detail delete method
                        if (isNetworkAvailable()) {
                            DeleteInvoiceDetail task = new DeleteInvoiceDetail();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    }else {
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class DeleteInvoiceDetail extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/DeleteDetInvoice?";

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
                        .url(URL + methodName + "InvoiceID="+ selectedOrder.getINV_InvoiceID())
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Delete");

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
                    if (messageDetail.contains("Deleted")){
                        // Step 3. Call in loop invoice detail save method
                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    }else {
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class SubmitDetailInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/SubmitDetInvoice?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            if (detailMethodCount == selectedOrder.getProductsDetails().size() - 1){
                isLastEntry = true;
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName + "InvoiceID=" + selectedOrder.getINV_InvoiceID()
                                + "&ProductID=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getProductID()), "UTF-8")
                                + "&CustomerId=" + URLEncoder.encode(String.valueOf(selectedOrder.getCusCustomerID()), "UTF-8")
                                + "&SalePrice=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getSalePrice()), "UTF-8")
                                + "&Quantity=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getQuantity()), "UTF-8")
                                + "&TradeOffer=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getTradeOffer()), "UTF-8")
                                + "&Offer_Amt=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getTradeOffer()), "UTF-8")
                                + "&TotalAmount=" + URLEncoder.encode(String.valueOf(selectedOrder.getProductsDetails().get(detailMethodCount).getTotalAmount()), "UTF-8")
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
                if (!message.contains("")){
                    showDialog("Deen's Cheese", "Some error occurred! Please try later!", "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    detailMethodCount++;

                    if (detailMethodCount < selectedOrder.getProductsDetails().size()){

                        if (detailMethodCount == selectedOrder.getProductsDetails().size()){
                            isLastEntry = true;
                        }

                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }

                    }else {
                        showDialog("Deen's Cheese", "Order updated successfully!", "OK", "", R.drawable.ic_tick,
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
                        ArrayList<Integer> productIds = new ArrayList<Integer>();

                        // Get all product ids which are added into invoice list already
                        // we will hide them in total product list
                        for (int i = 0 ; i < selectedOrder.getProductsDetails().size(); i++){
                            productIds.add(selectedOrder.getProductsDetails().get(i).getProductID());
                        }

                        products.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (!productIds.contains(jsonObject.getInt("ProductID"))){
                                products.add(new SOProductClass(jsonObject.getInt("ProductID"),
                                        jsonObject.getString("CompanyName"),
                                        jsonObject.getString("ProductCode"),
                                        jsonObject.getString("ProductName"),
                                        jsonObject.getDouble("ProductSalaPrice"),
                                        jsonObject.getString("ProductType"),
                                        jsonObject.getString("ProductUnit"),
                                        jsonObject.getDouble("TradeOffer"),
                                        jsonObject.getDouble("Discount")));
                            }
                        }
                    }else {
                        message = "Deen's Cheese";
                        messageDetail = "No product found!";
                    }
                }else {
                    message = "Deen's Cheese";
                    messageDetail = "No product found!";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Update")){
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

        if (methodName.equals("Delete")){
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }
    }

    private void loadDetailListView(){
        if (selectedOrder.getProductsDetails().size() > 0){
            invoiceAdapter = new SOInvoiceDetailAdapter(selectedOrder.getProductsDetails(),
                    SOInvoiceUpdate.this);
            invoiceProductsList.setAdapter(invoiceAdapter);

            // selectedOrder.getProductDetails()  == Our Added Product List in Invoice
            invoiceProductsList.setOnItemClickListener((adapterView, view, i, l) -> {
                if (view.getId() == R.id.add){
                    // Change ProductDetail Object values ==> Quantity, Offer Amount, Total Amount

                    // Update Quantity
                    selectedOrder.getProductsDetails().get(i).
                            setQuantity(selectedOrder.getProductsDetails().get(i).getQuantity() + 1);
                    // Update Offer Amount
                    updateOfferAmount(i);
                    // Update Total Amount
                    updateTotalAmount(i);
                    invoiceAdapter.notifyDataSetChanged();
                    // Change Invoice Total Amount/Discount Values
                    calculateAmounts();
                }
                if (view.getId() == R.id.less){
                    // Change ProductDetail Object values ==> Quantity, Offer Amount, Total Amount

                    if (selectedOrder.getProductsDetails().get(i).getQuantity() > 1){
                        // Update Quantity
                        selectedOrder.getProductsDetails().get(i).
                                setQuantity(selectedOrder.getProductsDetails().get(i).getQuantity() - 1);
                        // Update Offer Amount
                        updateOfferAmount(i);
                        // Update Total Amount
                        updateTotalAmount(i);
                        invoiceAdapter.notifyDataSetChanged();
                    }
                    // Change Invoice Total Amount/Discount Values
                    calculateAmounts();
                }
                if (view.getId() == R.id.remove){
                    // Remove from ArrayList<ProductDetailClass>
                    selectedOrder.getProductsDetails().remove(i);
                    invoiceAdapter.notifyDataSetChanged();
                    // Change Invoice Total Amount/Discount Values
                    calculateAmounts();

                    // Reload Total Product ListView
                    if (isNetworkAvailable()) {
                        GetProducts task = new GetProducts();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
                if (view.getId() == R.id.offer){
                    // Change ProductDetail Object values ==> Trade Offer, Offer Amount, Total Amount

                    // Take Trade Offer input from SO
                    showInputDialog(i);
                }
            });
        }
    }

    // Discount, Offer, Subtotal, Grand Amounts
    private void calculateAmounts (){
        int subtotal = 0, taxAmount = 0, offer = 0, discount = 0;

        for (int i = 0 ; i < selectedOrder.getProductsDetails().size(); i++){
            offer = offer + selectedOrder.getProductsDetails().get(i).getOffer_Amt();

            subtotal = subtotal + (selectedOrder.getProductsDetails().get(i).getQuantity()
                    * selectedOrder.getProductsDetails().get(i).getSalePrice());

            discount = discount + (selectedOrder.getProductsDetails().get(i).getQuantity()
                    * selectedOrder.getProductsDetails().get(i).getDiscountAmount());
        }

        taxAmount = (selectedOrder.getINV_TaxRate().intValue() * (subtotal - offer))/100;
        subtotalAmount.setText(String.valueOf(subtotal - offer));
        offerAmount.setText(String.valueOf(offer));
        taxAmounttxt.setText((taxAmount) + " @ " + selectedOrder.getINV_TaxRate().intValue() + "%");
        grandAmount.setText(String.valueOf(subtotal + taxAmount - offer));

        // Update our selectedOrder object Discount, Offer, Subtotal, Grand Amounts
        selectedOrder.setINV_GrandTotal(Integer.parseInt(grandAmount.getText().toString().trim()));
//        selectedOrder.setINV_SubTotal(Integer.parseInt(subtotalAmount.getText().toString().trim()));
        selectedOrder.setINV_PaymentDue(Integer.parseInt(grandAmount.getText().toString().trim()));
        selectedOrder.setINV_TaxAmount((double) taxAmount);
        selectedOrder.setINV_SubTotal((subtotal - offer));
        selectedOrder.setINV_DiscountAmount(discount);
    }

    private void updateOfferAmount(int i){
        selectedOrder.getProductsDetails().get(i).
                setOffer_Amt(selectedOrder.getProductsDetails().get(i).getTradeOffer()
                        * selectedOrder.getProductsDetails().get(i).getQuantity());
    }

    private void updateTotalAmount(int i){
        selectedOrder.getProductsDetails().get(i).
                setTotalAmount((selectedOrder.getProductsDetails().get(i).getQuantity() *
                        selectedOrder.getProductsDetails().get(i).getSalePrice()) -
                        selectedOrder.getProductsDetails().get(i).getOffer_Amt());
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
        selectedOrder = gson.fromJson(getIntent().getExtras().getString("SelectedOrder"), OrderClass.class);
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(SOInvoiceUpdate.this);
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
                        if (message.contains("updated")) {
                            Intent returnIntent = new Intent();
                            Gson gson = new Gson();
                            String order = gson.toJson(selectedOrder);
                            returnIntent.putExtra("order", order);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    private void showInputDialog(int i){
        LayoutInflater factory = LayoutInflater.from(SOInvoiceUpdate.this);
        final View view = factory.inflate(R.layout.input_dialog, null);
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setView(view);
        alert.setCancelable(false);

        NoboButton cancel, submit;
        EditText offerAmount;

        cancel = view.findViewById(R.id.cancel);
        submit = view.findViewById(R.id.submit);
        offerAmount = view.findViewById(R.id.amount);

        cancel.setOnClickListener(view1 -> {
            alert.dismiss();
        });

        submit.setOnClickListener(view1 -> {
            try {
                // Update Trade Offer
                selectedOrder.getProductsDetails().get(i).
                        setTradeOffer(Integer.parseInt(offerAmount.getText().toString().trim()));
                // Update Offer Amount
                updateOfferAmount(i);
                // Update Total Amount
                updateTotalAmount(i);
                invoiceAdapter.notifyDataSetChanged();

                // Change Invoice Total Amount/Discount Values
                calculateAmounts();

                hideSoftKeyboard(SOInvoiceUpdate.this);
                alert.dismiss();
            }catch (NumberFormatException ex){
                Log.w("Number Exception: ", ex.toString());
            }
        });

        alert.show();
    }

}