package com.deens.cheese.ui.stock;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.StockItem;
import com.deens.cheese.StockProductAdapter;
import com.deens.cheese.UpdateStock;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentStockBinding;
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
import java.util.Locale;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class StockFragment extends Fragment {

    private FragmentStockBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    UserClass userClass;
    TextView selectedDateField;

    ListView listView;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    ArrayList<StockItem> products = new ArrayList<>();
    Boolean isFirstTime = true;
    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    ElasticButton addStock;

    // Index count for products list items
    int indexCount = 0;

    ImageButton updateStock;

    public static StockFragment newInstance(String param1) {
        StockFragment fragment = new StockFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStockBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        listView = root.findViewById(R.id.productList);
        addStock = root.findViewById(R.id.addStock);
        updateStock = root.findViewById(R.id.updateStock);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();

        loadPreferences();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        Animation bounce = AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.bounce);
        updateStock.startAnimation(bounce);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        if (isNetworkAvailable()) {
            GetProducts task = new GetProducts();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        addStock.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                // Check if all necessary input present
                // Update Quantity, CostPrice, StockDate in products array entered by user
                try {
                    for (int i = 0; i < listView.getCount(); i++) {
                        View list = getViewByPosition(i, listView);
                        EditText quantity = list.findViewById(R.id.quantity);
                        EditText costPrice = list.findViewById(R.id.costPrice);
                        TextView stockDate = list.findViewById(R.id.stockDate);
                        EditText salePrice = list.findViewById(R.id.salePrice);
                        if (costPrice.getText().toString().trim().equals("")){
                            costPrice.setText("0");
                        }
                        if (quantity.getText().toString().trim().equals("")){
                            quantity.setText("0");
                        }
                        if (salePrice.getText().toString().trim().equals("")){
                            salePrice.setText("0");
                        }
                        products.get(i).setStockDate(stockDate.getText().toString().trim());
                        products.get(i).setQuantity(Integer.parseInt(quantity.getText().toString().trim()));
                        products.get(i).setCostPrice(Integer.parseInt(costPrice.getText().toString().trim()));
                        products.get(i).setProductSalaPrice(Double.parseDouble(salePrice.getText().toString().trim()));
                    }

                    indexCount = 0;

                    boolean isValidValue = false;
                    for (int i = 0 ; i < products.size() ; i++){
                        if (products.get(i).getQuantity() == 0){
                            indexCount++;
                        }else {
                            isValidValue = true;
                            break;
                        }
                    }

                    if (isValidValue){
                        AddStockProduct task = new AddStockProduct();
                        task.execute();
                    }else {
                        SnackAlert.error(parentView, "Please insert quantity & cost price");
                    }

                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    SnackAlert.error(getActivity().findViewById(android.R.id.content), "Please enter all values");
                }
            } else {
                SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
            }
        });

        updateStock.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), UpdateStock.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        return root;
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

    private class GetProducts extends AsyncTask<Void, Void, Void> {

        String methodName = "Invoice/GetProductList";

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
                parseJSONStringToJSONObject(networkResp, "Products");

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
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (products.size() > 0) {
                        StockProductAdapter adapter = new StockProductAdapter(products, getContext(), false);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (view.getId() == R.id.stockDate) {
                                View listItem = listView.getChildAt(i);
                                TextView stockDate = listItem.findViewById(R.id.stockDate);
                                selectedDateField = stockDate;
                                // Date variable initialized
                                date = (view1, year, monthOfYear, dayOfMonth) -> {
                                    myCalendar.set(Calendar.YEAR, year);
                                    myCalendar.set(Calendar.MONTH, monthOfYear);
                                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    updateLabel();
                                };

                                new DatePickerDialog(getContext(), date, myCalendar
                                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                            }
                        });
                    } else {
                        showDialog("Deen's Cheese", "No product found", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class AddStockProduct extends AsyncTask<Void, Void, Void> {

        String methodName = "Stock/AddNewStock?";

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
                                    + "ProductId=" + URLEncoder.encode(String.valueOf(products.get(indexCount).getProductID()), "UTF-8")
                                    + "&ProductCost=" + URLEncoder.encode(String.valueOf(products.get(indexCount).getCostPrice()), "UTF-8")
                                    + "&SalePrice=" + URLEncoder.encode(String.valueOf(products.get(indexCount).getProductSalaPrice().intValue()), "UTF-8")
                                    + "&Quantity=" + URLEncoder.encode(String.valueOf(products.get(indexCount).getQuantity()), "UTF-8")
                                    + "&StockDate=" + URLEncoder.encode(String.valueOf(products.get(indexCount).getStockDate()), "UTF-8")
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
            if (getContext() != null) {
                loadingView.setVisibility(View.GONE);

                if (messageDetail.contains("Added")) {
                    // count track of products numbers incremented
                    indexCount++;
                    if (indexCount < products.size()) {
                        boolean isValidValue = false;
                        for (int i = indexCount ; i < products.size(); i++){
                            if (products.get(i).getQuantity() == 0){
                                indexCount++;
                            }else {
                                isValidValue = true;
                                break;
                            }
                        }

                        if (isValidValue){
                            AddStockProduct task = new AddStockProduct();
                            task.execute();
                        }else {
                            showDialog("Deen's Cheese", "Stock saved successfully", "OK", "", R.drawable.ic_tick,
                                    R.drawable.ic_cancel, false);
                        }
                    } else {
                        showDialog("Deen's Cheese", "Stock saved successfully", "OK", "", R.drawable.ic_tick,
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

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Products")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("ProductID").equals("0")) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            products.add(new StockItem(jsonObject.getInt("ProductID"),
                                    jsonObject.getString("CompanyName"),
                                    jsonObject.getString("ProductCode"),
                                    jsonObject.getString("ProductName"),
                                    jsonObject.getDouble("ProductSalaPrice"),
                                    jsonObject.getString("ProductType"),
                                    jsonObject.getString("ProductUnit"),
                                    jsonObject.getDouble("TradeOffer")));
                        }
                    } else {
                        message = "Deen's Cheese";
                        messageDetail = "No prodcut found!";
                    }
                } else {
                    message = "Deen's Cheese";
                    messageDetail = "No prodcut found!";
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
                        if (message.contains("saved")) {
                            // Do something refresh view
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
            // Do something - refresh view when coming from update stock
        } else {
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

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectedDateField.setText(sdf.format(myCalendar.getTime()));
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

}