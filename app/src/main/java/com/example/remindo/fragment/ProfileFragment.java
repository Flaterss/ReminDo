package com.example.remindo.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remindo.EditProfile;
import com.example.remindo.History;
import com.example.remindo.MainActivity;
import com.example.remindo.Premium;
import com.example.remindo.R;
import com.example.remindo.Settings;
import com.example.remindo.SignIn;
import com.example.remindo.SignUp;
import com.example.remindo.adapter.DataUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    TextView nama, mail, ak, tanggal;
    ShapeableImageView photoProfile;
    Button editProfile;
    LinearLayout logout, history, premium, settings, terms;
    FirebaseAuth auth;
    FirebaseUser user;
    StorageReference sRef;

    Dialog dialog;
    Button btnDialogCancel, btnDialogLogout;

    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logout = view.findViewById(R.id.logout);
        history = view.findViewById(R.id.history);
        premium = view.findViewById(R.id.premium);
        settings = view.findViewById(R.id.settings);
        terms = view.findViewById(R.id.terms);

        photoProfile = view.findViewById(R.id.photoProfile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        sRef = FirebaseStorage.getInstance().getReference();

        editProfile = view.findViewById(R.id.editProfile);

        nama = view.findViewById(R.id.nama);
        mail = view.findViewById(R.id.mail);
        ak = view.findViewById(R.id.gender);
        tanggal = view.findViewById(R.id.date);

        userId = auth.getCurrentUser().getUid();

        if (user == null) {
            Intent i = new Intent(getActivity(), SignIn.class);
            startActivity(i);

        }

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_logout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.custom_dialog_bg));
        dialog.setCancelable(true);

        btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        btnDialogLogout = dialog.findViewById(R.id.btnDialogLogout);

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDialogLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getActivity(), SignIn.class);
                startActivity(i);
                getActivity().finish();
                dialog.dismiss();

                DataUser.setUpdateData(1);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), History.class);
                startActivity(i);
            }
        });

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Premium.class);
                startActivity(i);
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Our Terms and Policy not available right now", Toast.LENGTH_SHORT).show();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Settings.class);
                startActivity(i);
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditProfile.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        if (DataUser.updateData == 1) {
            fetchdataUser(userId);
        }

        return view;
    }

    public void fetchdataUser(String userId) {
        DocumentReference db = FirebaseFirestore.getInstance().collection("Users").document(userId);
        db.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    String username = value.getString("Username");
                    String email = value.getString("Email");
                    String gender = value.getString("Gender");
                    String birth = value.getString("Birth");

                    StorageReference refS = sRef.child(userId).child("Profile").child("profile.jpg");

                    refS.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DataUser.setImageSource(uri);
                            Picasso.get().load(DataUser.getImageSource()).fit().centerCrop().into(photoProfile);
                        }
                    });

                    DataUser.setUsername(username);
                    DataUser.setEmail(email);
                    DataUser.setGender(gender);
                    DataUser.setBirth(birth);

                    nama.setText(username);
                    mail.setText(email);
                    ak.setText(gender);
                    tanggal.setText(birth);

                    DataUser.setUpdateData(0);

                } else {

                }
            }
        });
    }
}