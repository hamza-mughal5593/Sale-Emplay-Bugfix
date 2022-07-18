package com.deens.cheese;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class SOProductAdapter extends ArrayAdapter<SOProductClass> {

    private ArrayList<SOProductClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView price;
        TextView companyName;
        TextView unit;
        TextView discount;
        NoboButton add;
        EditText quantity;
    }

    public SOProductAdapter(ArrayList<SOProductClass> data, Context context) {
        super(context, R.layout.so_product_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SOProductClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        SOProductAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new SOProductAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.so_product_item, parent, false);
            viewHolder.name =  convertView.findViewById(R.id.productName);
            viewHolder.discount =  convertView.findViewById(R.id.discount);
            viewHolder.price =  convertView.findViewById(R.id.salePrice);
            viewHolder.companyName =  convertView.findViewById(R.id.companyName);
            viewHolder.unit =  convertView.findViewById(R.id.unit);
            viewHolder.quantity =  convertView.findViewById(R.id.quantity);
            viewHolder.add =  convertView.findViewById(R.id.add);
            viewHolder.add.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SOProductAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.name.setText(dataModel.getProductName());
        viewHolder.price.setText(String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", "") + " PKR");
        viewHolder.companyName.setText(dataModel.getCompanyName());
        viewHolder.discount.setText("(Discount: "+ dataModel.getDiscount().intValue() + ")");
        viewHolder.unit.setText("(Unit: "+ dataModel.getProductUnit() + ")");
        viewHolder.quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (!viewHolder.quantity.getText().toString().trim().equals("")){
                        dataModel.setQuantity(Integer.parseInt(viewHolder.quantity.getText().toString().trim()));
                    }
                } catch (NumberFormatException ex){
                    Log.w("Number Exception: " , ex.toString());
                }
            }
        });

        return convertView;
    }

}
