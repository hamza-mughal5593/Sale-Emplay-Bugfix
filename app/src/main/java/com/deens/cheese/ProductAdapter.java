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

public class ProductAdapter extends ArrayAdapter<ProductClass> {

    private ArrayList<ProductClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView price;
        TextView companyName;
        TextView unit;
        TextView offer;
    }

    public ProductAdapter(ArrayList<ProductClass> data, Context context) {
        super(context, R.layout.prodcut_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ProductClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ProductAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ProductAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.prodcut_list_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.name);
            viewHolder.price =  convertView.findViewById(R.id.price);
            viewHolder.companyName =  convertView.findViewById(R.id.companyName);
            viewHolder.unit =  convertView.findViewById(R.id.unit);
            viewHolder.offer = convertView.findViewById(R.id.offer);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ProductAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.price.setText(String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", "") + " PKR");
        viewHolder.companyName.setText(dataModel.getCompanyName());
        viewHolder.unit.setText("Unit: "+ dataModel.getProductUnit());
        viewHolder.offer.setText("Trade Offer: " + String.valueOf(dataModel.getTradeOffer()).replace(".0", " PKR"));

        return convertView;
    }

}
