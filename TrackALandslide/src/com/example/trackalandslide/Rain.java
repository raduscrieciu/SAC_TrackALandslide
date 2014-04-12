package com.example.trackalandslide;

public class Rain {
	double latitude, longitude, rainIndex;
	
	public Rain(double latitude, double longitude, double rainIndex){
		this.latitude=latitude;
		this.longitude=longitude;
		this.rainIndex=rainIndex;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getRainIndex() {
		return rainIndex;
	}

	public void setRainIndex(double rainIndex) {
		this.rainIndex = rainIndex;
	}
}
