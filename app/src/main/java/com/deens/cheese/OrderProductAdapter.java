package com.deens.cheese;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class OrderProductAdapter extends ArrayAdapter<SOProductClass> {

    private ArrayList<SOProductClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView price;
        TextView companyName;
        TextView quantityTitle;
        TextView unit;
        TextView offerLabel;
        TextView totalAmount;
        TextView discount;
        NoboButton add, minus;
        EditText quantity, offerAmount;
    }

    public OrderProductAdapter(ArrayList<SOProductClass> data, Context context) {
        super(context, R.layout.order_product_item, data);
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
        SOProductClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        OrderProductAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        viewHolder = new OrderProductAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.order_product_item, parent, false);
        viewHolder.name = convertView.findViewById(R.id.productName);
        viewHolder.price = convertView.findViewById(R.id.salePrice);
        viewHolder.companyName = convertView.findViewById(R.id.companyName);
        viewHolder.unit = convertView.findViewById(R.id.unit);
        viewHolder.quantity = convertView.findViewById(R.id.quantity);
        viewHolder.minus = convertView.findViewById(R.id.minus);
        viewHolder.offerAmount = convertView.findViewById(R.id.offerAmount);
        viewHolder.quantityTitle = convertView.findViewById(R.id.quantityTitle);
        viewHolder.discount = convertView.findViewById(R.id.discount);
        viewHolder.add = convertView.findViewById(R.id.add);
        viewHolder.offerLabel = convertView.findViewById(R.id.offerLabel);
        viewHolder.totalAmount = convertView.findViewById(R.id.totalAmount);
        viewHolder.add.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.minus.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

        convertView.setTag(viewHolder);
        views.put(position, convertView);

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.price.setText("Unit Price: " + String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", "") + "");
        viewHolder.discount.setText("Discount: " + dataModel.getDiscount().intValue() + "");
        viewHolder.companyName.setText(dataModel.getCompanyName());
        viewHolder.unit.setText(" (Unit: " + dataModel.getProductUnit() + ")");

        return convertView;
    }

}
