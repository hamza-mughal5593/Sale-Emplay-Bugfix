package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
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
import java.util.Objects;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class EditEmployee extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EmployeeClass selectedEmployee;

    RelativeLayout parentView;
    RelativeLayout loadingView;
    Button forward, previous;
    ImageView back;
    ElasticButton editEmployee;
    RadioButton info, hierarchy;
    int stepperSelectedPosition = 0;
    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    // Hierarchy View Containing Hierarchy Spinner
    LinearLayout hierarchyView;
    // Info View Containing Info Spinners
    LinearLayout infoView;


    // Spinner containing all allowed user types this user can create
    Spinner userType;


    // Spinners used in hierarchy view
    Spinner gmSpinner, stSpinner, sdmSpinner, dmSpinner, soSpinner;


    // Views containing hierarchy spinner
    LinearLayout gmView, stView, dmView, sdmView, soView;


    // This list has all user types this logged in user can create
    ArrayList<EmployeeType> allowedUserTypes = new ArrayList<>();


    // Selected New User Type ID
    String selectedUserTypeId = "";

    // All Employees List from server populated once
    ArrayList<EmployeeClass> employees = new ArrayList<>();

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

    // Sale Man Driver List populated in result of selection made on SO Spinner
    ArrayList<EmployeeClass> smList = new ArrayList<>();


    // Info View All EditTexts, TextViews, Spinners, Date Pickers
    String username, password;
    EditText employeeName, employeeFatherName, employeeAddress, remarks, passwordTxt;
    MaskedEditText cnic, contactNo, contactNo2;
    TextView dob, doj;
    Spinner gender, city, branch, department, designation, status;


    // Miscellaneous Data lists from Server
    ArrayList<CityClass> cities = new ArrayList<>();
    ArrayList<DepartmentClass> departments = new ArrayList<>();
    ArrayList<BranchesClass> branches = new ArrayList<>();
    ArrayList<DesignationClass> designations = new ArrayList<>();
    ArrayList<StatusClass> statuses = new ArrayList<>();


    // Miscellaneous Methods used to get different data from server
    String[] miscMethodNames = {"GetCitiesList", "GetBranchList", "GetDepartmentList",
            "GetDesignationList", "GetEmpStatusList"};
    String[] methodType = {"City", "Branch", "Department",
            "Designation", "Status"};
    int methodCount = 0;


    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    // Selected Text Field for date selection
    TextView selectedDateField;


    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String loggedInUserType = "", loggedInUserID = "";
    UserClass userClass;

    String[] genders = {"Male", "Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_edit_employee);

        preferences = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(GlobalVariable.PREF_NAME, MODE_PRIVATE).edit();
        loggedInUserType = String.valueOf(preferences.getInt("UserType", 0));
        loggedInUserID = String.valueOf(preferences.getInt("UserID", 0));

        loadPreferences();

        parentView = findViewById(R.id.parent);
        loadingView = findViewById(R.id.loadingView);
        back = findViewById(R.id.back);
        passwordTxt = findViewById(R.id.password);
        userType = findViewById(R.id.typeSpinner);
        forward = findViewById(R.id.forward);
        previous = findViewById(R.id.previous);
        info = findViewById(R.id.info);
        hierarchy = findViewById(R.id.heirarchy);
        hierarchyView = findViewById(R.id.hierarchyView);
        infoView = findViewById(R.id.infoView);
        gmSpinner = findViewById(R.id.gmSpinner);
        sdmSpinner = findViewById(R.id.sdmSpinner);
        dmSpinner = findViewById(R.id.dmSpinner);
        soSpinner = findViewById(R.id.soSpinner);
        stSpinner = findViewById(R.id.stSpinner);
        gmView = findViewById(R.id.gmView);
        sdmView = findViewById(R.id.sdmView);
        dmView = findViewById(R.id.dmView);
        soView = findViewById(R.id.soView);
        stView = findViewById(R.id.stView);
        employeeName = findViewById(R.id.employeeName);
        employeeFatherName = findViewById(R.id.fatherName);
        employeeAddress = findViewById(R.id.employeeAddress);
        remarks = findViewById(R.id.remarks);
        cnic = findViewById(R.id.cnic);
        contactNo = findViewById(R.id.contactNo);
        contactNo2 = findViewById(R.id.contactNo2);
        dob = findViewById(R.id.dob);
        doj = findViewById(R.id.doj);
        gender = findViewById(R.id.genderSpinner);
        city = findViewById(R.id.citySpinner);
        branch = findViewById(R.id.branchSpinner);
        department = findViewById(R.id.departmentSpinner);
        designation = findViewById(R.id.designationSpinner);
        status = findViewById(R.id.status);
        editEmployee = findViewById(R.id.addUser);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(5000).// long
                setExitAnimDuration(5000).// long
                start();

        back.setOnClickListener(view -> {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);
        });

        if (isNetworkAvailable()) {
            GetAllowedUserTyps task = new GetAllowedUserTyps();
            task.execute();
        } else {
            SnackAlert.error(parentView, "Internet not available!");
        }

        forward.setOnClickListener(view -> {
            hideSoftKeyboard(this);

            stepperSelectedPosition++;

            if (stepperSelectedPosition == 2) {
                stepperSelectedPosition--;
            } else {
                switch (stepperSelectedPosition) {
                    case 0:
                        changeSwitchSelection(hierarchy, info);
                        animateView(infoView, hierarchyView);
                        editEmployee.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        changeSwitchSelection(info, hierarchy);
                        animateView(hierarchyView, infoView);
                        editEmployee.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        previous.setOnClickListener(view -> {
            hideSoftKeyboard(this);

            stepperSelectedPosition--;

            if (stepperSelectedPosition == -1) {
                stepperSelectedPosition++;
            } else {
                switch (stepperSelectedPosition) {
                    case 0:
                        changeSwitchSelection(hierarchy, info);
                        animateView(infoView, hierarchyView);
                        editEmployee.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        changeSwitchSelection(info, hierarchy);
                        animateView(hierarchyView, infoView);
                        editEmployee.setVisibility(View.VISIBLE);
                        break;
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

        dob.setOnClickListener(view -> {
            selectedDateField = dob;
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        doj.setOnClickListener(view -> {
            selectedDateField = doj;
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        editEmployee.setOnClickListener(view -> {
            if (selectedUserTypeId.equals("") ||
                    employeeName.getText().toString().trim().equals("") ||
                    employeeFatherName.getText().toString().trim().equals("") ||
                    cnic.getRawText().trim().equals("") ||
                    dob.getText().toString().trim().equals("") ||
                    gender.getSelectedItem().toString().trim().equals("") ||
                    city.getSelectedItem().toString().trim().equals("") ||
                    employeeAddress.getText().toString().trim().equals("") ||
                    contactNo.getRawText().trim().equals("") ||
                    branch.getSelectedItem().toString().trim().equals("") ||
                    department.getSelectedItem().toString().trim().equals("") ||
                    designation.getSelectedItem().toString().trim().equals("") ||
                    doj.getText().toString().trim().equals("") ||
                    passwordTxt.getText().toString().trim().equals("") ||
                    status.getSelectedItem().toString().trim().equals("") ||
                    remarks.getText().toString().trim().equals("")) {
                SnackAlert.error(parentView, "Provide all necessary fields");
            } else {
                password = passwordTxt.getText().toString().trim();
                if (isNetworkAvailable()) {
                    if (checkIfNecessaryParentIsSelected(selectedUserTypeId)){
                        EditEmployeeData task = new EditEmployeeData();
                        task.execute();
                    }else {
                        SnackAlert.error(parentView, "Parent is not selected!");
                    }
                } else {
                    SnackAlert.error(parentView, "Internet not available!");
                }
            }
        });

        // Load Gender Spinner
        loadGenderSpinner();

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
        // On Spinners nothing selected
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
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    selectedUserTypeId = String.valueOf(selectedEmployee.getUserTypeID());

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
                                // Selectable & isOnClickListener = false
                                showHideHierarchySpinnersViews(View.GONE, j,
                                        getResources().getDrawable(R.drawable.corner_bg),
                                        true, false);
                            } else {
                                // Selectable & isOnClickListener = true
                                // These are required values to be selected by user
                                showHideHierarchySpinnersViews(View.VISIBLE, j,
                                        getResources().getDrawable(R.drawable.corner_bg),
                                        true, true);
                            }
                        } else {
                            // Non Selectable & isOnClickListener = false
//                        showHideHierarchySpinnersViews(View.VISIBLE, j,
//                                getResources().getDrawable(R.drawable.corner_error_bg),
//                                false, false);
                            showHideHierarchySpinnersViews(View.GONE, j,
                                    getResources().getDrawable(R.drawable.corner_error_bg),
                                    false, false);
                        }
                    }

                    if (allowedUserTypes.size() > 0) {

//                        if (checkIfAllSpinnersAreHidden()){
//                            SnackAlert.info(parentView, "You cannot change hierarchy");
//                        }
                        // Will Call All employees list here
                        if (isNetworkAvailable()) {
                            GetEmployeesList task = new GetEmployeesList();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }

                    } else {
                        showDialog("Deen's Cheese", "You cannot add any employee!", "OK", "",
                                R.drawable.ic_tick, R.drawable.ic_cancel, false);
                    }
                }
            }
        }
    }

    private class EditEmployeeData extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/UpdateEmployee?";
        String selectedGMID = "0", selectedSTID = "0", selectedDMID = "0", selectedSDMID = "0", selectedSOID = "0";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);

            // Selected GM ID. If GM View is visible we will update id. Otherwise we will pass 0
            if (gmList.size() > 0 && gmView.getVisibility() == View.VISIBLE && gmSpinner.isEnabled()) {
                if (gmSpinner.getSelectedItemPosition() != -1) {
                    selectedGMID = String.valueOf(gmList.get(gmSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (Integer.parseInt(loggedInUserType) == 2) {
                    selectedGMID = String.valueOf(userClass.getUserID());
                } else {
                    selectedGMID = userClass.getGM();
                }
            }

            // Selected ST ID. If ST View is visible we will update id. Otherwise we will pass 0
            if (stList.size() > 0 && stView.getVisibility() == View.VISIBLE && stSpinner.isEnabled()) {
                if (stSpinner.getSelectedItemPosition() != -1) {
                    selectedSTID = String.valueOf(stList.get(stSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (Integer.parseInt(loggedInUserType) == 3) {
                    selectedSTID = String.valueOf(userClass.getUserID());
                } else {
                    selectedSTID = userClass.getST();
                }
            }

            // Selected DM ID. If DM View is visible we will update id. Otherwise we will pass 0
            if (dmList.size() > 0 && dmView.getVisibility() == View.VISIBLE && dmSpinner.isEnabled()) {
                if (dmSpinner.getSelectedItemPosition() != -1) {
                    selectedDMID = String.valueOf(dmList.get(dmSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (Integer.parseInt(loggedInUserType) == 4) {
                    selectedDMID = String.valueOf(userClass.getUserID());
                } else {
                    selectedDMID = userClass.getDM();
                }
            }

            // Selected SDM ID. If SDM View is visible we will update id. Otherwise we will pass 0
            if (sdmList.size() > 0 && sdmView.getVisibility() == View.VISIBLE && sdmSpinner.isEnabled()) {
                if (sdmSpinner.getSelectedItemPosition() != -1) {
                    selectedSDMID = String.valueOf(sdmList.get(sdmSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (Integer.parseInt(loggedInUserType) == 5) {
                    selectedSDMID = String.valueOf(userClass.getUserID());
                } else {
                    selectedSDMID = userClass.getSDM();
                }
            }

            // Selected SO ID. If SO View is visible we will update id. Otherwise we will pass 0
            if (soList.size() > 0 && soView.getVisibility() == View.VISIBLE && soSpinner.isEnabled()) {
                if (soSpinner.getSelectedItemPosition() != -1) {
                    selectedSOID = String.valueOf(soList.get(soSpinner.getSelectedItemPosition()).getUserID());
                }
            } else {
                if (Integer.parseInt(loggedInUserType) == 6) {
                    selectedSOID = String.valueOf(userClass.getUserID());
                } else {
                    selectedSOID = userClass.getSO();
                }
            }

        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "");

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "EmpID=" + URLEncoder.encode(String.valueOf(selectedEmployee.getEmpID()), "UTF-8")
                                + "&UserName=" + URLEncoder.encode(username, "UTF-8")
                                + "&Password=" + URLEncoder.encode(password, "UTF-8")
                                + "&UserTypeID=" + selectedUserTypeId
                                + "&EmpName=" + URLEncoder.encode(employeeName.getText().toString().trim(), "UTF-8")
                                + "&EmpFatherName=" + URLEncoder.encode(employeeFatherName.getText().toString().trim(), "UTF-8")
                                + "&CNIC=" + URLEncoder.encode(cnic.getRawText().trim(), "UTF-8")
                                + "&DOB=" + URLEncoder.encode(dob.getText().toString().trim(), "UTF-8")
                                + "&Gender=" + URLEncoder.encode(gender.getSelectedItem().toString(), "UTF-8")
                                + "&EmpCityID=" + cities.get(city.getSelectedItemPosition()).getCityId()
                                + "&EmpAddress=" + URLEncoder.encode(employeeAddress.getText().toString().trim(), "UTF-8")
                                + "&ContactNo=" + URLEncoder.encode(contactNo.getRawText().trim(), "UTF-8")
                                + "&ContactNo2nd=" + URLEncoder.encode(contactNo2.getRawText().trim(), "UTF-8")
                                + "&BranchID=" + branches.get(branch.getSelectedItemPosition()).getBranchID()
                                + "&DepartmentID=" + departments.get(department.getSelectedItemPosition()).getDepartmentID()
                                + "&DesignationID=" + designations.get(designation.getSelectedItemPosition()).getDesignationID()
                                + "&DOJ=" + URLEncoder.encode(doj.getText().toString().trim(), "UTF-8")
                                + "&EmpStatusID=" + statuses.get(status.getSelectedItemPosition()).getEmpStatusID()
                                + "&Remarks=" + URLEncoder.encode(remarks.getText().toString().trim(), "UTF-8")
                                + "&LoginUserID=" + loggedInUserID
                                + "&GM=" + selectedGMID
                                + "&ST=" + selectedSTID
                                + "&DM=" + selectedDMID
                                + "&SDM=" + selectedSDMID
                                + "&SO=" + selectedSOID)
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Update");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
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
                        .url(URL + methodName + "EmpID=0&UserTypeID=0&UserID=" + URLEncoder.encode(loggedInUserID, "UTF-8"))
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
            if (!isFinishing()){
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

                    // Get Miscellaneous Data from server of different lists
                    if (isNetworkAvailable()) {
                        GetDataFromServer task = new GetDataFromServer();
                        task.execute();
                    } else {
                        SnackAlert.error(parentView, "Internet not available!");
                    }
                }
            }
        }
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

    private class GetDataFromServer extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/" + miscMethodNames[methodCount] + "?";

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
                parseJSONStringToJSONObject(networkResp, methodType[methodCount]);

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    methodCount++;

                    if (methodCount < methodType.length) {
                        if (isNetworkAvailable()) {
                            GetDataFromServer task = new GetDataFromServer();
                            task.execute();
                        } else {
                            SnackAlert.error(parentView, "Internet not available!");
                        }
                    } else {
                        loadingView.setVisibility(View.VISIBLE);
                        loadMiscSpinners();
                    }
                }
            }
        }
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

        if (methodName.equals("City")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    cities.add(new CityClass(jsonObject.getInt("CityId"),
                            jsonObject.getString("CityName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Department")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    departments.add(new DepartmentClass(jsonObject.getInt("DepartmentID"),
                            jsonObject.getString("DepartmentName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Branch")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    branches.add(new BranchesClass(jsonObject.getInt("BranchID"),
                            jsonObject.getString("BranchName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Designation")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    designations.add(new DesignationClass(jsonObject.getInt("DesignationID"),
                            jsonObject.getString("DesignationName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Status")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    statuses.add(new StatusClass(jsonObject.getInt("EmpStatusID"),
                            jsonObject.getString("EmpStatusName")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (methodName.equals("Update")) {
            message = "Deen's Cheese";
            messageDetail = networkResp;
        }

    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(EditEmployee.this);
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
    public void onBackPressed() {
        super.onBackPressed();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
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
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void showDialog(String title, String message,
                            String positiveTitle, String negativeTitle,
                            int positiveIcon, int negativeIcon,
                            Boolean isNegative) {
        if (isNegative) {
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(this)
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
            BottomSheetMaterialDialog mBottomSheetDialog = new BottomSheetMaterialDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveTitle, positiveIcon, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        if (message.contains("saved")) {
                            super.onBackPressed();
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    private void changeSwitchSelection(RadioButton selected, RadioButton one) {
        runOnUiThread(() -> {
            selected.setChecked(true);
            one.setChecked(false);
        });
    }

    private void animateView(LinearLayout hideView, LinearLayout showView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideView.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                hideView.setVisibility(View.GONE);
                                showView.setVisibility(View.VISIBLE);
                                showView.setAlpha(1.0f);
                            }
                        });
            }
        });
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
                    populateSpinnerWithArray(gmSpinner, new String[]{selectedEmployee.getGMName()});
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
                    populateSpinnerWithArray(stSpinner, new String[]{selectedEmployee.getSTName()});
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
                    populateSpinnerWithArray(dmSpinner, new String[]{selectedEmployee.getDMName()});
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
                    populateSpinnerWithArray(sdmSpinner, new String[]{selectedEmployee.getSDMName()});
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
                    populateSpinnerWithArray(soSpinner, new String[]{selectedEmployee.getSOName()});
                } else {
                    if (isOnCLickListener) {
                        soSpinner.setOnItemSelectedListener(this);
                    }
                }
                break;
        }
    }

    private void populateSpinnerWithArray(Spinner spinner, String[] array) {
        ArrayAdapter adapter = new ArrayAdapter(EditEmployee.this, R.layout.spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void populateSpinner(Spinner spinner, ArrayList<EmployeeClass> list) {
        ArrayList<String> names = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            names.add(list.get(i).getEmpName());
        }

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, names.toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // We will call this method to clear spinner items of subsequent spinners if parent has no value
    private void clearSpinner(Spinner spinner) {
        if (spinner.getAdapter() != null) {
            spinner.setAdapter(null);
        }
    }

    private void loadGenderSpinner() {
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);
    }

    // Loading data of selected employee
    private void loadMiscSpinners() {
        ArrayList<String> cityNames = new ArrayList<>();
        ArrayList<String> branchName = new ArrayList<>();
        ArrayList<String> designationName = new ArrayList<>();
        ArrayList<String> statusName = new ArrayList<>();
        ArrayList<String> departmentName = new ArrayList<>();

        if (cities.size() > 0) {
            for (int i = 0; i < cities.size(); i++) {
                cityNames.add(cities.get(i).getCityName());
            }
            loadSpinner(city, cityNames);
        }

        if (branches.size() > 0) {
            for (int i = 0; i < branches.size(); i++) {
                branchName.add(branches.get(i).getBranchName());
            }
            loadSpinner(branch, branchName);
        }

        if (designations.size() > 0) {
            for (int i = 0; i < designations.size(); i++) {
                designationName.add(designations.get(i).getDesignationName());
            }
            loadSpinner(designation, designationName);
        }

        if (statuses.size() > 0) {
            for (int i = 0; i < statuses.size(); i++) {
                statusName.add(statuses.get(i).getEmpStatusName());
            }
            loadSpinner(status, statusName);
        }

        if (departments.size() > 0) {
            for (int i = 0; i < departments.size(); i++) {
                departmentName.add(departments.get(i).getDepartmentName());
            }
            loadSpinner(department, departmentName);
        }

        // Load Selected Employee Data from selected object //

        runOnUiThread(() -> {
            loadingView.setVisibility(View.VISIBLE);
            // Load GM Spinner
            ArrayList<Integer> gmIds = new ArrayList<>();
            for (int i = 0; i < gmList.size(); i++) {
                gmIds.add(gmList.get(i).getUserID());
            }
            if (gmIds.contains(selectedEmployee.getGM())) {
                gmSpinner.setSelection(gmIds.indexOf(selectedEmployee.getGM()));
            }

            // LOADING SPINNER SELECTION EVENTS WITH TIME DELAY SO NEXT SPINNER
            // SELECTION CANNOT BE LOADED BEFORE PREVIOUS SELECTION IS COMPLETED
            // THIS HACK REMOVED ERROR ;)
            new Handler().postDelayed(() -> {
                // Load ST Spinner
                ArrayList<Integer> stIds = new ArrayList<>();
                for (int i = 0; i < stList.size(); i++) {
                    stIds.add(stList.get(i).getUserID());
                }
                if (stIds.contains(selectedEmployee.getST())) {
                    stSpinner.setSelection(stIds.indexOf(selectedEmployee.getST()));
                }
            }, 1000);

            new Handler().postDelayed(() -> {
                // Load DM Spinner
                ArrayList<Integer> dmIds = new ArrayList<>();
                for (int i = 0; i < dmList.size(); i++) {
                    dmIds.add(dmList.get(i).getUserID());
                }
                if (dmIds.contains(selectedEmployee.getDM())) {
                    dmSpinner.setSelection(dmIds.indexOf(selectedEmployee.getDM()));
                }
            }, 1500);

            new Handler().postDelayed(() -> {
                // Load SDM Spinner
                ArrayList<Integer> sdmIds = new ArrayList<>();
                for (int i = 0; i < sdmList.size(); i++) {
                    sdmIds.add(sdmList.get(i).getUserID());
                }
                if (sdmIds.contains(selectedEmployee.getSDM())) {
                    sdmSpinner.setSelection(sdmIds.indexOf(selectedEmployee.getSDM()));
                }
            }, 2000);

            new Handler().postDelayed(() -> {
                // Load SO Spinner
                ArrayList<Integer> soIds = new ArrayList<>();
                for (int i = 0; i < soList.size(); i++) {
                    soIds.add(soList.get(i).getUserID());
                }
                if (soIds.contains(selectedEmployee.getSO())) {
                    soSpinner.setSelection(soIds.indexOf(selectedEmployee.getSO()));
                }
            }, 3000);

            // Load TextFields Data
            username = selectedEmployee.getUserName();
            password = selectedEmployee.getPassword();
            employeeName.setText(selectedEmployee.getEmpName());
            employeeFatherName.setText(selectedEmployee.getEmpFatherName());
            cnic.setText(selectedEmployee.getCNIC());
            dob.setText(selectedEmployee.getDOB().replace("T00:00:00", ""));
            employeeAddress.setText(selectedEmployee.getEmpAddress());
            contactNo.setText(selectedEmployee.getContactNo());
            contactNo2.setText(selectedEmployee.getContactNo2nd());
            doj.setText(selectedEmployee.getDOJ().replace("T00:00:00", ""));
            remarks.setText(selectedEmployee.getRemarks());
            passwordTxt.setText(selectedEmployee.getPassword());

            // Load Data Spinners
            ArrayList<String> genderList = new ArrayList<>();
            genderList.add("Male");
            genderList.add("Female");
            gender.setSelection(genderList.indexOf(selectedEmployee.getGender()));
            city.setSelection((cityNames).indexOf(selectedEmployee.getEmpCityName()));
            branch.setSelection((branchName).indexOf(selectedEmployee.getBranchName()));
            department.setSelection((departmentName).indexOf(selectedEmployee.getDepartmentName()));
            designation.setSelection((designationName).indexOf(selectedEmployee.getDesignationName()));
            status.setSelection((statusName).indexOf(selectedEmployee.getEmpStatusName()));

            loadingView.setVisibility(View.GONE);
        });

    }

    private void loadSpinner(Spinner spinner, ArrayList<String> list) {
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, list.toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectedDateField.setText(sdf.format(myCalendar.getTime()));
    }

    private void loadPreferences() {
        Gson gson = new Gson();
        selectedEmployee = gson.fromJson(getIntent().getExtras().getString("SelectedUser"), EmployeeClass.class);
        String json = preferences.getString("User", "");
        userClass = gson.fromJson(json, UserClass.class);
    }

    private Boolean checkIfNecessaryParentIsSelected(String selectedUserTypeId){
        switch (selectedUserTypeId){
            case "2":
                return true;
            case "3":
                if (gmSpinner.getAdapter() == null || gmSpinner.getAdapter().getCount() < 1){
                    return false;
                }
                break;
            case "4":
                if (stSpinner.getAdapter() == null || stSpinner.getAdapter().getCount() < 1){
                    return false;
                }
                break;
            case "5":
                if (dmSpinner.getAdapter() == null || dmSpinner.getAdapter().getCount() < 1){
                    return false;
                }
                break;
            case "6":
                if (sdmSpinner.getAdapter() == null || sdmSpinner.getAdapter().getCount() < 1){
                    return false;
                }
                break;
            case "7":
                if (soSpinner.getAdapter() == null || soSpinner.getAdapter().getCount() < 1){
                    return false;
                }
                break;
        }
        return true;
    }

    private Boolean checkIfAllSpinnersAreHidden(){
        return gmView.getVisibility() == View.GONE &&
                soView.getVisibility() == View.GONE &&
                sdmView.getVisibility() == View.GONE &&
                dmView.getVisibility() == View.GONE &&
                stView.getVisibility() == View.GONE;
    }
}
