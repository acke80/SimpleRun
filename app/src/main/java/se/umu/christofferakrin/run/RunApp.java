package se.umu.christofferakrin.run;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.room.Room;

import se.umu.christofferakrin.run.database.RunDatabase;

public class RunApp extends Application{
    public static final String CHANNEL_ID = "runningChannel";

    private static RunDatabase db;
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();
        context = getApplicationContext();
    }

    public static RunDatabase getDatabase(){
        if(db == null) {
            db = Room.databaseBuilder(context,
                    RunDatabase.class, "run-database").build();
        }
        return db;
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Running Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}
