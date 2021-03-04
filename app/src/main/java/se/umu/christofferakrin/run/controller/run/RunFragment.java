package se.umu.christofferakrin.run.controller.run;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import se.umu.christofferakrin.run.R;

import static android.content.Context.LOCATION_SERVICE;

public class RunFragment extends Fragment{

    private RunViewModel runViewModel;

    private LocationManager locationManager;
    private LocationListener locationListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        runViewModel =
                new ViewModelProvider(this).get(RunViewModel.class);
        View root = inflater.inflate(R.layout.fragment_run, container, false);

        final TextView textViewCounter = root.findViewById(R.id.text_counter);
        runViewModel.getCounterText().observe(getViewLifecycleOwner(), textViewCounter::setText);

        final TextView textViewDistance = root.findViewById(R.id.text_distance);
        runViewModel.getDistanceText().observe(getViewLifecycleOwner(), textViewDistance::setText);

        final Button startButton = root.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startRun());

        requestFineLocationPermission();

        return root;
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                1, locationListener);
    }

    public void requestFineLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

}