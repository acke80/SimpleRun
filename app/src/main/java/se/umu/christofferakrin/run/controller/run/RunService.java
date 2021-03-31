package se.umu.christofferakrin.run.controller.run;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import se.umu.christofferakrin.run.MainActivity;
import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.RunApp;
import se.umu.christofferakrin.run.model.CountDownCounter;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunEntity;

import static se.umu.christofferakrin.run.controller.run.RunFragment.PROGRESS_KEY;
import static se.umu.christofferakrin.run.model.RunGoal.GoalType;

import se.umu.christofferakrin.run.model.RunGoal;
import se.umu.christofferakrin.run.model.RunState;

import static se.umu.christofferakrin.run.RunApp.CHANNEL_ID;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTDOWN_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.RUN_GOAL_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.TEMPO_KEY;


public class RunService extends Service{

    private CountDownCounter countDownCounter;
    private Counter counter;
    private DistanceHandler distanceHandler;

    private String notificationTitle;
    private String notificationContext;
    private String tempoString;

    private BroadcastReceiver bReceiver;
    private LocalBroadcastManager bManager;

    /* Store the current value. We only need to broadcast when the counter changes (1/sec). */
    private String curCounterString = "";

    private NotificationCompat.Builder notificationBuilder;

    private Thread runningThread;
    private static boolean running;
    private static boolean paused;

    private static RunState curRunState;

    public static final String SET_PAUSE_KEY = "set_pause";

    private RunGoal runGoal;

    private boolean goalFinished = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback = new ServiceLocationCallback();

    private class ServiceLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;

            for (Location location : locationResult.getLocations()) {
                distanceHandler.setLocation(location);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        goalFinished = false;
        paused = false;
        curRunState = new RunState();
        runGoal = intent.getParcelableExtra(RUN_GOAL_KEY);
        startRun(curRunState);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        initBroadcastReceiver();

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
                    paused = intent.getBooleanExtra(SET_PAUSE_KEY, false);
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

        fusedLocationClient.removeLocationUpdates(locationCallback);

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

    /** Starts the Location listener and the run thread which handles broadcasting.*/
    private void startRun(RunState runState){
        countDownCounter = new CountDownCounter(3);

        counter = new Counter(runState.getElapsedSeconds());
        distanceHandler = new DistanceHandler(runState.getDistanceInMeters());

        startLocationListener();

        startRunningThread();
    }

    /** Suppresses Missing Permission as the Service expects the Permission to be handled
     * before it is started. */
    @SuppressLint("MissingPermission")
    private void startLocationListener(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2500);
        locationRequest.setFastestInterval(1500);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setWaitForAccurateLocation(true);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /** Defines and starts the thread for handling running updates. */
    private void startRunningThread(){
        runningThread = new Thread(() -> {
            running = true;
            countDownCounter.start();

            boolean counterStarted = false;

            while(running){
                if(countDownCounter.isFinished()){
                    if(!counter.isRunning() && !counterStarted){
                        counterStarted = true;
                        counter.start();
                    }

                    counter.setPaused(paused);
                    distanceHandler.setPaused(paused);

                    if(!curCounterString.equals(counter.getTimerString())){
                        curCounterString = counter.getTimerString();

                        update();

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
            counter.stop();
        });

        runningThread.start();
    }

    private void update(){
        tempoString = distanceHandler.getTempoString(counter.getElapsedSeconds());

        broadcast(TEMPO_KEY, tempoString);

        notificationTitle =
                "Distance: " + distanceHandler.getDistanceAsString();

        notificationContext = "Time: " + counter.getTimerString() +
                "  |  Tempo: " + tempoString;

        /* If we have a specific goal. */
        if(runGoal.getGoalType() != GoalType.BASIC && !goalFinished){
            int[] goalValues = runGoal.getValues();

            if(runGoal.getGoalType() == GoalType.DISTANCE)
                updateDistanceGoal(goalValues);
            else if(runGoal.getGoalType() == GoalType.TIME)
                updateTimeGoal(goalValues);

        }else{
            broadcast(DISTANCE_KEY, distanceHandler.getDistanceInMeters());
            broadcast(COUNTER_KEY, counter.getElapsedSeconds());
            broadcast(PROGRESS_KEY, -1);
        }

        if(goalFinished){
                notificationTitle =
                        "Goal reached! | " + "Distance: " +
                                distanceHandler.getDistanceAsString();
        }

        setNotificationContentText(notificationTitle, notificationContext);
    }

    private void updateDistanceGoal(int[] goalValues){
        broadcast(COUNTER_KEY, counter.getElapsedSeconds());

        int distanceGoal = DistanceHandler.distanceToMeters(goalValues[0] , goalValues[1]*100);
        float deltaDistance = distanceGoal - distanceHandler.getDistanceInMeters();

        if(deltaDistance <= 0){
            stopRun();
        }else{
            broadcast(DISTANCE_KEY, deltaDistance);

            if(distanceHandler.getDistanceInMeters() > 0){
                int progress = (int) (distanceHandler.getDistanceInMeters() /
                                (float) distanceGoal * 100f);
                broadcast(PROGRESS_KEY, progress);
            }
        }

        if(!goalFinished){
            notificationTitle =
                    "Distance left: " +
                            DistanceHandler.parseDistanceToString(deltaDistance);
        }
    }

    private void updateTimeGoal(int[] goalValues){
        broadcast(DISTANCE_KEY, distanceHandler.getDistanceInMeters());

        int secondsGoal = Counter.parseTimeToSeconds(goalValues[0], goalValues[1], goalValues[2]);
        int deltaSeconds = secondsGoal - counter.getElapsedSeconds();

        if(deltaSeconds <= 0){
            stopRun();
        }else{
            broadcast(COUNTER_KEY, deltaSeconds);

            if(counter.getElapsedSeconds() > 0){
                int progress = (int) ((float) counter.getElapsedSeconds() /
                        (float) secondsGoal * 100f);
                broadcast(PROGRESS_KEY, progress);
            }
        }

        if(!goalFinished){
            notificationContext = "Time left: " + Counter.parseSecondsToTimerString(deltaSeconds)
                    + "  |  Tempo: " + tempoString;
        }
    }

    private void stopRun(){
        vibrate();
        goalFinished = true;
    }

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    /** Sets the content text seen in the Notification for this Service. */
    private void setNotificationContentText(String contentTitle, String contentText){
        if(goalFinished){
            notificationBuilder.setColorized(true);
            notificationBuilder.setColor(ContextCompat.getColor(this, R.color.green_700));
        }
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

    public static boolean isPaused(){
        return paused;
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
