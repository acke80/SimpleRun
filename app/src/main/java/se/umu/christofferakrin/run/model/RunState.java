package se.umu.christofferakrin.run.model;

import android.os.Parcel;
import android.os.Parcelable;

/** Stores a parcelable state of a run. */
public class RunState implements Parcelable{

    private int elapsedSeconds;
    private float distanceInMeters;

    private boolean paused;

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

    public boolean isPaused(){
        return paused;
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

    protected RunState(Parcel in){
        elapsedSeconds = in.readInt();
        distanceInMeters = in.readFloat();
        paused = in.readInt() == 1;
    }

    public static final Creator<RunState> CREATOR = new Creator<RunState>(){
        @Override
        public RunState createFromParcel(Parcel in){
            return new RunState(in);
        }

        @Override
        public RunState[] newArray(int size){
            return new RunState[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(elapsedSeconds);
        dest.writeFloat(distanceInMeters);
        dest.writeInt(paused ? 1 : 0);
    }
}
