package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class ReceiveCash extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;
    Spinner paymentModeSpinner;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    CustomerClass selectedCustomer;
    PaymentListClass selectedPayment;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;

    ElasticButton postPayment;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    RadioGroup paymentTypeGroup;
    RadioButton cashButton, bankButton;
    TextView selectedDate;
    EditText amount, chequeNo, remarks, bankName, transaction;
    TextView paymentDate, chequeDate;
    LinearLayout chequeDateView, chequeNoView, modeSpinner, bankNameView, transactionView;
    String paymentType = "Cash";
    String paymentMode = "online";
    String chequeNumber = "";
    String chequePostDate = "";
    String selectedBankName = "";
    Boolean isCash = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_receive_cash);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        postPayment = findViewById(R.id.postPayment);
        paymentModeSpinner = findViewById(R.id.paymentModeSpinner);
        paymentDate = findViewById(R.id.paymentDate);
        amount = findViewById(R.id.amount);
        cashButton = findViewById(R.id.cash);
        bankButton = findViewById(R.id.bank);
        amount = findViewById(R.id.amount);
        paymentTypeGroup = findViewById(R.id.paymentTypeGroup);
        chequeDate = findViewById(R.id.chequeDate);
        chequeNo = findViewById(R.id.chequeNo);
        remarks = findViewById(R.id.remarks);
        chequeDateView = findViewById(R.id.chequeDateView);
        chequeNoView = findViewById(R.id.chequeNoView);
        modeSpinner = findViewById(R.id.modeSpinner);
        bankNameView = findViewById(R.id.bankNameView);
        bankName = findViewById(R.id.bankName);
        transaction = findViewById(R.id.transaction);
        transactionView = findViewById(R.id.transactionView);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        // Setup payment & cheque date to today
        paymentDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
        chequeDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        loadPreferences();

        back = findViewById(R.id.back);

        // Date variable initialized
        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        back.setOnClickListener(view -> {
            ReceiveCash.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        paymentDate.setOnClickListener(view -> {
            selectedDate = paymentDate;
            new DatePickerDialog(ReceiveCash.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        chequeDate.setOnClickListener(view -> {
            selectedDate = chequeDate;
            new DatePickerDialog(ReceiveCash.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        paymentTypeGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.bank){
               bankChecked();
            }else {
                cashChecked();
            }
        });

        postPayment.setOnClickListener(view -> {
            if (isNetworkAvailable()){
                try {
                    Integer.parseInt(amount.getText().toString().trim());
                    if (isCash){
                        if (amount.getText().toString().trim().equals("") ||
                                paymentDate.getText().toString().trim().equals("")
                                || remarks.getText().toString().trim().equals("")){
                            SnackAlert.error(parentView, "Fill all fields");
                        }else {
                            PostPayment task = new PostPayment();
                            task.execute();
                        }
                    }else {
                        if (amount.getText().toString().trim().equals("") ||
                                paymentDate.getText().toString().trim().equals("")
                                || remarks.getText().toString().trim().equals("")
                                || bankName.getText().toString().trim().equals("")){
                            SnackAlert.error(parentView, "Fill all fields");
                        }else {
                            if (paymentMode.equals("cheque")){
                                if (chequeNo.getText().toString().trim().equals("") ||
                                                chequeDate.getText().toString().trim().equals("")){
                                    SnackAlert.error(parentView, "Fill all fields");
                                }else {
                                    PostPayment task = new PostPayment();
                                    task.execute();
                                }
                            }else {
                                PostPayment task = new PostPayment();
                                task.execute();
                            }
                        }
                    }
                }catch (NumberFormatException ex){
                    ex.printStackTrace();
                    SnackAlert.error(parentView, "Please enter right amount");
                }
            }else {
                SnackAlert.error(parentView, "Internet not available");
            }
        });

    }

    private void loadPreferences() {
        Gson gson = new Gson();
        selectedCustomer = gson.fromJson(getIntent().getExtras().getString("SelectedUser"), CustomerClass.class);

        if (getIntent().getExtras().getBoolean("Edit")){
            selectedPayment = gson.fromJson(getIntent().getExtras().getString("Payment"), PaymentListClass.class);
            loadSavedDetails();
            bankButton.setEnabled(false);
            cashButton.setEnabled(false);
        }

        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private void loadSavedDetails(){
        amount.setText(String.valueOf(selectedPayment.getAmount()));
        paymentDate.setText(selectedPayment.getPaymentDate());
        if (selectedPayment.getPaymentType().equals("Bank")){
            bankButton.setChecked(true);
            cashButton.setChecked(false);
            bankChecked();
        }else {
            bankButton.setChecked(false);
            cashButton.setChecked(true);
            cashChecked();
        }
        if (selectedPayment.getPaymentMode().equals("cheque")){
            paymentModeSpinner.setSelection(1);
        }else{
            paymentModeSpinner.setSelection(0);
        }
        chequeNo.setText(selectedPayment.getChequeNo());
        chequeDate.setText(selectedPayment.getChequeDate());
        bankName.setText(selectedPayment.getBankName());
        remarks.setText(selectedPayment.getRemarks());
    }

    private void bankChecked(){
        modeSpinner.setVisibility(View.VISIBLE);
        bankNameView.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.VISIBLE);
        transaction.setText("");
        // Load Payment Mode Spinner
        String[] days = {"Online", "Cheque"};
        ArrayAdapter adapter = new ArrayAdapter(ReceiveCash.this, R.layout.spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentModeSpinner.setAdapter(adapter);
        paymentModeSpinner.setOnItemSelectedListener(ReceiveCash.this);
        paymentType = "Bank";
        isCash = false;
    }

    private void cashChecked(){
        modeSpinner.setVisibility(View.GONE);
        bankNameView.setVisibility(View.GONE);
        transactionView.setVisibility(View.GONE);
        transaction.setText("");
        chequeDateView.setVisibility(View.GONE);
        chequeNoView.setVisibility(View.GONE);
        paymentType = "Cash";
        isCash = true;
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(ReceiveCash.this);
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

    private class PostPayment extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/ReceivedPayment?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            if (isCash){
                paymentMode = "";
                chequeNumber = "";
                chequePostDate = "";
                selectedBankName = "";
            }else {
                chequeNumber = chequeNo.getText().toString().trim();
                chequePostDate = chequeDate.getText().toString().trim();
                selectedBankName = bankName.getText().toString().trim();
            }

            if (paymentModeSpinner.getSelectedItem() != null && paymentModeSpinner.getSelectedItem().equals("Online")){
                chequePostDate = "";
                chequeNumber = "";
            }else {
                if (!isCash){
                    chequePostDate = chequeDate.getText().toString().trim();
                    chequeNumber = chequeNo.getText().toString().trim();
                }
            }

            if (getIntent().getExtras().getBoolean("Edit")){
                methodName = "Invoice/UpdateReceivedPayment?PaymentID=" + selectedPayment.getPaymentID() + "&";
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "CustomerID=" + selectedCustomer.getCustomerID()
                                + "&LoginUserID=" + URLEncoder.encode(String.valueOf(userClass.getUserID()), "UTF-8")
                                + "&PaymentDate=" + URLEncoder.encode(paymentDate.getText().toString().trim(), "UTF-8")
                                + "&Amount=" + URLEncoder.encode(amount.getText().toString(), "UTF-8")
                                + "&PaymentType=" + URLEncoder.encode(paymentType, "UTF-8")
                                + "&PaymentMode=" + URLEncoder.encode(paymentMode, "UTF-8")
                                + "&Remarks=" + URLEncoder.encode(remarks.getText().toString().trim(), "UTF-8")
                                + "&ChequeNo=" + URLEncoder.encode(chequeNumber, "UTF-8")
                                + "&ChequeDate=" + URLEncoder.encode(chequePostDate, "UTF-8")
                                + "&BankName=" + URLEncoder.encode(selectedBankName, "UTF-8")
                                + "&TransID=" + URLEncoder.encode(transaction.getText().toString().trim(), "UTF-8")
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
                if (!messageDetail.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Post")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
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
                        if (message.contains("saved")) {
                            super.onBackPressed();
                            ReceiveCash.super.onBackPressed();
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectedDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (paymentModeSpinner.getSelectedItemPosition() == 1){
            chequeDateView.setVisibility(View.VISIBLE);
            chequeNoView.setVisibility(View.VISIBLE);
            transactionView.setVisibility(View.GONE);
            transaction.setText("");
            paymentMode = "cheque";
        }else {
            chequeDateView.setVisibility(View.GONE);
            chequeNoView.setVisibility(View.GONE);
            paymentMode = "online";
            transactionView.setVisibility(View.VISIBLE);
            transaction.setText("");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}