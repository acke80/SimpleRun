package se.umu.christofferakrin.run.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RunState{

    private int elapsedSeconds;
    private float distanceInMeters;

    public RunState(){
    }
    
    public void setElapsedSeconds(int seconds){
        if(seconds < 0)
            throw new IllegalArgumentException("Seconds can't be negative.");

        elapsedSeconds = seconds;
    }

    public void setDistanceInMeters(float distance){
        if(distance < 0)
            throw new IllegalArgumentException("Distance can't be negative.");

        distanceInMeters = distance;
    }

    public int getElapsedSeconds(){
        return elapsedSeconds;
    }

    public float getDistanceInMeters(){
        return distanceInMeters;
    }
}
