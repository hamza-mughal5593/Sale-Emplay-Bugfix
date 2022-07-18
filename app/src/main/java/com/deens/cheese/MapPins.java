package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class MapPins extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    ImageView back;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    RelativeLayout parentView, loadingView;

    ArrayList<OrderClass> orders = new ArrayList<>();
    ElasticButton deliverButton;
    String message = "", messageDetail = "";
    String selectedInvoiceId = "";
    UserClass userClass;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Current Location Variables
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    LocationManager lm;

    String lat = "", lng = "", loc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_pins);

        String ordersString = getIntent().getExtras().getString("orders");
        Type type = new TypeToken<List<OrderClass>>() {}.getType();
        orders = new Gson().fromJson(ordersString, type);

        back = findViewById(R.id.back);
        parentView = findViewById(R.id.parentView);
        loadingView = findViewById(R.id.loadingView);
        deliverButton = findViewById(R.id.deliverOrder);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapPins.this);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        back.setOnClickListener(view -> {
            MapPins.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        deliverButton.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                DeliverInvoice task = new DeliverInvoice();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        });

        // Current Location Variables Initialization
        lm = (LocationManager) MapPins.this.getSystemService(Context.LOCATION_SERVICE);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(MapPins.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapPins.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapPins.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            if (!Objects.requireNonNull(marker.getTag()).toString().equals("0")){
                deliverButton.setVisibility(View.VISIBLE);
                selectedInvoiceId = marker.getTag().toString();
            }
            return false;
        });

        mMap.setOnMapClickListener(latLng -> deliverButton.setVisibility(View.GONE));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String calculateCartons(ArrayList<ProductDetailClass> list) {
        String cartonDetail = "";
        int cartons;
        int packets;

        for (int i = 0; i < list.size(); i++) {
            try {
                if (list.get(i).getQuantity() > 0) {
                    packets = list.get(i).getQuantity() % 10;
                    cartons = (int) Math.floor(list.get(i).getQuantity() / 10);
                    cartonDetail = cartonDetail + list.get(i).getProductName() + ":" + "\n" + "Cartons | " + cartons + "\n" + "Packets | " + packets + "\n\n";
                }
            } catch (NumberFormatException ex) {
                Log.i("Exception", ex.toString());
            }
        }

        return cartonDetail;
    }

    private class DeliverInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            methodName = "Invoice/DeliverInvoice?InvoiceID=" + selectedInvoiceId
                    + "&LoginUserID=" + userClass.getUserID();
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
            if (!isFinishing()) {
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

        if (methodName.equals("Deliver")) {
            if (networkResp.contains("Submit")) {
                message = "Deen's Cheese";
                messageDetail = "Invoice delivered successfully";
            } else {
                message = "Deen's Cheese";
                messageDetail = networkResp;
            }
        }

    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(MapPins.this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(MapPins.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (messageDetail.contains("successfully")) {
                            MapPins.this.onBackPressed();
                            MapPins.this.overridePendingTransition(R.anim.slide_in_left,
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
                        status.startResolutionForResult(MapPins.this, 1000);
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

                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.clear();

                if (orders.size() > 0) {
                    PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

                    Marker myMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .icon(bitmapDescriptorFromVector(MapPins.this, R.drawable.ic_map_pin))
                            .snippet("Address: " + loc)
                            .title("Your Location"));
                    myMarker.setTag("0");
                    LatLng myPoint = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    options.add(myPoint);

                    for (int i = 0; i < orders.size(); i++) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(orders.get(i).getCusLat()),
                                        Double.parseDouble(orders.get(i).getCusLong())))
                                .icon(bitmapDescriptorFromVector(MapPins.this, R.drawable.ic_pin))
                                .snippet("Address: " + orders.get(i).getCusAddress() + "\n"
                                        + "Supply Detail: " + "\n" + calculateCartons(orders.get(i).getProductsDetails()))
                                .title(orders.get(i).getCusCustomerName()));
                        marker.setTag(String.valueOf(orders.get(i).getINV_InvoiceID()));

                        LatLng point = new LatLng(Double.parseDouble(orders.get(i).getCusLat()),
                                Double.parseDouble(orders.get(i).getCusLong()));

                        options.add(point);
                    }

                    mMap.addPolyline(options);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), 15));
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
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapPins.this);
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
        geocoder = new Geocoder(MapPins.this, Locale.getDefault());

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
            Toast.makeText(MapPins.this, "WRONG MAP DATA", Toast.LENGTH_LONG).show();
        }

        return userLocation;
    }

}