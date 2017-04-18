package com.socioty.smartik.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Floor {

	private Set<Room> rooms;

	protected Floor() {
	}

	public Floor(final Set<Room> rooms) {
		this.rooms = new HashSet<>(rooms);
	}
	
	public Set<Room> getRooms() {
		return Collections.unmodifiableSet(rooms);
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
