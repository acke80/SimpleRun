package se.umu.christofferakrin.run.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import se.umu.christofferakrin.run.model.RunEntity;

@Dao
public interface RunEntityDao{

    @Query("SELECT * FROM runentity")
    LiveData<List<RunEntity>> getAll();

    @Insert
    void insertAll(RunEntity... runEntities);

}
