package se.umu.christofferakrin.run.controller.result;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;

import static se.umu.christofferakrin.run.controller.run.RunFragment.COUNTER_KEY;
import static se.umu.christofferakrin.run.controller.run.RunFragment.DISTANCE_KEY;

public class ResultActivity extends AppCompatActivity{

    private float distanceInMeters;
    private int elapsedSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        distanceInMeters = intent.getFloatExtra(DISTANCE_KEY, 0f);
        elapsedSeconds = intent.getIntExtra(COUNTER_KEY, 0);

        final TextView textViewDistance = findViewById(R.id.text_distance);
        textViewDistance.setText(DistanceHandler.parseDistanceToString(distanceInMeters));

        final TextView textViewCounter = findViewById(R.id.text_counter);
        textViewCounter.setText(Counter.parseSecondsToTimerString(elapsedSeconds));

    }
}