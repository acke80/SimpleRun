package se.umu.christofferakrin.run.controller.run;

import android.annotation.SuppressLint;
import android.app.Notification;
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

import static se.umu.christofferakrin.run.RunApp.CHANNEL_ID;
import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;


public class RunService extends Service{

    private CountDownCounter countDownCounter;
    private Counter counter;
    private DistanceHandler distanceHandler;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Thread runningThread;

    /* Store the current values. We only want to broadcast when something changes. */
    private String curCounterString = "";
    private String curDistanceString = "";

    private boolean running;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        startRun();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Running")
                .setContentText("")
                .setSmallIcon(R.drawable.run)
                .setContentIntent(pendingIntent)
                .build();


        startForeground(1, notification);

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

    @SuppressLint("MissingPermission")
    private void startRun(){
        countDownCounter = new CountDownCounter(3);
        counter = new Counter();
        distanceHandler = new DistanceHandler();

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
                        broadcast(COUNTER_KEY, counter.getTimerString());
                    }

                    if(!curDistanceString.equals(distanceHandler.getDistanceAsString())){
                        curDistanceString = distanceHandler.getDistanceAsString();
                        broadcast(DISTANCE_KEY, distanceHandler.getDistanceAsString());
                    }


                }else{
                    if(!curCounterString.equals(countDownCounter.getStringValue())){
                        curCounterString = countDownCounter.getStringValue();
                        broadcast(COUNTER_KEY, countDownCounter.getStringValue());
                    }

                }

            }
        });

        runningThread.start();
    }

    private void broadcast(String action, String text){
        Intent RTReturn = new Intent(action);
        RTReturn.putExtra(action, text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
    }
}
