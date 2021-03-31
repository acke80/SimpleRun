package se.umu.christofferakrin.run.controller.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import se.umu.christofferakrin.run.R;
import se.umu.christofferakrin.run.RunApp;
import se.umu.christofferakrin.run.model.Counter;
import se.umu.christofferakrin.run.model.DistanceHandler;
import se.umu.christofferakrin.run.model.RunEntity;

public class HistoryFragment extends Fragment{

    private String[] distance;
    private String[] time;
    private String[] tempo;
    private String[] date;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.fragment_history, container, false);

        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        navView.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        RunApp.getDatabase().runEntityDao().getAll().observe(getActivity(), (runEntities -> {
            int size = runEntities.size();
            distance = new String[size];
            time = new String[size];
            tempo = new String[size];
            date = new String[size];

            for(int i = 0; i < size; i++){
                int j = (size - 1) - i; /* Iterate backwards so we get newest first. */
                distance[i] =
                        DistanceHandler.parseDistanceToString(
                                runEntities.get(j).distanceInMeter);
                time[i] = Counter.parseSecondsToTimerString(runEntities.get(j).elapsedSeconds);
                tempo[i] = DistanceHandler.parseTempoToString(runEntities.get(j).tempo);
                date[i] = runEntities.get(j).dateString;
            }


            recyclerView.setAdapter(new HistoryRecyclerAdapter(
                    container.getContext(), distance, time, tempo, date));
        }));

        return root;
    }

}