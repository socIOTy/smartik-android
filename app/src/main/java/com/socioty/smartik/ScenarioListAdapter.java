package com.socioty.smartik;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.socioty.smartik.Model.Scenario;
import com.socioty.smartik.Model.ScenarioAction;

import java.util.List;

/**
 * Created by serhiipianykh on 2017-04-17.
 */

public class ScenarioListAdapter extends RecyclerView.Adapter<ScenarioListAdapter.ViewHolder> {

    private final List<Scenario> scenarios;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImage;
        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.scenario_name);
            mImage = (ImageView) view.findViewById(R.id.scenario_image);
        }
    }

    public ScenarioListAdapter(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    @Override
    public ScenarioListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scenario_adapter, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public int getItemCount() {
        return scenarios.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(scenarios.get(position).getName());
        switch (scenarios.get(position).getAction()) {
            case lightsOn: {
                holder.mImage.setImageResource(R.drawable.light_on);
                break;
            }
            case lightsOff: {
                holder.mImage.setImageResource(R.drawable.light_off);
                break;
            }
            case stateHome: {
                holder.mImage.setImageResource(R.drawable.home);
                break;
            }
            case stateAway: {
                holder.mImage.setImageResource(R.drawable.away);
                break;
            }
            case energySaving: {
                holder.mImage.setImageResource(R.drawable.energy_saving);
                break;
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ScenariosService.class);
                Bundle data = new Bundle();
                data.putSerializable("action",scenarios.get(position).getAction());
                intent.putExtras(data);

                v.getContext().startService(intent);
            }
        });
    }
}
