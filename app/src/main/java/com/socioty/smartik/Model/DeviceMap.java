package com.socioty.smartik.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Willian on 2017-04-05.
 */

public class DeviceMap {

    private List<Floor> floors;

    protected DeviceMap() {
    }

    public DeviceMap(final List<Floor> floors) {
        this.floors = new ArrayList<>(floors);
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public void setFloors(final List<Floor> floors) {
        this.floors = floors;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("floors: ").append(floors).toString();
    }
}
