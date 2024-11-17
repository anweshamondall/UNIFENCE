package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceMarkingActivity extends AppCompatActivity {

    private EditText studentIdInput, nameInput, roomNumberInput;
    private Button markAttendanceButton;
    private DatabaseReference databaseReference;
    private String deviceId;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_marking);

        // Get device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Attendance");

        // Initialize UI components
        studentIdInput = findViewById(R.id.student_id_input);
        nameInput = findViewById(R.id.name_input);
        roomNumberInput = findViewById(R.id.room_number_input);
        markAttendanceButton = findViewById(R.id.mark_attendance_button);

        markAttendanceButton.setOnClickListener(v -> {
            String studentId = studentIdInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String roomNumber = roomNumberInput.getText().toString().trim();

            if (!studentId.isEmpty() && !name.isEmpty() && !roomNumber.isEmpty()) {
                checkAndMarkAttendance(name, roomNumber);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to check if attendance is already marked for the day
    private void checkAndMarkAttendance(String name, String roomNumber) {
        String currentDate = getCurrentDate();

        // Reference for attendance on the current date for the specified room number
        DatabaseReference roomRef = databaseReference.child(currentDate).child("roomNumber_" + roomNumber);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("deviceId").getValue(String.class).equals(deviceId)) {
                    Toast.makeText(AttendanceMarkingActivity.this, "Attendance already marked today on this device!", Toast.LENGTH_SHORT).show();
                } else {
                    markAttendance(name, roomNumber, currentDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceMarkingActivity.this, "Error checking attendance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to mark attendance and save it in Firebase
    private void markAttendance(String name, String roomNumber, String date) {
        long timestamp = System.currentTimeMillis();
        AttendanceRecord attendanceRecord = new AttendanceRecord(name, roomNumber, timestamp, deviceId);

        // Save the attendance under the date and room number
        databaseReference.child(date).child("roomNumber_" + roomNumber).setValue(attendanceRecord)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to get the current date in "YYYY-MM-DD" format
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
