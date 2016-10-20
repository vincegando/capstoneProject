package capstone.cs189.com.smartnetwork;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class HeatMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public GoogleMap mMap;
    private MapFragment mapFragment;
    private final static int MY_REQUEST = 6;
    protected  GoogleApiClient mGoogleApiClient;
    private double lat;
    private double lon;
    private Location mLocation;
    private Marker marker;


    protected static final String TAG = "HEAT MAP ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST);
        }
        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, HeatMapActivity.this);
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.heat_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_test) {
            Intent intent = new Intent(HeatMapActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_devices) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Log.d("MAP READY", "MAP IS READY");
        mMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //  permSet = true;

                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(new Criteria(), true);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, HeatMapActivity.this);

                    buildGoogleApiClient();
                    mGoogleApiClient.connect();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Allow location access to view map features", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        marker.remove();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(HeatMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        }
        else
            return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(ContextCompat.checkSelfPermission(HeatMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.disconnect();
        }
        else
            return;
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("FFFFFFFFFFFFFFFFFFFFFF", "Connected to GoogleApiClient");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        lat = mLocation.getLatitude();
        lon = mLocation.getLongitude();
        Log.d("FFFFFFFFFFFFFFFFF", "lat :" + mLocation.getLatitude() + " lon: " + mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 19.6f));
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this.context, "GPS Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(this.context, "GPS Enabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {

    }

}
