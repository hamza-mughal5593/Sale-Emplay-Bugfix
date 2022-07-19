package com.deens.cheese.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deens.cheese.R;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    // creating a variable for array list and context.
    private ArrayList<String> courseModalArrayList;
    private Context context;
    private AdapterCallback mAdapterCallback;

    // creating a constructor for our variables.
    public CourseAdapter(ArrayList<String> courseModalArrayList, Context context, AdapterCallback mAdapterCallback) {
        this.courseModalArrayList = courseModalArrayList;
        this.context = context;
        this.mAdapterCallback = mAdapterCallback;
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<String> filterllist) {
        // below line is to add our filtered
        // list in our course array list.
        courseModalArrayList = filterllist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // below line is to inflate our layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // setting data to our views of recycler view.
        String modal = courseModalArrayList.get(position);
        holder.courseDescTV.setText(modal);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.add_data(position,modal);
            }
        });

    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return courseModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our views.
        private TextView  courseDescTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our views with their ids.
            courseDescTV = itemView.findViewById(R.id.idTVCourseDescription);
        }
    }

    public interface AdapterCallback {
        void add_data(int pos, String holder);
    }
}
