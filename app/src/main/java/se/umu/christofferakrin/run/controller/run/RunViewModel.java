package se.umu.christofferakrin.run.controller.run;

import android.location.Location;
import android.location.LocationListener;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import se.umu.christofferakrin.run.model.CountDownCounter;
import se.umu.christofferakrin.run.model.RunHandler;

public class RunViewModel extends ViewModel{

    private MutableLiveData<String> mainText;
    private MutableLiveData<String> distanceText;

    private CountDownCounter countDownCounter;
    private RunHandler runHandler;

    public RunViewModel(){
        mainText = new MutableLiveData<>();
        distanceText = new MutableLiveData<>();
        countDownCounter = new CountDownCounter(3);
        runHandler = new RunHandler();
    }

    public void startRun(){
        if(runHandler.isRunning()) return;

        countDownCounter.start();

        new Thread(() -> {
            while(true){
                if(countDownCounter.isFinished()){
                    if(!runHandler.isRunning()) runHandler.start();

                    mainText.postValue(runHandler.getTimerString());
                }else{
                    mainText.postValue(countDownCounter.getStringValue());
                }

            }
        }).start();

    }

    public LiveData<String> getMainText(){
        return mainText;
    }

    public void setLocation(Location location){
        System.out.println("location");
    }
}