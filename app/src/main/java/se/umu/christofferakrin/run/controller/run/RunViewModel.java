package se.umu.christofferakrin.run.controller.run;


import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunState;

import static se.umu.christofferakrin.run.controller.run.RunFragment.STATE_KEY;


public class RunViewModel extends ViewModel{

    private SavedStateHandle stateHandle;

    private RunState runState;

    public RunViewModel(SavedStateHandle stateHandle){
        this.stateHandle = stateHandle;

        if(stateHandle.contains(STATE_KEY)){
            runState = stateHandle.get(STATE_KEY);
        }else{
            runState = new RunState();
        }

        saveState();
    }

    public void setDistanceInMeters(float distance){
        runState.setDistanceInMeters(distance);
        saveState();
    }

    public void setElapsedSeconds(int seconds){
        runState.setElapsedSeconds(seconds);
        saveState();
    }

    public String getTimerString(){
        return Counter.parseSecondsToTimerString(runState.getElapsedSeconds());
    }

    public String getDistanceString(){
        return DistanceHandler.parseDistanceToString(runState.getDistanceInMeters());
    }

    public void setPaused(boolean paused){
        runState.setPaused(paused);
        saveState();
    }

    public boolean isPaused(){
        return runState.isPaused();
    }


    public RunState getRunState(){
        return runState;
    }

    public void resetRunState(){
        runState = new RunState();
        saveState();
    }

    private void saveState(){
        stateHandle.set(STATE_KEY, runState);
    }

}