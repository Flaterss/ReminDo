package com.example.remindo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Premium extends AppCompatActivity {

    ImageView back;
    Button buy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        back = findViewById(R.id.back);
        buy = findViewById(R.id.buy);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Premium.this, CustomBottomNav.class);
                i.putExtra("updated", 1);
                startActivity(i);
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Premium.this, "This Feature not Available For Now", Toast.LENGTH_LONG).show();
            }
        });

    }
}