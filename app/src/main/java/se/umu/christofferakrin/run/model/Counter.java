package se.umu.christofferakrin.run.model;

/** Handles an active runs timer. */
public class Counter{

    private final int SIM = 1000; /* Seconds in milliseconds. */
    private long timer;

    private int second;
    private int minute;
    private int hour;

    private Thread runningThread;
    private boolean running;

    public Counter(int second, int minute, int hour){
        if(second < 0) second = 0;
        if(minute < 0) minute = 0;
        if(hour < 0) hour = 0;

        this.second = second;
        this.minute = minute;
        this.hour = hour;

    }

    public Counter(int second){
        this(second % 60, (second % 3600) / 60, second / 3600);
    }

    public Counter(){
        this(0, 0, 0);
    }

    public void start(){
        running = true;

        runningThread = new Thread(() -> {
            timer = System.currentTimeMillis();

            while(running){

                /* Every second. */
                if(System.currentTimeMillis() - timer >= SIM) {
                    System.out.println(Thread.currentThread());
                    timer += SIM;
                    tickHumanTime();
                }
            }
        });

        runningThread.start();
    }

    public synchronized void stop(){
        running = false;

        try {
            runningThread.join();
        }catch(InterruptedException e) {
            e.printStackTrace();
        }

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
        return parseSecondsToTimerString(second, minute, hour);
    }


    public boolean isRunning(){
        return running;
    }

    public int getElapsedSeconds(){
        return second + minute * 60 + hour * 3600;
    }

    public static int parseTimeToSeconds(int hour, int minute, int seconds){
        return hour * 3600 + minute * 60 + seconds;
    }

    public static String parseSecondsToTimerString(int second){
        int hour = second / 3600;
        int minute = (second % 3600) / 60;
        second = second % 60;

        return parseSecondsToTimerString(second, minute, hour);
    }

    public static String parseSecondsToTimerString(int second, int minute, int hour){
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

}
