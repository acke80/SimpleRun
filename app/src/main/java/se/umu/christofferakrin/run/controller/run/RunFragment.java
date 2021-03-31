package se.umu.christofferakrin.run.controller.run;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.controller.result.ResultActivity;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunGoal;

import static se.umu.christofferakrin.run.controller.run.RunService.SET_PAUSE_KEY;

public class RunFragment extends Fragment{

    private TextView textViewCounter;
    private TextView textViewDistance;
    private TextView textViewTempo;

    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button resumeButton;

    private Spinner spinner;

    private NumberPicker timePicker1;
    private NumberPicker timePicker2;
    private NumberPicker timePicker3;
    private ConstraintLayout timePickerLayout;

    private NumberPicker numberPicker1;
    private NumberPicker numberPicker2;
    private ConstraintLayout numberPickerLayout;

    private BottomNavigationView navView;

    private ProgressBar progressBar;

    public static final String DISTANCE_KEY = "distance";
    public static final String COUNTDOWN_KEY = "countdown";
    public static final String COUNTER_KEY = "counter";
    public static final String TEMPO_KEY = "tempo";
    public static final String RUN_GOAL_KEY = "run goal";
    public static final String PROGRESS_KEY = "progress";

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
        stopButton = root.findViewById(R.id.stop_button);
        pauseButton = root.findViewById(R.id.pause_button);
        resumeButton = root.findViewById(R.id.resume_button);
        createButtonOnClickListeners();

        timePickerLayout = root.findViewById(R.id.timePickerLayout);
        numberPickerLayout = root.findViewById(R.id.numberPickerLayout);

        spinner = root.findViewById(R.id.spinner);
        createSpinnerAdapter();

        timePicker1 = root.findViewById(R.id.timePicker1);
        timePicker2 = root.findViewById(R.id.timePicker2);
        timePicker3 = root.findViewById(R.id.timePicker3);
        numberPicker1 = root.findViewById(R.id.numberPicker1);
        numberPicker2 = root.findViewById(R.id.numberPicker2);
        createPickers();

        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.progressbar));

        adaptTextSize();

        updateView();

        return root;
    }

    private void createButtonOnClickListeners(){
        startButton.setOnClickListener(v -> {
            vibrate();
            startRun();
        });
        stopButton.setOnClickListener(v -> {
            vibrate();
            stopRun();
            toResultActivity();
        });
        pauseButton.setOnClickListener(v -> {
            vibrate();
            pauseRun();
        });
        resumeButton.setOnClickListener(v -> {
            vibrate();
            resumeRun();
        });
    }

    private void createSpinnerAdapter(){
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.spinner_item,
                        new String[]{"Basic training", "Distance goal", "Time goal"});
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                timePickerLayout.setVisibility(View.GONE);
                numberPickerLayout.setVisibility(View.GONE);

                if(position == 1){
                    numberPickerLayout.setVisibility(View.VISIBLE);
                }else if(position == 2){
                    timePickerLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void createPickers(){
        timePicker1.setMinValue(0);
        timePicker1.setMaxValue(10000);
        timePicker1.setWrapSelectorWheel(false);

        timePicker2.setMinValue(0);
        timePicker2.setMaxValue(59);

        timePicker3.setMinValue(0);
        timePicker3.setMaxValue(59);

        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(10000);
        numberPicker1.setWrapSelectorWheel(false);

        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(9);
    }

    @Override
    public void onResume(){
        super.onResume();

        updateView();
    }

    @Override
    public void onPause(){
        super.onPause();

        if(bManager != null)
            bManager.unregisterReceiver(bReceiver);

    }

    private void updateView(){
        if(isServiceRunning()){
            initBroadcastReceiver();

            navView.setVisibility(View.GONE);

            spinner.setVisibility(View.GONE);

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);

            if(RunService.isPaused()){
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                textViewDistance.setText("Paused");
            }else{
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
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
            spinner.setVisibility(View.VISIBLE);
            spinner.setSelection(0);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
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
        if(!checkFineLocationPermission()){
            requestFineLocationPermission();
            return;
        }

        RunGoal runGoal = new RunGoal(RunGoal.GoalType.BASIC, null);
        if(spinner.getSelectedItemPosition() == 1){
            if(numberPicker1.getValue() + numberPicker2.getValue() == 0){
                Toast.makeText(
                        getContext(), "Distance goal can't be zero", Toast.LENGTH_SHORT).show();
                return;
            }

            runGoal = new RunGoal(RunGoal.GoalType.DISTANCE,
                    new int[]{numberPicker1.getValue(), numberPicker2.getValue()});

            progressBar.setVisibility(View.VISIBLE);

        }else if(spinner.getSelectedItemPosition() == 2){
            if(timePicker1.getValue() + timePicker2.getValue() + timePicker3.getValue() == 0){
                Toast.makeText(getContext(), "Time goal can't be zero", Toast.LENGTH_SHORT).show();
                return;
            }
            runGoal = new RunGoal(RunGoal.GoalType.TIME,
                    new int[]{
                            timePicker1.getValue(),
                            timePicker2.getValue(),
                            timePicker3.getValue()});

            progressBar.setVisibility(View.VISIBLE);
        }

        spinner.setVisibility(View.GONE);
        timePickerLayout.setVisibility(View.GONE);
        numberPickerLayout.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        navView.setVisibility(View.GONE);

        initBroadcastReceiver();

        Intent intent = new Intent(getContext(), RunService.class);
        intent.putExtra(RUN_GOAL_KEY, runGoal);
        getContext().startService(intent);
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

    private void stopService(){
        Intent intent = new Intent(getContext(), RunService.class);
        getContext().stopService(intent);
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

                        pauseButton.setVisibility(View.GONE);
                        stopButton.setVisibility(View.GONE);

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
                    case PROGRESS_KEY:
                        int progress = intent.getIntExtra(PROGRESS_KEY, 0);
                        if(progress == -1){
                            progressBar.setVisibility(View.GONE);
                            progressBar.setProgress(0);
                        }else{
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress(progress);
                        }
                        break;
                }
            }
        };

        initBroadcastManager();
    }

    /** Initializes the BroadcastManager for our BroadcastReceiver. */
    private void initBroadcastManager(){
        bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DISTANCE_KEY);
        intentFilter.addAction(COUNTDOWN_KEY);
        intentFilter.addAction(COUNTER_KEY);
        intentFilter.addAction(TEMPO_KEY);
        intentFilter.addAction(PROGRESS_KEY);
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

    private void vibrate(){
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(10);
        }
    }

}