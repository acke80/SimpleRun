package se.umu.christofferakrin.run.controller.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import se.umu.christofferakrin.run.R;


public class StatisticsRecyclerAdapter extends RecyclerView.Adapter<StatisticsRecyclerAdapter.ViewHolder>{

    Context context;

    String[] title;
    String[] content;

    public StatisticsRecyclerAdapter(Context context, String[] title, String[] content){
        this.context = context;
        this.title = title;
        this.content = content;
    }

    @NonNull
    @Override
    public StatisticsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_statistics_row, parent, false);

        return new StatisticsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsRecyclerAdapter.ViewHolder holder, int position){
        holder.titleTextView.setText(title[position]);
        holder.contentTextView.setText(content[position]);
    }

    @Override
    public int getItemCount(){
        return title.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, contentTextView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            titleTextView = itemView.findViewById(R.id.textView1);
            contentTextView = itemView.findViewById(R.id.textView2);
        }
    }
}
