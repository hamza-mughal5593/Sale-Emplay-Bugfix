package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class AddCustomer extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    EditText latitude, longitude, openingBalance;
    ElasticButton gotoLocation;
    TextView customerLocation;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    ImageView mapMarker;

    LocationManager lm;

    String lat = "", lng = "", loc = "";

    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    Button forward, previous;
    RadioButton type, location, info;
    int stepperSelectedPosition = 0;
    ElasticButton addCustomer;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    // User Type View Containing User Type Spinner
    LinearLayout typeView;
    // Hierarchy View Containing Hierarchy Spinner
    LinearLayout locationView;
    // Info View Containing Info Spinners
    LinearLayout infoView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "0";
    String loggedInUserId = "0";
    String selectedUserTypeId = "7";
    UserClass userClass;

    // This list has all user types this logged in user can create
    ArrayList<EmployeeType> allowedUserTypes = new ArrayList<>();

    // Spinners used in SO Selection View
    Spinner gmSpinner, stSpinner, sdmSpinner, dmSpinner, soSpinner;
    // Views containing hierarchy spinner
    LinearLayout gmView, stView, dmView, sdmView, soView;
    // All Employees List from server populated once
    ArrayList<EmployeeClass> employees = new ArrayList<>();

    // General Manager List populated once filtered from all employees list
    ArrayList<EmployeeClass> gmList = new ArrayList<>();

    // Sale Team List populated in result of selection made on GM Spinner
    ArrayList<EmployeeClass> stList = new ArrayList<>();

    // Distribute Manager populated in result of selection made on Sale Team Spinner
    ArrayList<EmployeeClass> dmList = new ArrayList<>();

    // Sale Distribute Manager List populated in result of selection made on DM Spinner
    ArrayList<EmployeeClass> sdmList = new ArrayList<>();

    // Sale Officer List populated in result of selection made on SDM Spinner
    ArrayList<EmployeeClass> soList = new ArrayList<>();

    // City List from Server
    ArrayList<CityClass> cities = new ArrayList<>();

    // Miscellaneous Methods used to get different data from server
    String[] miscMethodNames = {"GetCitiesList"};
    String[] methodType = {"City"};
    int methodCount = 0;

    // Spinners and fields of Info View
    Spinner citySpinner, daySpinner;
    SwitchCompat isTax;
    EditText customerName, area, address, ntnNo, stnNo;
    MaskedEditText contactNo, contactNo2;
    TextView registrationDate;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_add_customer);

        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        gotoLocation = findViewById(R.id.gotoLocation);
        isTax = findViewById(R.id.isTax);
        ntnNo = findViewById(R.id.ntnNo);
        stnNo = findViewById(R.id.stnNo);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        forward = findViewById(R.id.forward);
        openingBalance = findViewById(R.id.openingBalance);
        back = findViewById(R.id.back);
        typeView = findViewById(R.id.typeView);
        type = findViewById(R.id.type);
        forward = findViewById(R.id.forward);
        previous = findViewById(R.id.previous);
        info = findViewById(R.id.info);
        locationView = findViewById(R.id.locationView);
        location = findViewById(R.id.location);
        infoView = findViewById(R.id.infoView);
        addCustomer = findViewById(R.id.addCustomer);
        gmSpinner = findViewById(R.id.gmSpinner);
        sdmSpinner = findViewById(R.id.sdmSpinner);
        dmSpinner = findViewById(R.id.dmSpinner);
        soSpinner = findViewById(R.id.soSpinner);
        stSpinner = findViewById(R.id.stSpinner);
        gmView = findViewById(R.id.gmView);
        sdmView = findViewById(R.id.sdmView);
        dmView = findViewById(R.id.dmView);
        soView = findViewById(R.id.soView);
        stView = findViewById(R.id.stView);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        customerLocation = findViewById(R.id.customerLocation);
        registrationDate = findViewById(R.id.dor);
        citySpinner = findViewById(R.id.citySpinner);
        daySpinner = findViewById(R.id.daySpinner);
        customerName = findViewById(R.id.customerName);
        area = findViewById(R.id.area);
        address = findViewById(R.id.address);
        contactNo = findViewById(R.id.contactNo);
        contactNo2 = findViewById(R.id.contactNo2);

        // Date variable initialized
        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        lm = (LocationManager) AddCustomer.this.getSystemService(Context.LOCATION_SERVICE);

        mapMarker = findViewById(R.id.mapMarker);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);    // 10 seconds, in milliseconds
        mLocationRequest.setFastestInterval(1000);   // 1 second, in milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(AddCustomer.this);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        forward.setOnClickListener(view -> {
            hideSoftKeyboard(AddCustomer.this);

            stepperSelectedPosition++;

            if (stepperSelectedPosition == 3) {
                stepperSelectedPosition--;
            } else {
                switch (stepperSelectedPosition) {
                    case 0:
                        changeSwitchSelection(type, location, info);
                        animateView(locationView, typeView);
                        addCustomer.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        changeSwitchSelection(location, type, info);
                        animateView(typeView, locationView);
                        addCustomer.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        changeSwitchSelection(info, type, location);
                        animateView(locationView, infoView);
                        addCustomer.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        previous.setOnClickListener(view -> {
            hideSoftKeyboard(AddCustomer.this);

            stepperSelectedPosition--;

            if (stepperSelectedPosition == -1) {
                stepperSelectedPosition++;
            } else {
                switch (stepperSelectedPosition) {
                    case 0:
                        changeSwitchSelection(type, location, info);
                        animateView(locationView, typeView);
                        addCustomer.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        changeSwitchSelection(location, type, info);
                        animateView(infoView, locationView);
                        addCustomer.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        changeSwitchSelection(info, type, location);
                        break;
                }
            }
        });

        back.setOnClickListener(view -> {
            AddCustomer.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        registrationDate.setOnClickListener(view -> {
            new DatePickerDialog(AddCustomer.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (isNetworkAvailable()) {
            GetAllowedUserTyps task = new GetAllowedUserTyps();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        addCustomer.setOnClickListener(view -> {
            if (latitude.getText().toString().trim().equals("") ||
                    longitude.getText().toString().trim().equals("") ||
                    customerLocation.getText().toString().trim().equals("")) {
                SnackAlert.error(parentView, "Map location not added");
            } else {
                if (customerName.getText().toString().trim().equals("") ||
                        area.getText().toString().trim().equals("") ||
                        address.getText().toString().trim().equals("") ||
                        contactNo.getRawText().trim().equals("") ||
                        registrationDate.getText().toString().trim().equals("") ||
                        openingBalance.getText().toString().trim().equals("")) {
                    SnackAlert.error(parentView, "Add necessary fields!");
                } else {
                    if (soList.size() <= 0){
                        // If Sale man/Driver cannot add customer we will remove usertype 7 from below
                        // Or If any other User Type cannot add customer we will restrict it here
                       if (loggedInUserType.equals("6") || loggedInUserType.equals("7")){
                           try {
                               Double.parseDouble(lat);
                               Double.parseDouble(lng);
                               Integer.parseInt(openingBalance.getText().toString().trim());
                               if (isNetworkAvailable()) {
                                   AddNewCustomer task = new AddNewCustomer();
                                   task.execute();
                               } else {
                                   SnackAlert.error(parentView, "Internet connectivity problem");
                               }
                           }catch (NumberFormatException ex){
                               SnackAlert.error(parentView, "Please enter a valid latitude, longitude, opening balance");
                           }
                       } else {
                           SnackAlert.error(parentView , "Sale Officer is not selected!");
                       }
                    }else {
                        try {
                            Double.parseDouble(lat);
                            Double.parseDouble(lng);
                            Integer.parseInt(openingBalance.getText().toString().trim());
                            if (isNetworkAvailable()) {
                                AddNewCustomer task = new AddNewCustomer();
                                task.execute();
                            } else {
                                SnackAlert.error(parentView, "Internet connectivity problem");
                            }
                        }catch (NumberFormatException ex){
                            SnackAlert.error(parentView, "Please enter a valid latitude, longitude, opening balance");
                        }
                    }
                }
            }
        });

        gotoLocation.setOnClickListener(view -> {
            try {
                lat = latitude.getText().toString().trim();
                lng = longitude.getText().toString().trim();
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }catch (NumberFormatException ex){
                SnackAlert.error(parentView, "Please enter a valid latitude/longitude");
            }
        });

        loadDaysSpinner();

    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            // TODO: Resource Ids will be non final from Gradle Version 8.0 Avoid using in Switch Statements
            case R.id.gmSpinner:
                stList = generateEmployeeList(3,
                        gmList.get(gmSpinner.getSelectedItemPosition()).getUserID());
                if (stList.size() == 0) {
                    // We will clear all downward spinners due to parent spinner is empty now
                    clearSpinner(stSpinner);
                    clearSpinner(dmSpinner);
                    clearSpinner(sdmSpinner);
                    clearSpinner(soSpinner);
                } else {
                    populateSpinner(stSpinner, stList);
                }
                break;
            case R.id.stSpinner:
                dmList = generateEmployeeList(4,
                        stList.get(stSpinner.getSelectedItemPosition()).getUserID());
                if (dmList.size() == 0) {
                    // We will clear all downward spinners due to parent spinner is empty now
                    clearSpinner(dmSpinner);
                    clearSpinner(sdmSpinner);
                    clearSpinner(soSpinner);
                } else {
                    populateSpinner(dmSpinner, dmList);
                }
                break;
            case R.id.dmSpinner:
                sdmList = generateEmployeeList(5,
                        dmList.get(dmSpinner.getSelectedItemPosition()).getUserID());
                if (sdmList.size() == 0) {
                    // We will clear all downward spinners due to parent spinner is empty now
                    clearSpinner(sdmSpinner);
                    clearSpinner(soSpinner);
                } else {
                    populateSpinner(sdmSpinner, sdmList);
                }
                break;
            case R.id.sdmSpinner:
                soList = generateEmployeeList(6,
                        sdmList.get(sdmSpinner.getSelectedItemPosition()).getUserID());
                if (soList.size() == 0) {
                    clearSpinner(soSpinner);
                } else {
                    populateSpinner(soSpinner, soList);
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private class GetAllowedUserTyps extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetUserTypeList?";

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
                        .url(URL + methodName + "UserTypeID=" + URLEncoder.encode(loggedInUserType, "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Type");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!AddCustomer.this.isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (allowedUserTypes.size() > 0) {
                        // Show all Spinners above selected user type to be created
                        // Hide all Spinners below and equal to selected user type

                        // Extract all user types ids
                        ArrayList<Integer> allowedUserTypesIDs = new ArrayList<>();
                        for (int k = 0; k < allowedUserTypes.size(); k++) {
                            allowedUserTypesIDs.add(Integer.parseInt(allowedUserTypes.get(k).getUserTypeID()));
                        }

                        // Extract all Selectable Spinners and Non Selectable
                        for (int j = 2; j < 7; j++) {
                            // If condition is true -- Spinner is selectable
                            // If false -- Spinner is non-selectable
                            if (allowedUserTypesIDs.contains(j)) {
                                // Selectable
                                if (Integer.parseInt(selectedUserTypeId) <= j) {
                                    // Spinners above and equal to Selected User Type ID
                                    // Selectable & Visible & isOnClickListener = false
                                    showHideHierarchySpinnersViews(View.GONE, j,
                                            getResources().getDrawable(R.drawable.corner_bg),
                                            true, false);
                                } else {
                                    // Selectable & Visible & isOnClickListener = true
                                    // These are required values to be selected by user
                                    showHideHierarchySpinnersViews(View.VISIBLE, j,
                                            getResources().getDrawable(R.drawable.corner_bg),
                                            true, true);
                                }
                            } else {
                                // Non Selectable & Visible & isOnClickListener = false
                                showHideHierarchySpinnersViews(View.GONE, j,
                                        getResources().getDrawable(R.drawable.corner_error_bg),
                                        false, false);
                            }
                        }

//                        if (checkIfAllSpinnersAreHidden()){
//                            SnackAlert.info(parentView, "You cannot change hierarchy");
//                        }
                        // Will Call All employees list here
                        if (isNetworkAvailable()) {
                            GetEmployeesList task = new GetEmployeesList();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }

                    } else {
                        showDialog("Deen's Cheese", "You cannot add any customer!", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class GetEmployeesList extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetEmployeeList?";

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
                        .url(URL + methodName + "EmpID=0&UserTypeID=0&UserID=" + URLEncoder.encode(loggedInUserId, "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Employees");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!AddCustomer.this.isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    // We will populate first level visible spinner index :  0 of allowed user types
                    // We will filter first level spinner data w.r.t. loggedInUserType

                    if (allowedUserTypes.size() > 0) {
                        switch (allowedUserTypes.get(0).getUserTypeID()) {
                            case "2":
                                for (int i = 0; i < employees.size(); i++) {
                                    if (employees.get(i).getUserTypeID() == 2) {
                                        gmList.add(employees.get(i));
                                    }
                                }
                                populateSpinner(gmSpinner, gmList);
                                break;
                            case "3":
                                stList = generateEmployeeList(3, (userClass.getUserID()));
                                populateSpinner(stSpinner, stList);
                                break;
                            case "4":
                                dmList = generateEmployeeList(4, userClass.getUserID());
                                populateSpinner(dmSpinner, dmList);
                                break;
                            case "5":
                                sdmList = generateEmployeeList(5, userClass.getUserID());
                                populateSpinner(sdmSpinner, sdmList);
                                break;
                            case "6":
                                soList = generateEmployeeList(6, userClass.getUserID());
                                populateSpinner(soSpinner, soList);
                                break;
                        }
                    }

                    // Get Miscellaneous Data from server of different lists
                    if (isNetworkAvailable()) {
                        GetDataFromServer task = new GetDataFromServer();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
            }
        }
    }

    private class GetDataFromServer extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/" + miscMethodNames[methodCount] + "?";

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
                parseJSONStringToJSONObject(networkResp, methodType[methodCount]);

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
                    methodCount++;

                    if (methodCount < methodType.length) {
                        if (isNetworkAvailable()) {
                            GetDataFromServer task = new GetDataFromServer();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        loadMiscSpinners();
                    }
                }
            }
        }
    }

    private class AddNewCustomer extends AsyncTask<Void, Void, Void> {

        String methodName = "Customer/AddNewCustomer?";
        String selectedSOID = "0";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            // Selected SO ID. If SO View is visible we will update id. Otherwise we will pass 0
            if (soList.size() > 0 && soView.getVisibility() == View.VISIBLE) {
                if (soSpinner.getSelectedItemPosition() != -1) {
                    selectedSOID = String.valueOf(soList.get(soSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (loggedInUserType.equals("6")) {
                    selectedSOID = String.valueOf(userClass.getUserID());
                }
                if (loggedInUserType.equals("7")) {
                    selectedSOID = String.valueOf(userClass.getSO());
                }
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
                                + "CustomerName=" + URLEncoder.encode(customerName.getText().toString().trim(), "UTF-8")
                                + "&CityID=" + URLEncoder.encode(String.valueOf(cities.get(citySpinner.getSelectedItemPosition()).CityId), "UTF-8")
                                + "&Area=" + URLEncoder.encode(area.getText().toString(), "UTF-8")
                                + "&Address=" + URLEncoder.encode(address.getText().toString().trim(), "UTF-8")
                                + "&Mobile=" + URLEncoder.encode(contactNo.getRawText().trim(), "UTF-8")
                                + "&Mobile1=" + URLEncoder.encode(contactNo2.getRawText().trim(), "UTF-8")
                                + "&RegistrationDate=" + URLEncoder.encode(registrationDate.getText().toString().trim(), "UTF-8")
                                + "&SO_ID=" + URLEncoder.encode(selectedSOID, "UTF-8")
                                + "&Visit_Day=" + daySpinner.getSelectedItem().toString()
                                + "&MapLocation=" + URLEncoder.encode(customerLocation.getText().toString().trim(), "UTF-8")
                                + "&Lat=" + URLEncoder.encode(latitude.getText().toString().trim(), "UTF-8")
                                + "&Long=" + URLEncoder.encode(longitude.getText().toString().trim(), "UTF-8")
                                + "&LoginUserID=" + loggedInUserId
                                + "&OpeningBalance=" + loggedInUserId
                                + "&Is_Tax_Register=" + (isTax.isChecked()? "1":"0")
                                + "&NTNNo=" + URLEncoder.encode(isTax.isChecked()? ntnNo.getText().toString().trim():"-", "UTF-8")
                                + "&STNNo=" + URLEncoder.encode(isTax.isChecked()? stnNo.getText().toString().trim():"-", "UTF-8")
                        )
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Add");

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

    private void loadDaysSpinner() {
        String[] genders = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter adapter = new ArrayAdapter(AddCustomer.this, R.layout.spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);
    }

    private void loadMiscSpinners() {
        ArrayList<String> cityNames = new ArrayList<>();

        if (cities.size() > 0) {
            for (int i = 0; i < cities.size(); i++) {
                cityNames.add(cities.get(i).getCityName());
            }
            loadSpinner(citySpinner, cityNames);
        }
    }

    private ArrayList<EmployeeClass> generateEmployeeList(int userTypeID, int selectedParentID) {

        ArrayList<EmployeeClass> list = new ArrayList<>();

        for (int i = 0; i < employees.size(); i++) {
            switch (userTypeID) {
                // ST Employees
                case 3:
                    if (selectedParentID == employees.get(i).getGM()
                            && userTypeID == employees.get(i).getUserTypeID()) {
                        list.add(employees.get(i));
                    }
                    break;
                // DM Employees
                case 4:
                    if (selectedParentID == employees.get(i).getST()
                            && userTypeID == employees.get(i).getUserTypeID()) {
                        list.add(employees.get(i));
                    }
                    break;
                // SDM Employees
                case 5:
                    if (selectedParentID == employees.get(i).getDM()
                            && userTypeID == employees.get(i).getUserTypeID()) {
                        list.add(employees.get(i));
                    }
                    break;
                // SO Employees
                case 6:
                    if (selectedParentID == employees.get(i).getSDM()
                            && userTypeID == employees.get(i).getUserTypeID()) {
                        list.add(employees.get(i));
                    }
                    break;
                // SM/DRIVER Employees
                case 7:
                    break;
            }
        }

        return list;
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Type")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    allowedUserTypes.add(new EmployeeType(jsonObject.getString("UserTypeID"),
                            jsonObject.getString("UserTypeName")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (methodName.equals("Employees")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    employees.add(new EmployeeClass(jsonObject.getInt("UserID"),
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

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (methodName.equals("City")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    cities.add(new CityClass(jsonObject.getInt("CityId"),
                            jsonObject.getString("CityName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Add")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

    }

    private void showHideHierarchySpinnersViews(int visibility, int index,
                                                Drawable background,
                                                Boolean isSelectable, Boolean isOnCLickListener) {
        switch (index) {
            case 2:
                gmView.setVisibility(visibility);
                gmView.setBackground(background);
                if (!isSelectable) {
                    gmSpinner.setEnabled(false);
                    if (Integer.parseInt(loggedInUserType) == index) {
                        populateSpinnerWithArray(gmSpinner, new String[]{userClass.getEmpName()});
                    } else {
                        populateSpinnerWithArray(gmSpinner, new String[]{userClass.getGMName()});
                    }
                } else {
                    if (isOnCLickListener) {
                        gmSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
            case 3:
                stView.setVisibility(visibility);
                stView.setBackground(background);
                if (!isSelectable) {
                    stSpinner.setEnabled(false);
                    if (Integer.parseInt(loggedInUserType) == index) {
                        populateSpinnerWithArray(stSpinner, new String[]{userClass.getEmpName()});
                    } else {
                        populateSpinnerWithArray(stSpinner, new String[]{userClass.getSTName()});
                    }
                } else {
                    if (isOnCLickListener) {
                        stSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
            case 4:
                dmView.setVisibility(visibility);
                dmView.setBackground(background);
                if (!isSelectable) {
                    dmSpinner.setEnabled(false);
                    if (Integer.parseInt(loggedInUserType) == index) {
                        populateSpinnerWithArray(dmSpinner, new String[]{userClass.getEmpName()});
                    } else {
                        populateSpinnerWithArray(dmSpinner, new String[]{userClass.getDMName()});
                    }
                } else {
                    if (isOnCLickListener) {
                        dmSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
            case 5:
                sdmView.setVisibility(visibility);
                sdmView.setBackground(background);
                if (!isSelectable) {
                    sdmSpinner.setEnabled(false);
                    if (Integer.parseInt(loggedInUserType) == index) {
                        populateSpinnerWithArray(sdmSpinner, new String[]{userClass.getEmpName()});
                    } else {
                        populateSpinnerWithArray(sdmSpinner, new String[]{userClass.getSDMName()});
                    }
                } else {
                    if (isOnCLickListener) {
                        sdmSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
            case 6:
                soView.setVisibility(visibility);
                soView.setBackground(background);
                if (!isSelectable) {
                    soSpinner.setEnabled(false);
                    if (Integer.parseInt(loggedInUserType) == index) {
                        populateSpinnerWithArray(soSpinner, new String[]{userClass.getEmpName()});
                    } else {
                        populateSpinnerWithArray(soSpinner, new String[]{userClass.getSOName()});
                    }
                } else {
                    if (isOnCLickListener) {
                        soSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
        }
    }

    private void populateSpinner(Spinner spinner, ArrayList<EmployeeClass> list) {
        ArrayList<String> names = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            names.add(list.get(i).getEmpName());
        }

        ArrayAdapter adapter = new ArrayAdapter(AddCustomer.this, R.layout.spinner_item, names.toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void populateSpinnerWithArray(Spinner spinner, String[] array) {
        ArrayAdapter adapter = new ArrayAdapter(AddCustomer.this, R.layout.spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // We will call this method to clear spinner items of subsequent spinners if parent has no value
    private void clearSpinner(Spinner spinner) {
        if (spinner.getAdapter() != null) {
            spinner.setAdapter(null);
        }
    }

    private void changeSwitchSelection(RadioButton selected, RadioButton one, RadioButton two) {
        AddCustomer.this.runOnUiThread(() -> {
            selected.setChecked(true);
            one.setChecked(false);
            two.setChecked(false);
        });
    }

    private void animateView(LinearLayout hideView, LinearLayout showView) {
        AddCustomer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideView.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                hideView.setVisibility(View.GONE);
                                showView.setVisibility(View.VISIBLE);
                                showView.setAlpha(1.0f);
                            }
                        });
            }
        });
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(AddCustomer.this);
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
                            AddCustomer.super.onBackPressed();
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

    private void loadSpinner(Spinner spinner, ArrayList<String> list) {
        ArrayAdapter adapter = new ArrayAdapter(AddCustomer.this, R.layout.spinner_item, list.toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog("DENIED PERMISSIONS", "Give required location permission to app \"Deen's Cheese\" in Settings & comeback", "Open Settings", "Cancel", true, "");
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnCameraIdleListener(() -> {
            mapMarker.setImageResource(R.drawable.ic_map_pin);

            mMap.clear();

            lat = String.valueOf(mMap.getCameraPosition().target.latitude);
            lng = String.valueOf(mMap.getCameraPosition().target.longitude);
            loc = returnLocation();

            latitude.setText(lat);
            longitude.setText(lng);
            customerLocation.setText(loc);

        });


        mMap.setOnMyLocationButtonClickListener(() -> {
            Boolean gps_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                Log.i("EXCEPTION: ", ex.toString());
            }
            if (gps_enabled) {
                if (mGoogleApiClient.isConnected()) {
                    getLocation();
                } else {
                    mGoogleApiClient.connect();
                }
            } else {
                Snackbar.make(parentView, "ENABLE GPS", Snackbar.LENGTH_SHORT).show();
            }
            return false;
        });
    }

    ///////////////////////////////////////
    ////// CURRENT LOCATION STARTS ////////

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!lat.equals("")) {
            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            mMap.clear();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        } else {
            new Handler().postDelayed(this::settingRequest, 4000);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Location Service Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // DO NOTHING
    }

    @SuppressLint("RestrictedApi")
    public void settingRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            final LocationSettingsStates state = result1.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can
                    // initialize location requests here.
                    getLocation();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(AddCustomer.this, 1000);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        if (mGoogleApiClient.isConnected()) {
                            getLocation();
                        } else {
//                            Toast.makeText(LocationSelect.this, "LOCATION API NOT CONNECTED YET", Toast.LENGTH_SHORT).show();
                            mGoogleApiClient.connect();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog("DENIED PERMISSIONS", "Give required location permission to app \"Deen's Cheese\" in Settings & comeback", "Open Settings", "Cancel", true, "");
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lng = String.valueOf(mLastLocation.getLongitude());
                loc = returnLocation();

                latitude.setText(lat);
                longitude.setText(lng);
                customerLocation.setText(loc);

                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            } else {
                Boolean gps_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                    Log.i("EXCEPTION: ", ex.toString());
                }

                if (gps_enabled) {
                    if (!mGoogleApiClient.isConnected())
                        mGoogleApiClient.connect();

                    if (mGoogleApiClient.isConnected()) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                                AddCustomer.this);
                    } else {
                        settingRequest();
                    }
                } else {
                    settingRequest();
                }
            }
        }
    }

    ////// CURRENT LOCATION ENDS //////////
    ///////////////////////////////////////

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void showDialog(String title, String content, String positiveText, String negativeText,
                            Boolean isNegative, String methodType) {
        new BottomDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.drawable.logo)
                .setContent(content)
                .setPositiveText(positiveText)
                .setPositiveBackgroundColorResource(R.color.color_secondry)
                .setPositiveTextColorResource(android.R.color.white)
                .setNegativeText(negativeText)
                .onPositive(dialog -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    Log.d("Alert", "Dialog Dismissed");
                }).show();
    }

    private String returnLocation() {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(AddCustomer.this, Locale.getDefault());

        String userLocation = "";

        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);

            if (addresses.size() > 0) {
                if (addresses.get(0).getAddressLine(0) != null) {
                    String postalCode = "";

                    if (addresses.get(0).getPostalCode() != null) {
                        postalCode = " " + addresses.get(0).getPostalCode();
                    }

                    userLocation = addresses.get(0).getAddressLine(0).replace("Pakistan", "")
                            .replace(addresses.get(0).getAdminArea() + postalCode + ", ", "")
                            .replace(", " + addresses.get(0).getSubAdminArea() + ", ", "");
                } else {
                    userLocation = "NOT FOUND";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AddCustomer.this, "WRONG MAP DATA", Toast.LENGTH_LONG).show();
        }

        return userLocation;
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
        registrationDate.setText(sdf.format(myCalendar.getTime()));
    }

    private Boolean checkIfAllSpinnersAreHidden(){
        return gmView.getVisibility() == View.GONE &&
                soView.getVisibility() == View.GONE &&
                sdmView.getVisibility() == View.GONE &&
                dmView.getVisibility() == View.GONE &&
                stView.getVisibility() == View.GONE;
    }

}