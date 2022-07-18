package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class POInvoiceDetailAdapter extends ArrayAdapter<POProductDetailClass> {

    private List<POProductDetailClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView totalPrice;
        TextView quantity;
        TextView name;
        TextView unitPrice;
        TextView discountDetail;
        NoboButton add, less, remove, offer;
    }

    public POInvoiceDetailAdapter(List<POProductDetailClass> data, Context context) {
        super(context, R.layout.po_invoice_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        POProductDetailClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        POInvoiceDetailAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new POInvoiceDetailAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.po_invoice_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.name);
            viewHolder.totalPrice =  convertView.findViewById(R.id.totalPrice);
            viewHolder.quantity =  convertView.findViewById(R.id.quantity);
            viewHolder.unitPrice =  convertView.findViewById(R.id.unitPrice);
            viewHolder.discountDetail = convertView.findViewById(R.id.discountDetail);
            viewHolder.add = convertView.findViewById(R.id.add);
            viewHolder.less = convertView.findViewById(R.id.less);
            viewHolder.remove = convertView.findViewById(R.id.remove);
            viewHolder.offer = convertView.findViewById(R.id.offer);

            viewHolder.add.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
            viewHolder.less.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
            viewHolder.remove.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
            viewHolder.offer.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (POInvoiceDetailAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.totalPrice.setText(String.valueOf(dataModel.getTotalAmount()).replace(".0", "") + " PKR");
        viewHolder.quantity.setText(String.valueOf(dataModel.getQuantity()).replace(".0", "") + " x ");
        viewHolder.unitPrice.setText("Unit Price: " + String.valueOf(dataModel.getSalePrice()).replace(".0", ""));

        return convertView;
    }

}
