package se.umu.christofferakrin.run.model;


import android.location.Location;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Handles the distance travelled by the phone. */
public class DistanceHandler{

    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final float MAX_NOISE = 0.1f; /* Minimum distance for new location in meters. */

    static {
        df.setRoundingMode(RoundingMode.FLOOR);
    }

    private float distanceInMeters;
    private Location curLocation;

    public DistanceHandler(float distanceInMeters){
        this.distanceInMeters = distanceInMeters;

    }

    public DistanceHandler(){
        this(0);
    }

    public String getDistanceAsString(){
        String dist = parseDistanceToString(distanceInMeters);

        if(dist == null) return "";
        else return dist;
    }

    /** @return String for tempo in min/km. */
    public String getTempoString(int seconds){
        return df.format(getTempo(seconds)) + " min/km";
    }

    /** @return Float for tempo in min/km. */
    public float getTempo(int seconds){
        float tempo = 0f;

        if(distanceInMeters > 0)
            tempo = ((float) seconds / 60f) / (distanceInMeters / 1000f);

        return tempo;
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

    public static int distanceToMeters(int km, int m){
        return km * 1000 + m;
    }

    public static String parseDistanceToString(float distance){
        if(distance < 1000)
            return (int) distance + " m";

        return df.format(distance / 1000) + " km";
    }

    public static String parseTempoToString(float tempo){
        return df.format(tempo) + " min/km";
    }

}
