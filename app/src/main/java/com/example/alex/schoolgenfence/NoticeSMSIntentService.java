package com.example.alex.schoolgenfence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

/**
 * Created by Alex on 2016/12/26.
 */

public class NoticeSMSIntentService extends IntentService {
    // region Properties

    private final String TAG = NoticeSMSIntentService.class.getName();

    private SharedPreferences prefs;
    private Gson gson;

    // endregion

    // region Constructors

    public NoticeSMSIntentService() {
        super("AreWeThereIntentService");
    }

    // endregion

    // region Overrides

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getApplicationContext().getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
        gson = new Gson();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();
                    for (Geofence geofence : event.getTriggeringGeofences()) {
                        geofenceIds.add(geofence.getRequestId());
                    }
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        onEnteredGeofences(geofenceIds);
                    }
                    // added onExitGeofences
                    if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        onExitGeofences(geofenceIds);
                    }
                }
            }
        }
    }

    // endregion

    // region Private

    private void onEnteredGeofences(List<String> geofenceIds) {
        for (String geofenceId : geofenceIds) {
            String geofenceName = "";
            //SMS
            String phoneNumber = "";

            // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String jsonString = prefs.getString(entry.getKey(), null);
                NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
                if (namedGeofence.id.equals(geofenceId)) {
                    geofenceName = namedGeofence.name;
                    //SMS
                    phoneNumber = namedGeofence.phoneNumber;
                    break;
                }
            }

            // Set the notification text and send the notification
            //String contextText = String.format(this.getResources().getString(R.string.Notification_Text), geofenceName);
            String contextText = String.format(this.getResources().getString(R.string.Notification_Text_Enter), geofenceName);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, AllGeofencesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(this.getResources().getString(R.string.Notification_Title))
                    .setContentText(contextText)
                    .setContentIntent(pendingNotificationIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contextText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(0, notification);

            //SMS
            //SmsManager enterSmsManager = SmsManager.getDefault();
           // enterSmsManager.sendTextMessage("+852"+phoneNumber ,null, "I arrived " + geofenceName, null, null);

        }
    }

    //add onExitGeofences
    private void onExitGeofences(List<String> geofenceIds) {
        for (String geofenceId : geofenceIds) {
            String geofenceName = "";
            //SMS
            String phoneNumber = "";

            // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String jsonString = prefs.getString(entry.getKey(), null);
                NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
                if (namedGeofence.id.equals(geofenceId)) {
                    geofenceName = namedGeofence.name;
                    //SMS
                    phoneNumber = namedGeofence.phoneNumber;
                    break;
                }
            }

            // Set the notification text and send the notification
            String contextText = String.format(this.getResources().getString(R.string.Notification_Text_Exit), geofenceName);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, AllGeofencesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(this.getResources().getString(R.string.Notification_Title))
                    .setContentText(contextText)
                    .setContentIntent(pendingNotificationIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contextText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(0, notification);

            //SMS
            SmsManager leaveSmsManager = SmsManager.getDefault();
            leaveSmsManager.sendTextMessage("+852"+phoneNumber,null, "I leave " + geofenceName, null, null);
        }
    }

    private void onError(int i) {
        Log.e(TAG, "Geofencing Error: " + i);
    }

    // endregion
}
