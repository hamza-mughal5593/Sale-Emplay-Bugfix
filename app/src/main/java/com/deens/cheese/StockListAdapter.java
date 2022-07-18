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

public class StockListAdapter extends ArrayAdapter<StockListItem> {

    private ArrayList<StockListItem> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView price;
        TextView companyName;
        TextView unit;
        TextView quantity, costPrice;
        TextView stockDate;
    }

    public StockListAdapter(ArrayList<StockListItem> data, Context context) {
        super(context, R.layout.spinner_stock_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StockListItem dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        StockListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new StockListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.spinner_stock_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.productName);
            viewHolder.price =  convertView.findViewById(R.id.salePrice);
            viewHolder.companyName =  convertView.findViewById(R.id.companyName);
            viewHolder.unit =  convertView.findViewById(R.id.unit);
            viewHolder.quantity =  convertView.findViewById(R.id.quantity);
            viewHolder.costPrice =  convertView.findViewById(R.id.costPrice);
            viewHolder.stockDate = convertView.findViewById(R.id.stockDate);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StockListAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.price.setText("Sale Price: "  + String.valueOf(dataModel.getSalePrice())
                .replace(".0", "") + " PKR");
        viewHolder.unit.setText(" (Unit: "+ dataModel.getStockType() + ")");
        viewHolder.stockDate.setText(dataModel.getStockDate().replace("T00:00:00", ""));
        viewHolder.quantity.setText(String.valueOf(dataModel.getQuantity()));
        viewHolder.costPrice.setText(dataModel.getProductCost() + " PKR");

        return convertView;
    }

}
