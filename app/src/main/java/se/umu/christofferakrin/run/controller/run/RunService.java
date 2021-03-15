package se.umu.christofferakrin.run.controller.run;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import se.umu.christofferakrin.run.MainActivity;
import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.RunApp;
import se.umu.christofferakrin.run.model.CountDownCounter;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunEntity;
import se.umu.christofferakrin.run.model.RunState;

import static se.umu.christofferakrin.run.RunApp.CHANNEL_ID;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTDOWN_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.TEMPO_KEY;


public class RunService extends Service{

    private CountDownCounter countDownCounter;
    private Counter counter;
    private DistanceHandler distanceHandler;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Thread runningThread;

    private BroadcastReceiver bReceiver;
    private LocalBroadcastManager bManager;

    /* Store the current value. We only need to broadcast when the counter changes (1/sec). */
    private String curCounterString = "";

    private NotificationCompat.Builder notificationBuilder;

    private static boolean running;

    private static RunState curRunState;

    public static final String SET_PAUSE_KEY = "set_pause";

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        curRunState = new RunState();
        startRun(curRunState);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        initBroadcastReceiver();

        /* This is deprecated, but I don't know how I can change the content of the
        * notification without accessing the builder instance. */
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("")
                .setContentTitle("Starting...")
                .setSmallIcon(R.drawable.run)
                .setContentIntent(pendingIntent)
                .setNotificationSilent()
                .setOnlyAlertOnce(true);

        startForeground(1, notificationBuilder.build());

        return START_NOT_STICKY;
    }

    /** Initializes the BroadcastReceiver. The RunFragment broadcasts if the RunService
     *  should pause or resume it's running process. */
    private void initBroadcastReceiver(){
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(SET_PAUSE_KEY)){
                    boolean pause = intent.getBooleanExtra(SET_PAUSE_KEY, false);
                    if(pause){
                        running = false;
                        try{
                            runningThread.join();
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        locationManager.removeUpdates(locationListener);
                    }else{
                        startRun(curRunState);
                    }
                }
            }
        };
        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SET_PAUSE_KEY);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    @Override
    public void onDestroy(){
        running = false;

        locationManager.removeUpdates(locationListener);

        try{
            runningThread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        /* Possibly store RunState to database. */
        if(curRunState != null &&
                curRunState.getElapsedSeconds() > 0 && curRunState.getDistanceInMeters() > 1){

            RunEntity runEntity = new RunEntity(curRunState);
            new Thread(()-> RunApp.getDatabase().runEntityDao().insertAll(runEntity)).start();

        }

        super.onDestroy();
    }

    /** Starts the Location listener and the run thread which handles broadcasting.
     * Suppresses Missing Permission as the Service expects the Permission to be handled
     * before it is started. */
    @SuppressLint("MissingPermission")
    private void startRun(RunState runState){
        countDownCounter = new CountDownCounter(3);

        counter = new Counter(runState.getElapsedSeconds());
        distanceHandler = new DistanceHandler(runState.getDistanceInMeters());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = location -> distanceHandler.setLocation(location);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500,
                5, locationListener);

        runningThread = new Thread(() -> {
            running = true;
            countDownCounter.start();

            while(running){
                if(countDownCounter.isFinished()){
                    if(!counter.isRunning()) counter.start();

                    if(!curCounterString.equals(counter.getTimerString())){
                        curCounterString = counter.getTimerString();

                        String tempo = distanceHandler.getTempoString(counter.getElapsedSeconds());

                        broadcast(COUNTER_KEY, counter.getElapsedSeconds());
                        broadcast(DISTANCE_KEY, distanceHandler.getDistanceInMeters());
                        broadcast(TEMPO_KEY, tempo);

                        setNotificationContentText(
                                "Distance: " + distanceHandler.getDistanceAsString(),
                                counter.getTimerString() + "  |  " + tempo);

                        curRunState.setElapsedSeconds(counter.getElapsedSeconds());
                        curRunState.setDistanceInMeters(distanceHandler.getDistanceInMeters());
                        curRunState.setTempo(distanceHandler.getTempo(counter.getElapsedSeconds()));
                    }

                }else{
                    if(!curCounterString.equals(countDownCounter.getStringValue())){
                        curCounterString = countDownCounter.getStringValue();
                        broadcast(COUNTDOWN_KEY, countDownCounter.getStringValue());
                    }
                }
            }
        });

        runningThread.start();
    }

    /** Sets the content text seen in the Notification for this Service. */
    private void setNotificationContentText(String contentTitle, String contentText){
        notificationBuilder.setContentTitle(contentTitle);
        notificationBuilder.setContentText(contentText);
        getSystemService(NotificationManager.class).notify(1, notificationBuilder.build());
    }

    /** Broadcast new information to the RunFragment. */
    private void broadcast(String action, Object data){
        Intent RTReturn = new Intent(action);

        if(data instanceof Integer)
            RTReturn.putExtra(action, (Integer) data);
        else if(data instanceof Float)
            RTReturn.putExtra(action, (Float) data);
        else if(data instanceof String)
            RTReturn.putExtra(action, (String) data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
    }

    public static boolean isRunning(){
        return running;
    }

    public static String getTimerString(){
        if(curRunState == null) return "";
        return Counter.parseSecondsToTimerString(curRunState.getElapsedSeconds());
    }

    public static String getDistanceString(){
        if(curRunState == null) return "";
        return DistanceHandler.parseDistanceToString(curRunState.getDistanceInMeters());
    }

    public static String getTempoString(){
        if(curRunState == null) return "";
        return DistanceHandler.parseTempoToString(curRunState.getTempo());
    }

    public static float getDistanceInMeters(){
        if(curRunState == null) return 0;
        return curRunState.getDistanceInMeters();
    }

    public static int getElapsedSeconds(){
        if(curRunState == null) return 0;
        return curRunState.getElapsedSeconds();
    }

    public static float getTempo(){
        if(curRunState == null) return 0;
        return curRunState.getTempo();
    }

}
