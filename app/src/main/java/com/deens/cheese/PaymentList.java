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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class PaymentList extends AppCompatActivity {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;
    ImageButton addPayment;
    ListView listView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;
    CustomerClass selectedCustomer;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    Boolean isFirstTime = true;
    ArrayList<PaymentListClass> payments = new ArrayList<>();
    PaymentListClass selectedPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_payment_list);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        back = findViewById(R.id.back);
        addPayment = findViewById(R.id.addPayment);
        listView = findViewById(R.id.listView);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        Animation bounce = AnimationUtils.loadAnimation(PaymentList.this.getBaseContext(), R.anim.bounce);
        addPayment.startAnimation(bounce);

        back.setOnClickListener(view -> {
            PaymentList.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        addPayment.setOnClickListener(view -> {
            Gson gson = new Gson();
            String selectedUser = gson.toJson(selectedCustomer);
            startActivity(new Intent(PaymentList.this, ReceiveCash.class)
                    .putExtra("SelectedUser", selectedUser)
                    .putExtra("Edit", false));
            overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        if (isNetworkAvailable()) {
            GetPaymentList task = new GetPaymentList();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }
    }

    private class GetPaymentList extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice//GetPaymentList?";

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
                        .url(URL + methodName + "LoginUserID=" + userClass.getUserID())
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "List");

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
                } else {
                    if (payments.size() > 0){
                        listView.setVisibility(View.VISIBLE);
                        PaymentListAdapter adapter = new PaymentListAdapter(payments, PaymentList.this);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (R.id.post == view.getId()){
                                selectedPayment = payments.get(i);
                                if (isNetworkAvailable()) {
                                    PostPayment task = new PostPayment();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet not available!");
                                }
                            }else if (R.id.delete == view.getId()){
                                selectedPayment = payments.get(i);
                                if (isNetworkAvailable()) {
                                    DeletePayment task = new DeletePayment();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet not available!");
                                }
                            } else if (R.id.update == view.getId()){
                                Gson gson = new Gson();
                                String selectedPaymentItem = gson.toJson(payments.get(i));
                                String selectedUser = gson.toJson(selectedCustomer);
                                startActivity(new Intent(PaymentList.this, ReceiveCash.class)
                                        .putExtra("Payment", selectedPaymentItem)
                                        .putExtra("SelectedUser", selectedUser)
                                        .putExtra("Edit", true));
                                overridePendingTransition(R.anim.slide_in_right,
                                        R.anim.slide_out_left);
                            }
                        });
                    }else {
                        listView.setVisibility(View.GONE);
                        message = "Deen's Cheese";
                        messageDetail = "No Payment item found";
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class PostPayment extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/PostReceivedPayment?";

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
                                + "PaymentID=" + selectedPayment.getPaymentID()
                                + "&LoginUserID=" + userClass.getUserID()
                        )
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

    private class DeletePayment extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/DeleteReceivedPayment?";

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
                                + "PaymentID=" + selectedPayment.getPaymentID()
                        )
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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("List")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);

                payments.clear();

                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getInt("PaymentID") != 0){
                        payments.add(new PaymentListClass(
                           jsonObject.getInt("PaymentID"),
                           jsonObject.getInt("CustomerID"),
                                jsonObject.getInt("LoginUserID"),
                                jsonObject.getString("PaymentDate").replace("T00:00:00", ""),
                                (int)jsonObject.getDouble("Amount"),
                                jsonObject.getString("PaymentType"),
                                jsonObject.getString("PaymentMode"),
                                jsonObject.getString("Remarks"),
                                jsonObject.getString("ChequeNo")
                                        .contains("null")? "":jsonObject.getString("ChequeNo")
                                        .replace("T00:00:00", ""),
                                jsonObject.getString("ChequeDate")
                                        .contains("null")? "":jsonObject.getString("ChequeDate")
                                        .replace("T00:00:00", ""),
                                jsonObject.getString("BankName")
                                        .contains("null")? "":jsonObject.getString("BankName")
                           ));
                    }
                }

            }catch (Exception ex){
                Log.w("Exception", ex.toString());
            }
        }

        if (methodName.equals("Post")){
            message = "Deen's Cheese";
            if (networkResp.contains("Posted")){
                messageDetail = "Payment posted successfully";
            }else {
                messageDetail = networkResp;
            }
        }

        if (methodName.equals("Delete")){
            message = "Deen's Cheese";
            if (networkResp.contains("Deleted")){
                messageDetail = "Payment deleted successfully";
            }else {
                messageDetail = networkResp;
            }
        }

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
                        if (messageDetail.contains("posted")) {
                            if (isNetworkAvailable()) {
                                GetPaymentList task = new GetPaymentList();
                                task.execute();
                            } else {
                                SnackAlert.error(parentView, "Internet not available!");
                            }
                        }
                        if (messageDetail.contains("deleted")) {
                            if (isNetworkAvailable()) {
                                GetPaymentList task = new GetPaymentList();
                                task.execute();
                            } else {
                                SnackAlert.error(parentView, "Internet not available!");
                            }
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        selectedCustomer = gson.fromJson(getIntent().getExtras().getString("SelectedUser"), CustomerClass.class);
        userClass = gson.fromJson(json, UserClass.class);
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }else{
            if (isNetworkAvailable()) {
                GetPaymentList task = new GetPaymentList();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet not available!");
            }
        }
    }
}