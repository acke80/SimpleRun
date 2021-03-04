package se.umu.christofferakrin.run.model;


import android.location.Location;

/** Handles the distance travelled by the phone. */
public class DistanceHandler{

    private int distanceInMeters;
    private Location curLocation;

    public DistanceHandler(){

    }

    public String getDistanceAsString(){
        return "0 km";
    }

    public void setLocation(Location location){
        if(location == null) return;


    }
}
