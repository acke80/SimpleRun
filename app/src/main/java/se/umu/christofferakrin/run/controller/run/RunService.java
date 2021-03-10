package se.umu.christofferakrin.run.controller.run;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import se.umu.christofferakrin.run.MainActivity;
import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.model.CountDownCounter;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunState;

import static se.umu.christofferakrin.run.RunApp.CHANNEL_ID;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTDOWN_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.STATE_KEY;


public class RunService extends Service{

    private CountDownCounter countDownCounter;
    private Counter counter;
    private DistanceHandler distanceHandler;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Thread runningThread;

    /* Store the current values. We only want to broadcast when something changes. */
    private String curCounterString = "";

    private boolean running;

    NotificationCompat.Builder notificationBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        startRun(intent.getParcelableExtra(STATE_KEY));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        /* This is deprecated, but I don't know how I can change the content of the
        * notification without accessing the builder instance. */
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("")
                .setContentTitle("Running")
                .setSmallIcon(R.drawable.run)
                .setContentIntent(pendingIntent)
                .setNotificationSilent()
                .setOnlyAlertOnce(true);

        startForeground(1, notificationBuilder.build());

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        running = false;

        try{
            runningThread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        locationManager.removeUpdates(locationListener);
    }

    /** Starts the Location listener and the run thread which handles broadcasting.
     * Suppresses Missing Permission as the Service expects the Permission to be handled
     * before it is started. */
    @SuppressLint("MissingPermission")
    private void startRun(RunState runState){
        if(runState == null || runState.getElapsedSeconds() > 0)
            countDownCounter = new CountDownCounter(0);
        else
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

                        broadcast(COUNTER_KEY, counter.getElapsedSeconds());
                        broadcast(DISTANCE_KEY, distanceHandler.getDistanceInMeters());

                        setNotificationContentText(
                                "Distance: " + distanceHandler.getDistanceAsString(),
                                counter.getTimerString()
                        );
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
}
