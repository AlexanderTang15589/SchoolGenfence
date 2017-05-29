package com.example.alex.schoolgenfence;

import android.support.annotation.NonNull;

import java.util.UUID;

import com.google.android.gms.location.Geofence;

/**
 * Created by Alex on 2016/12/26.
 */

public class NamedGeofence implements Comparable {

    // region Properties

    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public float radius;


    //SMS
    public String phoneNumber;
    public String address;

    // end region

    // region Public

    public Geofence geofence() {
        id = UUID.randomUUID().toString();
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    // endregion

    // region Comparable

    @Override
    public int compareTo(@NonNull Object another) {
        NamedGeofence other = (NamedGeofence) another;
        return name.compareTo(other.name);
    }

    // endregion

}
