package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.Calendar;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            Log.e("GeofenceBroadcastReceiver", "Error receiving geofence event: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // If the transition is GEOFENCE_TRANSITION_ENTER, proceed with the time check
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
            notificationHelper.sendHighPriorityNotification("Entering JKLU campus", "", MapsActivity.class);

            // Check if the current time is within the allowed window (10:00 PM to 10:30 PM)
            if (isWithinTimeWindow()) {
                // Time is within the allowed window, open the AttendanceMarkingActivity
                intent = new Intent(context, AttendanceMarkingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Ensure the activity opens in a new task
                context.startActivity(intent);
            } else {
                // Time is outside the allowed window, show a toast message
                Toast.makeText(context, "Attendance can only be marked between 10:00 PM and 10:30 PM.", Toast.LENGTH_LONG).show();
            }
        }

        // Logging geofence IDs that triggered the event
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        // Handle other transition types (e.g., GEOFENCE_TRANSITION_DWELL, GEOFENCE_TRANSITION_EXIT)
        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Inside JKLU campus", "", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Exiting JKLU campus", "", MapsActivity.class);
                break;
        }
    }

    // Check if the current time is between 10:00 PM and 10:30 PM
    private boolean isWithinTimeWindow() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);  // Get current hour (24-hour format)
        int minute = currentTime.get(Calendar.MINUTE);     // Get current minute

        // Check if current time is between 10:00 PM and 10:30 PM
        return hour == 22 && minute >= 0 && minute <= 30;
    }
}
