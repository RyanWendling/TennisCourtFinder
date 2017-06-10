package ryanwendling.tenniscourtfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ryanwendling.tenniscourtfinder.database.MyDBHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    ZoomControls zoom;
    Button geoLocationBt;
    Button clear;
    Button addBt;
    Button clearPathBt;
    Double myLatitude = null;
    Double myLongitude = null;
    Boolean addFlag = Boolean.FALSE;

    Boolean markerClicked;
    public PolylineOptions rectOptions;
    public Polyline line;

    Button infoButton;

    Button saveBt;
    Boolean saveFlag = Boolean.FALSE;

    // helps with finding AND using my location
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    protected static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        markerClicked = Boolean.FALSE;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(15 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        // will balance power activity on real device

        // zoom feature for map fragment
        zoom = (ZoomControls) findViewById(R.id.zcZoom);
        zoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        zoom.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        addBt = (Button) findViewById(R.id.btAdd);
        final Drawable d = addBt.getBackground();
        addBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addFlag == false) {
                    addFlag = true;
                    addBt.setBackgroundColor(Color.GREEN);
                } else {
                    addFlag = false;
                    addBt.setBackgroundDrawable(d);

                }
            }
        });

        saveBt = (Button) findViewById(R.id.saveDBBt);
        final Drawable e = saveBt.getBackground();
        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveFlag == false) {
                    saveFlag = true;
                    saveBt.setBackgroundColor(Color.GREEN);
                } else {
                    saveFlag = false;
                    saveBt.setBackgroundDrawable(e);

                }
            }
        });

        final Spinner spCourts = (Spinner) findViewById(R.id.spinnerCourts);
        ArrayList<String> data = new ArrayList<>();
        data.add("Bellingham High School, WA");
        data.add("Corwall Park");
        data.add("Western Washington University");
        data.add("Sehome High School");
        data.add("Whatcom Community College");
        data.add("Whatcom Falls Park");
        data.add("Bellingham Tennis Club");
        data.add("Squalicum High School");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.spinnerlayout, data);
        spCourts.setAdapter(adapter);
        String spinText = spCourts.getSelectedItem().toString();
        EditText searchText = (EditText) findViewById(R.id.etLocationEntry);
        searchText.setText(spinText);

        spCourts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String spinText = spCourts.getSelectedItem().toString();
                EditText searchText = (EditText) findViewById(R.id.etLocationEntry);
                searchText.setText(spinText);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        geoLocationBt = (Button) findViewById(R.id.btSearch);
        geoLocationBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchText = (EditText) findViewById(R.id.etLocationEntry);
                String search = searchText.getText().toString();

                if (search != null && !search.equals("")){
                    List<android.location.Address> addressList = null;
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        if (search.equals("Bellingham Tennis Club")) {
                            addressList = geocoder.getFromLocationName("washington state department of ecology", 1);
                        } else {
                            addressList = geocoder.getFromLocationName(search, 1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                    // put number of courts in title, from spinner or array or custom class?
                    if (search.equals("Bellingham High School, WA")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("5 courts"));
                    }
                    else if (search.equals("Corwall Park")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("2 courts"));
                    }
                    else if (search.equals("Western Washington University")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("7 courts"));
                    }
                    else if (search.equals("Sehome High School")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("6 courts"));
                    }
                    else if (search.equals("Whatcom Community College")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("4 courts"));
                    }
                    else if (search.equals("Whatcom Falls Park")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("2 courts"));
                    }
                    else if (search.equals("Bellingham Tennis Club")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("10 courts"));
                    }
                    else if (search.equals("Squalicum High School")) {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("6 courts"));
                    }
                    else {
                        mMap.addMarker(new MarkerOptions().position(latlng).title("from geocoder"));
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }
        });

        clear = (Button) findViewById(R.id.btClear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                if (line != null) {
                    line.remove();
                }
                markerClicked = false;
            }
        });

        clearPathBt = (Button) findViewById(R.id.btClearPath);
        clearPathBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (line != null) {
                    line.remove();
                }
                markerClicked = false;
            }
        });


        infoButton = (Button)findViewById(R.id.infoBt);
        infoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder a_builder = new AlertDialog.Builder(MapsActivity.this);
                        a_builder.setMessage("The spinner at the top holds all the known tennis courts in Bellingham. Select one of these locations and press \"GO\"," +
                                " a marker will be built at the location selected. Clicking on that marker will show the number of courts at the location.\n " +
                                "The user can also add markers wherever by toggling the \"ADD MARKS\" button. With this button toggled, clicking anywhere on the map will " +
                                "create a new marker that displays its latitude and longitude cordinate when clicked on.\n Other features include a button to find the user's location, paths, zooming" +
                                " and a database with which to save additional markers. The database options include a \"SAVE MARKS TO DB\" button, that when toggled will add" +
                                " marker locations to a database when the user clicks on the existing marker. We can also show and clear the database.");
                        AlertDialog alert = a_builder.create();
                        alert.setTitle("Here's how Tennis Court Finder works: ");
                        alert.show();
                    }
                }
        );
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(48.7519, -122.4787);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Bellingham"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (addFlag == true) {
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Lat " + mMap.getMyLocation().getLatitude() + " "
                            + "Long " + mMap.getMyLocation().getLongitude()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        });


        // if permissions granted, enable user location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);

            // allows user to give permissions in runtime
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker marker) {

                if (saveFlag == Boolean.TRUE) {
                    double thisLat = marker.getPosition().latitude;
                    double thisLng = marker.getPosition().longitude;
                    markLatLng thisLatLng = new markLatLng(thisLat, thisLng);
                    //MyDBHelper.addLatLng(thisLatLng);
                    newMarkerEntry(findViewById(android.R.id.content), thisLatLng);
                }


                if (markerClicked) {

                    marker.showInfoWindow();

                    if (line != null) {
                        line.remove();
                        line = null;
                    }


                        rectOptions.add(marker.getPosition());
                        rectOptions.color(Color.YELLOW);
                        line = mMap.addPolyline(rectOptions);

                } else {
                    if (line != null) {
                        line.remove();
                        line = null;
                    }

                    marker.showInfoWindow();

                    rectOptions = new PolylineOptions().add(marker.getPosition());
                    markerClicked = true;
                }


                return true;
            }

        });

    }

    // we passed in request code '101', address specific request codes specially, if user denies permissions, give toast
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i (TAG, "Connection Suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public void newMarkerEntry (View view, markLatLng aMark) {
        MyDBHelper dbHandler = new MyDBHelper(this, null, null, 1);

        dbHandler.addLatLng(aMark);
    }

    public void lookupMarkers (View view) {
        MyDBHelper dbHandler = new MyDBHelper(this, null, null, 1);

        String allstuff =
                dbHandler.findLatLng();

        AlertDialog.Builder a_builder = new AlertDialog.Builder(MapsActivity.this);
        a_builder.setMessage(allstuff);
        AlertDialog alert = a_builder.create();
        alert.setTitle("Result: ");
        alert.show();
    }
    public void callDeleteAll (View view) {
        MyDBHelper dbHandler = new MyDBHelper(this, null, null, 1);

        dbHandler.deleteAll();

        AlertDialog.Builder a_builder = new AlertDialog.Builder(MapsActivity.this);
        a_builder.setMessage("all entries deleted");
        AlertDialog alert = a_builder.create();
        alert.setTitle("Result: ");
        alert.show();
    }

}
