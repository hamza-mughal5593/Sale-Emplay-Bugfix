package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttendanceAdapter extends ArrayAdapter<AttendanceItemClass> {

    private ArrayList<AttendanceItemClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView workingMinute;
        TextView lateMinute;
        TextView designation;
        TextView present;
        TextView time;
        TextView location;
      }

    public AttendanceAdapter(ArrayList<AttendanceItemClass> data, Context context) {
        super(context, R.layout.attendance_list_item, data);
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
        AttendanceItemClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        AttendanceAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new AttendanceAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.attendance_list_item, parent, false);
        viewHolder.name = convertView.findViewById(R.id.customerName);
        viewHolder.workingMinute = convertView.findViewById(R.id.workingMinute);
        viewHolder.lateMinute = convertView.findViewById(R.id.lateMinute);
        viewHolder.designation = convertView.findViewById(R.id.designation);
        viewHolder.present = convertView.findViewById(R.id.present);
        viewHolder.time = convertView.findViewById(R.id.time);
        viewHolder.location = convertView.findViewById(R.id.location);

        convertView.setTag(viewHolder);
        views.put(position, convertView);

        viewHolder.name.setText(dataModel.getEmpName());
        viewHolder.workingMinute.setText("Working Minute: "+ dataModel.getWorkingMinutes());
        viewHolder.lateMinute.setText("Late Minute: "+ dataModel.getLateMinutes());
        viewHolder.designation.setText(dataModel.getDesignationTitle());
        viewHolder.location.setText("Location: " + dataModel.getLocation());

        if (dataModel.getAttDescription().equals("Absent")){
            viewHolder.present.setBackground(mContext.getDrawable(R.drawable.all_corner_cancel));
            viewHolder.present.setVisibility(View.VISIBLE);
        }else {
            viewHolder.present.setVisibility(View.GONE);
        }

        viewHolder.time.setText("Check in time: " + dataModel.getCheckIn());

        return convertView;
    }

}
