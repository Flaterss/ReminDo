package com.example.remindo.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remindo.R;
import com.example.remindo.adapter.AdapterHistory;
import com.example.remindo.database.User;
import com.example.remindo.style.EventDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private AdapterHistory adapterHistory;
    private ArrayList<User> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recycleView);

        list = new ArrayList<>();
        adapterHistory = new AdapterHistory(getContext(), list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapterHistory);

        markDatesWithSchedule(true);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(date.getDate());
            long selectedDateInMillis = selectedDate.getTimeInMillis();

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            String selectedDateString = dateFormat.format(selectedDate.getTime());

            getUserListFromDataSource(selectedDateString);

            checkScheduleForSelectedDate(selectedDateInMillis);
        });

        return view;
    }

    private void getUserListFromDataSource(String selectedDateString) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();

        CollectionReference tasksCollectionRef = db.collection("Tasks").document(userId).collection("Jadwal");

        tasksCollectionRef.whereEqualTo("date", selectedDateString)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            newList.add(user);
                        }

                        list.clear();
                        list.addAll(newList);

                        adapterHistory.notifyDataSetChanged();
                        Log.d(TAG, "RecyclerView updated with new data");

                        if (list.isEmpty()) {

                        } else {

                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


    private void checkScheduleForSelectedDate(long selectedDateInMillis) {
        Locale localeID = new Locale("id", "ID");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", localeID);
        String selectedDateString = dateFormat.format(new Date(selectedDateInMillis));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();
        CollectionReference scheduleCollectionRef = db.collection("Tasks").document(userId).collection("Jadwal");

        scheduleCollectionRef.whereEqualTo("date", selectedDateString)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<CalendarDay> scheduleDates = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateString = document.getString("date");

                            CalendarDay calendarDay = CalendarDay.from(parseDate(dateString));
                            scheduleDates.add(calendarDay);
                        }

                    } else {

                        Log.e(TAG, "Error checking schedule for selected date", task.getException());
                    }
                });
    }

    private Date parseDate(String dateString) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id","ID"));
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void markDatesWithSchedule(boolean hasSchedule) {
        if (hasSchedule) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth fAuth = FirebaseAuth.getInstance();
            String userId = fAuth.getCurrentUser().getUid();
            CollectionReference scheduleCollectionRef = db.collection("Tasks").document(userId).collection("Jadwal");

            scheduleCollectionRef.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error);
                    return;
                }

                ArrayList<CalendarDay> scheduleDates = new ArrayList<>();
                for (QueryDocumentSnapshot document : value) {

                    String dateString = document.getString("date");

                    CalendarDay calendarDay = CalendarDay.from(parseDate(dateString));
                    scheduleDates.add(calendarDay);
                }

                calendarView.removeDecorators();

                Context context = getContext();
                if (context != null) {
                    int cyanColor = ContextCompat.getColor(context, R.color.cyan);
                    calendarView.addDecorator(new EventDecorator(scheduleDates, cyanColor));
                } else {

                }
            });
        }
    }



}
