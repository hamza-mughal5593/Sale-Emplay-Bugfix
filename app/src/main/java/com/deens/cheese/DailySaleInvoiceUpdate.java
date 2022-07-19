package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class DailySaleInvoiceUpdate extends AppCompatActivity {

    NonScrollListView productList, invoiceProductsList;

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;
    EditText remarks;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;
    POInvoiceClass selectedOrder;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    POInvoiceDetailAdapter invoiceAdapter;

    ArrayList<SOProductClass> products = new ArrayList<>();

    NoboButton update;

    int detailMethodCount = 0;

    Boolean isLastEntry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailysale_update);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        back = findViewById(R.id.back);
        invoiceProductsList = findViewById(R.id.invoiceProductList);
        productList = findViewById(R.id.productList);
        update = findViewById(R.id.update);
        remarks = findViewById(R.id.remarks);

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

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        update.setOnClickListener(view -> {
            if (selectedOrder.getPOProductDetailClass().size() > 0) {
                if (isNetworkAvailable()) {
                    if (remarks.getText().toString().equals("")) {
                        SnackAlert.error(parentView, "Please add remarks");
                    } else {
                        UpdateMasterInvoice task = new UpdateMasterInvoice();
                        task.execute();
                    }
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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        productList.setVisibility(View.VISIBLE);

                        SOProductAdapter productsAdapter = new SOProductAdapter(products, DailySaleInvoiceUpdate.this);
                        productList.setAdapter(productsAdapter);

                        productList.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (view.getId() == R.id.add) {
                                quantityAdditionDialog(i);
                            }
                        });
                    } else {
                        productList.setVisibility(View.GONE);
                    }
                }
            }
        }
    }


    public void quantityAdditionDialog(int i) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(DailySaleInvoiceUpdate.this);
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = in.inflate(R.layout.my_dialogue_box, null);
        EditText quantity = v.findViewById(R.id.quantity);
        NoboButton add = v.findViewById(R.id.add);
        NoboButton cancel = v.findViewById(R.id.cancel);
        builder.setTitle("Enter Quantity");
        builder.setView(v);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        add.setOnClickListener(view -> {



            hideSoftKeyboard(DailySaleInvoiceUpdate.this);
            // Create a new Invoice Product List
            List<POProductDetailClass> invoiceNewList = selectedOrder.getPOProductDetailClass();
            if (!quantity.getText().toString().trim().equals("")) {
                products.get(i).setQuantity(Integer.parseInt(quantity.getText().toString().trim()));
            }
            if (products.get(i).getQuantity() != 0) {



                int id = 1;

                if (selectedOrder.getPOProductDetailClass().size() > 0) {
                    id = selectedOrder.getPOProductDetailClass().get(selectedOrder.getPOProductDetailClass().size() - 1).getID() + 1;
                }

                POProductDetailClass productDetail = new POProductDetailClass(id
                        , selectedOrder.getPOID()
                        , products.get(i).getProductID()
                        , products.get(i).getProductName()
                        , products.get(i).getQuantity()
                        , (int) (products.get(i).getQuantity() * products.get(i).getProductSalaPrice())
                        , products.get(i).getProductSalaPrice().intValue());
                invoiceNewList.add(productDetail);
                selectedOrder.setPOProductDetailClass(invoiceNewList);
                invoiceAdapter = new POInvoiceDetailAdapter(selectedOrder.getPOProductDetailClass(),
                        DailySaleInvoiceUpdate.this);
                invoiceProductsList.setAdapter(invoiceAdapter);

                // Reload Total Product ListView
                if (isNetworkAvailable()) {
                    GetProducts task = new GetProducts();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet not available!");
                }

            } else {
                SnackAlert.error(parentView, "Enter quantity!");
            }
            dialog.dismiss();
        });

        dialog.show();
    }


    private class UpdateMasterInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/UpdateMstrSale?";

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
                        .url(URL + methodName + "POID=" + selectedOrder.getPOID()
                                + "&PODate=" + URLEncoder.encode(selectedOrder.getPODate())
                                + "&CustomerID=" + selectedOrder.getCusCustomerID()
                                + "&Remarks=" + URLEncoder.encode(remarks.getText().toString().trim(), "UTF-8"))
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
                if (!message.equals("")) {
                    // Show some message from server
                    if (messageDetail.contains("Update")) {
                        // Step 2. Call in loop invoice detail delete method
                        if (isNetworkAvailable()) {
                            DeleteInvoiceDetail task = new DeleteInvoiceDetail();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class DeleteInvoiceDetail extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/DeleteDetSale?";

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
                        .url(URL + methodName + "POID=" + selectedOrder.getPOID())
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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    if (messageDetail.contains("Deleted")) {
                        // Step 3. Call in loop invoice detail save method
                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class SubmitDetailInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/SubmitDetSale?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            if (detailMethodCount == selectedOrder.getPOProductDetailClass().size() - 1) {
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
                        .url(URL + methodName + "POID=" + selectedOrder.getPOID()
                                + "&ProductID=" + URLEncoder.encode(String.valueOf(selectedOrder.getPOProductDetailClass().get(detailMethodCount).getProductID()), "UTF-8")
                                + "&CustomerID=" + URLEncoder.encode(String.valueOf(selectedOrder.getCusCustomerID()), "UTF-8")
                                + "&Quantity=" + URLEncoder.encode(String.valueOf(selectedOrder.getPOProductDetailClass().get(detailMethodCount).getQuantity()), "UTF-8")
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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.contains("")) {
                    showDialog("Deen's Cheese", "Some error occurred! Please try later!", "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    detailMethodCount++;

                    if (detailMethodCount < selectedOrder.getPOProductDetailClass().size()) {

                        if (detailMethodCount == selectedOrder.getPOProductDetailClass().size()) {
                            isLastEntry = true;
                        }

                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }

                    } else {
                        showDialog("Deen's Cheese", "Daily Sale updated successfully!", "OK", "", R.drawable.ic_tick,
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
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")) {
                        ArrayList<Integer> productIds = new ArrayList<Integer>();

                        // Get all product ids which are added into invoice list already
                        // we will hide them in total product list
                        for (int i = 0; i < selectedOrder.getPOProductDetailClass().size(); i++) {
                            productIds.add(selectedOrder.getPOProductDetailClass().get(i).getProductID());
                        }

                        products.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (!productIds.contains(jsonObject.getInt("ProductID"))) {
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

        if (methodName.equals("Update")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

        if (methodName.equals("Delete")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }
    }

    private void loadDetailListView() {
        if (selectedOrder.getPOProductDetailClass().size() > 0) {
            invoiceAdapter = new POInvoiceDetailAdapter(selectedOrder.getPOProductDetailClass(),
                    DailySaleInvoiceUpdate.this);
            invoiceProductsList.setAdapter(invoiceAdapter);

            // selectedOrder.getProductDetails()  == Our Added Product List in Invoice
            invoiceProductsList.setOnItemClickListener((adapterView, view, i, l) -> {
                if (view.getId() == R.id.add) {
                    // Change ProductDetail Object values ==> Quantity, Offer Amount, Total Amount

                    // Update Quantity
                    selectedOrder.getPOProductDetailClass().get(i).
                            setQuantity(selectedOrder.getPOProductDetailClass().get(i).getQuantity() + 1);
                    // Update Total Amount
                    updateTotalAmount(i);
                    invoiceAdapter.notifyDataSetChanged();
                }
                if (view.getId() == R.id.less) {
                    // Change ProductDetail Object values ==> Quantity, Offer Amount, Total Amount

                    if (selectedOrder.getPOProductDetailClass().get(i).getQuantity() > 1) {
                        // Update Quantity
                        selectedOrder.getPOProductDetailClass().get(i).
                                setQuantity(selectedOrder.getPOProductDetailClass().get(i).getQuantity() - 1);
                        // Update Total Amount
                        updateTotalAmount(i);
                        invoiceAdapter.notifyDataSetChanged();
                    }
                }
                if (view.getId() == R.id.remove) {
                    // Remove from ArrayList<ProductDetailClass>
                    selectedOrder.getPOProductDetailClass().remove(i);
                    invoiceAdapter.notifyDataSetChanged();

                    // Reload Total Product ListView
                    if (isNetworkAvailable()) {
                        GetProducts task = new GetProducts();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
                if (view.getId() == R.id.offer) {
                    // Change ProductDetail Object values ==> Trade Offer, Offer Amount, Total Amount

                    // Take Trade Offer input from SO
                    showInputDialog(i);
                }
            });
        }
    }

    private void updateTotalAmount(int i) {
        selectedOrder.getPOProductDetailClass().get(i).
                setTotalAmount((selectedOrder.getPOProductDetailClass().get(i).getQuantity() *
                        selectedOrder.getPOProductDetailClass().get(i).getSalePrice()));
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
        selectedOrder = gson.fromJson(getIntent().getExtras().getString("SelectedInvoice"), POInvoiceClass.class);
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(DailySaleInvoiceUpdate.this);
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

    private void showInputDialog(int i) {
        LayoutInflater factory = LayoutInflater.from(DailySaleInvoiceUpdate.this);
        final View view = factory.inflate(R.layout.input_dialog, null);
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setView(view);
        alert.setCancelable(false);

        NoboButton cancel, submit;

        cancel = view.findViewById(R.id.cancel);
        submit = view.findViewById(R.id.submit);

        cancel.setOnClickListener(view1 -> {
            alert.dismiss();
        });

        submit.setOnClickListener(view1 -> {
            try {
                // Update Total Amount
                updateTotalAmount(i);
                invoiceAdapter.notifyDataSetChanged();

                hideSoftKeyboard(DailySaleInvoiceUpdate.this);
                alert.dismiss();
            } catch (NumberFormatException ex) {
                Log.w("Number Exception: ", ex.toString());
            }
        });

        alert.show();
    }

}