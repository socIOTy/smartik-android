package com.socioty.smartik.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Floor {
	private static final Comparator<Room> roomComparatorByName = new Comparator<Room>() {
		@Override
		public int compare(final Room o1, final Room o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	private Set<Room> rooms;

	protected Floor() {
	}

	public Floor(final Set<Room> rooms) {
		this.rooms = new HashSet<>(rooms);
	}
	
	public Set<Room> getRooms() {
		return Collections.unmodifiableSet(rooms);
	}

	public void addRoom(final Room room) {
		this.rooms.add(room);
	}

	public List<Room> getRoomsList() {
		final List<Room> result = new ArrayList<>(rooms);
		Collections.sort(result, roomComparatorByName);
		return Collections.unmodifiableList(result);
	}

	public boolean removeRoom(final Room room) {
		return this.rooms.remove(room);
	}

	public int countRooms() {
		return rooms.size();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append("rooms: ").append(rooms).toString();
	}
}
