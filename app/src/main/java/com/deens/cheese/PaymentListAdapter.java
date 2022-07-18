package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ornach.nobobutton.NoboButton;

import java.util.ArrayList;

/**
 * Created by munirahmad on 30/01/2017.
 */

public class PaymentListAdapter extends ArrayAdapter<PaymentListClass> {

    private ArrayList<PaymentListClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView amount;
        TextView bankName;
        TextView chequeNo;
        TextView paymentDate;
        TextView chequeDate;
        NoboButton delete, post, update;
        RelativeLayout chequePaymentView;
    }

    public PaymentListAdapter(ArrayList<PaymentListClass> data, Context context) {
        super(context, R.layout.cash_list_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PaymentListClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        PaymentListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new PaymentListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.cash_list_item, parent, false);
            viewHolder.icon =  convertView.findViewById(R.id.icon);
            viewHolder.amount =  convertView.findViewById(R.id.amount);
            viewHolder.bankName =  convertView.findViewById(R.id.bankName);
            viewHolder.chequeNo =  convertView.findViewById(R.id.chequeNo);
            viewHolder.chequeDate =  convertView.findViewById(R.id.chequeDate);
            viewHolder.paymentDate =  convertView.findViewById(R.id.paymentDate);
            viewHolder.delete =  convertView.findViewById(R.id.delete);
            viewHolder.update =  convertView.findViewById(R.id.update);
            viewHolder.post =  convertView.findViewById(R.id.post);
            viewHolder.chequePaymentView =  convertView.findViewById(R.id.chequeView);

            viewHolder.delete.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
            viewHolder.post.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));
            viewHolder.update.setOnClickListener(v -> ((ListView) parent).performItemClick(v, position, 0));

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PaymentListAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.amount.setText((dataModel.getAmount() + " PKR"));
        viewHolder.paymentDate.setText("Payment Date: " + dataModel.getPaymentDate());
        if (dataModel.getPaymentType().equals("Bank")){
            viewHolder.icon.setImageResource(R.drawable.ic_bank);
            viewHolder.bankName.setText(dataModel.getBankName());
        }else {
            viewHolder.icon.setImageResource(R.drawable.ic_cash);
            viewHolder.bankName.setText("Cash Received");
        }
        if (dataModel.getPaymentMode().equals("cheque")){
            viewHolder.chequePaymentView.setVisibility(View.VISIBLE);
            viewHolder.chequeDate.setText("(Cheque Date: "+ dataModel.getChequeDate() + ")");
            viewHolder.chequeNo.setText("(Cheque No: " + dataModel.getChequeNo() + ")");
        }else {
            viewHolder.chequePaymentView.setVisibility(View.GONE);
        }

        return convertView;
    }

}
