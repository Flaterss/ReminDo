package com.example.remindo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.remindo.adapter.AdapterDone;
import com.example.remindo.adapter.AdapterHistory;
import com.example.remindo.adapter.DataUser;
import com.example.remindo.database.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    ImageView backs;
    FloatingActionButton delete;

    AdapterHistory adapterHistory;
    ArrayList<User> list;
    String userId;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("History");

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;

    LinearLayout taskHistory;

    Dialog dialog;
    Button btnDialogCancel, btnDialogDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recycleView);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference("History");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapterHistory = new AdapterHistory(this, list);
        recyclerView.setAdapter(adapterHistory);

        userId = firebaseAuth.getCurrentUser().getUid();

        backs = findViewById(R.id.backs);
        delete = findViewById(R.id.delete_item);

        taskHistory = findViewById(R.id.taskHistory);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(true);

        btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        btnDialogDelete = dialog.findViewById(R.id.btnDialogDelete);

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Path = "History/" + userId + "/Jadwal";
                CollectionReference colRef = db.collection(Path);

                colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                            documentSnapshot.getReference().delete();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(History.this, "Failed to Flush History, Try Again!", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.dismiss();
            }
        });


        backs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(History.this, CustomBottomNav.class);
                i.putExtra("updated", 1);
                startActivity(i);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        fetchDataFromFirestore();

    }

    private void fetchDataFromFirestore() {
        Log.d("Firestore", "Fetching data from Firestore...");
        db.collection("History").document(userId).collection("Jadwal").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening for data: ", error);
                Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show();
                taskHistory.setVisibility(View.VISIBLE);
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

                adapterHistory.notifyDataSetChanged();
                Log.d("Firestore", "RecyclerView updated with new data");
                recyclerView.setVisibility(View.VISIBLE);
                taskHistory.setVisibility(View.GONE);
            } else {
                Log.d("Firestore", "Data is empty");
                taskHistory.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}