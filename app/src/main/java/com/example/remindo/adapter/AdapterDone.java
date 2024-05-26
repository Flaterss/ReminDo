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

public class AdapterDone extends RecyclerView.Adapter<AdapterDone.DoneViewHolder> {

    private Context context;
    private ArrayList<User> list;


    public AdapterDone(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public DoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.done_layout, parent, false);
        return new DoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoneViewHolder holder, int position) {
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
                popupMenu.inflate(R.menu.done_menu);
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

                            if (itemId == R.id.done_item) {

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                                String userId = fAuth.getCurrentUser().getUid();
                                String sourcePath = "Done/" + userId + "/Jadwal/" + date + "|" + time;
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

    public static class DoneViewHolder extends RecyclerView.ViewHolder{

        TextView tv_task, tv_desc, tv_time, tv_date, tv_priority;

        ImageView options;
        CardView navigationView;

        public DoneViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.done_date);
            tv_desc = itemView.findViewById(R.id.done_description);
            tv_priority = itemView.findViewById(R.id.done_priority);
            tv_task = itemView.findViewById(R.id.done_title);
            tv_time = itemView.findViewById(R.id.done_time);

            options = itemView.findViewById(R.id.done_options);


        }
    }

}
