package se.umu.christofferakrin.run.model;

import android.os.Parcel;
import android.os.Parcelable;

/** Defines the goal for a run. */
public class RunGoal implements Parcelable{

    public enum GoalType{
        BASIC,
        DISTANCE,
        TIME
    }

    private GoalType goalType;
    private int[] values;

    public RunGoal(GoalType goalType, int[] values){
        this.goalType = goalType;
        this.values = values;
    }

    public GoalType getGoalType(){
        return goalType;
    }

    public int[] getValues(){
        return values;
    }

    protected RunGoal(Parcel in){
        goalType = GoalType.valueOf(in.readString());
        values = in.createIntArray();
    }

    public static final Creator<RunGoal> CREATOR = new Creator<RunGoal>(){
        @Override
        public RunGoal createFromParcel(Parcel in){
            return new RunGoal(in);
        }

        @Override
        public RunGoal[] newArray(int size){
            return new RunGoal[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(goalType.name());
        dest.writeIntArray(values);
    }
}
