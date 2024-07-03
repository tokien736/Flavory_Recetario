package com.example.prueba;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etResetEmail;
    private Button btnSendResetLink, btnCancel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etResetEmail = findViewById(R.id.etResetEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        btnCancel = findViewById(R.id.btnCancel);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnSendResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etResetEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "Enlace de restablecimiento enviado a su correo", Toast.LENGTH_SHORT).show();
                                    // Redirigir a la actividad de inicio de sesión
                                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // Finalizar la actividad actual
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "Error al enviar el enlace de restablecimiento", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finaliza la actividad y regresa a la pantalla anterior
                finish();
            }
        });
    }
}
