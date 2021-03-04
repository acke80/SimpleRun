package se.umu.christofferakrin.run.model;

import android.os.CountDownTimer;


public class CountDownCounter{

    private String countString;
    private final CountDownTimer countDownTimer;
    private boolean finishedCountDown;

    public CountDownCounter(int seconds){
        countDownTimer = new CountDownTimer((seconds + 1) * 1000, 1000){
            @Override
            public void onTick(long millisUntilFinished){
                if(millisUntilFinished / 1000 < 1) /* If on last (extra) second. */
                    countString = "Go!";
                else
                    countString = Integer.toString((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish(){
                finishedCountDown = true;
            }
        };
    }

    public void start(){
        countDownTimer.start();
    }

    public boolean isFinished(){
        return finishedCountDown;
    }

    public String getStringValue(){
        return countString;
    }

}
