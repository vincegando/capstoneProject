package capstone.cs189.com.smartnetwork.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import capstone.cs189.com.smartnetwork.R;

public class HeatMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public GoogleMap mMap;
    private MapFragment mapFragment;
    private final static int MY_REQUEST = 6;
    protected  GoogleApiClient mGoogleApiClient;
    private double lat;
    private double lon;
    private Location mLocation;
    private Marker marker, pin;
    private FloatingActionMenu floatingActionMenu, fab_test_menu;
    private com.github.clans.fab.FloatingActionButton fab1, fab2, fab3, fab4, fab_test_1, fab_test_2;
    private boolean isPlacingPin = false;
    private boolean isPlacingRouter = false;
    private HeatmapTileProvider provider;
    private TileOverlay overlay;
    private ArrayList<WeightedLatLng> list, mDynamicList, mTestPinList;
    private boolean isScaled = true , isScaledFar = true, isScaledFarther = true, isScaledAway = true;
    private LatLng currentPinLocation;
    private LatLng routerLoaction;

    private float zoomLevel = 20.0f;

    protected static final String TAG = "HEAT MAP ACTIVITY";

    private ProgressDialog progressDialog;

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
        fab_test_menu = (FloatingActionMenu) findViewById(R.id.fab_menu_start_test);
        fab_test_1 = (FloatingActionButton) findViewById(R.id.fab_menu_item_test_1);
        fab_test_2 = (FloatingActionButton) findViewById(R.id.fab_menu_item_test_2);

        fab_test_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_test_menu.close(true);
               // mDynamicList.add(new WeightedLatLng(currentPinLocation, 0.3));
                mTestPinList.add(new WeightedLatLng(currentPinLocation, 0.5));
                addHeatMap3();
                //fab_test_menu.setVisibility(View.GONE);
                fab_test_menu.animate().translationY(floatingActionMenu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                floatingActionMenu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            }
        });

        fab_test_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_test_menu.close(true);
                if (pin != null) {
                    pin.remove();
                }
                //fab_test_menu.setVisibility(View.GONE);
                fab_test_menu.animate().translationY(floatingActionMenu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                floatingActionMenu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            }
        });

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fab1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item1);
        fab2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item2);
        fab3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item3);
        fab4 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item4);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                if (!isPlacingRouter) {
                    isPlacingRouter = true;
                }
                else {

                }
                Toast.makeText(getApplicationContext(), "Tap to place pin at router location", Toast.LENGTH_SHORT).show();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                if (isPlacingRouter) {
                    isPlacingRouter = false;
                    isPlacingPin = true;
                }
                else {
                    isPlacingPin = true;
                }

                floatingActionMenu.animate().translationY(floatingActionMenu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                fab_test_menu.animate().translationY(floatingActionMenu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                Toast.makeText(getApplicationContext(), "Tap to place pin at test location", Toast.LENGTH_SHORT).show();
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                new SaveHeatMapAsync().execute();
            }
        });

        mDynamicList = new ArrayList<>();
        mTestPinList = new ArrayList<>();


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
        else if (id == R.id.action_load_heat_map) {
            new LoadHeatMapAsync().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(HeatMapActivity.this, HomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_speed_test) {
            Intent intent = new Intent(HeatMapActivity.this, SpeedTestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Log.d("MAP READY", "MAP IS READY");
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
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
        if (marker != null) {
            marker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("My location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
        Log.d(TAG, "Connected to GoogleApiClient");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        lat = mLocation.getLatitude();
        lon = mLocation.getLongitude();
        Log.d(TAG, "lat :" + mLocation.getLatitude() + " lon: " + mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 19.6f));
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("My location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                floatingActionMenu.close(true);

                if (isPlacingPin) {
                    currentPinLocation = new LatLng(latLng.latitude, latLng.longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Pin at lat: " + latLng.latitude + " lon: " + latLng.longitude).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Log.d(TAG, "New pin drop lat: " + latLng.latitude + " lon: " + latLng.longitude);
                    pin = mMap.addMarker(markerOptions);
                    isPlacingPin = false;
                    fab_test_menu.setVisibility(View.VISIBLE);
                    fab_test_menu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
                } else if (isPlacingRouter) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("My Router").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    Log.d(TAG, "New pin drop lat: " + latLng.latitude + " lon: " + latLng.longitude);
                    pin = mMap.addMarker(markerOptions);
                    isPlacingRouter = false;
                }
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("ZOOM", "Zoom: " + cameraPosition.zoom);
                float f = cameraPosition.zoom;
                if (f == 21.0f && isScaled) {
                    if (provider == null) {

                    } else {
                        isScaled = false;
                        isScaledFar = true;
                        isScaledAway = true;
                        isScaledFarther = true;
                        provider.setRadius(130);
                        //overlay.clearTileCache();
                        overlay.remove();
                        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

                    }

                } else if (f < 21.0f && f > 20.5f && isScaledFar) {
                    if (provider == null) {

                    } else {
                        isScaled = true;
                        isScaledFar = true;
                        isScaledFarther = true;
                        isScaledAway = true;
                        provider.setRadius(70);
                        overlay.clearTileCache();
                    }
                } else if (f <= 20.0f && f >= 19.0f && isScaledFarther) {
                    if (provider == null) {

                    } else {
                        isScaledFarther = true;
                        isScaled = true;
                        isScaledFar = true;
                        isScaledAway = true;
                        provider.setRadius(30);
                        overlay.clearTileCache();
                    }
                } else if (f < 19.0f && f > 16.0f && isScaledAway) {
                    if (provider == null) {

                    } else {
                        isScaledAway = false;
                        isScaledFarther = true;
                        isScaled = true;
                        isScaledFar = true;
                        provider.setRadius(20);
                        overlay.clearTileCache();
                    }
                }

                /*if (cameraPosition.zoom < zoomLevel) {
                    if (provider == null) {

                    } else {
                        overlay.remove();
                        provider.setRadius(50);
                        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                    }
                }
                else {
                    overlay.remove();
                    provider.setRadius(100);
                    overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                }*/
            }
        });
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

    private void addHeatMap() {
        list = new ArrayList<>();
        list.add(new WeightedLatLng(new LatLng(34.414742005466145, -119.85553208738565), 0.3));
       list.add(new WeightedLatLng(new LatLng(34.414742005466145, -119.8556376993656), 0.2));
        list.add(new WeightedLatLng(new LatLng(34.41466511282376, -119.85563702881335), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.41464215565424, -119.85556226223709), 0.9));
        list.add(new WeightedLatLng(new LatLng(34.41470106982359, -119.85552404075861), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.414693878424856, -119.85558740794657), 0.5));
        list.add(new WeightedLatLng(new LatLng(34.41473564384734, -119.85557265579702), 0.2));
        list.add(new WeightedLatLng(new LatLng(34.414703835746025, -119.85563535243273), 0.4));
        list.add(new WeightedLatLng(new LatLng(34.41464796409531, -119.85560383647679), 0.7));
        list.add(new WeightedLatLng(new LatLng(34.414672580817296, -119.85554683953524), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.41465653845998, -119.85555287450552), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.414737579992234, -119.85561087727548), 0.2));
        list.add(new WeightedLatLng(new LatLng(34.41471766535677, -119.8556024953723), 0.3));
        list.add(new WeightedLatLng(new LatLng(34.414709920775024, -119.85555790364742), 0.5));

        provider = new HeatmapTileProvider.Builder().weightedData(list).radius(50).opacity(0.5).build();
        provider.setRadius(30);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    private void addHeatMap2() {

        if (overlay != null) {
            overlay.remove();
        }
        provider = new HeatmapTileProvider.Builder().weightedData(mDynamicList).radius(50).opacity(0.5).build();
        provider.setRadius(200);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        MarkerOptions markerOptions = new MarkerOptions().position(routerLoaction).title("My Router").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
       // Log.d(TAG, "New pin drop lat: " + latLng.latitude + " lon: " + latLng.longitude);
        pin = mMap.addMarker(markerOptions);
    }

    private void addHeatMap3() {

        if (overlay != null) {
            overlay.remove();
        }
        provider = new HeatmapTileProvider.Builder().weightedData(mTestPinList).radius(50).opacity(0.5).build();
        provider.setRadius(100);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
       // MarkerOptions markerOptions = new MarkerOptions().position(currentPinLocation).title("Test Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        // Log.d(TAG, "New pin drop lat: " + latLng.latitude + " lon: " + latLng.longitude);
       // pin = mMap.addMarker(markerOptions);
    }


    private class SaveHeatMapAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog =  new ProgressDialog(HeatMapActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Saving Heat Map...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Heat Map successfully saved!", Toast.LENGTH_SHORT).show();

        }
    }

    private class LoadHeatMapAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog =  new ProgressDialog(HeatMapActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Loading Heat Map...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            getHeatMapPointsFromBackend();
            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //HeatMapService();

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Heat Map successfully loaded!", Toast.LENGTH_SHORT).show();
            addHeatMap2();
        }
    }

    private void HeatMapService() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://cs1.smartrg.link:3000/heatmaps/1.json";
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject json = new JSONObject(response);
                                String s = json.optString("created_at");
                                Log.d("JSON IS HERE: ", s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                       @Override
                        public void onErrorResponse(VolleyError error) {
                           error.printStackTrace();
                       }
                    });
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getHeatMapPointsFromBackend() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //String url = "http://cs1.smartrg.link:3000/heatmap_points/1.json";
        final ArrayList<WeightedLatLng> testPointList = new ArrayList<>();
        for (int i = 5; i < 20; i++) {
            String url = "http://cs1.smartrg.link:3000/heatmap_points/" + (i+1) + ".json";
            try {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    String lat = json.optString("latitude");
                                    String lon = json.optString("longitude");
                                    String intensity = json.optString("jitter");
                                    double latitude = Double.parseDouble(lat);
                                    double longitude = Double.parseDouble(lon);
                                    double intens = Double.parseDouble(intensity);
                                    //Log.d("FFFFFFFFFFFFFFFFFF", "LATITUDE: " + lat);
                                    //Log.d("FFFFFFFFFFFFFFFFFF", "LONGITUDE: " + lon);

                                    mDynamicList.add(new WeightedLatLng(new LatLng(latitude, longitude), intens));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                requestQueue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String url2 = "http://cs1.smartrg.link:3000/routers/2.json";
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject json = new JSONObject(response);
                                String lat = json.optString("latitude");
                                String lon = json.optString("longitude");
                                double latitude = Double.parseDouble(lat);
                                double longitude = Double.parseDouble(lon);
                                routerLoaction = new LatLng(latitude, longitude);
                                //Log.d("FFFFFFFFFFFFFFFFFF", "LATITUDE: " + lat);
                                //Log.d("FFFFFFFFFFFFFFFFFF", "LONGITUDE: " + lon);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
