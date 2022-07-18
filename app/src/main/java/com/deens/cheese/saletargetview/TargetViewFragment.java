package com.deens.cheese.saletargetview;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.CustomerClass;
import com.deens.cheese.EmployeeClass;
import com.deens.cheese.EmployeeType;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.SaleTargetClass;
import com.deens.cheese.SaleTargetView;
import com.deens.cheese.TargetList;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentTargetBinding;
import com.deens.cheese.databinding.FragmentTargetViewBinding;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class TargetViewFragment extends Fragment {

    private FragmentTargetViewBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    String userid = "";
    SaleTargetClass detail;

    TextView month, fromDate, toDate, totalTarget, totalDays, achieveTarget,
            achieveDays, remainingTarget, remainingDays, pendingTarget, currentRatio,
            requiredRatio, remarks;

    UserClass userClass;
    // Shared Preference
    SharedPreferences preferences;

    public static TargetViewFragment newInstance(String param1) {
        TargetViewFragment fragment = new TargetViewFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTargetViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);

        month = root.findViewById(R.id.month);
        fromDate = root.findViewById(R.id.fromDate);
        toDate = root.findViewById(R.id.toDate);
        totalDays = root.findViewById(R.id.totalDays);
        achieveDays = root.findViewById(R.id.achieveDays);
        totalTarget = root.findViewById(R.id.totalTarget);
        achieveTarget = root.findViewById(R.id.achieveTarget);
        remainingTarget = root.findViewById(R.id.remainingTarget);
        remainingDays = root.findViewById(R.id.remainingDays);
        pendingTarget = root.findViewById(R.id.pendingTarget);
        currentRatio = root.findViewById(R.id.currentRatio);
        requiredRatio = root.findViewById(R.id.requiredRatio);
        remarks = root.findViewById(R.id.remarks);

        loadPreferences();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        if (isNetworkAvailable()){
            GetSaleTargetDetail task = new GetSaleTargetDetail();
            task.execute();
        }else {
            SnackAlert.error(parentView, "Internet not available");
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private class GetSaleTargetDetail extends AsyncTask<Void, Void, Void> {

        String methodName = "Dashboard/GetSaleTargetDetails?";

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
                        .url(URL + methodName
                                + "UserID=" + userid)
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Detail");

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
                loadingView.setVisibility(View.INVISIBLE);
                if (!message.equals("")){
                    loadingView.setVisibility(View.GONE);
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                }else {
                    if (detail.getResult().equals("null")){
                        // Show some message from server
                        showDialog("Deen's Cheese", "No detail received", "OK", "", R.drawable.ic_tick,
                                R.drawable.ic_cancel, false);
                    }else {
                        month.setText(detail.getMonthName());
                        fromDate.setText(detail.getDateFrom());
                        toDate.setText(detail.getDateTo());
                        totalTarget.setText(detail.getSaleTarget());
                        totalDays.setText(detail.getTotalDays().replace(" Days", ""));
                        achieveTarget.setText(detail.getAchieveTarget());
                        achieveDays.setText(detail.getPassedDays().replace(" Days", ""));
                        remainingTarget.setText(detail.getRemainingTarget());
                        remainingDays.setText(detail.getRemainingDays().replace(" Days", ""));
                        pendingTarget.setText(detail.getRemainingTarget());
                        currentRatio.setText(detail.getCurrentTargetRatio());
                        requiredRatio.setText(detail.getRequiredTargetPerday());
                        remarks.setText(detail.getResult());

                        if (detail.getResultColour().toUpperCase().equals("RED")){
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_red));
                        }else if (detail.getResultColour().toUpperCase().equals("GREEN")){
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_green));
                        } else {
                            requiredRatio.setBackground(getResources().getDrawable(R.drawable.carton_bg_yellow));
                        }
                    }
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {

        if (methodName.equals("Detail")) {
            try {
                JSONObject jsonObject = new JSONObject(networkResp);
                detail = new SaleTargetClass(jsonObject.getString("Result"),
                        jsonObject.getString("ResultColour"),
                        jsonObject.getString("DateFrom"),
                        jsonObject.getString("DateTo"),
                        jsonObject.getString("MonthName"),
                        jsonObject.getString("TotalDays"),
                        jsonObject.getString("PassedDays"),
                        jsonObject.getString("RemainingDays"),
                        jsonObject.getString("SaleTarget"),
                        jsonObject.getString("AchieveTarget"),
                        jsonObject.getString("RemainingTarget"),
                        jsonObject.getString("PerDayTarget"),
                        jsonObject.getString("CurrentTargetRatio"),
                        jsonObject.getString("RequiredTargetPerday"));
            } catch (JSONException e) {
                e.printStackTrace();
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
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void loadPreferences(){
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
        userid = String.valueOf(userClass.getUserID());
    }
}