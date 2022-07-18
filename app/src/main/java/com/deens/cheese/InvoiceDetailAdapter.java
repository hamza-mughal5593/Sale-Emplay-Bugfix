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

public class InvoiceDetailAdapter extends ArrayAdapter<ProductDetailClass> {

    private ArrayList<ProductDetailClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView totalPrice;
        TextView quantity;
        TextView name;
        TextView unitPrice;
        TextView discountDetail;
    }

    public InvoiceDetailAdapter(ArrayList<ProductDetailClass> data, Context context) {
        super(context, R.layout.invoice_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ProductDetailClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        InvoiceDetailAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new InvoiceDetailAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.invoice_list_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.name);
            viewHolder.totalPrice =  convertView.findViewById(R.id.totalPrice);
            viewHolder.quantity =  convertView.findViewById(R.id.quantity);
            viewHolder.unitPrice =  convertView.findViewById(R.id.unitPrice);
            viewHolder.discountDetail = convertView.findViewById(R.id.discountDetail);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (InvoiceDetailAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.totalPrice.setText(String.valueOf(dataModel.getTotalAmount()).replace(".0", "") + " PKR");
        viewHolder.quantity.setText(String.valueOf(dataModel.getQuantity()).replace(".0", "") + " x ");
        viewHolder.unitPrice.setText("(Unit Price: " + String.valueOf(dataModel.getSalePrice()).replace(".0", "") + ")" );
        viewHolder.discountDetail.setText("(Trade Offer: " + String.valueOf(dataModel.getOffer_Amt()).replace(".0", "") +" PKR)");

        return convertView;
    }

}
