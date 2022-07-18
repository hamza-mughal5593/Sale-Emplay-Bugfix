package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.antonious.materialdaypicker.MaterialDayPicker;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class CustomerAdapter extends ArrayAdapter<CustomerClass> {

    private ArrayList<CustomerClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView customerName;
        TextView areaAddress;
        TextView approved;
        ImageView approvedImage;
        TextView letter;
        NoboButton ledger;
        MaterialDayPicker dayPicker;
        RelativeLayout nameView;
    }

    public CustomerAdapter(ArrayList<CustomerClass> data, Context context) {
        super(context, R.layout.customer_list_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    Map<Integer, View> views = new HashMap<Integer, View>();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (views.containsKey(position)) {
            return views.get(position);
        }

        // Get the data item for this position
        CustomerClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        CustomerAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new CustomerAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.customer_list_item, parent, false);
        viewHolder.customerName = convertView.findViewById(R.id.customerName);
        viewHolder.areaAddress = convertView.findViewById(R.id.areaAddress);
        viewHolder.approved = convertView.findViewById(R.id.approved);
        viewHolder.approvedImage = convertView.findViewById(R.id.approvedIcon);
        viewHolder.letter = convertView.findViewById(R.id.letter);
        viewHolder.dayPicker = convertView.findViewById(R.id.day_picker);
        viewHolder.nameView = convertView.findViewById(R.id.nameView);
        viewHolder.ledger = convertView.findViewById(R.id.ledger);
        viewHolder.nameView.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.dayPicker.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.ledger.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

        convertView.setTag(viewHolder);
        views.put(position, convertView);

        viewHolder.customerName.setText(dataModel.getCustomerName());
        viewHolder.areaAddress.setText(dataModel.getArea() + ", " + dataModel.getAddress());

        if (dataModel.isApproved()) {
            viewHolder.approved.setText("Approved limit: " + String.valueOf(dataModel.
                    getCreditLimit()).replace(".0", " PKR"));
            viewHolder.approvedImage.setImageResource(R.drawable.ic_check_green);
        } else {
            viewHolder.approved.setText("Not Approved yet!");
            viewHolder.approvedImage.setImageResource(R.drawable.ic_check_disabled);
        }

        char letterOne = dataModel.getCustomerName().charAt(0);
        viewHolder.letter.setText(Character.toString(letterOne));
        viewHolder.letter.setTextColor(MatrialColorPalette.getRandomColor("500"));

        viewHolder.dayPicker.setSelectedDays(returnDayOfWeek(dataModel.getVisit_Day()));
        viewHolder.dayPicker.disableAllDays();

        return convertView;
    }

    private MaterialDayPicker.Weekday returnDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Friday":
                return MaterialDayPicker.Weekday.FRIDAY;
            case "Saturday":
                return MaterialDayPicker.Weekday.SATURDAY;
            case "Sunday":
                return MaterialDayPicker.Weekday.SUNDAY;
            case "Monday":
                return MaterialDayPicker.Weekday.MONDAY;
            case "Tuesday":
                return MaterialDayPicker.Weekday.TUESDAY;
            case "Wednesday":
                return MaterialDayPicker.Weekday.WEDNESDAY;
            case "Thursday":
                return MaterialDayPicker.Weekday.THURSDAY;
        }
        return MaterialDayPicker.Weekday.FRIDAY;
    }
}
