package com.socioty.smartik;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socioty.smartik.Model.Scenario;
import com.socioty.smartik.Model.ScenarioAction;

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
}
