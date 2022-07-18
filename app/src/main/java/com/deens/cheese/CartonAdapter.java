package com.deens.cheese;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class CartonAdapter extends ArrayAdapter<CartonClass> {

    private ArrayList<CartonClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView productName;
        TextView assignedQuantityCtn, assignedQuantityPkt;
        EditText pickedQuantityCtn, pickedQuantityPkt;
        RelativeLayout parentView;
        ProgressBar progressBar;
    }

    public CartonAdapter(ArrayList<CartonClass> data, Context context) {
        super(context, R.layout.carton_list_item, data);
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
        CartonClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        CartonAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new CartonAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.carton_list_item, parent, false);
        viewHolder.productName = convertView.findViewById(R.id.productName);
        viewHolder.assignedQuantityCtn = convertView.findViewById(R.id.assignedQuantityCtn);
        viewHolder.assignedQuantityPkt = convertView.findViewById(R.id.assignedQuantityPkt);
        viewHolder.pickedQuantityCtn = convertView.findViewById(R.id.pickedQuantityCtn);
        viewHolder.pickedQuantityPkt = convertView.findViewById(R.id.pickedQuantityPkt);

        viewHolder.parentView = convertView.findViewById(R.id.parentView);
        viewHolder.progressBar = convertView.findViewById(R.id.progressBar);

        convertView.setTag(viewHolder);
        views.put(position, convertView);

        viewHolder.productName.setText(dataModel.getProductName());
        viewHolder.assignedQuantityCtn.setText("Cartons : " + dataModel.getQuantityCtn());
        viewHolder.assignedQuantityPkt.setText("Packets : " + dataModel.getQuantityPkt());
        viewHolder.progressBar.setMax(((dataModel.getQuantityCtn() * 10) + (dataModel.getQuantityPkt())));

        viewHolder.pickedQuantityPkt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int pickedCartonsQuantity = 0;
                    int pickedPacketsQuantity = 0;

                    if (!viewHolder.pickedQuantityPkt.getText().toString().equals("")) {
                        pickedPacketsQuantity = Integer.parseInt(viewHolder.pickedQuantityPkt.getText().toString().trim());
                    }
                    if (!viewHolder.pickedQuantityCtn.getText().toString().equals("")) {
                        pickedCartonsQuantity = Integer.parseInt(viewHolder.pickedQuantityCtn.getText().toString().trim()) * 10;
                    }

                    if ((dataModel.getQuantityPkt() + (dataModel.getQuantityCtn() * 10)) >= (pickedCartonsQuantity + pickedPacketsQuantity)){
                        // Total picked quantity is less than or equal to total assigned quantity

                        // Total Packet number shouldn't be more than assigned
                        if (dataModel.getQuantityPkt() >= Integer.parseInt(viewHolder.pickedQuantityPkt.getText().toString().trim())
                                && Integer.parseInt(viewHolder.pickedQuantityPkt.getText().toString().trim()) >= 0){
                            // Picked Packets numbers are less or equal
                            viewHolder.progressBar.setProgress(pickedCartonsQuantity + pickedPacketsQuantity);
                            dataModel.setPickedQuantityCtn(pickedCartonsQuantity);
                            dataModel.setPickedQuantityPkt(pickedPacketsQuantity);
                        }else {
                            // Picked Packets numbers are more than assigned
                            Toast.makeText(mContext, "Packets numbers are not right", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        // total picked quantity is more than assigned is picked
                        Toast.makeText(mContext, "Can't pick more than assigned", Toast.LENGTH_LONG).show();
                    }

                } catch (NumberFormatException ex) {
                    Log.w("Exception", "--- Invalid Number ---");
                }
            }
        });

        viewHolder.pickedQuantityCtn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int pickedCartonsQuantity = 0;
                    int pickedPacketsQuantity = 0; // Carton has 10 kg

                    if (!viewHolder.pickedQuantityPkt.getText().toString().equals("")) {
                        pickedPacketsQuantity = Integer.parseInt(viewHolder.pickedQuantityPkt.getText().toString().trim());
                    }
                    if (!viewHolder.pickedQuantityCtn.getText().toString().equals("")) {
                        pickedCartonsQuantity = Integer.parseInt(viewHolder.pickedQuantityCtn.getText().toString().trim()) * 10;
                    }

                    if ((dataModel.getQuantityPkt() + (dataModel.getQuantityCtn() * 10)) >= (pickedCartonsQuantity + pickedPacketsQuantity)){
                        // Total picked quantity is less than or equal to total assigned quantity

                        // Total Cartons number shouldn't be more than assigned && greater than zero
                        if (dataModel.getQuantityCtn() >= Integer.parseInt(viewHolder.pickedQuantityCtn.getText().toString().trim()) &&
                        Integer.parseInt(viewHolder.pickedQuantityCtn.getText().toString().trim()) >= 0){
                            // Picked Cartons numbers are less or equal
                            viewHolder.progressBar.setProgress(pickedCartonsQuantity + pickedPacketsQuantity);
                            dataModel.setPickedQuantityCtn(pickedCartonsQuantity);
                            dataModel.setPickedQuantityPkt(pickedPacketsQuantity);
                        }else {
                            // Picked Cartons numbers are more than assigned
                            Toast.makeText(mContext, "Cartons numbers are not right", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        // total picked quantity is more than assigned is picked
                        Toast.makeText(mContext, "Can't pick more than assigned", Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException ex) {

                    Log.w("Exception", "--- Invalid Number ---");
                }
            }
        });

        return convertView;
    }

}
