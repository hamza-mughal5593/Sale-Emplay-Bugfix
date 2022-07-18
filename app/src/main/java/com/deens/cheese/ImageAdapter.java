package com.deens.cheese;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageAdapter extends ArrayAdapter<ImageClass> {

    private ArrayList<ImageClass> dataSet;
    Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView imageName;
        ImageView placeHolder;
    }

    public ImageAdapter(ArrayList<ImageClass> data, Context context) {
        super(context, R.layout.view_image_item, data);
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
        ImageClass dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ImageAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new ImageAdapter.ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.view_image_item, parent, false);
        viewHolder.imageName = convertView.findViewById(R.id.title);
        viewHolder.placeHolder = convertView.findViewById(R.id.placeHolder);

        convertView.setTag(viewHolder);
        views.put(position, convertView);

        viewHolder.imageName.setText(dataModel.getImageTime().replace("T", " "));
        Picasso.get().load(dataModel.getImageURL()).into(viewHolder.placeHolder);

        return convertView;
    }

}
