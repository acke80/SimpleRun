package se.umu.christofferakrin.run.model;


public class RunState{

    private int elapsedSeconds;
    private float distanceInMeters;
    private float tempo;

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

    public void setTempo(float tempo){
        if(tempo < 0)
            throw new IllegalArgumentException("Tempo can't be negative.");

        this.tempo = tempo;
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
