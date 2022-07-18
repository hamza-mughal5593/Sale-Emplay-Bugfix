package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class EmployeeAdapter extends ArrayAdapter<EmployeeClass> {

    private ArrayList<EmployeeClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView designation;
        TextView doj;
        TextView gender;
        TextView letter;
    }

    public EmployeeAdapter(ArrayList<EmployeeClass> data, Context context) {
        super(context, R.layout.employee_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        EmployeeClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        EmployeeAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new EmployeeAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.employee_list_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.name);
            viewHolder.designation =  convertView.findViewById(R.id.designation);
            viewHolder.gender =  convertView.findViewById(R.id.gender);
            viewHolder.doj =  convertView.findViewById(R.id.doj);
            viewHolder.letter =  convertView.findViewById(R.id.letter);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (EmployeeAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getEmpName());
        viewHolder.designation.setText(dataModel.getDesignationName());
        String gender = dataModel.getGender().equals("Male")? "M":"F";
        if (gender.equals("M")){
            viewHolder.gender.setText("M");
            viewHolder.gender.setCompoundDrawablesWithIntrinsicBounds((R.drawable.ic_male), 0, 0, 0);
        }else {
            viewHolder.gender.setText("F");
            viewHolder.gender.setCompoundDrawablesWithIntrinsicBounds((R.drawable.ic_female), 0, 0, 0);
        }
        char letterOne = dataModel.getEmpName().charAt(0);
        viewHolder.letter.setText(Character.toString(letterOne));
        viewHolder.letter.setTextColor(MatrialColorPalette.getRandomColor("500"));
        viewHolder.doj.setText("DOJ: "+ dataModel.getDOJ().replace("T00:00:00", ""));

        return convertView;
    }

}
