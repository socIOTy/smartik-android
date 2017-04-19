package com.socioty.smartik.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Room implements Serializable {

	private String name;
	private byte[] imageBytes;
	private Set<String> deviceIds;

	protected Room() {
	}

	public Room(final String name, final Set<String> deviceIds) {
		this.name = name;
		this.deviceIds = new HashSet<>(deviceIds);
	}

	public String getName() {
		return name;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	public Set<String> getDeviceIds() {
		return Collections.unmodifiableSet(deviceIds);
	}
	
	public void addDevice(final String deviceId) {
		this.deviceIds.add(deviceId);
	}
	
	public boolean removeDevice(final String deviceId) {
		return this.deviceIds.remove(deviceId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder
				.append("name: ").append(name).append(", ")
				.append("imageBytes: ").append(imageBytes == null ? null : imageBytes.length).append(", ")
				.append("deviceIds: ").append(deviceIds).append(", ").toString();
	}
}
