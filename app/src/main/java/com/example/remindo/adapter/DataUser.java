package com.example.remindo.adapter;

import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.remindo.database.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DataUser {

    static String Username, Email, Gender, Birth;
    static Uri ImageSource;
    public static int updateData = 1;


    public DataUser(String username, String email, String gender, String birth, Uri imageSource) {
        Username = username;
        Email = email;
        Gender = gender;
        Birth = birth;
        ImageSource = imageSource;
    }

    public static String getUsername() {
        return Username;
    }

    public static String getEmail() {
        return Email;
    }

    public static String getGender() {
        return Gender;
    }

    public static String getBirth() {
        return Birth;
    }

    public static Uri getImageSource() {
        return ImageSource;
    }

    public static void setUsername(String username) {
        Username = username;
    }

    public static void setEmail(String email) {
        Email = email;
    }

    public static void setGender(String gender) {
        Gender = gender;
    }

    public static void setBirth(String birth) {
        Birth = birth;
    }

    public static void setImageSource(Uri imageSource) {
        ImageSource = imageSource;
    }

    public static void setUpdateData(int updateData) {
        DataUser.updateData = updateData;
    }
}