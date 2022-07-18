package com.deens.cheese;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deens.cheese.ui.order.OrderFragment;
import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class POOrderAdapter extends ArrayAdapter<POInvoiceClass> {

    private ArrayList<POInvoiceClass> dataSet;
    Context mContext;
    Activity activity;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        RelativeLayout topView;
        TextView customerName;
        TextView subtotal;
        TextView areaName;
        RelativeLayout discountView;
        TextView date;
        NoboButton delete, post;
    }

    public POOrderAdapter(ArrayList<POInvoiceClass> data, Context context) {
        super(context, R.layout.po_list_item, data);
        this.dataSet = data;
        this.mContext = context;
        this.activity = activity;
    }

    Map<Integer, View> views = new HashMap<Integer, View>();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (views.containsKey(position)) {
            return views.get(position);
        }

        // Get the data item for this position
        POInvoiceClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        POOrderAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new POOrderAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v1 = inflater.inflate(R.layout.po_list_item, parent, false);
        viewHolder.topView = v1.findViewById(R.id.topView);
        viewHolder.customerName = v1.findViewById(R.id.customerName);
        viewHolder.discountView = v1.findViewById(R.id.discountView);
        viewHolder.delete = v1.findViewById(R.id.delete);
        viewHolder.subtotal = v1.findViewById(R.id.subtotal);
        viewHolder.date = v1.findViewById(R.id.date);
        viewHolder.post = v1.findViewById(R.id.post);
        viewHolder.areaName = v1.findViewById(R.id.areaName);
        viewHolder.delete.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.topView.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.discountView.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.post.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.areaName.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.date.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

        v1.setTag(viewHolder);
        views.put(position, v1);

        viewHolder.customerName.setText(dataModel.getCusCustomerName());
        viewHolder.subtotal.setText((dataModel.getTotalAmt()) + " PKR");
        viewHolder.areaName.setText(dataModel.getCusAddress() + ", " + dataModel.getCusArea());
        viewHolder.date.setText("Date: " + dataModel.getPODate().replace("T00:00:00", ""));

        return v1;
    }


}
