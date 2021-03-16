package se.umu.christofferakrin.run.controller.statistics;

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
import se.umu.christofferakrin.run.model.StatisticsManager;
import se.umu.christofferakrin.run.model.RunEntity;

public class StatisticsFragment extends Fragment{

    private String[] title;
    private String[] content;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        navView.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        Thread thread = new Thread(() -> {
            ArrayList<RunEntity> runEntities =
                    (ArrayList<RunEntity>) RunApp.getDatabase().runEntityDao().getAll();

            StatisticsManager am = new StatisticsManager(runEntities);

            title = new String[4];
            content = new String[4];

            title[0] = "Total Distance";
            content[0] = am.getTotalDistanceString();

            title[1] = "Total Time";
            content[1] = am.getTotalTimeString();

            title[2] = "Average Tempo";
            content[2] = am.getAvgTempoString();

            title[3] = "Number of Runs";
            content[3] = am.getNumOfRunsString();


        });

        thread.start();

        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        recyclerView.setAdapter(
                new StatisticsRecyclerAdapter(container.getContext(), title, content));

        return root;
    }
}