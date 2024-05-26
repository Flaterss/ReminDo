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

public class AdapterItem extends RecyclerView.Adapter<AdapterItem.ItemViewHolder> {

    private Context context;
    private ArrayList<User> list;
    private static ArrayList<User> listTask;


    public AdapterItem(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    public static void updateTaskList(ArrayList<User> newTaskList) {
        listTask = newTaskList;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        User user = list.get(position);

        holder.tv_date.setText(user.getDate());
        holder.tv_desc.setText(user.getDescription());
        holder.tv_priority.setText(user.getStatus());
        holder.tv_task.setText(user.getTask());
        holder.tv_time.setText(user.getTime());

        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.options);
                popupMenu.inflate(R.menu.nav_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        int position = holder.getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            User selectedUser = list.get(position);
                            String date = selectedUser.getDate();
                            String time = selectedUser.getTime();

                            if (itemId == R.id.complete_item) {

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                                String userId = fAuth.getCurrentUser().getUid();
                                String sourcePath = "Tasks/" + userId + "/Jadwal/" + date + "|" + time;
                                String destinationPath = "Done/" + userId + "/Jadwal/" + date + "|" + time;

                                DocumentReference SourceDocRef = db.document(sourcePath);
                                SourceDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            DocumentReference DestinationDocRef = db.document(destinationPath);
                                            DestinationDocRef.set(documentSnapshot.getData()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    SourceDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    }). addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Your Data Failed Mark as Completed!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


                            } else if (itemId == R.id.delete_item) {

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                                String userId = fAuth.getCurrentUser().getUid();
                                String sourcePath = "Tasks/" + userId + "/Jadwal/" + date + "|" + time;
                                String destinationPath = "History/" + userId + "/Jadwal/" + date + "|" + time;

                                DocumentReference SourceDocRef = db.document(sourcePath);
                                SourceDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            DocumentReference DestinationDocRef = db.document(destinationPath);
                                            DestinationDocRef.set(documentSnapshot.getData()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    SourceDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    }). addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Your Data Failed to Delete!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }


                        }


                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView tv_task, tv_desc, tv_time, tv_date, tv_priority;

        ImageView options;
        CardView navigationView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.date);
            tv_desc = itemView.findViewById(R.id.description);
            tv_priority = itemView.findViewById(R.id.priority);
            tv_task = itemView.findViewById(R.id.title);
            tv_time = itemView.findViewById(R.id.time);

            options = itemView.findViewById(R.id.options);


        }
    }

}
