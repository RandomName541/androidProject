package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views
        Button signupButton = findViewById(R.id.SignupButton);
        Button loginButton = findViewById(R.id.loginButton);
        Button btnQuickLogin = findViewById(R.id.btnQuickLogin); // Make sure this ID exists in XML

        // Handle system bar padding
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Standard Login Dialog
        loginButton.setOnClickListener(view -> {
            LoginDialog dialog = new LoginDialog();
            dialog.show(getSupportFragmentManager(), "login");
        });

        // Standard Signup Dialog
        signupButton.setOnClickListener(view -> {
            SignupDialog dialog = new SignupDialog();
            dialog.show(getSupportFragmentManager(), "signup");
        });

        // --- AUTOMATIC QUICK LOGIN ---
        btnQuickLogin.setOnClickListener(v -> {
            // TODO: Change these to your actual test credentials
            String testEmail = "asdfg@gmail.com";
            String testPassword = "asdfghjkl";

            mAuth.signInWithEmailAndPassword(testEmail, testPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Auto-Login Success!", Toast.LENGTH_SHORT).show();

                            // Go to Market/Main screen (Change MainMenuActivity if yours is named differently)
                            Intent intent = new Intent(MainActivity.this, MainMenu.class);
                            startActivity(intent);
                            finish(); // Close MainActivity so user can't go back to login
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                            Toast.makeText(MainActivity.this, "Auto-Login Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}