package capstone.cs189.com.smartnetwork.Classes;

import com.google.android.gms.maps.model.LatLng;

public class TestPoint {

    private double latitude;
    private double longitude;
    private double intensity;
    private String testInfo;

    public TestPoint() {
    }

    public TestPoint(double lat, double lon) {
        latitude = lat;
        longitude = lon;
        testInfo = "";
    }

    public void setLocation(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double i) {
        intensity = i;
    }

    public String getTestInfo() {
        return testInfo;
    }
}
