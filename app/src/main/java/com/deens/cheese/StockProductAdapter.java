package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class StockProductAdapter extends ArrayAdapter<StockItem> {

    private ArrayList<StockItem> dataSet;
    Context mContext;
    private int lastPosition = -1;
    Boolean fromStockRecord = false;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView companyName;
        TextView unit;
        EditText quantity, costPrice, price, issuedStock;
        LinearLayout issuedStockView;
        TextView stockDate;
    }

    public StockProductAdapter(ArrayList<StockItem> data, Context context, Boolean isStockRecord) {
        super(context, R.layout.stock_item, data);
        this.dataSet = data;
        this.mContext=context;
        this.fromStockRecord = isStockRecord;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StockItem dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        StockProductAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new StockProductAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.stock_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.productName);
            viewHolder.price =  convertView.findViewById(R.id.salePrice);
            viewHolder.companyName =  convertView.findViewById(R.id.companyName);
            viewHolder.unit =  convertView.findViewById(R.id.unit);
            viewHolder.issuedStockView =  convertView.findViewById(R.id.issuedStockView);
            viewHolder.quantity =  convertView.findViewById(R.id.quantity);
            viewHolder.costPrice =  convertView.findViewById(R.id.costPrice);
            viewHolder.issuedStock =  convertView.findViewById(R.id.issuedStock);
            viewHolder.stockDate = convertView.findViewById(R.id.stockDate);
            viewHolder.stockDate.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StockProductAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.issuedStockView.setVisibility(View.GONE);

        if (fromStockRecord){
            viewHolder.issuedStockView.setVisibility(View.VISIBLE);
            viewHolder.quantity.setText(String.valueOf(dataModel.getQuantity()));
        }
        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.price.setText(String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", ""));
        viewHolder.companyName.setText(dataModel.getCompanyName());
        viewHolder.unit.setText(" (Unit: "+ "Carton" + ")");
        viewHolder.stockDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        return convertView;
    }

}
