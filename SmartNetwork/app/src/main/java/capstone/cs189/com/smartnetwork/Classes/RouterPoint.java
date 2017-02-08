package capstone.cs189.com.smartnetwork.Classes;

import com.google.android.gms.maps.model.LatLng;

public class RouterPoint {

    private double longitude;
    private double latitude;

    public RouterPoint() {
    }

    public RouterPoint(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public void setLocation(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }
}
