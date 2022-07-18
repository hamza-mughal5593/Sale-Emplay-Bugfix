package com.deens.cheese.ui.dailysale;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.DailySaleInvoiceUpdate;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.MappedCustomerView;
import com.deens.cheese.POInvoiceClass;
import com.deens.cheese.POInvoiceUpdate;
import com.deens.cheese.POOrderAdapter;
import com.deens.cheese.POProductDetailClass;
import com.deens.cheese.R;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentDailysaleBinding;
import com.deens.cheese.databinding.FragmentPreinvoiceBinding;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class DailySaleFragment extends Fragment {

    private FragmentDailysaleBinding binding;
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

    public static DailySaleFragment newInstance(String param1) {
        DailySaleFragment fragment = new DailySaleFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDailysaleBinding.inflate(inflater, container, false);
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
            startActivity(new Intent(getContext(), MappedCustomerView.class).putExtra("From", "Generate Daily Sale"));
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

        String methodName = "KeyAccount/GetSaleDetails?UserID=" + userClass.getUserID();

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
                                startActivity(new Intent(getContext(), DailySaleInvoiceUpdate.class)
                                        .putExtra("SelectedInvoice", selectedInvoice)
                                );
                            }
                        });
                    } else {
                        listView.setVisibility(View.GONE);
                        SnackAlert.error(parentView, "No daily sale found");
                    }
                }
            }
        }
    }

    private class DeleteInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "KeyAccount/DeleteSale?";

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

        String methodName = "KeyAccount/PostSale?";

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