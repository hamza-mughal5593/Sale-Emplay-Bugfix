package com.deens.cheese.ui.attendance;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deens.cheese.Adapter.CourseAdapter;
import com.deens.cheese.CustomerClass;
import com.deens.cheese.FileUtils;
import com.deens.cheese.MappedCustomerView;
import com.deens.cheese.R;
import com.deens.cheese.ViewImages;
import com.deens.cheese.databinding.FragmentAttendenceBinding;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.LeaveClass;
import com.deens.cheese.UserClass;
import com.ornach.nobobutton.NoboButton;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

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

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.KeyGenerator;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import me.aflak.libraries.callback.FingerprintDialogCallback;
import me.aflak.libraries.dialog.DialogAnimation;
import me.aflak.libraries.dialog.FingerprintDialog;
import okhttp3.MultipartBody;

public class AttendanceFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int UPLOAD_IMAGE_REQUEST_CODE = 200;
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private FragmentAttendenceBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;
    TextView leaveDate;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;
    ImageView selectImage, selectImageFromGallery;
    NoboButton uploadImage;

    ImageView viewImages;
    ListView listView;
    ElasticButton cancelDialog, markLeave;
    NoboButton bottomButton, markAttendance;
    RelativeLayout leaveView;
    ImageView placeHolder;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    // Current Location Related Variables
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    LocationManager lm;

    String lat = "", lng = "", loc = "";
    Boolean isMarkLeave = false;
    ArrayList<LeaveClass> leaves = new ArrayList<>();
    EditText remarks;
    Spinner reasonsSpinner;
    String selectedLeaveId = "";
    String selectedImageString = "";
    String selectedImageName = "";

    Uri mCurrentPhotoPath;
    File mediaStorageDir;
    File mediaFile;

    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET};

    int PERMISSION_ALL = 1;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    NoboButton cancelPicture, uploadPicture;
    RelativeLayout placeHolderView;
    LinearLayout mediaSelectionView;

    public static AttendanceFragment newInstance(String param1) {
        AttendanceFragment fragment = new AttendanceFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        customerSpinner = root.findViewById(R.id.customerSpinner);
        customerView = root.findViewById(R.id.customerView);
        listmain = root.findViewById(R.id.listmain);
        listrecycler = root.findViewById(R.id.listrecycler);

        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        listView = root.findViewById(R.id.listView);
        bottomButton = root.findViewById(R.id.bottomButton);
        markAttendance = root.findViewById(R.id.markAttendance);
        cancelDialog = root.findViewById(R.id.cancelButton);
        markLeave = root.findViewById(R.id.markLeave);
        leaveView = root.findViewById(R.id.leaveView);
        reasonsSpinner = root.findViewById(R.id.reasonSpinner);
        remarks = root.findViewById(R.id.remarks);
        selectImage = root.findViewById(R.id.selectImage);
        uploadImage = root.findViewById(R.id.uploadImage);
        leaveDate = root.findViewById(R.id.date);
        viewImages = root.findViewById(R.id.viewImage);
        placeHolder = root.findViewById(R.id.placeHolder);
        cancelPicture = root.findViewById(R.id.cancelPicture);
        uploadPicture = root.findViewById(R.id.uploadPicture);
        mediaSelectionView = root.findViewById(R.id.mediaSelectionView);
        placeHolderView = root.findViewById(R.id.placeHolderView);
        selectImageFromGallery = root.findViewById(R.id.selectImageFromGallary);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));
        loadPreferences();

        back = root.findViewById(R.id.back);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        bottomButton.setOnClickListener(view -> {
            isMarkLeave = true;
            // Check for Location Permission
            if (!hasPermissions(getContext(), PERMISSIONS)) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
            } else {
                Boolean gps_enabled = false;
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                    Log.i("EXCEPTION: ", ex.toString());
                }
                if (gps_enabled) {
                    loadingView.setVisibility(View.VISIBLE);
                    initializeLocationRequest();
                } else {
                    SnackAlert.error(parentView, "Please enable GPS");
                }
            }
        });

        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        markAttendance.setOnClickListener(view -> {
            isMarkLeave = false;
            // Check for Location Permission
            if (!hasPermissions(getContext(), PERMISSIONS)) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
            } else {
                Boolean gps_enabled = false;
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                    Log.i("EXCEPTION: ", ex.toString());
                }
                if (gps_enabled) {
                    loadingView.setVisibility(View.VISIBLE);
                    initializeLocationRequest();
                } else {
                    SnackAlert.error(parentView, "Please enable GPS");
                }
            }
        });

        cancelDialog.setOnClickListener(view -> {
            hideSoftKeyboard(getActivity());
            leaveView.setVisibility(View.GONE);
        });

        markLeave.setOnClickListener(view -> {
            if (remarks.getText().toString().trim().equals("")) {
                SnackAlert.error(getActivity().findViewById(android.R.id.content), "Please add remarks!");
            } else {
                if (isNetworkAvailable()) {
                    MarkLeave task = new MarkLeave();
                    task.execute();
                } else {
                    SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                }
            }
        });

        // Date variable initialized
        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        leaveDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        leaveDate.setOnClickListener(view -> {
            new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();

        });

        if (isNetworkAvailable()) {
            GetLeaveType task = new GetLeaveType();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        selectImage.setOnClickListener(view -> {
            placeHolderView.setVisibility(View.VISIBLE);
            mediaSelectionView.setVisibility(View.GONE);
            captureImage();
        });

        uploadImage.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                if (selectedImageString.equals("") || selectedImageName.equals("")) {
                    SnackAlert.error(parentView, "Please pick one image");
                } else {
                    UploadImageTask task = new UploadImageTask();
                    task.execute();
                }
            } else {
                SnackAlert.error(parentView, "INTERNET NOT FOUND");
            }
        });

        selectImageFromGallery.setOnClickListener(view -> {
            placeHolderView.setVisibility(View.VISIBLE);
            mediaSelectionView.setVisibility(View.GONE);
            imageUpload();
        });

        viewImages.setOnClickListener(view -> startActivity(new Intent(getContext(), ViewImages.class).putExtra("ID", String.valueOf(userClass.getUserID()))));

        cancelPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeHolderView.setVisibility(View.GONE);
                selectedImageName = "";
                selectedImageString = "";
                placeHolder.setImageResource(R.drawable.ic_placeholder);
                mediaSelectionView.setVisibility(View.VISIBLE);
            }
        });

        uploadPicture.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                if (selectedImageString.equals("") || selectedImageName.equals("")) {
                    SnackAlert.error(parentView, "Please pick one image");
                } else {
                    UploadImageTask task = new UploadImageTask();
                    task.execute();
                }
            } else {
                SnackAlert.error(parentView, "INTERNET NOT FOUND");
            }
        });
        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetMappedCustomers task = new GetMappedCustomers();
            task.execute();
        } else {
            SnackAlert.error(root.findViewById(android.R.id.content), "Internet not available!");
        }
        return root;
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private void initializeLocationRequest() {
        try {
            if (!lat.equals("")) {
                getLocation();
            } else {
                mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(10000);    // 10 seconds, in milliseconds
                mLocationRequest.setFastestInterval(1000);   // 1 second, in milliseconds
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
        } catch (Exception ex) {
            Log.w("Exception: ", ex.toString());
        }
    }




    ArrayList<CustomerClass> customers = new ArrayList<>();
    String selectedCustomerID = "";
    TextView customerSpinner;
    RelativeLayout customerView;
    RelativeLayout listmain;
    RecyclerView listrecycler;
    CourseAdapter adapter;
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
                parseJSONStringToJSONObject_list(networkResp, "Customer");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!getActivity().isFinishing()) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    Toast.makeText(getActivity(), "Customers not found", Toast.LENGTH_SHORT).show();
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

                    }
                }
            }
        }
    }




    private void setlist(ArrayList<String> names){



        // initializing our variables.

        // calling method to
        // build recycler view.
        buildRecyclerView(names);



        EditText searchView = binding.searchField;

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
            Toast.makeText(getActivity(), "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist);
        }
    }
    private void buildRecyclerView(ArrayList<String> names) {

        // below line we are creating a new array list


        // initializing our adapter class.
        adapter = new CourseAdapter(names, getActivity(), new CourseAdapter.AdapterCallback() {
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
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        listrecycler.setHasFixedSize(true);

        // setting layout manager
        // to our recycler view.
        listrecycler.setLayoutManager(manager);

        // setting adapter to
        // our recycler view.
        listrecycler.setAdapter(adapter);
    }



    private void parseJSONStringToJSONObject_list(String networkResp, String methodName) {
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





    private class MarkAttendance extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/MarkAttendance?";

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
                        .url(URL + methodName + "LoginUserID=" + URLEncoder.encode(loggedInUserId, "UTF-8")
                                + "&Lat=" + lat
                                + "&Long=" + lng
                                + "&Location=" + URLEncoder.encode(loc, "UTF-8")
                                + "&StoreID" + selectedCustomerID)

                        .method("POST", body)
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
                loadingView.setVisibility(View.GONE);
                if (!messageDetail.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class MarkLeave extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/MarkLeave?", currentTime = "";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            currentTime = leaveDate.getText().toString().trim();
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "LoginUserID=" + URLEncoder.encode(loggedInUserId, "UTF-8")
                                + "&LeaveID=" + URLEncoder.encode(selectedLeaveId, "UTF-8")
                                + "&LeaveDate=" + URLEncoder.encode(currentTime, "UTF-8")
                                + "&Remarks=" + URLEncoder.encode(remarks.getText().toString().trim(), "UTF-8")
                                + "&Lat=" + lat
                                + "&Long=" + lng
                                + "&Location=" + URLEncoder.encode(loc, "UTF-8"))
                        .method("POST", body)
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
                loadingView.setVisibility(View.GONE);
                if (!messageDetail.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class UploadImageTask extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/AddNewImage?LoginUserID=" + userClass.getUserID() + "&StoreID" + selectedCustomerID;

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("text/plain");
                okhttp3.RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("", selectedImageName,
                                okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/octet-stream"),
                                        new File(selectedImageString))).build();

                okhttp3.Request request = new okhttp3.Request.Builder().url(URL + methodName)
                        .method("POST", body)
                        .addHeader("Web_API_Key", "8156sdcas1dcc1d8c4894Coiuj784C8941e856")
                        .build();
                okhttp3.Response response = client.newCall(request).execute();

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
                loadingView.setVisibility(View.GONE);
                if (!messageDetail.equals("")) {
                    // Show some message from server
                    if (messageDetail.contains("Save")) {
                        placeHolder.setImageResource(R.drawable.ic_placeholder);
                        placeHolderView.setVisibility(View.GONE);
                        selectedImageName = "";
                        placeHolder.setImageResource(R.drawable.ic_placeholder);
                        selectedImageString = "";
                        mediaSelectionView.setVisibility(View.VISIBLE);

                    }
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class GetLeaveType extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetLeaveType";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
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
                parseJSONStringToJSONObject(networkResp, "Leaves");

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
                    Toast.makeText(getContext(), "Leave types not found", Toast.LENGTH_SHORT).show();
                } else {
                    if (leaves.size() > 0) {
                        selectedLeaveId = String.valueOf(leaves.get(0).getLeaveID().intValue());
                        ArrayList<String> names = new ArrayList<>();
                        for (int i = 0; i < leaves.size(); i++) {
                            names.add(leaves.get(i).getLeaveTitle());
                        }
                        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, names.toArray());
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        reasonsSpinner.setAdapter(adapter);
                        reasonsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedLeaveId = String.valueOf(leaves.get(i).getLeaveID().intValue());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCurrentPhotoPath = FileProvider.getUriForFile(getContext().getApplicationContext(),
                getContext().getApplicationContext().getPackageName() + ".com.deens.cheese.fileprovider",
                getOutputMediaFile(MEDIA_TYPE_IMAGE));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * returning image / video
     */
    private File getOutputMediaFile(int type) {
        // External sdcard location
        mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Deens");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir, timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("Leaves")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    leaves.add(new LeaveClass(jsonObject.getDouble("LeaveID"),
                            jsonObject.getString("LeaveTitle")));
                }
                if (leaves.size() == 0) {
                    message = "Deen's Cheese";
                    messageDetail = "No leave found";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }
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
                        if (messageDetail.contains("Leaved Marked")) {
                            leaveView.setVisibility(View.GONE);
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
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    ///////////////////////////////////////
    ////// CURRENT LOCATION STARTS ////////

    private void previewMedia(String imageURL) {
        placeHolder.setVisibility(View.VISIBLE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 6;
        final Bitmap bitmap = BitmapFactory.decodeFile(imageURL, options);
        placeHolder.setImageBitmap(bitmap);
    }

    private void imageUpload() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(i, UPLOAD_IMAGE_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException ex) {
            SnackAlert.error(parentView, "Please install a file manager");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = null;
                    bitmap = convertImage(mediaFile.toString());
                    if (bitmap != null) {
                        // Create a media file name
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                Locale.getDefault()).format(new Date());
                        saveToInternalStorage(bitmap, "Deens", timeStamp + ".jpg");

                        // /storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20210318-WA0002.jpg
                        selectedImageString = mediaFile.toString();

                        String[] separated = selectedImageString.split("/");
                        // IMG-20210318-WA0002.jpg
                        selectedImageName = separated[separated.length - 1];

                        previewMedia(selectedImageString);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Task capture
                placeHolderView.setVisibility(View.VISIBLE);
                mediaSelectionView.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == UPLOAD_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data.getData() != null) {
                        Uri mImageUri = data.getData();
                        // content://com.android.providers.media.documents/document/image%3A15780

                        String path = FileUtils.getPath(getContext(), mImageUri);
                        if (!path.equals("")) {
                            String[] temp = path.split("\\.");
                            if (path.length() > 0) {
                                if (temp[(temp.length) - 1].equalsIgnoreCase("gif") || temp[(temp.length) - 1].equalsIgnoreCase("jpeg") ||
                                        temp[(temp.length) - 1].equalsIgnoreCase("jfif") || temp[(temp.length) - 1].equalsIgnoreCase("png") ||
                                        temp[(temp.length) - 1].equalsIgnoreCase("jpg")) {
                                    // /storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20210318-WA0002.jpg
                                    selectedImageString = path;

                                    String[] separated = selectedImageString.split("/");
                                    // IMG-20210318-WA0002.jpg
                                    selectedImageName = separated[separated.length - 1];

                                    previewMedia(selectedImageString);
                                } else {
                                    SnackAlert.error(parentView, "SELECT FROM INTERNAL STORAGE");
                                }
                            }
                        } else {
                            SnackAlert.error(parentView, "SELECT FROM INTERNAL STORAGE");
                        }
                    }
                } catch (Exception e) {
                    SnackAlert.error(parentView, "Something went wrong");
                }
            } else {
                SnackAlert.error(parentView, "No image selected");
            }
        } else {
            switch (resultCode) {
                case RESULT_OK:
                    // All required changes were successfully made
                    if (mGoogleApiClient.isConnected()) {
                        getLocation();
                    } else {
                        mGoogleApiClient.connect();
                    }
                    break;
                case RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(getContext(), "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private Bitmap convertImage(String filePath) {
        try {
            Bitmap bmp = BitmapFactory.decodeFile(filePath);    // Create Bitmap object for the original image
            File convertedImage = new File(filePath);
            // Create FileOutputStream object to write data to the converted image file
            FileOutputStream outStream = new FileOutputStream(convertedImage);
            // Keep 100 quality of the original image when converting
            boolean success = bmp.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
            outStream.flush();
            outStream.close();
            return bmp;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String directoryName, String fileName) {
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(directoryName, MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mypath)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
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

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (lat.equals("")) {
            new Handler().postDelayed(this::settingRequest, 4000);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), "Location Service Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), 90000);
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
                        status.startResolutionForResult(getActivity(), 1000);
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

    @SuppressLint("MissingPermission")
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog("DENIED PERMISSIONS", "Give required location permission to app \"Deen's Cheese\" in Settings & comeback", "Open Settings", "Cancel", true, "");
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lng = String.valueOf(mLastLocation.getLongitude());
                loc = returnLocation();

                loadingView.setVisibility(View.GONE);

                // We have received our location
                // Now we will present mark attendance button

                if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) && isBiometryAvailable()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        if (setBIOSetup()) {
                            if (isMarkLeave) {
                                hideSoftKeyboard(getActivity());
                                leaveView.setVisibility(View.VISIBLE);
                            } else {
                                finerPrintLogin();
                            }
                        }
                    } else {
                        if (isMarkLeave) {
                            hideSoftKeyboard(getActivity());
                            leaveView.setVisibility(View.VISIBLE);
                        } else {
                            finerPrintLogin();
                        }
                    }
                }else {
                    // If Bio Metric not available
                    // Direct user to mark attendance
                    if (isMarkLeave) {
                        hideSoftKeyboard(getActivity());
                        leaveView.setVisibility(View.VISIBLE);
                    } else {
                        if (isNetworkAvailable()) {
                            MarkAttendance task = new MarkAttendance();
                            task.execute();
                        } else {
                            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                        }
                    }
                }

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
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, AttendanceFragment.this);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission for Location Granted
                    initializeLocationRequest();
                } else {
                    // Permission denied, Disable the functionality that depends on it.
                    Toast.makeText(getContext(), "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void showDialog(String title, String content, String positiveText, String negativeText,
                            Boolean isNegative, String methodType) {
        new BottomDialog.Builder(getContext())
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
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    Log.d("Alert", "Dialog Dismissed");
                }).show();
    }

    private String returnLocation() {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

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
            Toast.makeText(getContext(), "WRONG MAP DATA", Toast.LENGTH_LONG).show();
        }

        return userLocation;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isBiometryAvailable() {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            return false;
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            return false;
        }

        if (keyGenerator == null || keyStore == null) {
            return false;
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder("dummy_key",
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            return false;
        }
        return true;
    }

    private void finerPrintLogin() {
        FingerprintDialog.initialize(getContext())
                .title("Deen's Cheese")
                .message("Please mark your attendance")
                .enterAnimation(DialogAnimation.Enter.RIGHT)
                .exitAnimation(DialogAnimation.Exit.RIGHT)
                .circleScanningColor(R.color.color_secondry)
                .callback(new FingerprintDialogCallback() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        if (isNetworkAvailable()) {
                            MarkAttendance task = new MarkAttendance();
                            task.execute();
                        } else {
                            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                        }
                    }

                    @Override
                    public void onAuthenticationCancel() {
                    }

                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)

    private boolean setBIOSetup() {
        BiometricManager biometricManager = BiometricManager.from(getActivity());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getContext(), "No biometric hardware installed",
                        Toast.LENGTH_LONG).show();
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getContext(), "Biometric hardware unavailable.",
                        Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getContext(), "No biometrics enrolled", Toast.LENGTH_LONG).show();
                break;
        }
        return false;
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        leaveDate.setText(sdf.format(myCalendar.getTime()));
    }

}