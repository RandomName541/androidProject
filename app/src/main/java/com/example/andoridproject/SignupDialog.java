package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SignupDialog extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        View view = inflater.inflate(R.layout.signup_dialog, container, false);

        Button signup = view.findViewById(R.id.btnSignup);
        EditText username = view.findViewById(R.id.username);
        EditText email = view.findViewById(R.id.email);
        EditText password = view.findViewById(R.id.password);

        signup.setOnClickListener(v -> {
            String emailStr = email.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            String usernameStr = username.getText().toString().trim();

            if (usernameStr.isEmpty()) {
                username.setError("Username required");
                return;
            }
            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                email.setError("Invalid email");
                return;
            }
            if (passwordStr.length() < 6) {
                password.setError("Password must be at least 6 characters");
                return;
            }

            // Create Firebase Auth User
            mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            // 1. Save user profile to database
                            saveUserToDatabase(uid, usernameStr);

                            Toast.makeText(getContext(), "Signup Successful! Logging in...", Toast.LENGTH_SHORT).show();

                            // 2. AUTOMATIC LOGIN: Redirect to the main app screen
                            // Note: Firebase automatically signs in the user locally upon creation.
                            navigateToMainScreen();

                            dismiss();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                email.setError("Email already in use");
                            }
                            Log.e("AuthError", "Error: " + task.getException().getMessage());
                        }
                    });
        });

        return view;
    }

    private void saveUserToDatabase(String uid, String usernameStr) {
        float startingMoney = 100000.0f;
        List<Stock> emptyStocks = new ArrayList<>();

        User newUser = new User(uid, startingMoney, usernameStr, emptyStocks);

        mDatabase.child(uid).setValue(newUser)
                .addOnSuccessListener(aVoid -> Log.d("DB", "Database record created"))
                .addOnFailureListener(e -> Log.e("DBError", "Write failed: " + e.getMessage()));
    }

    private void navigateToMainScreen() {
        if (getActivity() != null) {
            // Replace 'ChartActivity.class' with your actual landing activity (e.g., MarketActivity)
            Intent intent = new Intent(getActivity(), ChartActivity.class);
            // Add flags to clear the activity stack so the user can't "Go Back" to the signup screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}