package com.example.timetrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class JobSetting extends AppCompatActivity {

    Button save, back;
    EditText jobName, hourlyRate, startDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_setting);

        save = findViewById(R.id.button);
        back = findViewById(R.id.backBtnJob);
        jobName = findViewById(R.id.editTextText);
        hourlyRate = findViewById(R.id.editTextText12);
        startDate = findViewById(R.id.editTextDate);

        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);

        back.setOnClickListener(v -> finish());

        // Pre-fill data if it exists
        String email = sp.getString("user", "");
        if (!email.isEmpty()) {
            db.collection("jobs").document(email).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    jobName.setText(documentSnapshot.getString("jobName"));
                    jobName.setHint(documentSnapshot.getString("jobName"));
                    hourlyRate.setText(documentSnapshot.getString("hourlyRate"));
                    startDate.setText(documentSnapshot.getString("startDate"));
                }
            });
        }

        startDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(JobSetting.this, R.style.DatePickerDialogTheme,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        save.setOnClickListener(v -> {
            if (jobName.getText().toString().isEmpty() || hourlyRate.getText().toString().isEmpty() || startDate.getText().toString().isEmpty()) {
                Toast.makeText(JobSetting.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> job = new HashMap<>();
                try {
                    double hr = Double.parseDouble(hourlyRate.getText().toString());
                    if (hr < 0) {
                        Toast.makeText(JobSetting.this, "Invalid rate", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(JobSetting.this, "Invalid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                job.put("jobName", jobName.getText().toString());
                job.put("hourlyRate", hourlyRate.getText().toString());
                job.put("startDate", startDate.getText().toString());

                db.collection("jobs").document(email).set(job).addOnSuccessListener(aVoid -> {
                    Toast.makeText(JobSetting.this, "Settings updated", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(JobSetting.this, "Error saving settings", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
