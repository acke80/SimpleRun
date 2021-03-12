package se.umu.christofferakrin.run.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Metrics{

    private static final DecimalFormat df = new DecimalFormat("#.##");

    static {
        df.setRoundingMode(RoundingMode.FLOOR);
    }

    private Metrics(){
    }

    /** @return String for tempo in min/km. */
    public static String getTempoString(int seconds, float distanceInMeters){
        return df.format(getTempo(seconds, distanceInMeters)) + " min/km";
    }

    /** @return Float for tempo in min/km. */
    public static float getTempo(int seconds, float distanceInMeters){
        float tempo = 0f;

        if(distanceInMeters > 0)
            tempo = ((float) seconds / 60f) / (distanceInMeters / 1000f);

        return tempo;
    }

    public static String parseTempoToString(float tempo){
        return df.format(tempo) + " min/km";
    }
}
