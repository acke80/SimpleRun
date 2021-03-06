package se.umu.christofferakrin.run.controller.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import se.umu.christofferakrin.run.R;


/** Adapter used by the RecyclerView in HistoryFragment. */
public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>{

    Context context;

    String[] distance;
    String[] time;
    String[] tempo;
    String[] date;

    public HistoryRecyclerAdapter(Context context, String[] distance, String[] time,
                                  String[] tempo, String[] date){
        this.context = context;
        this.distance = distance;
        this.time = time;
        this.tempo = tempo;
        this.date = date;
    }

    @NonNull
    @Override
    public HistoryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_history_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerAdapter.ViewHolder holder, int position){
        holder.distanceTextView.setText(distance[position]);
        holder.timeTextView.setText(time[position]);
        holder.tempoTextView.setText(tempo[position]);
        holder.dateTextView.setText(date[position]);
    }

    @Override
    public int getItemCount(){
        return distance.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView distanceTextView, timeTextView, tempoTextView, dateTextView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            distanceTextView = itemView.findViewById(R.id.textView1);
            timeTextView = itemView.findViewById(R.id.textView2);
            tempoTextView = itemView.findViewById(R.id.textView3);
            dateTextView = itemView.findViewById(R.id.textView4);
        }
    }
}
