package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deens.cheese.Adapter.OrderProductAdapterModified;
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
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class ProductListDailySale extends AppCompatActivity {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;
    String selectedCustomerID = "";
    RecyclerView listView;

    NoboButton place;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    EditText remarks;
    ArrayList<SOProductClass> products = new ArrayList<>();
    SOProductClass selectedProduct;

    int detailMethodCount = 0;
    String invoiceID = "";

    Boolean isLastEntry = false;

    EditText searchField;
    ImageView search, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_product_list_dailysale);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        selectedCustomerID = getIntent().getExtras().getString("CID", "");
        parentView = findViewById(R.id.parentView);
        remarks = findViewById(R.id.remarks);
        loadingView = findViewById(R.id.loadingView);
        listView = findViewById(R.id.listView);
        place = findViewById(R.id.place);
        searchField = findViewById(R.id.searchField);
        cancel = findViewById(R.id.cancel);
        search = findViewById(R.id.search);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        loadPreferences();

        back = findViewById(R.id.back);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        back.setOnClickListener(view -> {
            ProductListDailySale.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        place.setOnClickListener(view -> {
            selectedProduct = products.get(detailMethodCount);
            if (remarks.getText().toString().trim().equals("")){
                SnackAlert.error(parentView, "Please put remarks");
            }else {
                if (isNetworkAvailable()) {
                    SubmitMasterInvoice task = new SubmitMasterInvoice();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet not available!");
                }
            }
        });

        search.setOnClickListener(view -> {
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getProductName().toUpperCase().contains(searchField.getText().toString().trim().toUpperCase())) {
                    listView.smoothScrollToPosition(i);
                }
            }
        });

        cancel.setOnClickListener(view -> {
            searchField.setText("");
            // Re assign adapter to listview of all customers
            hideSoftKeyboard(ProductListDailySale.this);
        });
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }
    OrderProductAdapterModified adapter;
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
                        .url(URL + methodName + "CustomerID=" + selectedCustomerID)
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
            if (!ProductListDailySale.this.isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        LinearLayoutManager LayoutManager = new LinearLayoutManager(ProductListDailySale.this);
                        listView.setLayoutManager(LayoutManager);
                        adapter = new OrderProductAdapterModified(products, ProductListDailySale.this, new OrderProductAdapterModified.AdapterCallback() {
                            @Override
                            public void add_data(int pos, View holder) {
                                quantityAdditionDialog(pos,holder);
                            }

                            @Override
                            public void minus_data(int pos, View holder) {
                                if (products.get(pos).getQuantity() > 0){
                                    quantityMinusDialog(pos,holder);
                                }else {
                                    Toast.makeText(ProductListDailySale.this,
                                            "Quantity is zero",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        listView.setAdapter(adapter);

//                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
//                            if (view.getId() == R.id.add) {
//                                quantityAdditionDialog(i);
//                            }
//                            if (view.getId() == R.id.minus) {
//                                if (products.get(i).getQuantity() > 0){
//                                    quantityMinusDialog(i);
//                                }else {
//                                    Toast.makeText(ProductListDailySale.this,
//                                            "Quantity is zero",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }

    }

    private class SubmitMasterInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/SubmitMstrSale?";
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
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "PODate=" + URLEncoder.encode(invoiceDate, "UTF-8")
                                + "&CustomerID=" + URLEncoder.encode(String.valueOf(selectedCustomerID), "UTF-8")
                                + "&Remarks=" + URLEncoder.encode(remarks.getText().toString().trim(), "UTF-8")
                                + "&UserID=" + URLEncoder.encode(loggedInUserId, "UTF-8"))
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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (message.equals("Deen's Cheese")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
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

        String methodName = "KeyAccount/SubmitDetSale?";

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
                        .url(URL + methodName + "POID=" + invoiceID
                                + "&ProductID=" + URLEncoder.encode(String.valueOf(selectedProduct.getProductID()), "UTF-8")
                                + "&CustomerId=" + URLEncoder.encode(String.valueOf(selectedCustomerID), "UTF-8")
                                + "&Quantity=" + URLEncoder.encode(String.valueOf(selectedProduct.getQuantity()), "UTF-8")
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
                if (!message.contains("Submit")) {
                    showDialog("Deen's Cheese", "Some error occurred! Please try later!", "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    detailMethodCount++;

                    if (detailMethodCount < products.size()) {
                        selectedProduct = products.get(detailMethodCount);

                        if (detailMethodCount == products.size() - 1) {
                            isLastEntry = true;
                        }

                        if (isNetworkAvailable()) {
                            SubmitDetailInvoice task = new SubmitDetailInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        showDialog("Deen's Cheese", "Daily sale added successfully!", "OK", "", R.drawable.ic_tick,
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

        if (methodName.equals("Master")) {
            message = networkResp;

            if (!message.matches("\\d+(?:\\.\\d+)?")) {
                message = "Panda Fries";
                messageDetail = "Order couldn't be placed!";
            }
        }

        if (methodName.equals("Detail")) {
            message = networkResp;
        }
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(ProductListDailySale.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(ProductListDailySale.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (message.contains("successfully")) {
                            ProductListDailySale.this.onBackPressed();
                            ProductListDailySale.this.overridePendingTransition(R.anim.slide_in_left,
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
                hideSoftKeyboard(ProductListDailySale.this);
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

    public View getViewByPosition(int pos, RecyclerView listView) {


//
//        RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder)
//                listView.findViewHolderForAdapterPosition(pos);
//        if (null != holder) {
//            return  holder.itemView;
//        }else return null;
         int firstListItemPosition=0;
        if (listView != null) {
            final RecyclerView.LayoutManager layoutManager = listView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                firstListItemPosition =  ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            }
        }
//        return 0;


//        final int firstListItemPosition = listView.getFirstVisiblePosition();
        int lastListItemPosition=0;
        if (listView != null) {
            lastListItemPosition = firstListItemPosition
                    + listView.getChildCount() - 1;
        }

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            if (listView != null) {
                return Objects.requireNonNull(listView.findViewHolderForAdapterPosition(pos)).itemView;
            }
        } else {
            final int childIndex = pos - firstListItemPosition;
            if (listView != null) {
                return listView.getChildAt(childIndex);
            }
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void quantityAdditionDialog(int index, View holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductListDailySale.this);
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = in.inflate(R.layout.my_dialogue_box, null);
        EditText quantity = v.findViewById(R.id.quantity);
        NoboButton add = v.findViewById(R.id.add);
        NoboButton cancel = v.findViewById(R.id.cancel);
        builder.setTitle("Enter Quantity");
        builder.setView(v);
        AlertDialog dialog = builder.create();

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        add.setOnClickListener(view -> {
//            View listItem = getViewByPosition(index, listView);
            TextView quantityLabel = holder.findViewById(R.id.quantityTitle);
            TextView totalAmount = holder.findViewById(R.id.totalAmount);
            if (!quantity.getText().toString().trim().equals("")) {
                try {
                    int previousQuantity = products.get(index).getQuantity();

                    if ((previousQuantity + Integer.parseInt(quantity.getText().toString().trim())) > -1) {

                        products.get(index).setQuantity(previousQuantity + Integer.parseInt(quantity.getText().toString().trim()));
//                        quantityLabel.setText((products.get(index).getQuantity()) + "x");
                        quantity.setText("");
//                        totalAmount.setText((products.get(index).getQuantity() *
//                                (products.get(index).getProductSalaPrice().intValue()
//                                        - products.get(index).getOfferAmount())) + " PKR");
//adapter.notifyDataSetChanged();
                        adapter.notifyItemChanged(index);

                    }
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, "NumberFormatException", Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    public void quantityMinusDialog(int index, View holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductListDailySale.this);
        LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = in.inflate(R.layout.my_dialogue_box, null);
        EditText quantity = v.findViewById(R.id.quantity);
        NoboButton minus = v.findViewById(R.id.add);
        NoboButton cancel = v.findViewById(R.id.cancel);
        minus.setText("  Minus  ");
        builder.setTitle("Enter Quantity");
        builder.setView(v);
        AlertDialog dialog = builder.create();

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        minus.setOnClickListener(view -> {
//            View listItem = getViewByPosition(index, listView);
            TextView quantityLabel = holder.findViewById(R.id.quantityTitle);
            TextView totalAmount = holder.findViewById(R.id.totalAmount);
            if (!quantity.getText().toString().trim().equals("")) {
                try {
                    int previousQuantity = products.get(index).getQuantity();

                    if ((previousQuantity - Integer.parseInt(quantity.getText().toString().trim())) > -1) {
                        products.get(index).setQuantity(previousQuantity -
                                Integer.parseInt(quantity.getText().toString().trim()));
                        quantityLabel.setText((products.get(index).getQuantity()) + "x");
                        quantity.setText("");
                        totalAmount.setText((products.get(index).getQuantity() *
                                (products.get(index).getProductSalaPrice().intValue()
                                        - products.get(index).getOfferAmount())) + " PKR");
                    }else {
                        Toast.makeText(ProductListDailySale.this,
                                "You cannot minus more than added",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

}