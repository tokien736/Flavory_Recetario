package com.example.prueba;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    intent = new Intent(Splash.this, MainActivity.class);
                } else {
                    intent = new Intent(Splash.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000); // 3-second delay
    }
}