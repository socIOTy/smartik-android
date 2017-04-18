package com.socioty.smartik.Model;

/**
 * Created by serhiipianykh on 2017-04-17.
 */

public class Scenario {

    private final String name;
    private final ScenarioAction action;

    public Scenario(String name, ScenarioAction action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public ScenarioAction getAction() {
        return action;
    }
}
