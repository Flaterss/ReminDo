package com.example.remindo.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.remindo.service.MyBroadcastReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoveTaskToDoneWorker extends Worker {

    public MoveTaskToDoneWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve input data
        String date = getInputData().getString("date");
        String time = getInputData().getString("time");
        String title = getInputData().getString("title");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String sourcePath = "Tasks/" + userId + "/Jadwal/" + date + "|" + time;
        String destinationPath = "Done/" + userId + "/Jadwal/" + date + "|" + time;

        DocumentReference SourceDocRef = db.document(sourcePath);
        SourceDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                DocumentReference DestinationDocRef = db.document(destinationPath);
                DestinationDocRef.set(documentSnapshot.getData()).addOnSuccessListener(unused -> {
                    SourceDocRef.delete().addOnSuccessListener(unused1 -> {
                        startNotificationService(title, time); // Start notification service
                    }).addOnFailureListener(e -> {
                        // Handle error deleting task
                    });
                }).addOnFailureListener(e -> {
                    // Handle error setting destination doc
                });
            }
        }).addOnFailureListener(e -> {
            // Handle error fetching source doc
        });

        return Result.success();
    }

    private void startNotificationService(String title, String time) {
        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReminDo!";
            String description = "You Have Task To Do!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("ReminDo!", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MyBroadcastReceiver.class);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_time", time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        context.sendBroadcast(intent);
    }
}
