package com.deens.cheese.ui.preinvoice;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import androidx.fragment.app.Fragment;

import com.deens.cheese.CustomerClass;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.LeaveClass;
import com.deens.cheese.MappedCustomerView;
import com.deens.cheese.OrderAdapter;
import com.deens.cheese.OrderClass;
import com.deens.cheese.POInvoiceClass;
import com.deens.cheese.POInvoiceUpdate;
import com.deens.cheese.POOrderAdapter;
import com.deens.cheese.POProductDetailClass;
import com.deens.cheese.ProductDetailClass;
import com.deens.cheese.R;
import com.deens.cheese.SOInvoiceDetailAdapter;
import com.deens.cheese.SOInvoiceUpdate;
import com.deens.cheese.SOProductAdapter;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentPreinvoiceBinding;
import com.deens.cheese.ui.order.OrderFragment;
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
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.ornach.nobobutton.NoboButton;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.KeyGenerator;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import me.aflak.libraries.callback.FingerprintDialogCallback;
import me.aflak.libraries.dialog.DialogAnimation;
import me.aflak.libraries.dialog.FingerprintDialog;

public class PreInvoiceFragment extends Fragment {

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
    ImageButton addPO;
    ListView listView;
    ArrayList<POInvoiceClass> poInvoices = new ArrayList<>();
    String selectedPOID = "";
    Boolean isFirstTime = true;

    public static PreInvoiceFragment newInstance(String param1) {
        PreInvoiceFragment fragment = new PreInvoiceFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreinvoiceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        addPO = root.findViewById(R.id.addPO);
        listView = root.findViewById(R.id.listView);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        Animation bounce = AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.bounce);
        addPO.startAnimation(bounce);

        loadPreferences();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        addPO.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), MappedCustomerView.class).putExtra("From", "Generate Purchase Order"));
        });

        if (isNetworkAvailable()) {
            GETPOInvoices task = new GETPOInvoices();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        return root;
    }

    private class GETPOInvoices extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/GetPODetails?UserID=" + userClass.getUserID();

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            poInvoices.clear();
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
                parseJSONStringToJSONObject(networkResp, "POs");

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
                if (!message.equals("")) {
                    // Show some message from server
                    listView.setVisibility(View.GONE);
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (poInvoices.size() > 0) {
                        listView.setVisibility(View.VISIBLE);

                        POOrderAdapter productsAdapter = new POOrderAdapter(poInvoices, getContext());
                        listView.setAdapter(productsAdapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            selectedPOID = String.valueOf(poInvoices.get(i).getPOID());
                            if (view.getId() == R.id.delete) {
                                if (isNetworkAvailable()) {
                                    DeleteInvoice task = new DeleteInvoice();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet not available!");
                                }
                            } else if (view.getId() == R.id.post) {
                                if (isNetworkAvailable()) {
                                    PostInvoice task = new PostInvoice();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet not available!");
                                }
                            } else {
                                Gson gson = new Gson();
                                String selectedInvoice = gson.toJson(poInvoices.get(i));
                                startActivity(new Intent(getContext(), POInvoiceUpdate.class)
                                        .putExtra("SelectedInvoice", selectedInvoice)
                                );
                            }
                        });
                    } else {
                        listView.setVisibility(View.GONE);
                        SnackAlert.error(parentView, "No Purchase Order found");
                    }
                }
            }
        }
    }

    private class DeleteInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/DeletePO?";

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
                                + "POID=" + selectedPOID
                                + "&UserID=" + userClass.getUserID()
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
            if (getContext() != null) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class PostInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/PostPO?";

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
                                + "POID=" + selectedPOID
                                + "&UserID=" + userClass.getUserID()
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
            if (getContext() != null) {
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("POs")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (jsonObject.getInt("POID") != 0) {

                        JSONArray jsonArray1 = jsonObject.getJSONArray("ProducDetails");

                        ArrayList<POProductDetailClass> products = new ArrayList<>();

                        for (int j = 0; j < jsonArray1.length(); j++) {
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(j);

                            products.add(new POProductDetailClass(jsonObject1.getInt("ID"),
                                    jsonObject1.getInt("POID"),
                                    jsonObject1.getInt("ProductID"),
                                    jsonObject1.getString("ProductName"),
                                    (int) (jsonObject1.getDouble("Quantity")),
                                    (int) (jsonObject1.getDouble("Total")),
                                    (int) (jsonObject1.getDouble("SalePrice"))
                            ));
                        }

                        poInvoices.add(new POInvoiceClass(jsonObject.getInt("POID"),
                                jsonObject.getString("PO_Number"),
                                jsonObject.getString("PODate"),
                                jsonObject.getString("Remarks"),
                                Integer.parseInt(String.valueOf(Math.round(jsonObject.getDouble("SubTotal_Amt")))),
                                Integer.parseInt(String.valueOf(Math.round(jsonObject.getDouble("TotalTax")))),
                                Integer.parseInt(String.valueOf(Math.round(jsonObject.getDouble("TotalAmt")))),
                                jsonObject.getInt("CusCustomerID"),
                                jsonObject.getString("CusCustomerCode"),
                                jsonObject.getString("CusCustomerName"),
                                jsonObject.getInt("CusCityID"),
                                jsonObject.getString("CusCityName"),
                                jsonObject.getString("CusArea"),
                                jsonObject.getString("CusAddress"),
                                jsonObject.getString("CusMobile"),
                                jsonObject.getString("CusMobile1"),
                                products));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                messageDetail = "No Purchase Order found";
                message = "Deen's Cheese";
            }
        }

        if (methodName.equals("Delete")) {
            messageDetail = networkResp;
            message = "Deen's Cheese";
        }

        if (methodName.equals("Post")) {
            messageDetail = networkResp;
            message = "Deen's Cheese";
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
                        if (messageDetail.contains("successfully") || messageDetail.contains("Submit")) {
                            if (isNetworkAvailable()) {
                                GETPOInvoices task = new GETPOInvoices();
                                task.execute();
                            } else {
                                SnackAlert.error(parentView, "Internet Connectivity Problem!");
                            }
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

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstTime) {
            isFirstTime = false;
        } else {
            if (isNetworkAvailable()) {
                GETPOInvoices task = new GETPOInvoices();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet not available!");
            }
        }
    }
}