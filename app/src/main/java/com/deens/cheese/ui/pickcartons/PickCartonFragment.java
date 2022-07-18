package com.deens.cheese.ui.pickcartons;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.CartonAdapter;
import com.deens.cheese.CartonClass;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.OrderClass;
import com.deens.cheese.ProductDetailClass;
import com.deens.cheese.R;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentCustomerBinding;
import com.deens.cheese.databinding.FragmentPickorderBinding;
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

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class PickCartonFragment extends Fragment {

    private FragmentPickorderBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;

    ListView listView;

    // Cartons List
    ArrayList<CartonClass> cartonsList = new ArrayList<>();
    ArrayList<Integer> productIds = new ArrayList<>();

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    Boolean isFirstTime = true;

    // Selected ProductID & Picked Quantity to be submitted
    String selectedProductID = "";
    String selectedPickedQuantityCtn = "";
    String selectedPickedQuantityPkt = "";
    String selectedQuantityCtn = "";
    String selectedQuantityPkt = "";
    String selectedProductName = "";
    NoboButton submit;
    int indexCount;

    ArrayList<OrderClass> orders = new ArrayList<>();

    public static PickCartonFragment newInstance(String param1) {
        PickCartonFragment fragment = new PickCartonFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPickorderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        listView = root.findViewById(R.id.listView);
        submit = root.findViewById(R.id.submit);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserId = String.valueOf(preferences.getInt("UserID", 0));

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        loadPreferences();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetCartonsList task = new GetCartonsList();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        submit.setOnClickListener(view -> {
            if (cartonsList.size() > 0){
                indexCount = 0;
                selectedQuantityCtn = String.valueOf(cartonsList.get(indexCount).getQuantityCtn());
                selectedQuantityPkt = String.valueOf(cartonsList.get(indexCount).getQuantityPkt());
                selectedPickedQuantityCtn = String.valueOf(cartonsList.get(indexCount).getPickedQuantityCtn()/10);
                selectedPickedQuantityPkt = String.valueOf(cartonsList.get(indexCount).getPickedQuantityPkt());
                selectedProductID = String.valueOf(cartonsList.get(indexCount).getProductID());
                selectedProductName = String.valueOf(cartonsList.get(indexCount).getProductName());

                if (isNetworkAvailable()) {
                    SubmitQuantity task = new SubmitQuantity();
                    task.execute();
                } else {
                    SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                }
            } else {
                SnackAlert.error(getActivity().findViewById(android.R.id.content), "No assigned stock");
            }
        });

        return root;
    }
    
    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private class GetCartonsList extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetInvoiceDetails?LoginUserID=" + userClass.getUserID() + "&IsAssign=true&Is_Post=true";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
            orders.clear();
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
                parseJSONStringToJSONObject(networkResp, "Cartons");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (getContext() != null){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (orders.size() > 0) {
                        listView.setVisibility(View.VISIBLE);
                        // Calculate each product cartons and packets
                        cartonsList.clear();
                        productIds.clear();

                        for (int i = 0 ; i < orders.size() ; i++){
                            for (int j = 0 ; j < orders.get(i).getProductsDetails().size() ; j++){
                                if (!productIds.contains(orders.get(i).getProductsDetails().get(j).getProductID())){
                                    // Add New Product to cartons list
                                    int packets = orders.get(i).getProductsDetails().get(j).getQuantity() % 10;
                                    int cartons = (int)Math.floor(orders.get(i).getProductsDetails().get(j).getQuantity() / 10);
                                    cartonsList.add(new CartonClass(
                                            orders.get(i).getProductsDetails().get(j).getProductID(),
                                            orders.get(i).getProductsDetails().get(j).getProductName(),
                                            cartons, packets
                                    ));
                                    productIds.add(orders.get(i).getProductsDetails().get(j).getProductID());
                                }else {
                                    // Product is already added to cartons list. Add quantity.
                                    int index = productIds.indexOf(orders.get(i).getProductsDetails().get(j).getProductID());
                                    int oldPacketQuantity = cartonsList.get(index).getQuantityPkt();
                                    int oldCartonQuantity = cartonsList.get(index).getQuantityCtn();
                                    int packets = orders.get(i).getProductsDetails().get(j).getQuantity() % 10;
                                    int cartons = (int)Math.floor(orders.get(i).getProductsDetails().get(j).getQuantity() / 10);
                                    cartonsList.get(index).setQuantityCtn(oldCartonQuantity + cartons);
                                    cartonsList.get(index).setQuantityPkt(oldPacketQuantity + packets);
                                }
                            }
                        }

                        CartonAdapter adapter = new CartonAdapter(cartonsList, getContext());
                        listView.setAdapter(adapter);

                    } else {
                        listView.setVisibility(View.GONE);
                        showDialog("Deen's Cheese", "No cartons assigned to you!", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class SubmitQuantity extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/SubmitLoadedCarton?";

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
                                + "SaleManID=" + URLEncoder.encode(loggedInUserId, "UTF-8")
                                + "&ProductID=" + URLEncoder.encode(selectedProductID, "UTF-8")
                                + "&QuantityCtn=" + URLEncoder.encode(selectedQuantityCtn, "UTF-8")
                                + "&QuantityPkt=" + URLEncoder.encode(selectedQuantityPkt, "UTF-8")
                                + "&PickQuantityCtn=" + URLEncoder.encode(selectedPickedQuantityCtn, "UTF-8")
                                + "&PickQuantityPkt=" + URLEncoder.encode(selectedPickedQuantityPkt, "UTF-8")
                        )
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Submit");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (getContext() != null){
                loadingView.setVisibility(View.GONE);
                if (!messageDetail.contains("submitted")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    indexCount++;

                    if (indexCount < cartonsList.size()){

                        selectedQuantityCtn = String.valueOf(cartonsList.get(indexCount).getQuantityCtn());
                        selectedQuantityPkt = String.valueOf(cartonsList.get(indexCount).getQuantityPkt());
                        selectedPickedQuantityCtn = String.valueOf(cartonsList.get(indexCount).getPickedQuantityCtn());
                        selectedPickedQuantityPkt = String.valueOf(cartonsList.get(indexCount).getPickedQuantityPkt());
                        selectedProductID = String.valueOf(cartonsList.get(indexCount).getProductID());
                        selectedProductName = String.valueOf(cartonsList.get(indexCount).getProductName());

                        if (isNetworkAvailable()) {
                            SubmitQuantity task = new SubmitQuantity();
                            task.execute();
                        } else {
                            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
                        }
                    }else {
                        // Show some message from server
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Cartons")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                orders.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    JSONArray jsonArray1 = jsonObject.getJSONArray("ProducDetails");

                    ArrayList<ProductDetailClass> products = new ArrayList<>();

                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject1 = jsonArray1.getJSONObject(j);

                        products.add(new ProductDetailClass(jsonObject1.getInt("Id"),
                                jsonObject1.getInt("ProductID"),
                                jsonObject1.getString("ProductName"),
                                jsonObject1.getInt("SalePrice"),
                                jsonObject1.getInt("Quantity"),
                                jsonObject1.getInt("TradeOffer"),
                                jsonObject1.getInt("Offer_Amt"),
                                jsonObject1.getInt("TotalAmount"),
                                jsonObject1.getInt("DiscountAmount"),
                                jsonObject1.getInt("TotalDiscount")));
                    }

                    if (jsonObject.getInt("INV_InvoiceID") != 0) {
                        orders.add(new OrderClass(jsonObject.getInt("INV_InvoiceID"),
                                jsonObject.getString("INV_InvoiceNo"),
                                jsonObject.getString("INV_InvoiceDate"),
                                jsonObject.getInt("INV_SubTotal"),
                                Integer.parseInt(String.valueOf(jsonObject.getDouble("INV_TradeOfferAmount")).replace(".0", "")),
                                jsonObject.getInt("INV_DiscountAmount"),
                                jsonObject.getInt("INV_GrandTotal"),
                                jsonObject.getInt("INV_PaymentReceived"),
                                jsonObject.getInt("INV_PaymentDue"),
                                jsonObject.getString("INV_MapLocation"),
                                jsonObject.getString("INV_Lat"),
                                jsonObject.getString("INV_Long"),
                                jsonObject.getInt("CusCustomerID"),
                                jsonObject.getString("CusCustomerCode"),
                                jsonObject.getString("CusCustomerName"),
                                jsonObject.getInt("CusCityID"),
                                jsonObject.getString("CusCityName"),
                                jsonObject.getString("CusArea"),
                                jsonObject.getString("CusAddress"),
                                jsonObject.getString("CusMobile"),
                                jsonObject.getString("CusMobile1"),
                                jsonObject.getString("CusRegistrationDate"),
                                jsonObject.getBoolean("CusIsActive"),
                                jsonObject.getString("CusVisit_Day"),
                                jsonObject.getString("CusCreditLimit"),
                                jsonObject.getString("CusPaymentTypeName"),
                                jsonObject.getBoolean("CusIsApproved"),
                                jsonObject.getString("CusApprovedOn"),
                                jsonObject.getString("CusMapLocation"),
                                jsonObject.getString("CusLat"),
                                jsonObject.getString("CusLong"),
                                jsonObject.getInt("SO_GM"),
                                jsonObject.getString("SO_GMName"),
                                jsonObject.getInt("SO_ST"),
                                jsonObject.getString("SO_STName"),
                                jsonObject.getInt("SO_DM"),
                                jsonObject.getString("SO_DMName"),
                                jsonObject.getInt("SO_SDM"),
                                jsonObject.getString("SO_SDMName"),
                                jsonObject.getInt("SO_ID"),
                                jsonObject.getString("SO_Name"),
                                jsonObject.getDouble("INV_TaxRate"),
                                jsonObject.getDouble("INV_TaxAmount"),
                                products));
                    }
                }

                if (orders.size() == 0){
                    message = "Deen's Cheese";
                    messageDetail = "No cartons assigned to you";
                }

            } catch (JSONException e) {
                e.printStackTrace();
                message = "Deen's Cheese";
                messageDetail = "Some error occurred";
            }
        }

        if (methodName.equals("Submit")) {
            if (networkResp.contains("Submit")) {
                message = "Deen's Cheese";
                messageDetail = "Picked stock submitted";
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
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFirstTime){
        }else {
            isFirstTime = false;
        }
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(getActivity());
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

}