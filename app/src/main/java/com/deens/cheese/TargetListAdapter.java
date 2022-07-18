package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class TargetListAdapter extends ArrayAdapter<TargetClass> {

    private ArrayList<TargetClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView soName;
        TextView target;
        TextView startDate;
        TextView endDate;
    }

    public TargetListAdapter(ArrayList<TargetClass> data, Context context) {
        super(context, R.layout.target_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TargetClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        TargetListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new TargetListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.target_list_item, parent, false);
            viewHolder.soName =  convertView.findViewById(R.id.soName);
            viewHolder.target =  convertView.findViewById(R.id.target);
            viewHolder.startDate =  convertView.findViewById(R.id.startDate);
            viewHolder.endDate =  convertView.findViewById(R.id.endDate);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TargetListAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.soName.setText("SO Name: "+ dataModel.getSOName());
        viewHolder.target.setText("Target: " + (dataModel.getSaleTarget().intValue()) + " Kg");
        viewHolder.startDate.setText("Start Date: "+ dataModel.getDateFrom().replace("T00:00:00", ""));
        viewHolder.endDate.setText("End Date: "+ dataModel.getDateTo().replace("T00:00:00", ""));

        return convertView;
    }

}
