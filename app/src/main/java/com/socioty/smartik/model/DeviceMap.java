package com.socioty.smartik.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Willian on 2017-04-05.
 */

public class DeviceMap {

    private static final Comparator<Room> roomComparatorByName = new Comparator<Room>() {
        @Override
        public int compare(final Room o1, final Room o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

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

    public int countRooms() {
        int result = 0;
        for (final Floor floor : floors) {
           result += floor.countRooms();
        }
        return result;
    }

    public List<Room> getAllRooms() {
        final List<Room> result = new ArrayList<>(countRooms());
        for (final Floor floor : floors) {
            final List<Room> sortedRoomsByFloor = new ArrayList<>(floor.getRooms());
            Collections.sort(sortedRoomsByFloor, roomComparatorByName);
            result.addAll(sortedRoomsByFloor);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("floors: ").append(floors).toString();
    }
}
