package se.umu.christofferakrin.run.controller.run;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class RunFragment extends Fragment{

    private TextView textViewCounter;
    private TextView textViewDistance;

    private Button startButton;
    private Button stopButton;
    private Button pauseButton;

    public static final String DISTANCE_KEY = "distance";
    public static final String COUNTER_KEY = "counter";

    private BroadcastReceiver bReceiver;
    LocalBroadcastManager bManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.fragment_run, container, false);

        textViewCounter = root.findViewById(R.id.text_counter);
        textViewDistance = root.findViewById(R.id.text_distance);

        startButton = root.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRun());

        stopButton = root.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> stopRunningService());

        pauseButton = root.findViewById(R.id.pause_button);

        requestFineLocationPermission();

        adaptTextSize();

        if(isServiceRunning()){
            initBroadcastReceiver();
        }

        return root;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(bManager != null)
            bManager.unregisterReceiver(bReceiver);
    }

    private void startRunningService(){
        Intent intent = new Intent(getActivity(), RunService.class);
        getActivity().startService(intent);
    }

    private void stopRunningService(){
        bManager.unregisterReceiver(bReceiver);
        Intent intent = new Intent(getActivity(), RunService.class);
        getActivity().stopService(intent);
    }

    /** Checks if the RunService is running. */
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
        if(ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestFineLocationPermission();
            return;
        }

        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        initBroadcastReceiver();
        startRunningService();
    }

    private void requestFineLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void adaptTextSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int DeviceTotalWidth = metrics.widthPixels;

        textViewCounter.setTextSize(DeviceTotalWidth/20f);
        textViewDistance.setTextSize(DeviceTotalWidth/20f);
    }

    private void initBroadcastReceiver(){
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(DISTANCE_KEY)) {
                    String distance = intent.getStringExtra(DISTANCE_KEY);
                    textViewDistance.setText(distance);
                }else if(intent.getAction().equals(COUNTER_KEY)){
                    String counter = intent.getStringExtra(COUNTER_KEY);
                    textViewCounter.setText(counter);
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DISTANCE_KEY);
        intentFilter.addAction(COUNTER_KEY);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

}