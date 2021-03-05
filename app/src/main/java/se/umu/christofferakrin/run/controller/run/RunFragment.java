package se.umu.christofferakrin.run.controller.run;

import android.Manifest;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import se.umu.christofferakrin.run.R;

import static android.content.Context.LOCATION_SERVICE;

public class RunFragment extends Fragment{

    private RunViewModel runViewModel;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private TextView textViewCounter;
    private TextView textViewDistance;

    private Button startButton;
    private Button stopButton;
    private Button pauseButton;

    public static final String RECEIVE_DISTANCE = "distance";

    private BroadcastReceiver bReceiver;
    LocalBroadcastManager bManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        runViewModel =
                new ViewModelProvider(this).get(RunViewModel.class);
        View root = inflater.inflate(R.layout.fragment_run, container, false);

        textViewCounter = root.findViewById(R.id.text_counter);
        runViewModel.getCounterText().observe(getViewLifecycleOwner(), textViewCounter::setText);

        textViewDistance = root.findViewById(R.id.text_distance);
        runViewModel.getDistanceText().observe(getViewLifecycleOwner(), textViewDistance::setText);

        startButton = root.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRun());

        stopButton = root.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> stopRunningService());

        pauseButton = root.findViewById(R.id.pause_button);

        requestFineLocationPermission();

        adaptTextSize();

        /*
        if(runViewModel.isRunning()){
            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
        }*/

        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(RECEIVE_DISTANCE)) {
                    float distance = intent.getFloatExtra("distance", 0f);
                    System.out.println("Recieved: "+ distance);
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_DISTANCE);
        bManager.registerReceiver(bReceiver, intentFilter);

        return root;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        bManager.unregisterReceiver(bReceiver);
    }

    public void startRunningService(){
        Intent intent = new Intent(getActivity(), RunService.class);
        intent.putExtra("distance", "10 m");

        getActivity().startService(intent);
    }

    public void stopRunningService(){
        Intent intent = new Intent(getActivity(), RunService.class);
        getActivity().stopService(intent);
    }

    public void startRun(){
        if(ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestFineLocationPermission();
            return;
        }

        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        runViewModel.startRun();

        locationListener = location -> runViewModel.setLocation(location);

        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500,
                5, locationListener);

        startRunningService();
    }

    public void requestFineLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    public void adaptTextSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int DeviceTotalWidth = metrics.widthPixels;

        textViewCounter.setTextSize(DeviceTotalWidth/20f);
        textViewDistance.setTextSize(DeviceTotalWidth/20f);
    }

}