package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginDialog extends DialogFragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the layout
        View view = inflater.inflate(R.layout.login_dialog, container, false);

        // 2. Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 3. Initialize UI elements
        etEmail = view.findViewById(R.id.email);
        etPassword = view.findViewById(R.id.password);
        btnLogin = view.findViewById(R.id.btnLogin);

        // 4. Set Click Listener
        btnLogin.setOnClickListener(v -> handleLogin());

        return view;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic Validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        // Firebase Sign In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // SUCCESS: User is authenticated
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                        // Start MainMenu Activity
                        Intent intent = new Intent(getActivity(), MainMenu.class);
                        startActivity(intent);

                        // Close the login dial og and the activity behind it
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                        dismiss();
                    } else {
                        // FAILURE: Show error message (e.g., wrong password)
                        String error = task.getException() != null ? task.getException().getMessage() : "Authentication Failed";
                        Toast.makeText(getContext(), "Login Failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Optional: Make the dialog look nice and wide
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}