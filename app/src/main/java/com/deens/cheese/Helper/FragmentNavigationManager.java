package com.deens.cheese.Helper;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.deens.cheese.BuildConfig;
import com.deens.cheese.Interface.NavigationManager;
import com.deens.cheese.MainActivity;
import com.deens.cheese.R;
import com.deens.cheese.SaleTargetClass;
import com.deens.cheese.SaleTargetView;
import com.deens.cheese.databinding.FragmentPreinvoiceBinding;
import com.deens.cheese.saletargetview.TargetViewFragment;
import com.deens.cheese.ui.approve.ApproveFragment;
import com.deens.cheese.ui.attendance.AttendanceFragment;
import com.deens.cheese.ui.attendancehistory.AttendanceHistoryFragment;
import com.deens.cheese.ui.cash.CashFragment;
import com.deens.cheese.ui.customer.CustomerFragment;
import com.deens.cheese.ui.dailysale.DailySaleFragment;
import com.deens.cheese.ui.dashboard.DashboardFragment;
import com.deens.cheese.ui.employees.EmployeeFragment;
import com.deens.cheese.ui.order.OrderFragment;
import com.deens.cheese.ui.pickcartons.PickCartonFragment;
import com.deens.cheese.ui.preinvoice.PreInvoiceFragment;
import com.deens.cheese.ui.profile.ProfileFragment;
import com.deens.cheese.ui.record.StockRecordFragment;
import com.deens.cheese.ui.report.ReportFragment;
import com.deens.cheese.ui.salereport.SaleReportFragment;
import com.deens.cheese.ui.stock.StockFragment;
import com.deens.cheese.ui.target.TargetFragment;

public class FragmentNavigationManager implements NavigationManager {

    private static FragmentNavigationManager mInstance;
    private FragmentManager fragmentManager;
    private MainActivity mainActivity;

    public static FragmentNavigationManager getMInstance(MainActivity activity) {
        if (mInstance == null)
            mInstance = new FragmentNavigationManager();
        mInstance.configure(activity);
        return mInstance;
    }

    private void configure(MainActivity activity) {
        mainActivity = activity;
        fragmentManager = mainActivity.getSupportFragmentManager();
    }

    @Override
    public void showFragment(String title) {
        if (title.equals("Dashboard")){
            showFragment(DashboardFragment.newInstance(title), false);
        } else if (title.equals("Mark Attendance")){
            showFragment(AttendanceFragment.newInstance(title), false);
        } else if (title.equals("Attendance Report")){
            showFragment(AttendanceHistoryFragment.newInstance(title), false);
        } else if (title.equals("Employee Management")){
            showFragment(EmployeeFragment.newInstance(title), false);
        } else if (title.equals("Customer Management")){
            showFragment(CustomerFragment.newInstance(title), false);
        } else if (title.equals("Approve Customer")){
            showFragment(ApproveFragment.newInstance(title), false);
        } else if (title.equals("Cash Received")){
            showFragment(CashFragment.newInstance(title), false);
        } else if (title.equals("Stock Report")){
            showFragment(ReportFragment.newInstance(title), false);
        } else if (title.equals("Sale Report")){
            showFragment(SaleReportFragment.newInstance(title), false);
        } else if (title.equals("Order Management")){
            showFragment(OrderFragment.newInstance(title), false);
        } else if (title.equals("Profile")){
            showFragment(ProfileFragment.newInstance(title), false);
        } else if (title.equals("Sale Target")){
            showFragment(TargetFragment.newInstance(title), false);
        } else if (title.equals("Pick Order")){
            showFragment(PickCartonFragment.newInstance(title), false);
        } else if (title.equals("View Stock")){
            showFragment(StockFragment.newInstance(title), false);
        } else if (title.equals("Record Stock")){
            showFragment(StockRecordFragment.newInstance(title), false);
        }else if (title.equals("View Sale Target")){
            showFragment(TargetViewFragment.newInstance(title), false);
        }else if (title.equals("Manage PO")){
            showFragment(PreInvoiceFragment.newInstance(title), false);
        }else if (title.equals("Daily Sale")){
            showFragment(DailySaleFragment.newInstance(title), false);
        }
    }

    private void showFragment(Fragment fragment, Boolean state){
        FragmentManager fm = fragmentManager;
        FragmentTransaction ft = fm.beginTransaction().replace(R.id.container, fragment);
        ft.addToBackStack(null);
        if (state || !BuildConfig.DEBUG){
            ft.commitAllowingStateLoss();
        }else {
            ft.commit();
        }
        fm.executePendingTransactions();
    }
}
