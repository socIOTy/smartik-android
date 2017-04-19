package com.socioty.smartik.model;

import java.util.List;

import cloud.artik.model.Device;

/**
 * Created by Willian on 2017-04-19.
 */

public class DeviceAdapter {

    private final Device device;
    private final int floorNumber;
    private final Room room;

    public DeviceAdapter(final Device device) {
        int tempFloorNumber = -1;
        Room tempRoom = null;
        final List<Floor> floors = Token.sToken.getDeviceMap().getFloors();
        for (int k = 0; k < floors.size(); k++) {
            for (final Room room : floors.get(k).getRooms()) {
                if (room.getDeviceIds().contains(device.getId())) {
                    tempRoom = room;
                    tempFloorNumber = k;
                    break;
                }
            }
            if (tempRoom != null) {
                break;
            }
        }

        this.device = device;
        this.floorNumber = tempFloorNumber;
        this.room = tempRoom;
    }

    public Device getDevice() {
        return device;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public Room getRoom() {
        return room;
    }
}
