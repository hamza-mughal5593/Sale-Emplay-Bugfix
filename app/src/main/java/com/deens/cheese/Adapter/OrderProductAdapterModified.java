package com.deens.cheese.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deens.cheese.R;
import com.deens.cheese.SOProductClass;
import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;

public class OrderProductAdapterModified extends RecyclerView.Adapter<OrderProductAdapterModified.MyHolderImage> {

    private ArrayList<SOProductClass> dataSet;
    Context mContext;
    private AdapterCallback mAdapterCallback;


    public OrderProductAdapterModified(ArrayList<SOProductClass> data, Context context, OrderProductAdapterModified.AdapterCallback mAdapterCallback) {
        this.dataSet = data;
        this.mContext = context;
        this.mAdapterCallback = mAdapterCallback;
    }

    @Override
    public MyHolderImage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.order_product_item, parent, false);
        return new MyHolderImage(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolderImage holder, @SuppressLint("RecyclerView") int position) {


        SOProductClass dataModel = dataSet.get(position);


        holder.name.setText(dataModel.getProductName());
        holder.price.setText("Unit Price: " + String.valueOf(dataModel.getProductSalaPrice())
                .replace(".0", "") + "");
        holder.discount.setText("Discount: " + dataModel.getDiscount().intValue() + "");
        holder.companyName.setText(dataModel.getCompanyName());
        holder.unit.setText(" (Unit: " + dataModel.getProductUnit() + ")");


        holder.quantityTitle.setText(dataModel.getQuantity() + "x");

        if (dataModel.getQuantity() != 0){

            holder.totalAmount.setText((dataModel.getQuantity() *
                    (dataModel.getProductSalaPrice().intValue()
                            - dataModel.getOfferAmount())) + " PKR");
        }else
            holder.totalAmount.setText("");


        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.add_data(position, holder.itemView);
            }
        });
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.minus_data(position, holder.itemView);
            }
        });
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public class MyHolderImage extends RecyclerView.ViewHolder {

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


        public MyHolderImage(@NonNull View convertView) {
            super(convertView);


            name = convertView.findViewById(R.id.productName);
            price = convertView.findViewById(R.id.salePrice);
            companyName = convertView.findViewById(R.id.companyName);
            unit = convertView.findViewById(R.id.unit);
            quantity = convertView.findViewById(R.id.quantity);
            minus = convertView.findViewById(R.id.minus);
            offerAmount = convertView.findViewById(R.id.offerAmount);
            quantityTitle = convertView.findViewById(R.id.quantityTitle);
            discount = convertView.findViewById(R.id.discount);
            add = convertView.findViewById(R.id.add);
            offerLabel = convertView.findViewById(R.id.offerLabel);
            totalAmount = convertView.findViewById(R.id.totalAmount);


        }
    }

    public interface AdapterCallback {
        void add_data(int pos, View holder);

        void minus_data(int pos, View holder);
    }
}
