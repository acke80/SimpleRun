package se.umu.christofferakrin.run.model;

import java.util.ArrayList;

/** Model for information shown in the StatisticsFragment. */
public class StatisticsManager{

    private float totalDistance;
    private int totalTime;
    private float avgTempo;
    private int numOfRuns;

    public StatisticsManager(ArrayList<RunEntity> runEntities){

        int totTempo = 0;
        for(RunEntity re : runEntities){
            totalDistance += re.distanceInMeter;
            totalTime += re.elapsedSeconds;
            totTempo += re.tempo;
        }

        numOfRuns = runEntities.size();
        avgTempo = totTempo / (float) numOfRuns;
    }

    public String getTotalDistanceString(){
        return DistanceHandler.parseDistanceToString(totalDistance);
    }

    public String getTotalTimeString(){
        return Counter.parseSecondsToTimerString(totalTime);
    }

    public String getAvgTempoString(){
        return DistanceHandler.parseTempoToString(avgTempo);
    }

    public String getNumOfRunsString(){
        return Integer.toString(numOfRuns);
    }
}
