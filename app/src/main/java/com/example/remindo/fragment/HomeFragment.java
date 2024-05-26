package com.example.remindo.fragment;

import static androidx.browser.customtabs.CustomTabsClient.getPackageName;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remindo.AddSchedule;
import com.example.remindo.CheckListPage;
import com.example.remindo.CustomBottomNav;
import com.example.remindo.R;
import com.example.remindo.adapter.AdapterItem;
import com.example.remindo.service.MyBroadcastReceiver;
import com.example.remindo.database.User;
import com.example.remindo.worker.MoveTaskToDoneWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String CHANNEL_ID = "ReminDoChannel";
    private static final int NOTIFICATION_ID = 1;

    private Button btnAdd;
    private CustomBottomNav customBottomNav;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private String userId;

    private LinearLayout taskGone;
    private RecyclerView recyclerView;
    private AdapterItem adapterItem;
    private ArrayList<User> list;
    private ArrayList<User> filteredList;
    private Context context;
    ImageView taskDone;

    private boolean isDataFetched = false;
    private CheckBox priorityCheckBox;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnAdd = view.findViewById(R.id.btn_add);
        customBottomNav = (CustomBottomNav) getActivity();
        recyclerView = view.findViewById(R.id.recycleView);
        taskGone = view.findViewById(R.id.taskGone);
        priorityCheckBox = view.findViewById(R.id.priority);
        taskDone = view.findViewById(R.id.taskDone);

        taskDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CheckListPage.class);
                startActivity(i);

            }
        });

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        context = getContext();
        userId = firebaseAuth.getCurrentUser().getUid();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapterItem = new AdapterItem(getContext(), filteredList);
        recyclerView.setAdapter(adapterItem);

        fetchDataFromFirestore();

        priorityCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> filterData());

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AddSchedule.class);
            startActivity(i);
        });

        updateDateTime(view);
        checkAndMoveCompletedTasks();

        return view;
    }

    private void fetchDataFromFirestore() {
        Log.d(TAG, "Fetching data from Firestore...");
        db.collection("Tasks")
                .document(userId)
                .collection("Jadwal")
                .orderBy("date") // Urutkan data berdasarkan tanggal
                .orderBy("time") // Kemudian urutkan berdasarkan jam
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for data: ", error);
                        Toast.makeText(context, "Gagal menampilkan data", Toast.LENGTH_SHORT).show();
                        taskGone.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE); // Tambahkan ini untuk mengatur visibilitas RecyclerView menjadi GONE
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "Data retrieval Successful");

                        list.clear(); // Bersihkan list sebelum menambahkan data baru
                        for (QueryDocumentSnapshot document : value) {
                            // Ambil nilai string dari Firestore
                            String dateString = document.getString("date");
                            String timeString = document.getString("time");

                            // Parse tanggal dan waktu menjadi objek Date
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("id", "ID"));
                            Date date = null;
                            Date time = null;
                            try {
                                if (dateString != null && timeString != null) {
                                    // Parse tanggal dan waktu menjadi objek Date
                                    date = dateFormat.parse(dateString);
                                    time = timeFormat.parse(timeString);
                                } else {
                                    // Handle null values, e.g., provide default values or skip parsing
                                    continue; // Skip this iteration of the loop
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing date/time: " + e.getMessage());
                                // Handle parsing error, e.g., provide default values or skip parsing
                                continue; // Skip this iteration of the loop
                            }

                            // Gabungkan tanggal dan waktu menjadi satu objek Date
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
                            calendar.set(Calendar.MINUTE, time.getMinutes());
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date dateTime = calendar.getTime();

                            // Buat objek User dan tambahkan ke list
                            User user = document.toObject(User.class);
                            user.setTimestamp(new Timestamp(dateTime.getTime()));
                            list.add(user);
                            Log.d(TAG, "Task added: " + user.toString());
                        }

                        // Urutkan list berdasarkan timestamp
                        Collections.sort(list, (user1, user2) -> user1.getTimestamp().compareTo(user2.getTimestamp()));

                        // Setelah mendapatkan data baru, panggil metode filterData() untuk memperbarui tampilan
                        filterData();
                        Log.d(TAG, "RecyclerView updated with new data");
                        recyclerView.setVisibility(View.VISIBLE);
                        taskGone.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "Data is empty");
                        taskGone.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        priorityCheckBox.setEnabled(false);

                    }
                });
    }

    private void refreshPage() {
        if (!isDataFetched) {
            fetchDataFromFirestore();
            isDataFetched = true;

        }
    }

    private void filterData() {
        filteredList.clear();
        boolean isPriorityChecked = priorityCheckBox.isChecked();
        final boolean[] hasPriorityData = {false};

        db.collection("Tasks")
                .document(userId)
                .collection("Jadwal")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String status = document.getString("status");
                            if ("priority".equalsIgnoreCase(status)) {
                                hasPriorityData[0] = true;
                                break;
                            }
                        }

                        priorityCheckBox.setEnabled(hasPriorityData[0]);

                        for (User user : list) {
                            if (isPriorityChecked && "priority".equalsIgnoreCase(user.getStatus())) {
                                filteredList.add(user);
                            } else if (!isPriorityChecked) {
                                filteredList.add(user);
                            }
                        }

                        adapterItem.notifyDataSetChanged();

                        if (filteredList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            taskGone.setVisibility(View.VISIBLE);

                            priorityCheckBox.setEnabled(false);
                            fetchDataFromFirestore();
                        } else {
                            taskGone.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }







    private void updateDateTime(View view) {
        final Activity activity = getActivity();
        Thread t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000);
                    activity.runOnUiThread(() -> {
                        TextView tDate = view.findViewById(R.id.date_now);
                        TextView tTime = view.findViewById(R.id.time_now);
                        long date = System.currentTimeMillis();
                        long time = System.currentTimeMillis();
                        Locale localeID = new Locale("en", "ID");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", localeID);
                        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", localeID);
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Makassar")); // WITA timezone
                        simpleTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Makassar")); // WITA timezone
                        String dateString = simpleDateFormat.format(date);
                        String timeString = simpleTimeFormat.format(time);

                        tDate.setText(dateString);
                        tTime.setText(timeString);
                    });
                }
            } catch (InterruptedException e) {
                // Handle thread interruption here
            }
        });

        t.start();
    }

    private void checkAndMoveCompletedTasks() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && isAdded()) { // Pastikan fragment sudah terlampir ke aktivitas sebelum memanggil getActivity()
                try {
                    Thread.sleep(1000); // Check every minute
                    if (getActivity() != null) { // Periksa apakah getActivity() tidak null sebelum digunakan
                        getActivity().runOnUiThread(() -> { // Menggunakan getActivity() untuk mendapatkan aktivitas yang terlampir
                            long currentTimeMillis = System.currentTimeMillis();
                            Locale localeID = new Locale("id", "ID");
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", localeID);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", localeID);

                            String currentDate = dateFormat.format(new Date(currentTimeMillis));
                            String currentTime = timeFormat.format(new Date(currentTimeMillis));

                            for (User user : new ArrayList<>(list)) {
                                try {
                                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", localeID);

                                    String taskDateTimeString = user.getDate() + " " + user.getTime();
                                    Date taskDateTime = dateTimeFormat.parse(taskDateTimeString);
                                    Date currentDateTime = new Date(currentTimeMillis);

                                    if (currentDateTime.after(taskDateTime)) {
                                        moveTaskToDone(user);
                                        Log.d(TAG, "Tugas telah Selesai" + user.toString());
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error parsing date/time: ", e);
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread interrupted", e);
                }
            }
        }).start();
    }


    private void moveTaskToDone(User user) {
        Data inputData = new Data.Builder()
                .putString("date", user.getDate())
                .putString("time", user.getTime())
                .putString("title", user.getTask())
                .build();

        OneTimeWorkRequest moveTaskRequest = new OneTimeWorkRequest.Builder(MoveTaskToDoneWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(moveTaskRequest);

        list.remove(user);
        AdapterItem.updateTaskList(list);

        // Memanggil fetchDataFromFirestore() setelah penundaan 5 detik
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchDataFromFirestore();
            }
        }, 3000); // 5000 milliseconds = 5 detik
    }


    /**private void moveTaskToDone(User user) {
        String date = user.getDate();
        String time = user.getTime();
        String title = user.getTask();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        String userId = fAuth.getCurrentUser().getUid();
        String sourcePath = "Tasks/" + userId + "/Jadwal/" + date + "|" + time;
        String destinationPath = "Done/" + userId + "/Jadwal/" + date + "|" + time;

        DocumentReference SourceDocRef = db.document(sourcePath);
        SourceDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                DocumentReference DestinationDocRef = db.document(destinationPath);
                DestinationDocRef.set(documentSnapshot.getData()).addOnSuccessListener(unused -> {
                    SourceDocRef.delete().addOnSuccessListener(unused1 -> {
                        list.remove(user);
                        filterData();
                        Log.d(TAG, "Task moved to Done: " + user.toString());
                        startNotificationService(title, time);
                    }).addOnFailureListener(e -> Log.e(TAG, "Error deleting task: ", e));
                }).addOnFailureListener(e -> Toast.makeText(context, "Your Data Failed Mark as Completed!", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching task: ", e));
    }**/

    /**private void startNotificationService(String title, String time) {
        Intent serviceIntent = new Intent(getActivity(), MyBroadcastReceiver.class);
        serviceIntent.putExtra("notification_title", title);
        serviceIntent.putExtra("notification_time", time);
        getActivity().startService(serviceIntent);
    }**/

    private void startNotificationService(String title, String time) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "ReminDo!";
            String description = "You Have Task To Do!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel  = new NotificationChannel("ReminDo!", name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(requireContext(), MyBroadcastReceiver.class);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_time", time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        context.sendBroadcast(intent);

    }




}
