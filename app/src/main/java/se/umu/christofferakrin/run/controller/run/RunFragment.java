package se.umu.christofferakrin.run.controller.run;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.controller.result.ResultActivity;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

import static se.umu.christofferakrin.run.controller.run.RunService.SET_PAUSE_KEY;

public class RunFragment extends Fragment{

    private TextView textViewCounter;
    private TextView textViewDistance;

    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button resumeButton;

    public static final String DISTANCE_KEY = "distance";
    public static final String COUNTDOWN_KEY = "countdown";
    public static final String COUNTER_KEY = "counter";

    private BroadcastReceiver bReceiver;
    private LocalBroadcastManager bManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.fragment_run, container, false);

        textViewCounter = root.findViewById(R.id.text_counter);
        textViewDistance = root.findViewById(R.id.text_distance);

        startButton = root.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRun());

        stopButton = root.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> stopRun());

        pauseButton = root.findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(v -> pauseRun());

        resumeButton = root.findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(v -> resumeRun());

        requestFineLocationPermission();

        adaptTextSize();

        if(isServiceRunning()){
            initBroadcastReceiver();

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);

            textViewCounter.setText(RunService.getTimerString());
            textViewDistance.setText(RunService.getDistanceString());

            if(RunService.isRunning()){
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
            }else{
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
            }
        }

        return root;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(bManager != null)
            bManager.unregisterReceiver(bReceiver);
    }

    /** Checks if the RunService is running. Even if android were to destroy our service
     * unexpectedly, this method would give us the correct answer.
     * @return true if RunService is running, else false. */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RunService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startRun(){
        if(!checkFineLocationPermission())
            return;

        startButton.setVisibility(View.GONE);

        initBroadcastReceiver();
        startService();
    }

    private void stopRun(){
        stopService();

        Intent intent = new Intent(getActivity(), ResultActivity.class);
        intent.putExtra(DISTANCE_KEY, RunService.getDistanceInMeters());
        intent.putExtra(COUNTER_KEY, RunService.getElapsedSeconds());
        startActivity(intent);

    }

    private void pauseRun(){
        broadcastOnPause(true);

        pauseButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.VISIBLE);

    }

    private void resumeRun(){
        broadcastOnPause(false);

        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void startService(){
        Intent intent = new Intent(getActivity(), RunService.class);
        getActivity().startService(intent);
    }

    private void stopService(){
        Intent intent = new Intent(getActivity(), RunService.class);
        getActivity().stopService(intent);
    }

    private boolean checkFineLocationPermission(){
        if(ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestFineLocationPermission();
            return false;
        }

        return true;
    }

    private void requestFineLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    /** Initializes the BroadcastReceiver used to handle broadcast sent by the RunService. */
    private void initBroadcastReceiver(){
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()){
                    case DISTANCE_KEY:
                        float distance = intent.getFloatExtra(DISTANCE_KEY, 0f);
                        textViewDistance.setText(DistanceHandler.parseDistanceToString(distance));
                        break;
                    case COUNTDOWN_KEY:
                        String countdown = intent.getStringExtra(COUNTDOWN_KEY);
                        textViewCounter.setText(countdown);
                        break;
                    case COUNTER_KEY:
                        int counter = intent.getIntExtra(COUNTER_KEY, 0);
                        textViewCounter.setText(Counter.parseSecondsToTimerString(counter));
                        stopButton.setVisibility(View.VISIBLE);
                        pauseButton.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DISTANCE_KEY);
        intentFilter.addAction(COUNTDOWN_KEY);
        intentFilter.addAction(COUNTER_KEY);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    private void broadcastOnPause(boolean paused){
        Intent RTReturn = new Intent(SET_PAUSE_KEY);
        RTReturn.putExtra(SET_PAUSE_KEY, paused);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(RTReturn);
    }

    /** Adapts text size to size of screen. */
    private void adaptTextSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int DeviceTotalWidth = metrics.widthPixels;

        textViewCounter.setTextSize(DeviceTotalWidth/20f);
        textViewDistance.setTextSize(DeviceTotalWidth/20f);
    }

}