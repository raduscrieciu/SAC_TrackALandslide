package com.example.trackalandslide;

public class Earthquake {
	private double longitude, latitude, magnitude;
	
	public Earthquake(double longitude, double latitude, double magnitude){
		this.longitude=longitude;
		this.latitude=latitude;
		this.magnitude=magnitude;
	}
	
	public Earthquake(){
		
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}
	
	
}
