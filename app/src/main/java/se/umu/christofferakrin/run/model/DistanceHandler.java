package se.umu.christofferakrin.run.model;


import android.location.Location;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Handles the distance travelled by the phone. */
public class DistanceHandler{

    private static final DecimalFormat df = new DecimalFormat("#.##");

    /* Set location accuracy limiter, were lower value is better accuracy.
    * This helps with ignoring GPS startup inaccuracy, or if the location
    * has a bad GPS signal (indoors etc). Even indoors i notice that the signal
    * tends to be accurate for the first few invokes, but stabilizes overtime and
    * becomes quite accurate. */
    private static final float  LOCATION_ACCURACY_MAX = 9f;

    static {
        df.setRoundingMode(RoundingMode.FLOOR);
    }

    private float distanceInMeters;
    private Location curLocation;

    private boolean paused;

    public DistanceHandler(float distanceInMeters){
        this.distanceInMeters = distanceInMeters;

    }

    public String getDistanceAsString(){
        return parseDistanceToString(distanceInMeters);
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
        if(location == null || location.getAccuracy() > LOCATION_ACCURACY_MAX)
            return;

        if(curLocation == null){ /* If this is first invoke. */
            curLocation = location;
            return;
        }

        if(!paused)
            distanceInMeters += curLocation.distanceTo(location);

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

    public void setPaused(boolean paused){
        this.paused = paused;
    }
}
