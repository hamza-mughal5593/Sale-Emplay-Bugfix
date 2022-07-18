package com.deens.cheese.ui.employees;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

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

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.EditEmployee;
import com.deens.cheese.EmployeeAdapter;
import com.deens.cheese.EmployeeClass;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.UserClass;
import com.deens.cheese.UserValidate;
import com.deens.cheese.databinding.FragmentEmployeesBinding;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class EmployeeFragment extends Fragment {

    private FragmentEmployeesBinding binding;
    ListView listView;
    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    RelativeLayout parentView;
    RelativeLayout loadingView;
    // Employees List from Server received once
    ArrayList<EmployeeClass> employees = new ArrayList<>();
    ImageButton addEmployee;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    UserClass userClass;

    public static EmployeeFragment newInstance(String param1) {
        EmployeeFragment fragment = new EmployeeFragment();
        return fragment;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEmployeesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        loadPreferences();

        listView = root.findViewById(R.id.listView);
        loadingView = root.findViewById(R.id.loadingView);
        addEmployee = root.findViewById(R.id.addEmployee);
        parentView = root.findViewById(R.id.parentView);

        Animation bounce = AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.bounce);
        addEmployee.startAnimation(bounce);

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        addEmployee.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), UserValidate.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        });

        return root;
    }

    private class GetEmployeesList extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetEmployeeList?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
            employees.clear();
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName + "EmpID=0&UserTypeID=0&UserID=" +
                                URLEncoder.encode(String.valueOf(userClass.getUserID()), "UTF-8"))
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
            if (getContext() != null){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    // Populate our ListView
                    EmployeeAdapter adapter = new EmployeeAdapter(employees, getContext());
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((adapterView, view, i, l) -> {
                        Gson gson = new Gson();
                        String selectedUser = gson.toJson(employees.get(i));
                        startActivity(new Intent(getContext(), EditEmployee.class).putExtra("SelectedUser", selectedUser));
                    });
                }
            }
        }
    }

    private void loadPreferences(){
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

        if (methodName.equals("Employees")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (userClass.getUserTypeID() < jsonObject.getInt("UserTypeID")){
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
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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

    @Override
    public void onResume() {
        super.onResume();

        // Will Call All employees list here
        if (isNetworkAvailable()) {
            GetEmployeesList task = new GetEmployeesList();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }
    }
}