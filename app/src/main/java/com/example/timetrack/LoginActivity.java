package com.example.timetrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextTextEmailAddress, editTextPassword;
    Button SignIn;
    TextView SignUp;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextPassword = findViewById(R.id.password);
        editTextTextEmailAddress = findViewById(R.id.email);
        SignIn = findViewById(R.id.login);
        SignUp = findViewById(R.id.sign_up);

        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);

        SignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        SignIn.setOnClickListener(v -> {
            String email = String.valueOf(editTextTextEmailAddress.getText()).trim();
            String password = String.valueOf(editTextPassword.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("user", email);
                    editor.putString("password", password);
                    editor.apply();
                    
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login Failed";
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("LoginError", "Auth failed", task.getException());
                }
            });
        });

        // Auto-fill only, don't auto-click if we just logged out
        if (sp.contains("user") && !getIntent().getBooleanExtra("loggedOut", false)){
            editTextTextEmailAddress.setText(sp.getString("user", ""));
            editTextPassword.setText(sp.getString("password", ""));
        }
    }
}
