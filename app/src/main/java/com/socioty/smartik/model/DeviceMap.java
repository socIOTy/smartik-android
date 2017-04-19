package com.socioty.smartik.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public void addRoom(final int floorNumber, final String name) {
        final int currentFloorsSize = floors.size();
        if (floorNumber >= currentFloorsSize) {
            for (int k = currentFloorsSize; k <= floorNumber; k++) {
                floors.add(new Floor(Collections.<Room> emptySet()));
            }
        }
        floors.get(floorNumber).addRoom(new Room(name, Collections.<String>emptySet()));
    }

    public Room getRoom(final String name) {
        for (final Floor floor : floors) {
            for (final Room room : floor.getRooms()) {
                if (room.getName().equals(name)) {
                    return room;
                }
            }
        }
        return null;
    }

    public boolean removeRoom(final Room roomToDelete) {
        for (final Floor floor : floors) {
            for (final Room room : floor.getRooms()) {
                if (room.equals(roomToDelete)) {
                    return floor.removeRoom(room);
                }
            }
        }
        return false;
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
            result.addAll(floor.getRoomsList());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("floors: ").append(floors).toString();
    }
}
