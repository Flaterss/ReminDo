package com.example.remindo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignIn extends AppCompatActivity {

    TextView signup;
    EditText editTextemail, editTextpassword;
    Button button;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private String loggedInUsername;

    @Override
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
        setContentView(R.layout.activity_sign_in);

        signup = findViewById(R.id.signup);
        editTextemail = findViewById(R.id.email);
        editTextpassword = findViewById(R.id.password);
        button = findViewById(R.id.login);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignIn.this, SignUp.class);
                startActivity(i);
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = editTextemail.getText().toString().trim();
                password = editTextpassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    editTextemail.setError("Email tidak boleh kosong!");
                    editTextemail.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextpassword.setError("Password tidak boleh kosong!");
                    editTextpassword.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null) {
                                        loggedInUsername = currentUser.getEmail();
                                        String userId = currentUser.getUid();

                                        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        startActivity(new Intent(SignIn.this, CustomBottomNav.class));
                                                        finish();
                                                    } else {
                                                        addUserToFirestore(userId);
                                                    }
                                                } else {
                                                    Toast.makeText(SignIn.this, "Data tidak terdaftar", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                                }
                            }
                });

            }
        });
    }

    private void addUserToFirestore(String userId) {
        db.collection("Users").document(userId).set(new HashMap<String, Object>() {{
            put("Username", loggedInUsername);
        }}).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(SignIn.this, CustomBottomNav.class));
                    finish();
                } else {
                    Toast.makeText(SignIn.this, "Kesalahan!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}