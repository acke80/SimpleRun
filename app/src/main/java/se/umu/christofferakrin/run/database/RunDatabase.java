package se.umu.christofferakrin.run.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import se.umu.christofferakrin.run.model.RunEntity;

@Database(entities = {RunEntity.class}, version = 1)
public abstract class RunDatabase extends RoomDatabase{

    public abstract RunEntityDao runEntityDao();

}
