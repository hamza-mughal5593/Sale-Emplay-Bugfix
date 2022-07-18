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

public class OrderAdapter extends ArrayAdapter<OrderClass> {

    private ArrayList<OrderClass> dataSet;
    Context mContext;
    Activity activity;
    private int lastPosition = -1;
    Boolean isUnPost, isUnAssign, isDeliver;

    // View lookup cache
    private static class ViewHolder {
        RelativeLayout topView;
        TextView customerName;
        TextView amount;
        RelativeLayout discountView;
        TextView tradeOffer;
        TextView discountAmount;
        TextView soName;
        ImageView checkmark;
        RelativeLayout orderView;
        EditText order;
        NoboButton delete, print, finish;
    }

    public OrderAdapter(ArrayList<OrderClass> data, Context context,
                        Boolean isUnPost, Boolean isUnAssign, Boolean isDeliver, Activity activity) {
        super(context, R.layout.order_list_item, data);
        this.dataSet = data;
        this.mContext = context;
        this.isUnPost = isUnPost;
        this.isUnAssign = isUnAssign;
        this.isDeliver = isDeliver;
        this.activity = activity;
    }

    Map<Integer, View> views = new HashMap<Integer, View>();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (views.containsKey(position)) {
            return views.get(position);
        }

        // Get the data item for this position
        OrderClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        OrderAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new OrderAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v1 = inflater.inflate(R.layout.order_list_item, parent, false);
        viewHolder.topView = v1.findViewById(R.id.topView);
        viewHolder.customerName = v1.findViewById(R.id.customerName);
        viewHolder.amount = v1.findViewById(R.id.amount);
        viewHolder.discountView = v1.findViewById(R.id.discountView);
        viewHolder.tradeOffer = v1.findViewById(R.id.tradeOffer);
        viewHolder.discountAmount = v1.findViewById(R.id.discountAmount);
        viewHolder.soName = v1.findViewById(R.id.saleOfficerName);
        viewHolder.checkmark = v1.findViewById(R.id.checkmark);
        viewHolder.orderView = v1.findViewById(R.id.sortView);
        viewHolder.order = v1.findViewById(R.id.order);
        viewHolder.delete = v1.findViewById(R.id.delete);
        viewHolder.print = v1.findViewById(R.id.showBill);
        viewHolder.finish = v1.findViewById(R.id.finish);
        viewHolder.delete.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.topView.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.soName.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.discountView.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
        viewHolder.finish.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

        v1.setTag(viewHolder);
        views.put(position, v1);

        viewHolder.customerName.setText(dataModel.getCusCustomerName());
        viewHolder.amount.setText(dataModel.getINV_GrandTotal() + " PKR");
        viewHolder.discountAmount.setText("Discount: " + dataModel.getINV_DiscountAmount() + " PKR");
        int tradeOffer = dataModel.getINV_Discount_Rate();
        viewHolder.tradeOffer.setText("Trade Offer: " + (tradeOffer) + " PKR");
        viewHolder.soName.setText("Sale Officer: " + (dataModel.getSO_Name()));

        if (tradeOffer == 0) {
            viewHolder.tradeOffer.setBackground(mContext.getResources().getDrawable(R.drawable.trade_bg_empty));
            viewHolder.tradeOffer.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        if (dataModel.getINV_Discount_Rate() == 0) {
            viewHolder.discountAmount.setBackground(mContext.getResources().getDrawable(R.drawable.discount_bg_empty));
            viewHolder.discountAmount.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        if (isUnPost) {
            viewHolder.delete.setVisibility(View.VISIBLE);
        }

        if (isUnAssign) {
            viewHolder.orderView.setVisibility(View.VISIBLE);
        }

        if (isDeliver) {
            viewHolder.orderView.setVisibility(View.GONE);
            viewHolder.finish.setVisibility(View.VISIBLE);
        }

        viewHolder.checkmark.setOnClickListener(view -> {
            OrderFragment.hideSoftKeyboard(activity);
            if (viewHolder.checkmark.getTag().equals("0")) {
                viewHolder.checkmark.setImageResource(R.drawable.ic_check_green);
                viewHolder.checkmark.setTag("1");
                viewHolder.order.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkmark.setImageResource(R.drawable.ic_check_disabled);
                viewHolder.checkmark.setTag("0");
                viewHolder.order.setVisibility(View.INVISIBLE);
                viewHolder.order.setText("");
            }
        });

        viewHolder.print.setOnClickListener(view -> {
                String request = GlobalVariable.REPORT_URL + "Reports/GetInvoicePrint?InvoiceID="+dataModel.getINV_InvoiceID()
                        +"&&AuthKey=8156sdcas1dcc1d8c4894Coiuj784C8941e856";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request));
                mContext.startActivity(browserIntent);
        });

        return v1;
    }


}
