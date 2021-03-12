package se.umu.christofferakrin.run.model;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RunState{

    private int elapsedSeconds;
    private float distanceInMeters;
    private float tempo;

    private String date = "";

    public RunState(){
    }

    public RunState(int elapsedSeconds, float distanceInMeters, float tempo){
        setElapsedSeconds(elapsedSeconds);
        setDistanceInMeters(distanceInMeters);
        setTempo(tempo);
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

    public void setTempo(float tempo){
        if(tempo < 0)
            throw new IllegalArgumentException("Tempo can't be negative.");

        this.tempo = tempo;
    }

    public void setDateStampToNow(){
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public int getElapsedSeconds(){
        return elapsedSeconds;
    }

    public float getDistanceInMeters(){
        return distanceInMeters;
    }

    public float getTempo(){
        return tempo;
    }
}
