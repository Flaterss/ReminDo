package com.example.remindo;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.remindo.adapter.DataUser;
import com.example.remindo.database.Tasks;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import com.example.remindo.fragment.HomeFragment;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddSchedule extends AppCompatActivity {

    TextView text;
    Button button, dialogDate, dialogTime, insert;

    EditText task, descs, dates, times;

    DatabaseReference Remindo;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private String user;

    Calendar calendar = Calendar.getInstance();
    Context context;

    Locale id = new Locale("id", "ID");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd-MMMM-yyyy", id);
    SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH-mm", id);
    RadioGroup rb_group;
    RadioButton radioButton;
    Date datess, timess;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        context = this;

        button = findViewById(R.id.btnBack);
        insert = findViewById(R.id.btn_insert);

        task = findViewById(R.id.tugas);
        descs = findViewById(R.id.deskripsi);
        dates = findViewById(R.id.tanggal);
        times = findViewById(R.id.jam);

        dialogDate = findViewById(R.id.btn_tanggal);
        dialogTime = findViewById(R.id.btn_jam);

        rb_group = findViewById(R.id.radioGrup);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();


        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            user = currentUser.getEmail();
        } else {
            Intent intent = new Intent(AddSchedule.this, SignIn.class);
            startActivity(intent);
            finish();
        }


        Remindo = FirebaseDatabase.getInstance().getReference().child("Tasks");

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AddSchedule.this, CustomBottomNav.class);
                startActivity(i);

            }
        });

        dialogDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialogDate();

            }
        });

        dialogTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogTime();
            }
        });
    }


    private void inputData() {

    }

    private void saveData() {
        final CollectionReference taskRef = db.collection("Tasks");

        RadioButton radioP = findViewById(R.id.priority);

        String tugas = task.getText().toString();
        String desk = descs.getText().toString();
        String tanggal = dates.getText().toString();
        String jam = times.getText().toString();

        String Jadwal = tanggal + "|" + jam;

        final String status;

        if (radioP.isChecked()) {
            status = "Priority";
        } else {
            status = "Normal";
        }

        if (!tugas.isEmpty() && !desk.isEmpty() && !tanggal.isEmpty() && !jam.isEmpty()) {
            taskRef.document(userId).collection("Jadwal").document(Jadwal).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("task", tugas);
                    data.put("description", desk);
                    data.put("date", tanggal);
                    data.put("time", jam);
                    data.put("status", status);
                    data.put("User", user);

                    taskRef.document(userId).collection("Jadwal").document(Jadwal).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent i = new Intent(AddSchedule.this, CustomBottomNav.class);
                            startActivity(i);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddSchedule.this, "Data Gagal!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Terdapat Kolom yang Belum di Isi", Toast.LENGTH_SHORT).show();
        }
    }


        private void openDialogDate() {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    calendar.set(year, month, day);

                    if (calendar.getTimeInMillis() >= System.currentTimeMillis()) {
                        String timeString = simpleDateFormat.format(calendar.getTime());
                        String formattedDate = timeString.replace("-", " ");
                        dates.setText(formattedDate);
                        datess = calendar.getTime();
                    } else {
                        Toast.makeText(getApplicationContext(), "You cannot select a past date.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

            datePickerDialog.show();
        }



    private  void openDialogTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                String timeString = simpleTimeFormat.format(calendar.getTime());
                String formattedTime = timeString.replace("-", ":");
                times.setText(formattedTime);
                timess = calendar.getTime();

            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

}