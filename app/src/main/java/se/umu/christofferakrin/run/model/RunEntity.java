package se.umu.christofferakrin.run.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Final entity for a finished Run. */
@Entity
public class RunEntity{

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int elapsedSeconds;
    public float distanceInMeter;
    public float tempo;

    public String dateString;

    public RunEntity(int elapsedSeconds, float distanceInMeter, float tempo){
        this.elapsedSeconds = elapsedSeconds;
        this.distanceInMeter = distanceInMeter;
        this.tempo = tempo;

        this.dateString =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public RunEntity(RunState runstate){
        this(
                runstate.getElapsedSeconds(),
                runstate.getDistanceInMeters(),
                runstate.getTempo()
        );
    }
}
