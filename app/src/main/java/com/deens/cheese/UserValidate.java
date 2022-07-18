package com.deens.cheese;

import static com.deens.cheese.GlobalVariable.API_KEY;
import static com.deens.cheese.GlobalVariable.KEY_NAME;
import static com.deens.cheese.GlobalVariable.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.skydoves.elasticviews.ElasticButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.cr0wd.snackalert.SnackAlert;

import java.net.URLEncoder;
import java.util.Objects;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class UserValidate extends AppCompatActivity {

    ScrollView parentView;
    RelativeLayout loadingView;
    MaskedEditText contactNo, cnic;
    EditText username, password;
    ElasticButton checkUserDetail;
    ImageView back;

    // Message title/detail from Server
    String message = "";
    String messageDetail = "";

    // If User is coming back from adding user successfully, we will go back to main menu.
    public static Boolean isFromAddingUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change Status Bar Color & Hide Title Bar .. Depends on Android Lollipop
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.color_secondry));
        setContentView(R.layout.activity_user_validate);

        parentView = findViewById(R.id.parent);
        loadingView = findViewById(R.id.loadingView);
        contactNo = findViewById(R.id.phone);
        checkUserDetail = findViewById(R.id.checkUserDetail);
        back = findViewById(R.id.back);
        cnic = findViewById(R.id.cnic);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        // Hide Keyboard when touch outside an edittext
        setupUI(parentView);

        // Hides Action Bar.. We don't need in this activity
        Objects.requireNonNull(getSupportActionBar()).hide();

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        checkUserDetail.setOnClickListener(view -> {
            hideSoftKeyboard(UserValidate.this);
            if (contactNo.getRawText().trim().equals("")){
                SnackAlert.error(parentView, "Please provided contact number!");
            }else {
                if (contactNo.getRawText().trim().length() != 11 || contactNo.getRawText().charAt(0) != '0') {
                    SnackAlert.error(parentView, "Phone number is not valid!");
                }else {
                    if (cnic.getRawText().trim().equals("")) {
                        SnackAlert.error(parentView, "Please provide CNIC!");
                    }else {
                        if (cnic.getRawText().trim().length() != 13) {
                            SnackAlert.error(parentView, "CNIC is not valid!");
                        }else {
                            if (username.getText().toString().trim().equals("")
                                    || password.getText().toString().trim().equals("" +
                                    "")){
                                SnackAlert.error(parentView, "Username/Password are required!");
                            }else {
                                if (username.length() < 8 || password.length() < 8){
                                    SnackAlert.error(parentView, "Username/Password at least 8 characters long!");
                                }else {
                                    if (isNetworkAvailable()) {
                                        CheckUserTask task = new CheckUserTask();
                                        task.execute();
                                    } else {
                                        SnackAlert.error(parentView, "Internet not available!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        back.setOnClickListener(view -> {
            UserValidate.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);

        });

    }

    private class CheckUserTask extends AsyncTask<Void, Void, Void> {

        String methodName = "Auth/UserValidate?";

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
                        .url(URL + methodName + "ContactNo_CNIC=" + URLEncoder.encode(contactNo.getRawText().trim(), "UTF-8"))
                        .method("POST", body)
                        .addHeader(KEY_NAME, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "");

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error";
                messageDetail = "Some error occurred";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            loadingView.setVisibility(View.GONE);
            if (!message.contains("New Entry")) {
                // Show some message from server
                showDialog("Deen's Cheese", "Provided info is not valid for registration", "OK", "", R.drawable.ic_tick, R.drawable.ic_cancel, false);
            } else {
                startActivity(new Intent(UserValidate.this, AddEmployee.class).
                        putExtra("contact", contactNo.getRawText().trim()).
                        putExtra("cnic", cnic.getRawText().trim()).
                        putExtra("username", username.getText().toString().trim()).
                        putExtra("password", password.getText().toString().trim()));
                overridePendingTransition(R.anim.slide_in_right,
                        R.anim.slide_out_left);
            }
        }
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        message = networkResp;
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

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(UserValidate.this);
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
        UserValidate.super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
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
                    })
                    .build();

            // Show Dialog
            mBottomSheetDialog.show();
        }
    }

    private void animateView(RelativeLayout hideView, RelativeLayout showView, String buttonText){
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
                        checkUserDetail.setText(buttonText);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromAddingUser){
            isFromAddingUser = false;
            super.onBackPressed();
            UserValidate.super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right);

        }
    }
}
