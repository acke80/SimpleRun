package se.umu.christofferakrin.run.controller.result;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.TEMPO_KEY;

public class ResultActivity extends AppCompatActivity{

    private float distanceInMeters;
    private int elapsedSeconds;
    private float tempo;

    private TextView textViewDistance;
    private TextView textViewCounter;
    private TextView textViewTempo;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Results");

        Intent intent = getIntent();
        distanceInMeters = intent.getFloatExtra(DISTANCE_KEY, 0f);
        elapsedSeconds = intent.getIntExtra(COUNTER_KEY, 0);
        tempo = intent.getFloatExtra(TEMPO_KEY, 0f);

        textViewDistance = findViewById(R.id.text_distance);
        textViewDistance.setText(DistanceHandler.parseDistanceToString(distanceInMeters));

        textViewCounter = findViewById(R.id.text_counter);
        textViewCounter.setText(Counter.parseSecondsToTimerString(elapsedSeconds));

        textViewTempo = findViewById(R.id.text_tempo);
        textViewTempo.setText(DistanceHandler.parseTempoToString(tempo));

        adaptTextSize();

    }

    /** Adapts text size to size of screen. */
    private void adaptTextSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int DeviceTotalWidth = metrics.widthPixels;

        textViewDistance.setTextSize(DeviceTotalWidth/20f);
        textViewCounter.setTextSize(DeviceTotalWidth/30f);
        textViewTempo.setTextSize(DeviceTotalWidth/30f);
    }
}