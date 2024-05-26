package com.example.remindo.adapter;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remindo.History;
import com.example.remindo.R;
import com.example.remindo.database.User;
import com.example.remindo.fragment.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.HistoryViewHolder> {

    private Context context;
    private ArrayList<User> list;


    public AdapterHistory(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_layout, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        User user = list.get(position);

        holder.tv_date.setText(user.getDate());
        holder.tv_desc.setText(user.getDescription());
        holder.tv_priority.setText(user.getStatus());
        holder.tv_task.setText(user.getTask());
        holder.tv_time.setText(user.getTime());
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView tv_task, tv_desc, tv_time, tv_date, tv_priority;
        CardView navigationView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.h_date);
            tv_desc = itemView.findViewById(R.id.h_description);
            tv_priority = itemView.findViewById(R.id.h_priority);
            tv_task = itemView.findViewById(R.id.h_title);
            tv_time = itemView.findViewById(R.id.h_time);
        }
    }

}
