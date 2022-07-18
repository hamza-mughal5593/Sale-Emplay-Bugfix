package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
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

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class AttendanceHistory extends AppCompatActivity {

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";
    String loggedInUserID = "";

    RelativeLayout parentView;
    Spinner employeeSpinner;
    TextView attendanceDate;
    RelativeLayout loadingView;
    ElasticButton bottomButton;

    // Date Selection Variables
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();
    String userId = "";
    ArrayList<AttendanceItemClass> list = new ArrayList<>();
    ArrayList<EmployeeClass> employeesList = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_attendance_history);

        parentView = findViewById(R.id.parentView);
        attendanceDate = findViewById(R.id.date);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        loadingView = findViewById(R.id.loadingView);
        listView = findViewById(R.id.listView);
        bottomButton = findViewById(R.id.bottomButton);
        loggedInUserID = getIntent().getExtras().getString("ID");
        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Date variable initialized
        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        attendanceDate.setOnClickListener(view -> {
            new DatePickerDialog(AttendanceHistory.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        bottomButton.setOnClickListener(view -> {
            if (attendanceDate.getText().toString().trim().equals("")){
                SnackAlert.error(parentView, "Please select a date");
            }else {
                if (isNetworkAvailable()) {
                    GetAttendanceList task = new GetAttendanceList();
                    task.execute();
                } else {
                    SnackAlert.error(findViewById(android.R.id.content), "Internet not available!");
                }
            }
        });

        if (isNetworkAvailable()) {
            GetEmployeesList task = new GetEmployeesList();
            task.execute();
        } else {
            SnackAlert.error(findViewById(android.R.id.content), "Internet not available!");
        }

        findViewById(R.id.back).setOnClickListener(view -> AttendanceHistory.super.onBackPressed());
    }

    private class GetAttendanceList extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/GetAttendanceList?";

        @Override
        protected void onPreExecute() {
            message = "";
            messageDetail = "";
            loadingView.setVisibility(View.VISIBLE);
            list.clear();
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(URL + methodName
                                + "UserID=" + URLEncoder.encode(userId, "UTF-8")
                                + "&AttendanceDate=" + URLEncoder.encode(attendanceDate.getText().toString().trim(), "UTF-8")
                        )
                        .get()
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "List");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!AttendanceHistory.this.isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
                } else {
                    if (list.size() > 0) {
                        // Re assign adapter to listview
                        listView.setVisibility(View.VISIBLE);
                        AttendanceAdapter adapter = new AttendanceAdapter(list, AttendanceHistory.this);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                startActivity(new Intent(AttendanceHistory.this, AttendanceMap.class)
                                        .putExtra("Lat", list.get(i).getLat())
                                        .putExtra("Long", list.get(i).getLong())
                                        .putExtra("Loc", list.get(i).getLocation())
                                );
                            }
                        });
                    } else {
                        listView.setVisibility(View.GONE);
                        showDialog("Deen's Cheese", "No data found", "OK", "",
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
            if (!AttendanceHistory.this.isFinishing()){
                loadingView.setVisibility(View.GONE);
                if (!message.equals("")) {
                    // Show some message from server
                    showDialog(message, messageDetail, "OK", "", R.drawable.ic_tick,
                            R.drawable.ic_cancel, false);
                } else {
                    ArrayList<String> employeeNames = new ArrayList<>();

                    for (int i = 0; i < employeesList.size() ; i++){
                        employeeNames.add(employeesList.get(i).getEmpName());
                    }

                    ArrayAdapter adapter = new ArrayAdapter(AttendanceHistory.this, R.layout.spinner_item, employeeNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    employeeSpinner.setAdapter(adapter);

                    employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            userId = String.valueOf(employeesList.get(i).getUserID());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {}
                    });
                }
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("List")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (!jsonObject.getString("EmpCode").equals("null")){
                        list.add(new AttendanceItemClass(jsonObject.getString("EmpCode"),
                                jsonObject.getString("EmpName"),
                                jsonObject.getString("DesignationTitle"),
                                jsonObject.getString("AttDescription"),
                                jsonObject.getString("DutyTime"),
                                jsonObject.getString("CheckIn"),
                                jsonObject.getString("LateMinutes"),
                                jsonObject.getString("CheckOut"),
                                jsonObject.getString("WorkingMinutes"),
                                jsonObject.getString("Lat"),
                                jsonObject.getString("Long"),
                                jsonObject.getString("Location")
                        ));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (methodName.equals("Employees")) {
            try {
                JSONArray jsonArray = new JSONArray(networkResp);
                employeesList.add(new EmployeeClass(0, "",
                        "", 0, "",
                        0, "","ALL",
                        "", "", "", "",
                        0,
                        "","","","",
                        0, "", 0, "",
                        0, "","",
                        0,"","","",
                        0,"",0,"",0,"",0,"",0,""));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    employeesList.add(new EmployeeClass(jsonObject.getInt("UserID"),
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
                if (employeesList.size() == 0){
                    message = "Deen's Cheese";
                    messageDetail = "No Employee Found";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        if (messageDetail.contains("saved")) {
                            AttendanceHistory.super.onBackPressed();
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    // Update label for Date fields
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        attendanceDate.setText(sdf.format(myCalendar.getTime()));
    }
}