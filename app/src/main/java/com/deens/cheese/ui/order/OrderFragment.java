package com.deens.cheese.ui.order;

import static android.content.Context.MODE_PRIVATE;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.EmployeeClass;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.InvoiceDetail;
import com.deens.cheese.OrderAdapter;
import com.deens.cheese.OrderClass;
import com.deens.cheese.OrdersSO;
import com.deens.cheese.ProductDetailClass;
import com.deens.cheese.R;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentOrderBinding;
import com.paulrybitskyi.valuepicker.ValuePickerView;
import com.paulrybitskyi.valuepicker.model.Item;
import com.paulrybitskyi.valuepicker.model.PickerItem;
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
import java.util.ArrayList;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import info.hoang8f.android.segmented.SegmentedGroup;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageButton addOrder;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    UserClass userClass;

    ListView listView;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    Boolean isFirstTime = true;
    ArrayList<OrderClass> orders = new ArrayList<>();
    ArrayList<OrderClass> selectedOrdersList = new ArrayList<>();
    int selectedOrderIndex = 0;

    Boolean isSaleOfficer = false;

    SegmentedGroup segmentView;
    RadioButton button1, button2, button3;
    String parameter = "";
    Boolean isUnPost = false, isUnAssign = false, isDeliver = false;
    String selectedInvoiceId = "";
    ElasticButton assignPost;

    ElasticButton cancelButton, assignButton;
    // Sale Man List
    ArrayList<EmployeeClass> smList = new ArrayList<>();
    ValuePickerView salemanPicker;
    RelativeLayout salesmanSelectionView;

    public static OrderFragment newInstance(String param1) {
        OrderFragment fragment = new OrderFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentOrderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        listView = root.findViewById(R.id.listView);
        addOrder = root.findViewById(R.id.addOrder);
        segmentView = root.findViewById(R.id.segmentView);
        button1 = root.findViewById(R.id.button1);
        button2 = root.findViewById(R.id.button2);
        button3 = root.findViewById(R.id.button3);
        assignPost = root.findViewById(R.id.assignPost);
        salemanPicker = root.findViewById(R.id.salemanPicker);
        salesmanSelectionView = root.findViewById(R.id.salesmanSelectionView);
        cancelButton = root.findViewById(R.id.cancelButton);
        assignButton = root.findViewById(R.id.assignButton);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();
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

        Animation bounce = AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.bounce);
        addOrder.startAnimation(bounce);

        addOrder.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), OrdersSO.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        if (loggedInUserType.equals("6")) {
            isSaleOfficer = true;
            button1.setText("  Not Posted  ");
            button2.setText("    Posted    ");
            parameter = "&IsAssign=false&Is_Post=false";
            isUnPost = true;
            isUnAssign = false;
            assignPost.setVisibility(View.GONE);
            segmentView.removeViewAt(2);
            segmentView.updateBackground();
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    getContext().getResources().getDimensionPixelSize(R.dimen.tab_height), 1.0f);
            segmentView.findViewById(R.id.button1).setLayoutParams(params);
            segmentView.findViewById(R.id.button2).setLayoutParams(params);
        } else {
            isSaleOfficer = false;
            button1.setText("  Not Assigned  ");
            button2.setText("    Assigned    ");
            parameter = "&IsAssign=false&Is_Post=true";
            isUnPost = false;
            isUnAssign = true;
            assignPost.setVisibility(View.VISIBLE);

            if (isNetworkAvailable()) {
                GetSalesmanList task = new GetSalesmanList();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        }

        if (loggedInUserType.equals("7")) {
            addOrder.setVisibility(View.GONE);
        }

        if (isNetworkAvailable()) {
            GetOrders task = new GetOrders();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet Connectivity Problem!");
        }

        segmentView.setOnCheckedChangeListener((radioGroup, i) -> {
            // Not Assigned, Not Posted
            if (i == R.id.button1) {
                if (isSaleOfficer) {
                    // Sale Officer is logged in
                    // Not Posted
                    parameter = "&IsAssign=false&Is_Post=false";
                    isUnPost = true;
                    isUnAssign = false;
                    assignPost.setVisibility(View.GONE);
                } else {
                    // Other than Sale Officer
                    // Not Assigned
                    parameter = "&IsAssign=false&Is_Post=true";
                    isUnPost = false;
                    isUnAssign = true;
                    assignPost.setVisibility(View.VISIBLE);
                }

                isDeliver = false;

                if (isNetworkAvailable()) {
                    GetOrders task = new GetOrders();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                }

            } else if (i == R.id.button3){
                // Delivered
                parameter = "&IsAssign=true&Is_Post=true&Is_Deliver=true";
                isUnPost = false;
                isUnAssign = false;
                isDeliver = true;
                assignPost.setVisibility(View.GONE);

                if (isNetworkAvailable()) {
                    GetOrders task = new GetOrders();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                }

            } else {
                // Assigned, Posted
                if (isSaleOfficer) {
                    // Sale Officer is logged in
                    // Posted
                    parameter = "&IsAssign=false&Is_Post=true";
                    isUnPost = false;
                    isUnAssign = false;
                    assignPost.setVisibility(View.GONE);
                } else {
                    // Other than Sale Officer
                    // Assigned
                    parameter = "&IsAssign=true&Is_Post=true";
                    isUnPost = false;
                    isUnAssign = false;
                    assignPost.setVisibility(View.GONE);
                }

                isDeliver = false;

                if (isNetworkAvailable()) {
                    GetOrders task = new GetOrders();
                    task.execute();
                } else {
                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                }
            }
        });

        assignPost.setOnClickListener(view -> {
            selectedOrderIndex = 0;
            salesmanSelectionView.setVisibility(View.VISIBLE);
        });

        cancelButton.setOnClickListener(view -> {
            salesmanSelectionView.setVisibility(View.GONE);
        });

        assignButton.setOnClickListener(view -> {
            salesmanSelectionView.setVisibility(View.GONE);
            // Assign Invoices in Loop after selection of Sale Officer
            // 1. Fetch all orders which are selected
            try {
                ArrayList<Integer> orderNumbersList = new ArrayList<>();
                Boolean isSortNumberNotRight = false;

                for (int i = 0; i < listView.getCount(); i++) {
                    View list = getViewByPosition(i, listView);
                    ImageView checkmark = list.findViewById(R.id.checkmark);
                    EditText order = list.findViewById(R.id.order);
                    if (checkmark.getTag().equals("1")) {
                        // It's checked
                        if (order.getText().toString().trim().equals("")) {
                            order.setText("0");
                            isSortNumberNotRight = true;
                            break;
                        }

                        orders.get(i).setOrder(Integer.parseInt(order.getText().toString().trim()));

                        if (!orderNumbersList.contains(Integer.parseInt(order.getText().toString().trim()))) {
                            orderNumbersList.add(Integer.parseInt(order.getText().toString().trim()));
                        } else {
                            isSortNumberNotRight = true;
                            break;
                        }

                        selectedOrdersList.add(orders.get(i));
                    }
                }

                // 2. Upload selected orders to server
                if (selectedOrdersList.size() == 0) {
                    if (isSortNumberNotRight) {
                        SnackAlert.error(parentView, "Order number must be unique and not 0");
                    } else {
                        SnackAlert.error(parentView, "Please select at least one order");
                    }
                } else {
                    if (isSortNumberNotRight) {
                        SnackAlert.error(parentView, "Order number must be unique and not 0");
                    }else {
                        if (isNetworkAvailable()) {
                            AssignPostInvoice task = new AssignPostInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available");
                        }
                    }
                }

            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                SnackAlert.error(parentView, "Order value is not right");
            }
        });

        return root;
    }

    private class GetOrders extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetInvoiceDetails?";

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
                        .url(URL + methodName + "LoginUserID=" + loggedInUserId
                                + parameter)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "orders");

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
                } else {
                    // Populate our ListView
                    if (orders.size() == 0) {
                        listView.setVisibility(View.INVISIBLE);
                        assignPost.setVisibility(View.GONE);
                    } else {
                        listView.setVisibility(View.VISIBLE);
                        OrderAdapter adapter = new OrderAdapter(orders, getContext(), isUnPost, isUnAssign, isDeliver ,getActivity());
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (view.getId() == R.id.delete) {
                                // Delete Un-posted Invoice
                                selectedInvoiceId = String.valueOf(orders.get(i).getINV_InvoiceID());
                                if (isNetworkAvailable()) {
                                    DeleteInvoice task = new DeleteInvoice();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                                }
                            } else if (R.id.finish == view.getId()){
                                // Finish Delivered Invoice
                                selectedInvoiceId = String.valueOf(orders.get(i).getINV_InvoiceID());
                                if (isNetworkAvailable()) {
                                    FinishInvoice task = new FinishInvoice();
                                    task.execute();
                                } else {
                                    SnackAlert.error(parentView, "Internet Connectivity Problem!");
                                }
                            } else {
                                Boolean isAlreadyDone = false;

                                if (button2.isChecked()) {
                                    isAlreadyDone = true;
                                }

                                Gson gson = new Gson();
                                String selectedOrder = gson.toJson(orders.get(i));
                                startActivity(new Intent(getContext(), InvoiceDetail.class)
                                        .putExtra("SelectedOrder", selectedOrder)
                                        .putExtra("Done", isAlreadyDone).putExtra("IsDeliver", false));
                            }
                        });
                    }
                }
            }
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class DeleteInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/DeleteInvoice?";

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
                        .url(URL + methodName + "InvoiceID=" + selectedInvoiceId)
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

    private class FinishInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/FinishInvoice?";

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
                        .url(URL + methodName + "InvoiceID=" + selectedInvoiceId + "&LoginUserID=" + userClass.getUserID())
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

    private class AssignPostInvoice extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/PostInvoice?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            methodName = "Invoice/AssignInvoice?LoginUserID=" + loggedInUserId + "&InvoiceID="
                    + selectedOrdersList.get(selectedOrderIndex).getINV_InvoiceID()
                    + "&SOID=" + selectedOrdersList.get(selectedOrderIndex).getSO_ID()
                    + "&SaleManID=" + smList.get(salemanPicker.getSelectedItem().getId()).getUserID()
                    + "&SortNo=" + selectedOrdersList.get(selectedOrderIndex).getOrder();
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
                if (messageDetail.contains("successfully")) {
                    selectedOrderIndex++;
                    if (selectedOrderIndex < selectedOrdersList.size()) {
                        if (isNetworkAvailable()) {
                            AssignPostInvoice task = new AssignPostInvoice();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available");
                        }
                    } else {
                        // Show some message from server
                        showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }
                } else {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }
            }
        }
    }

    private class GetSalesmanList extends AsyncTask<Void, Void, Void> {

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
                        .url(URL + methodName + "EmpID=0&UserTypeID=7&UserID=" + URLEncoder.encode(loggedInUserId, "UTF-8"))
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Saleman");

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
                } else {
                    ArrayList<Item> salemanPickerItems = new ArrayList<>();

                    for (int i = 0; i < smList.size(); i++) {
                        salemanPickerItems.add(new PickerItem(i, smList.get(i).getEmpName()));
                    }

                    if (salemanPickerItems.size() > 0) {
                        salemanPicker.setItems(salemanPickerItems);
                        salemanPicker.setSelectedItem(salemanPickerItems.get(0));
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("orders")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);

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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Delete")) {
            messageDetail = networkResp;
            message = "Deen's Cheese";
        }

        if (methodName.equals("Post")) {
            if (networkResp.contains("Submit")) {
                message = "Deen's Cheese";
                messageDetail = "All selected orders assigned successfully";
            } else {
                message = "Deen's Cheese";
                messageDetail = "All selected orders assigned successfully";
            }
        }

        if (methodName.equals("Saleman")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    smList.add(new EmployeeClass(jsonObject.getInt("UserID"),
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

                if (smList.size() == 0) {
                    message = "Deen's Cheese";
                    messageDetail = "No Salesman/driver under you found";
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
                                GetOrders task = new GetOrders();
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
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isFirstTime) {
            if (isNetworkAvailable()) {
                GetOrders task = new GetOrders();
                task.execute();
            } else {
                SnackAlert.error(parentView, "Internet Connectivity Problem!");
            }
        } else {
            isFirstTime = false;
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition
                + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // TODO Re Create All Docs File
}