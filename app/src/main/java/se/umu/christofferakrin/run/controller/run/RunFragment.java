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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.controller.result.ResultActivity;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

import static se.umu.christofferakrin.run.controller.run.RunService.SET_PAUSE_KEY;

public class RunFragment extends Fragment{

    private TextView textViewCounter;
    private TextView textViewDistance;
    private TextView textViewTempo;

    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button resumeButton;

    private BottomNavigationView navView;

    public static final String DISTANCE_KEY = "distance";
    public static final String COUNTDOWN_KEY = "countdown";
    public static final String COUNTER_KEY = "counter";
    public static final String TEMPO_KEY = "tempo";

    private BroadcastReceiver bReceiver;
    private LocalBroadcastManager bManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.fragment_run, container, false);

        navView = getActivity().findViewById(R.id.nav_view);
        navView.setVisibility(View.VISIBLE);

        textViewCounter = root.findViewById(R.id.text_counter);
        textViewDistance = root.findViewById(R.id.text_distance);
        textViewTempo = root.findViewById(R.id.text_tempo);

        startButton = root.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRun());

        stopButton = root.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> {
            stopRun();
            toResultActivity();
        });

        pauseButton = root.findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(v -> pauseRun());

        resumeButton = root.findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(v -> resumeRun());

        requestFineLocationPermission();

        adaptTextSize();

        updateView();

        return root;
    }

    @Override
    public void onResume(){
        super.onResume();

        updateView();
    }

    private void updateView(){
        if(isServiceRunning()){
            initBroadcastReceiver();

            navView.setVisibility(View.GONE);

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);

            textViewCounter.setText(RunService.getTimerString());
            textViewDistance.setText(RunService.getDistanceString());
            textViewTempo.setText(RunService.getTempoString());

            if(RunService.isRunning()){
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
            }else{
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
            }
        }else{
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.GONE);
            textViewDistance.setText("");
            textViewCounter.setText("");
            textViewTempo.setText("");
            navView.setVisibility(View.VISIBLE);
        }
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
        ActivityManager manager =
                (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
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

        navView.setVisibility(View.GONE);

        initBroadcastReceiver();
        startService();
    }

    private void stopRun(){
        stopService();
    }

    private void toResultActivity(){
        Intent intent = new Intent(getActivity(), ResultActivity.class);
        intent.putExtra(DISTANCE_KEY, RunService.getDistanceInMeters());
        intent.putExtra(COUNTER_KEY, RunService.getElapsedSeconds());
        intent.putExtra(TEMPO_KEY, RunService.getTempo());
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

                        if(countdown.equals("Go!"))
                            textViewCounter.setTextColor(
                                    ContextCompat.getColor(context, R.color.green_700));

                        textViewDistance.setText("");
                        textViewTempo.setText("");
                        break;
                    case COUNTER_KEY:
                        int counter = intent.getIntExtra(COUNTER_KEY, 0);
                        textViewCounter.setText(Counter.parseSecondsToTimerString(counter));
                        textViewCounter.setTextColor(ContextCompat.getColor(context, R.color.gray));
                        stopButton.setVisibility(View.VISIBLE);
                        pauseButton.setVisibility(View.VISIBLE);
                        break;
                    case TEMPO_KEY:
                        String tempo = intent.getStringExtra(TEMPO_KEY);
                        textViewTempo.setText(tempo);
                        break;
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DISTANCE_KEY);
        intentFilter.addAction(COUNTDOWN_KEY);
        intentFilter.addAction(COUNTER_KEY);
        intentFilter.addAction(TEMPO_KEY);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    /** Broadcast to the RunService that we want to pause/resume the service's running proccess.
     * This does NOT stop the RunService, but signals that we do/don't want new information. */
    private void broadcastOnPause(boolean paused){
        Intent RTReturn = new Intent(SET_PAUSE_KEY);
        RTReturn.putExtra(SET_PAUSE_KEY, paused);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(RTReturn);
    }

    /** Adapts text size to size of screen. */
    private void adaptTextSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int DeviceTotalWidth = metrics.widthPixels;

        textViewDistance.setTextSize(DeviceTotalWidth/20f);
        textViewCounter.setTextSize(DeviceTotalWidth/25f);
        textViewTempo.setTextSize(DeviceTotalWidth/35f);
    }

}