package com.example.remindo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remindo.R;
import com.example.remindo.database.User;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterCalendar extends RecyclerView.Adapter<AdapterCalendar.CalendarViewHolder> {
    private Context context;
    private ArrayList<User> originalList; // Daftar item asli
    private ArrayList<User> filteredList; // Daftar item yang sudah difilter

    public AdapterCalendar(Context context, ArrayList<User> list) {
        this.context = context;
        this.originalList = new ArrayList<>(list); // Salin daftar item asli
        this.filteredList = new ArrayList<>(list); // Salin daftar item asli untuk daftar yang sudah difilter
    }

    // Fungsi untuk melakukan filter berdasarkan status
    public void filterItemsByStatus(boolean done) {
        filteredList.clear(); // Kosongkan daftar yang sudah difilter

        // Tambahkan item yang sesuai dengan status yang dipilih ke daftar yang sudah difilter
        for (User user : originalList) {
            if (user.isDone() == done) {
                filteredList.add(user);
            }
        }

        notifyDataSetChanged(); // Perbarui tampilan RecyclerView
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        User user = filteredList.get(position); // Gunakan daftar yang sudah difilter

        holder.tv_date.setText(user.getDate());

        // Atur visibilitas item berdasarkan status
        if (user.isDone()) {
            holder.itemView.setVisibility(View.GONE); // Sembunyikan item jika sudah selesai
        } else {
            holder.itemView.setVisibility(View.VISIBLE); // Tampilkan item jika belum selesai
        }

        // Tambahkan listener ke options jika perlu
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder{

        TextView tv_date;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.date);
        }
    }
}
