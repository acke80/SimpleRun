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
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

import static se.umu.christofferakrin.run.RunApp.CHANNEL_ID;


public class RunService extends Service{

    private Counter counter;
    private DistanceHandler distanceHandler;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        counter = new Counter(
                intent.getIntExtra("second", 0),
                intent.getIntExtra("minute", 0),
                intent.getIntExtra("hour", 0)
        );

        distanceHandler = new DistanceHandler(intent.getFloatExtra("distance", 0f));


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = location -> distanceHandler.setLocation(location);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500,
                5, locationListener);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Running")
                .setContentText(Float.toString(intent.getFloatExtra("distance", 0f)))
                .setSmallIcon(R.drawable.run)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        broadcast();
    }

    private void broadcast(){
        Intent RTReturn = new Intent("distance");
        RTReturn.putExtra("distance", distanceHandler.getDistanceInMeters());
        LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
    }
}
