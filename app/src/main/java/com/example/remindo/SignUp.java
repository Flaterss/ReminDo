package com.example.remindo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    ImageView back;
    EditText editTextemail, editTextpassword, editTextuser, editTexthbd;
    Button button, date;
    RadioGroup rb_group;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersCollection = db.collection("Users");
    Calendar calendar = Calendar.getInstance();
    Locale id = new Locale("id", "ID");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-YYYY", id);

    ProgressBar progressBar;

    Date hbds;

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i = new Intent(getApplicationContext(), CustomBottomNav.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.back);

        editTextemail = findViewById(R.id.email);
        editTextpassword = findViewById(R.id.password);
        editTextuser = findViewById(R.id.username);
        editTexthbd = findViewById(R.id.hbd);
        date = findViewById(R.id.date);

        rb_group = findViewById(R.id.radioGrup);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogDate();
            }
        });

        button = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressBar);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUp.this, SignIn.class);
                startActivity(i);
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                final String email, username, birth, gender;
                email = editTextemail.getText().toString().trim();
                String password = editTextpassword.getText().toString().trim();
                username = editTextuser.getText().toString().trim();
                birth = editTexthbd.getText().toString().trim();

                RadioButton radioM = findViewById(R.id.male);

                if (radioM.isChecked()) {
                    gender = "Male";
                } else {
                    gender = "Female";
                }

                if (TextUtils.isEmpty(username)) {
                    editTextuser.setError("Username tidak boleh kosong!");
                    editTextuser.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    editTextemail.setError("Email tidak boleh kosong!");
                    editTextemail.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (password.length() < 6) {
                    editTextpassword.setError("Password must be 6 character!");
                    editTextpassword.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextpassword.setError("Password tidak boleh kosong!");
                    editTextpassword.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    currentUser = mAuth.getCurrentUser();
                                    assert currentUser != null;
                                    String UserId = currentUser.getUid();

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Email", email);
                                    user.put("Username", username);
                                    user.put("Birth", birth);
                                    user.put("Gender", gender);

                                    usersCollection.document(UserId).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignUp.this, "Account Created!", Toast.LENGTH_SHORT).show();

                                                Intent i = new Intent(SignUp.this, SignIn.class);
                                                startActivity(i);
                                                finish();
                                            } else {
                                                Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                });
            }
        });

    }
    private void openDialogDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {

                calendar.set(year, month, day);
                String timeString = simpleDateFormat.format(calendar.getTime());
                String formattedDate = timeString.replace("-", " ");
                editTexthbd.setText(formattedDate);
                hbds = calendar.getTime();

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


}