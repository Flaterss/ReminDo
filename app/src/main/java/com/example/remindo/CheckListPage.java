package com.example.remindo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.remindo.adapter.AdapterDone;
import com.example.remindo.adapter.AdapterItem;
import com.example.remindo.database.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class CheckListPage extends AppCompatActivity {

    ImageView back;

    AdapterDone adapterDone;
    ArrayList<User> list;
    String userId;
    FloatingActionButton delete;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Done");

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    Dialog dialog;

    LinearLayout taskDone;
    Button btnDialogCancel, btnDialogDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list_page);

        recyclerView = findViewById(R.id.recycleView);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        delete = findViewById(R.id.delete_item);

        taskDone = findViewById(R.id.taskDone);

        database = FirebaseDatabase.getInstance().getReference("Done");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapterDone = new AdapterDone(this, list);
        recyclerView.setAdapter(adapterDone);

        userId = firebaseAuth.getCurrentUser().getUid();

        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CheckListPage.this, CustomBottomNav.class);
                startActivity(i);
                finish();
            }
        });

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(true);

        btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        btnDialogDelete = dialog.findViewById(R.id.btnDialogDelete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourcePath = "Done/" + userId + "/Jadwal";
                String destinationPath = "History/" + userId + "/Jadwal";

                // Membuat referensi koleksi dari jalur sumber dan tujuan
                CollectionReference sourceColRef = db.collection(sourcePath);
                CollectionReference destinationColRef = db.collection(destinationPath);

                // Mendapatkan semua dokumen dari koleksi sumber
                sourceColRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Mendapatkan data dokumen
                        Map<String, Object> data = documentSnapshot.getData();

                        // Menyimpan data ke koleksi tujuan
                        destinationColRef.document(documentSnapshot.getId())
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Setelah data berhasil dipindahkan, hapus dari koleksi sumber
                                    documentSnapshot.getReference().delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                // Handle onSuccess
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle onFailure
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Handle onFailure
                                });
                    }
                }).addOnFailureListener(e -> {
                    // Handle onFailure
                });

                dialog.dismiss(); // Tutup dialog setelah proses dimulai
            }
        });



        fetchDataFromFirestore();

    }

    private void fetchDataFromFirestore() {
        Log.d("Firestore", "Fetching data from Firestore...");
        db.collection("Done").document(userId).collection("Jadwal").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening for data: ", error);
                Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show();
                taskDone.setVisibility(View.VISIBLE);
                return;
            }

            if (value != null && !value.isEmpty()) {
                Log.d("Firestore", "Data retrieval Successful");

                list.clear();
                for (QueryDocumentSnapshot document : value) {
                    User user = document.toObject(User.class);
                    list.add(user);
                    Log.d("Firestore", "Task added: " + user.toString());
                }

                adapterDone.notifyDataSetChanged();
                Log.d("Firestore", "RecyclerView updated with new data");
                recyclerView.setVisibility(View.VISIBLE);
                taskDone.setVisibility(View.GONE);
            } else {
                Log.d("Firestore", "Data is empty");
                taskDone.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

}