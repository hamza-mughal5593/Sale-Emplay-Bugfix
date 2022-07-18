package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class CartAdapter extends ArrayAdapter<Cart_list_item> {

    private ArrayList<Cart_list_item> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView salePrice;
        TextView quantity;
        TextView tradeOffer;
        TextView offerAmount;
        TextView total;
        RelativeLayout parentView;
    }

    public CartAdapter(ArrayList<Cart_list_item> data, Context context) {
        super(context, R.layout.cart_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Cart_list_item dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        CartAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new CartAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.cart_list_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.productName);
            viewHolder.salePrice =  convertView.findViewById(R.id.salePrice);
            viewHolder.tradeOffer =  convertView.findViewById(R.id.tradeOffer);
            viewHolder.offerAmount =  convertView.findViewById(R.id.offerAmount);
            viewHolder.quantity = convertView.findViewById(R.id.quantity);
            viewHolder.total = convertView.findViewById(R.id.total);
            viewHolder.parentView = convertView.findViewById(R.id.parentView);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CartAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getName());
        viewHolder.salePrice.setText(dataModel.getSalePrice());
        viewHolder.quantity.setText(dataModel.getQuantity());
        viewHolder.offerAmount.setText(dataModel.getOfferAmount());
        viewHolder.tradeOffer.setText(dataModel.getTradeOffer());
        viewHolder.total.setText(dataModel.getTotal());

        if (position % 2 == 0){
            viewHolder.parentView.setBackgroundColor(mContext.getResources().getColor(R.color.off_white));
        }

        return convertView;
    }

}
