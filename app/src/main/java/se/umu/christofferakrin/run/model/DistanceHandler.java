package se.umu.christofferakrin.run.model;


import android.location.Location;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Handles the distance travelled by the phone. */
public class DistanceHandler{

    private final float MAX_NOISE = 0.1f; /* Minimum distance for new location in meters. */

    private float distanceInMeters;
    private Location curLocation;

    private final DecimalFormat decimalFormat;

    public DistanceHandler(float distanceInMeters){
        this.distanceInMeters = distanceInMeters;
        decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
    }

    public DistanceHandler(){
        this(0);
    }

    public String getDistanceAsString(){
        if(distanceInMeters < 1000)
            return (int)distanceInMeters + " m";

        return decimalFormat.format(distanceInMeters / 1000) + " km";
    }

    public void setLocation(Location location){
        if(location == null) return;

        if(curLocation == null){ /* If this is first invoke. */
            curLocation = location;
            return;
        }

        float distance = curLocation.distanceTo(location);

        if(distance < MAX_NOISE) return;

        distanceInMeters += distance;
        curLocation = location;
    }

    public float getDistanceInMeters(){
        return distanceInMeters;
    }
}
