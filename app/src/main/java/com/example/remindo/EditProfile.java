package com.example.remindo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remindo.adapter.DataUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    TextView nama, mail, ak, tanggal;
    StorageReference sRef;
    ShapeableImageView photoProfile;
    Uri imageSource;

    Button save, date;
    ImageView back;

    Calendar calendar = Calendar.getInstance();
    Locale id = new Locale("id", "ID");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-YYYY", id);
    Date hbds;

    FirebaseAuth auth;
    FirebaseUser user;

    String userId;
    private RadioButton radioM;

    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersCollection = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        save = findViewById(R.id.save);
        back = findViewById(R.id.back);

        sRef = FirebaseStorage.getInstance().getReference();

        RadioGroup radioGroup = findViewById(R.id.radioGrup);
        radioM = findViewById(R.id.male);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.male) {
                    ak.setText("Male");
                } else {
                    ak.setText("Female");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditProfile.this, CustomBottomNav.class);
                i.putExtra("updated", 1);
                startActivity(i);
                finish();
            }
        });


        photoProfile = findViewById(R.id.photoProfile);

        date = findViewById(R.id.date);

        nama = findViewById(R.id.nama);
        mail = findViewById(R.id.mail);
        ak = findViewById(R.id.ak);
        tanggal = findViewById(R.id.tanggal);

        userId = auth.getCurrentUser().getUid();

        Handler handler = new Handler(Looper.getMainLooper());
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sedang Menyimpan...");

        if (user == null) {
            Intent i = new Intent(EditProfile.this, SignIn.class);
            startActivity(i);

        }

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogDate();
            }
        });

        photoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent BukaGaleri = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(BukaGaleri, 1319);


            }
        });

        Picasso.get().load(DataUser.getImageSource()).fit().centerCrop().into(photoProfile);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                try {
                    uploadGambar(imageSource);
                } catch (Exception e) {

                }

                String username = nama.getText().toString().trim();
                String email = mail.getText().toString().trim();
                String gender = ak.getText().toString().trim();
                String date = tanggal.getText().toString().trim();

                currentUser = auth.getCurrentUser();
                assert currentUser != null;
                String UserId = currentUser.getUid();

                Map<String, Object> user = new HashMap<>();
                user.put("Email", email);
                user.put("Username", username);
                user.put("Birth", date);
                user.put("Gender", gender);

                usersCollection.document(UserId).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DataUser.setUpdateData(1);

                                    Intent i = new Intent(EditProfile.this, CustomBottomNav.class);
                                    i.putExtra("updated", 1);
                                    progressDialog.dismiss();
                                    startActivity(i);

                                }
                            }, 5000);
                        } else {
                            Toast.makeText(EditProfile.this, "Update Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        nama.setText(DataUser.getUsername());
        mail.setText(DataUser.getEmail());
        ak.setText(DataUser.getGender());
        tanggal.setText(DataUser.getBirth());

    }

    public void uploadGambar(Uri ImageUri) {
        StorageReference refS = sRef.child(userId).child("Profile").child("profile.jpg");

        refS.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                refS.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DataUser.setImageSource(uri);
                    }
                });
                Picasso.get().load(DataUser.getImageSource()).fit().centerCrop().into(photoProfile);
                Toast.makeText(EditProfile.this, "Upload Gambar Berhasil!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1319) {
            imageSource = data.getData();
            photoProfile.setImageURI(imageSource);

            Picasso.get().load(imageSource).fit().centerCrop().into(photoProfile);
        }

    }

    private void openDialogDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {

                calendar.set(year, month, day);
                String timeString = simpleDateFormat.format(calendar.getTime());
                String formattedDate = timeString.replace("-", " ");
                tanggal.setText(formattedDate);
                hbds = calendar.getTime();

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}