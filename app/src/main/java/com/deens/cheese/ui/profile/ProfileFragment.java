package com.deens.cheese.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.google.gson.Gson;
import com.inihood.backgroundcoloranimation.BackgroundColorAnimation;
import com.deens.cheese.GlobalVariable;
import com.deens.cheese.R;
import com.deens.cheese.ResetPassword;
import com.deens.cheese.UserClass;
import com.deens.cheese.databinding.FragmentProfileBinding;

import br.com.sapereaude.maskedEditText.MaskedEditText;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    ScrollView parentView;
    TextView username, employeeName, employeeCode, branchName, departmentName, designationName,
            userTypeName, password;
    MaskedEditText phoneNo;

    // Shared Preference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    ImageView edit;

    UserClass userClass;

    public static ProfileFragment newInstance(String param1) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        parentView = root.findViewById(R.id.parentView);
        username = root.findViewById(R.id.username);
        password = root.findViewById(R.id.password);
        employeeCode = root.findViewById(R.id.employeeCode);
        employeeName = root.findViewById(R.id.employeeName);
        phoneNo = root.findViewById(R.id.phoneNo);
        branchName = root.findViewById(R.id.branchName);
        departmentName = root.findViewById(R.id.departmentName);
        designationName = root.findViewById(R.id.designationName);
        userTypeName = root.findViewById(R.id.userType);
        edit = root.findViewById(R.id.edit);

        preferences = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(GlobalVariable.PREF_NAME , MODE_PRIVATE).edit();

        loadPreferences();

        username.setText(userClass.getUserName());
        employeeCode.setText(userClass.getEmpCode());
        employeeName.setText(userClass.getEmpName());
        phoneNo.setText(userClass.getPhoneNo());
        branchName.setText(userClass.getBranchName());
        departmentName.setText(userClass.getDepartmentName());
        designationName.setText(userClass.getDesignationName());
        userTypeName.setText(userClass.getUserTypeName());
        password.setText(userClass.getUserName());

        new BackgroundColorAnimation().
                init(parentView).
                setEnterAnimDuration(3000).// long
                setExitAnimDuration(3000).// long
                start();

        edit.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), ResetPassword.class).putExtra("ID", userClass.getUserID()));
        });

        return root;
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
}