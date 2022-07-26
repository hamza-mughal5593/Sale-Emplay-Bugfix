package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deens.cheese.Adapter.CourseAdapter;
import com.deens.cheese.databinding.FragmentPreinvoiceBinding;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.ornach.nobobutton.NoboButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MappedCustomerView extends AppCompatActivity {

    private FragmentPreinvoiceBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    ArrayList<CustomerClass> customers = new ArrayList<>();
    String selectedCustomerID = "";
    TextView customerSpinner;
    NoboButton preInvoice;
    ImageView back;
    String whatToGenerate = "";
    RelativeLayout listmain;
    LinearLayout customerView;

    final Calendar myCalendar= Calendar.getInstance();
    EditText selectdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapped_customer_view);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        customerSpinner = findViewById(R.id.customerSpinner);
        preInvoice = findViewById(R.id.preInvoice);
        back = findViewById(R.id.back);
        listmain = findViewById(R.id.listmain);
        customerView = findViewById(R.id.customerView);

        whatToGenerate = getIntent().getExtras().getString("From", whatToGenerate);
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

        if (isNetworkAvailable()) {
            GetMappedCustomers task = new GetMappedCustomers();
            task.execute();
        } else {
            SnackAlert.error(findViewById(android.R.id.content), "Internet not available!");
        }

        preInvoice.setOnClickListener(view -> {
            if (whatToGenerate.contains("Purchase Order")){
                startActivity(new Intent(MappedCustomerView.this, ProductListPO.class)
                        .putExtra("CID", selectedCustomerID).putExtra("dated",selectdate.getText().toString()));
                finish();
            }else {
                startActivity(new Intent(MappedCustomerView.this, ProductListDailySale.class)
                        .putExtra("CID", selectedCustomerID).putExtra("dated",selectdate.getText().toString()));
                finish();
            }
        });

        preInvoice.setText(whatToGenerate);

        back.setOnClickListener(view -> onBackPressed());



        selectdate =(EditText) findViewById(R.id.selectdate);
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };
        selectdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MappedCustomerView.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }
    private void updateLabel(){
        String myFormat="yyyy-MM-dd";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        selectdate.setText(dateFormat.format(myCalendar.getTime()));
    }

    @Override
    public void onBackPressed() {

        if (listmain.getVisibility() == View.VISIBLE) {
            customerView.setVisibility(View.VISIBLE);
            listmain.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }


    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private class GetMappedCustomers extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/GetMapedCustomer?UserID=" + userClass.getUserID();

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
            if (!isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    Toast.makeText(MappedCustomerView.this, "Customers not found", Toast.LENGTH_SHORT).show();
                } else {
                    if (customers.size() > 0){
                        selectedCustomerID = String.valueOf(customers.get(0).getCustomerID());
                        ArrayList<String> names = new ArrayList<>();
                        for (int i = 0; i < customers.size(); i++) {
                            names.add(customers.get(i).getCustomerName());
                        }

                        if (names.size()>0){
                            customerSpinner.setText(names.get(0));
                            customerSpinner.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    customerView.setVisibility(View.GONE);
                                    listmain.setVisibility(View.VISIBLE);
                                    setlist(names);
                                }
                            });
                        }




//                        ArrayAdapter adapter = new ArrayAdapter(MappedCustomerView.this, R.layout.spinner_item, names.toArray());
//                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        customerSpinner.setAdapter(adapter);
//                        customerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                            @Override
//                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                                selectedCustomerID = String.valueOf(customers.get(i).getCustomerID());
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> adapterView) {}
//                        });
                    }
                }
            }
        }
    }



RecyclerView listrecycler;
    CourseAdapter adapter;
    private void setlist(ArrayList<String> names){



        // initializing our variables.
        listrecycler = findViewById(R.id.listrecycler);

        // calling method to
        // build recycler view.
        buildRecyclerView(names);



        EditText searchView = findViewById(R.id.searchField);

        // below line is to call set on query text listener method.
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s,names);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // inside on query text change method we are
//                // calling a method to filter our recycler view.
//
//                return false;
//            }
//        });
    }

    private void filter(CharSequence text,ArrayList<String> data) {
        // creating a new array list to filter our data.
        ArrayList<String> filteredlist = new ArrayList<>();

        // running a for loop to compare elements.
        for (String item : data) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.toLowerCase().contains(text)) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist);
        }
    }
    private void buildRecyclerView(ArrayList<String> names) {

        // below line we are creating a new array list


        // initializing our adapter class.
        adapter = new CourseAdapter(names, MappedCustomerView.this, new CourseAdapter.AdapterCallback() {
            @Override
            public void add_data(int pos, String holder) {
                selectedCustomerID = String.valueOf(customers.get(pos).getCustomerID());
                customerView.setVisibility(View.VISIBLE);
                listmain.setVisibility(View.GONE);

                if (names.size()>0){
                    customerSpinner.setText(holder);
                }

            }
        });

        // adding layout manager to our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(this);
        listrecycler.setHasFixedSize(true);

        // setting layout manager
        // to our recycler view.
        listrecycler.setLayoutManager(manager);

        // setting adapter to
        // our recycler view.
        listrecycler.setAdapter(adapter);
    }



    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("Customer")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0){
                    if (!jsonArray.getJSONObject(0).getString("CustomerCode").equals("null")){
                        customers.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            customers.add(new CustomerClass(jsonObject.getInt("CustomerID"),
                                    jsonObject.getString("CustomerCode"),
                                    jsonObject.getString("CustomerName"),
                                    jsonObject.getInt("CityID"),
                                    jsonObject.getString("CityName"),
                                    jsonObject.getString("Area"),
                                    jsonObject.getString("Address"),
                                    jsonObject.getString("Mobile"),
                                    jsonObject.getString("Mobile1")
                            ));
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

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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
}