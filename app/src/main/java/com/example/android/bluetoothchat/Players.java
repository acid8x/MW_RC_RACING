package com.example.android.bluetoothchat;

import android.support.annotation.NonNull;

public class Players implements Comparable<Players> {

	private int id;
	private String name;
	private int totalGates;
	private int totalKills;
	private int totalDeaths;
	private int totalLaps;
	private int nextGate;
	private int truckColorR;
	private int truckColorG;
	private int truckColorB;
	private boolean TurboAvailable;
	private boolean GunAvailable;

	public Players(int id) {
		this.id = id;
		this.name = "TRUCK";
		this.totalGates = 0;
		this.totalKills = 0;
		this.totalDeaths = 0;
		this.totalLaps = -1;
		this.nextGate = 1;
		this.truckColorR = 0;
		this.truckColorG = 0;
		this.truckColorB = 0;
		this.TurboAvailable = true;
		this.GunAvailable = true;
	}

	public int compareTo(@NonNull Players other) { // RaceType: (1)Race (2)Race with guns (3)Guns only
		if (MainActivity.raceType < 3) {
			if (other.totalGates < this.totalGates) return -1;
			if (other.totalGates > this.totalGates) return 1;
		} else {
			if (other.totalKills < this.totalKills) return -1;
			if (other.totalKills > this.totalKills) return 1;
		}
		return 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotalGates() {
		return totalGates;
	}

	public void setTotalGates(int totalGates) {
		this.totalGates = totalGates;
	}

	public int getTotalKills() {
		return totalKills;
	}

	public void setTotalKills(int totalKills) {
		this.totalKills = totalKills;
	}

	public int getTotalDeaths() {
		return totalDeaths;
	}

	public void setTotalDeaths(int totalDeaths) {
		this.totalDeaths = totalDeaths;
	}

	public int getTotalLaps() {
		return totalLaps;
	}

	public void setTotalLaps(int totalLaps) {
		this.totalLaps = totalLaps;
	}

	public int getNextGate() {
		return nextGate;
	}

	public void setNextGate(int nextGate) {
		this.nextGate = nextGate;
	}

	public int getTruckColorR() {
		return truckColorR;
	}

	public void setTruckColorR(int truckColorR) {
		this.truckColorR = truckColorR;
	}

	public int getTruckColorG() {
		return truckColorG;
	}

	public void setTruckColorG(int truckColorG) {
		this.truckColorG = truckColorG;
	}

	public int getTruckColorB() {
		return truckColorB;
	}

	public void setTruckColorB(int truckColorB) {
		this.truckColorB = truckColorB;
	}

	public boolean isTurboAvailable() {
		return TurboAvailable;
	}

	public void setTurboAvailable(boolean turboAvailable) {
		TurboAvailable = turboAvailable;
	}

	public boolean isGunAvailable() {
		return GunAvailable;
	}

	public void setGunAvailable(boolean gunAvailable) {
		GunAvailable = gunAvailable;
	}

    public String getTruckInfo() {
		String returnedString = id + "," + name + "," + totalGates + "," + totalKills + "," + totalDeaths + "," + totalLaps + "," + nextGate + ",";
		if (TurboAvailable) returnedString += '1';
		else returnedString += '0';
		returnedString += ',';
		if (GunAvailable) returnedString += '1';
		else returnedString += '0';
		return returnedString;
	}
}