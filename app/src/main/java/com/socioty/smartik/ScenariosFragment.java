package com.socioty.smartik;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socioty.smartik.model.Scenario;
import com.socioty.smartik.model.ScenarioAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class ScenariosFragment extends Fragment {

    public static ScenariosFragment newInstance() {

        Bundle args = new Bundle();

        ScenariosFragment fragment = new ScenariosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("scenarioAction");
        getContext().registerReceiver(actionReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_scenarios, container, false);

        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.scenarios_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        List<Scenario> scenarios = new ArrayList<>();
        scenarios.add(new Scenario("Turn off the lights", ScenarioAction.lightsOff));
        scenarios.add(new Scenario("Turn on the lights", ScenarioAction.lightsOn));
        scenarios.add(new Scenario("Energy saving mode", ScenarioAction.energySaving));
        scenarios.add(new Scenario("Mode \"Home\"", ScenarioAction.stateHome));
        scenarios.add(new Scenario("Mode \"Away\"", ScenarioAction.stateAway));
        ScenarioListAdapter adapter = new ScenarioListAdapter(scenarios);
        recyclerView.setAdapter(adapter);

        return v;
    }

    private final BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            String message = data.getString("broadcastMessage");

            Snackbar.make(getView(),message, Snackbar.LENGTH_SHORT).show();
        }
    };

}
