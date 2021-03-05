package se.umu.christofferakrin.run.controller.run;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import se.umu.christofferakrin.run.model.CountDownCounter;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

public class RunViewModel extends ViewModel{

    private MutableLiveData<String> counterText;
    private MutableLiveData<String> distanceText;

    private CountDownCounter countDownCounter;
    private Counter counter;
    private DistanceHandler distanceHandler;

    public RunViewModel(){
        counterText = new MutableLiveData<>();
        distanceText = new MutableLiveData<>();
        countDownCounter = new CountDownCounter(3);
        counter = new Counter();

        distanceHandler = new DistanceHandler();
    }

    public void startRun(){
        if(counter.isRunning()) return;

        countDownCounter.start();

        new Thread(() -> {
            while(true){
                if(countDownCounter.isFinished()){
                    if(!counter.isRunning()) counter.start();

                    counterText.postValue(counter.getTimerString());
                    distanceText.postValue(distanceHandler.getDistanceAsString());

                }else{
                    counterText.postValue(countDownCounter.getStringValue());
                }

            }
        }).start();
    }

    public LiveData<String> getCounterText(){
        return counterText;
    }

    public LiveData<String> getDistanceText(){
        return distanceText;
    }

    public boolean isRunning(){
        return counter.isRunning() || countDownCounter.isRunning();
    }

    public void setLocation(Location location){
        if(!counter.isRunning()) return;

        distanceHandler.setLocation(location);
    }


}