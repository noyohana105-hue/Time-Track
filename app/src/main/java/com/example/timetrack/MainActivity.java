package com.example.timetrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    TextView jobNameView, date ;
    TextView totalHoursView, totalSalaryView;
    Button plus, logout, settings;
    ScrollView scrollView;
    double hourlyRate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        totalHoursView = findViewById(R.id.totalHours);
        totalSalaryView = findViewById(R.id.totalSalary);
        jobNameView = findViewById(R.id.jobNameView);
        logout = findViewById(R.id.logoutBtn);
        settings = findViewById(R.id.settingsBtn);
        plus = findViewById(R.id.plusBtn);
        scrollView = findViewById(R.id.scrollView);
        date = findViewById(R.id.date);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);
        String email = sp.getString("user", "");

        LocalDateTime now = LocalDateTime.now();
        date.setText(now.toLocalDate().toString());

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JobSetting.class);
            startActivity(intent);
        });

        plus.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShiftSetting.class);
            startActivity(intent);
        });

        fetchJobData(db, email);
    }

    private void fetchJobData(FirebaseFirestore db, String email) {
        DocumentReference docRef = db.collection("jobs").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Map<String, Object> data = document.getData();
                    jobNameView.setText(data.get("jobName").toString());
                    
                    Object rateObj = data.get("hourlyRate");
                    if (rateObj != null) {
                        hourlyRate = Double.parseDouble(rateObj.toString());
                    }
                    loadShifts(db, email);
                } else {
                    startActivity(new Intent(MainActivity.this, JobSetting.class));
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        String email = sp.getString("user", "");
        if (!email.isEmpty()) {
            fetchJobData(db, email);
        }
    }

    private void loadShifts(FirebaseFirestore db, String email) {
        db.collection("shifts").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                double totalHours = 0;
                List<Shift> shifts = new LinkedList<>();
                for(DocumentSnapshot document : task.getResult()){
                    Map<String, Object> data = document.getData();
                    LocalDateTime start = Shift.fromMap((Map<String, Object>) data.get("startTime"));
                    LocalDateTime end = Shift.fromMap((Map<String, Object>) data.get("endTime"));
                    totalHours += document.getDouble("totalTime");
                    shifts.add(new Shift(start, end, email, document.getId()));
                }
                updateUI(shifts, totalHours);
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(List<Shift> shifts, double totalHours) {
        LinearLayout mainContainer = new LinearLayout(MainActivity.this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(10);
        mainContainer.setPadding(padding, padding, padding, padding);
        
        for (final Shift shift : shifts) {
            LinearLayout itemLayout = new LinearLayout(MainActivity.this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            
            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.rgb(245, 240, 255)); // צבע רקע מעט בהיר יותר
            border.setCornerRadius(dpToPx(12));
            border.setStroke(dpToPx(1), Color.LTGRAY);
            itemLayout.setBackground(border);
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, dpToPx(12));
            itemLayout.setLayoutParams(layoutParams);

            TextView textView = new TextView(MainActivity.this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            textView.setText(shift.toString());
            textView.setTextSize(15);
            textView.setTextColor(Color.BLACK);

            // כפתורי עריכה ומחיקה בגודל מתאים
            int btnSize = dpToPx(40);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnSize, btnSize);
            btnParams.setMargins(dpToPx(4), 0, 0, 0);

            Button editBtn = new Button(MainActivity.this);
            editBtn.setText("✎");
            editBtn.setTextSize(18);
            editBtn.setPadding(0, 0, 0, 0);
            editBtn.setLayoutParams(btnParams);
            editBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ShiftSetting.class);
                intent.putExtra("shiftId", shift.getId());
                startActivity(intent);
            });

            Button delBtn = new Button(MainActivity.this);
            delBtn.setText("🗑");
            delBtn.setTextSize(18);
            delBtn.setPadding(0, 0, 0, 0);
            delBtn.setLayoutParams(btnParams);
            delBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            delBtn.setOnClickListener(v -> {
                shift.DeleteShiftFromDB();
                loadShifts(FirebaseFirestore.getInstance(), shift.email);
            });

            itemLayout.addView(textView);
            itemLayout.addView(editBtn);
            itemLayout.addView(delBtn);
            mainContainer.addView(itemLayout);
        }
        
        scrollView.removeAllViews();
        scrollView.addView(mainContainer);
        
        DecimalFormat df = new DecimalFormat("#.##");
        totalHoursView.setText("Hours: " + df.format(totalHours));
        totalSalaryView.setText("Salary: " + df.format(totalHours * hourlyRate) + " ₪");
    }
}
