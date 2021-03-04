package se.umu.christofferakrin.run.model;

/** Handles an active run. */
public class RunHandler implements Runnable{

    private final int SIM = 1000; /* Seconds in milliseconds. */
    private long timer;

    private int second;
    private int minute;
    private int hour;

    private Thread runningThread;
    private boolean running;

    private DistanceHandler distanceHandler;

    public RunHandler(int second, int minute, int hour){
        if(second > 0) second = 0;
        if(minute > 0) minute = 0;
        if(hour > 0) hour = 0;

        this.second = second;
        this.minute = minute;
        this.hour = hour;

        distanceHandler = new DistanceHandler();

    }

    public RunHandler(){
        this(0, 0, 0);
    }

    public void start(){
        running = true;
        runningThread = new Thread(this);
        runningThread.start();
    }

    public void stop(){
        running = false;

        try {
            runningThread.join();
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        timer = System.currentTimeMillis();

        while(running){

            /* Every second. */
            if(System.currentTimeMillis() - timer >= SIM) {
                timer += SIM;
                tickHumanTime();
            }
        }

        stop();
    }

    private void tickHumanTime(){
        if(++second >= 60){
            second = 0;
            if(++minute >= 60){
                minute = 0;
                hour++;
            }
        }
    }

    public String getTimerString(){
        String secondString = "";
        String minuteString = "";
        String hourString = "";

        if(second < 10)
            secondString = "0" + second + "s";
        else
            secondString = second + "s";

        if(minute > 0 || hour > 0){
            if(minute < 10)
                minuteString = "0" + minute + "m" + " : ";
            else
                minuteString = minute + "m" + " : ";
        }

        if(hour > 0)
            hourString = hour + "h" + " : ";

        return hourString + minuteString + secondString;
    }

    public boolean isRunning(){
        return running;
    }

}
