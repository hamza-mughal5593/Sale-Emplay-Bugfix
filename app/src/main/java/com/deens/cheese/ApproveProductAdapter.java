package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ApproveProductAdapter extends ArrayAdapter<ApproveProductClass> {

    private ArrayList<ApproveProductClass> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView name;
        TextView salePrice;
        TextView companyName;
        TextView unit;
        EditText price;
        EditText discount;
    }

    public ApproveProductAdapter(ArrayList<ApproveProductClass> data, Context context) {
        super(context, R.layout.approve_product_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ApproveProductClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ApproveProductAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ApproveProductAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.approve_product_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.productName);
            viewHolder.salePrice =  convertView.findViewById(R.id.salePrice);
            viewHolder.companyName =  convertView.findViewById(R.id.companyName);
            viewHolder.unit =  convertView.findViewById(R.id.unit);
            viewHolder.price =  convertView.findViewById(R.id.price);
            viewHolder.discount =  convertView.findViewById(R.id.discount);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ApproveProductAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.salePrice.setText(String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", "") + " PKR");
        viewHolder.companyName.setText(dataModel.getCompanyName());
        viewHolder.unit.setText(" (Unit: "+ dataModel.getProductUnit() + ")");
        viewHolder.price.setText(String.valueOf(dataModel.getProductSalaPrice().intValue()));
        viewHolder.discount.setText(String.valueOf(dataModel.getDiscount().intValue()));

        return convertView;
    }

}
