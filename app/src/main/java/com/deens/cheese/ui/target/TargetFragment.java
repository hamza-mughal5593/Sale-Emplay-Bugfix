package com.deens.cheese.ui.target;

import static android.content.Context.MODE_PRIVATE;
import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.CustomerClass;
import com.deens.cheese.EmployeeClass;
import com.deens.cheese.EmployeeType;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.TargetList;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentCustomerBinding;
import com.deens.cheese.databinding.FragmentTargetBinding;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;
import java.net.URLEncoder;
import java.util.ArrayList;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class TargetFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentTargetBinding binding;
    RelativeLayout parentView;
    RelativeLayout loadingView;
    ImageView back;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "";
    String loggedInUserId = "0";
    String selectedUserTypeId = "7";
    UserClass userClass;

    ListView listView;
    LinearLayout typeView, customerListView;
    ElasticButton bottomButton;

    // This list has all user types this logged in user can create
    ArrayList<EmployeeType> allowedUserTypes = new ArrayList<>();

    // Spinners used in SO Selection View
    Spinner gmSpinner, stSpinner, sdmSpinner, dmSpinner, soSpinner;
    // Views containing hierarchy spinner
    LinearLayout gmView, stView, dmView, sdmView, soView;
    // All Employees List from server populated once
    ArrayList<EmployeeClass> employees = new ArrayList<>();

    // All Customer List based on SO ID
    ArrayList<CustomerClass> customers = new ArrayList<>();

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

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    public static TargetFragment newInstance(String param1) {
        TargetFragment fragment = new TargetFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTargetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        loadingView = root.findViewById(R.id.loadingView);
        listView = root.findViewById(R.id.listView);
        customerListView = root.findViewById(R.id.customerListView);
        typeView = root.findViewById(R.id.typeView);
        bottomButton = root.findViewById(R.id.bottomButton);
        gmSpinner = root.findViewById(R.id.gmSpinner);
        sdmSpinner = root.findViewById(R.id.sdmSpinner);
        dmSpinner = root.findViewById(R.id.dmSpinner);
        soSpinner = root.findViewById(R.id.soSpinner);
        stSpinner = root.findViewById(R.id.stSpinner);
        gmView = root.findViewById(R.id.gmView);
        sdmView = root.findViewById(R.id.sdmView);
        dmView = root.findViewById(R.id.dmView);
        soView = root.findViewById(R.id.soView);
        stView = root.findViewById(R.id.stView);

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

        bottomButton.setOnClickListener(view -> {
            if (soList.size() <= 0) {
                // If Sale man/Driver cannot add customer we will remove usertype 7 from below
                // Or If anyother User Type cannot add customer we will restrict it here
                if (loggedInUserType.equals("6") || loggedInUserType.equals("7")) {
                    if (soSpinner.getSelectedItemPosition() == -1) {
                        SnackAlert.error(getActivity().findViewById(android.R.id.content),
                                "Sale Officer is not selected!");
                    } else {
                        // Goto Target List
                        gotoTargetList();
                    }
                } else {
                    SnackAlert.error(getActivity().findViewById(android.R.id.content), "Sale Officer is not selected!");
                }
            } else {
                if (soSpinner.getSelectedItemPosition() == -1) {
                    SnackAlert.error(getActivity().findViewById(android.R.id.content),
                            "Sale Officer is not selected!");
                } else {
                    // Goto Target List
                    gotoTargetList();
                }
            }
        });

        if (isNetworkAvailable()) {
            GetAllowedUserTyps task = new GetAllowedUserTyps();
            task.execute();
        } else {
            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
        }

        return root;
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private void gotoTargetList() {
        String selectedSOID = "0";
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
        startActivity(new Intent(getContext(), TargetList.class).putExtra("SO", selectedSOID));

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
            if (getContext() != null) {
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

//                        if (checkIfAllSpinnersAreHidden()) {
//                            SnackAlert.info(parentView, "You cannot change hierarchy");
//                        }
                        // Will Call All employees list here
                        if (isNetworkAvailable()) {
                            GetEmployeesList task = new GetEmployeesList();
                            task.execute();
                        } else {
                            SnackAlert.error(getActivity().findViewById(android.R.id.content), "Internet not available!");
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
                        .url(URL + methodName + "UserTypeID=" + URLEncoder.encode("0", "UTF-8"))
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
            if (getContext() != null) {
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
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

        if (methodName.equals("Customer")) {

            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                if (jsonArray.length() > 0) {
                    if (!jsonArray.getJSONObject(0).getString("CustomerCode").equals("null")) {
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
                                    jsonObject.getString("Mobile1"),
                                    jsonObject.getString("RegistrationDate"),
                                    jsonObject.getBoolean("IsActive"),
                                    jsonObject.getInt("GM"),
                                    jsonObject.getString("GMName"),
                                    jsonObject.getInt("ST"),
                                    jsonObject.getString("STName"),
                                    jsonObject.getInt("DM"),
                                    jsonObject.getString("DMName"),
                                    jsonObject.getInt("SDM"),
                                    jsonObject.getString("SDMName"),
                                    jsonObject.getInt("SO_ID"),
                                    jsonObject.getString("SO_Name"),
                                    jsonObject.getString("Visit_Day"),
                                    jsonObject.getString("CreditLimit"),
                                    jsonObject.getString("PaymentTypeID"),
                                    jsonObject.getString("PaymentTypeName"),
                                    jsonObject.getBoolean("IsApproved"),
                                    jsonObject.getString("ApprovedBy"),
                                    jsonObject.getString("ApprovedOn"),
                                    jsonObject.getString("MapLocation"),
                                    jsonObject.getDouble("Lat"),
                                    jsonObject.getDouble("Long")));
                        }
                    } else {
                        message = "Deen's Cheese";
                        messageDetail = "No customer found!";
                    }
                } else {
                    message = "Deen's Cheese";
                    messageDetail = "No customer found!";
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

        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, names.toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void populateSpinnerWithArray(Spinner spinner, String[] array) {
        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // We will call this method to clear spinner items of subsequent spinners if parent has no value
    private void clearSpinner(Spinner spinner) {
        if (spinner.getAdapter() != null) {
            spinner.setAdapter(null);
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
                            getActivity().onBackPressed();
                            getActivity().overridePendingTransition(R.anim.slide_in_left,
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
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private Boolean checkIfAllSpinnersAreHidden() {
        return gmView.getVisibility() == View.GONE &&
                soView.getVisibility() == View.GONE &&
                sdmView.getVisibility() == View.GONE &&
                dmView.getVisibility() == View.GONE &&
                stView.getVisibility() == View.GONE;
    }

}