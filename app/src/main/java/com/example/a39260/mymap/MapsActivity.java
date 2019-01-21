package com.example.a39260.mymap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    RequestQueue queue;
    private GoogleMap mMap;
    Context mContext = this;
    //Hold latitudes and longitudes and levels for quakes and volcanoes
    ArrayList<String> latitudes = new ArrayList<>();
    ArrayList<String> longitudes = new ArrayList<>();
    ArrayList<String> Levels = new ArrayList<>();
    ArrayList<String> volcanoes = new ArrayList<>();
    ArrayList<String> quakes= new ArrayList<>();
    double myLatitude=0;
    double myLongitude=0;
    float zoom=6;
    double dangerDistance=250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        queue = Volley.newRequestQueue(getApplicationContext());
        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final EditText txtZoom=(EditText)findViewById(R.id.txtZoom);
        final EditText txtSize=(EditText)findViewById(R.id.txtSize);
        final TextView txtLati=(TextView)findViewById(R.id.myLati);
        final TextView txtLong=(TextView)findViewById(R.id.myLong);
        final Button btnVolcano = (Button) findViewById(R.id.btnVolcano);
        String url = "https://api.geonet.org.nz/volcano/val";
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("features");
                    for (int index = 0; index < data.length(); index++) {
                        JSONObject quake = data.getJSONObject(index);
                        JSONObject properties = quake.getJSONObject("properties");

                        //add volcano title to the list
                        volcanoes.add(properties.getString("volcanoTitle"));
                        Levels.add(properties.getString("level"));
                        JSONObject geometry = quake.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        String longitude = coordinates.getString(0);
                        String latitude = coordinates.getString(1);
                        //add volcano location
                        latitudes.add(latitude);
                        longitudes.add(longitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        String urlQ = "https://api.geonet.org.nz/quake?MMI=5";
        JsonObjectRequest jsonObjectRequestQ = new JsonObjectRequest(Request.Method.GET, urlQ, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("features");
                    for (int index = 0; index < data.length(); index++) {

                        JSONObject quake = data.getJSONObject(index);
                        JSONObject properties = quake.getJSONObject("properties");

                        //add locality of quake
                        quakes.add(properties.getString("locality"));
                        Levels.add(properties.getString("magnitude"));
                        JSONObject geometry = quake.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        String longitude = coordinates.getString(0);
                        String latitude = coordinates.getString(1);
                        //add quake location
                        latitudes.add(latitude);
                        longitudes.add(longitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
       queue.add(jsonObjectRequest);
        queue.add(jsonObjectRequestQ);
        btnVolcano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Task<Location> location = client.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                          if(task.getResult()!=null){
                              mMap.clear();
                              //solve the too big or too small zoon and distance
                              if (Float.valueOf(txtZoom.getText().toString())>25    ){
                                  txtZoom.setText("25");
                                  }
                              if (Float.valueOf(txtZoom.getText().toString())<1    ){
                                  txtZoom.setText("1");
                              }
                              if ( Double.valueOf(txtSize.getText().toString())>1000 ){
                                  txtSize.setText("1000");
                              }
                              if ( Double.valueOf(txtSize.getText().toString())<1 ){
                                  txtSize.setText("1");
                              }
                              zoom=Float.valueOf(txtZoom.getText().toString());
                              dangerDistance=Double.valueOf(txtSize.getText().toString());
                              //get my location
                              System.err.println(task.getResult().getLatitude());
                              myLatitude=   task.getResult().getLatitude();
                              myLongitude =   task.getResult().getLongitude();
                              txtLati.setText(String.valueOf(myLatitude));
                              txtLong.setText(String.valueOf( myLongitude));
                              LatLng currentLL = new LatLng(myLatitude, myLongitude);
                              Geocoder geocoder = new Geocoder(getApplicationContext());
                                try {
                                    //get my address
                                    List<Address> addressList = geocoder.getFromLocation(myLatitude,myLongitude, 1);
                                    String address ="I am in: "+ addressList.get(0).getLocality()+" ";
                                    address += addressList.get(0).getCountryName();
                                    //add marker at my location
                                    mMap.addMarker(new MarkerOptions().position(currentLL).title(address));
                                    //move camera to my location
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLL, zoom));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //loop all volcanoes
                              for(int index = 0; index < volcanoes.size(); index++) {
                                  LatLng volcano = new LatLng(Double.valueOf( latitudes.get(index)), Double.valueOf( longitudes.get(index)));
                                  double distanceToVolcano=Math.sqrt((Double.valueOf( latitudes.get(index))-myLatitude)*(Double.valueOf( latitudes.get(index))-myLatitude)*12544
                                          +(Double.valueOf( longitudes.get(index))-myLongitude)*(Double.valueOf( longitudes.get(index))-myLongitude)*8100);
                                 //if the volcano is active
                                  if (Double.valueOf( Levels.get(index))>0 ){
                                      //if the volcano is in the dangerDistance that you set.
                                      if(distanceToVolcano< dangerDistance){
                                          mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.volcano2)).position(volcano).title(volcanoes.get(index)));}
                                      else {
                                          mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.volcano1)).position(volcano).title(volcanoes.get(index)));
                                      }
                                  }
                                  else {
                                      mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.volcano0)).position(volcano).title(volcanoes.get(index)));
                                  }
                              }
                              for(int index = 0; index < quakes.size(); index++) {
                                  LatLng quake = new LatLng(Double.valueOf( latitudes.get(index+volcanoes.size())), Double.valueOf( longitudes.get(index+volcanoes.size())));
                                  double aaa=Math.sqrt((Double.valueOf( latitudes.get(index+volcanoes.size()))-myLatitude)*(Double.valueOf( latitudes.get(index+volcanoes.size()))-myLatitude)*12544
                                          +(Double.valueOf( longitudes.get(index+volcanoes.size()))-myLongitude)*(Double.valueOf( longitudes.get(index+volcanoes.size()))-myLongitude)*8100);


                                  //if the quake's magnitude is more than 5.
                                  if (Double.valueOf( Levels.get(index+volcanoes.size()))>5 ){
                                      if(aaa< dangerDistance){
                                          //if the quake is in the dangerDistance that you set.
                                          mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.quake2)).position(quake).title(quakes.get(index)));}
                                      else {
                                          mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.quake1)).position(quake).title(quakes.get(index)));
                                      }
                                  }
                                  else {
                                      mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.quake0)).position(quake).title(quakes.get(index)));
                                  }
                              }
                            }
                          }
                        });
                }
                catch(SecurityException ex)
                {
                    ex.printStackTrace();
                }

        }

        });
        LocationRequest req = new LocationRequest();
        req.setInterval(10000); // 10 seconds
        req.setFastestInterval(5000); // 5 seconds
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client.requestLocationUpdates(req,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e("location:",locationResult.getLastLocation().toString());
            }
        },null);
    }

    //Below is the Netword and GPS method to get location
     /*   locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
btnVolcano.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    LatLng currentLL = new LatLng(latitude, longtitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longtitude, 1);
                        String address ="My position: "+ addressList.get(0).getLocality()+" ";
                        address += addressList.get(0).getCountryName();
                        mMap.addMarker(new MarkerOptions().position(currentLL).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLL, 7));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
                @Override
                public void onProviderEnabled(String provider) {
                }
                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    LatLng currentLL = new LatLng(latitude, longtitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longtitude, 1);
                        String address ="My position: "+ addressList.get(0).getLocality()+" ";
                        address += addressList.get(0).getCountryName();
                        mMap.addMarker(new MarkerOptions().position(currentLL).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLL, 10));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
                @Override
                public void onProviderEnabled(String provider) {
                }
                @Override
                public void onProviderDisabled(String provider) {
                }
            }
            );
        }

        }
    }
});
    }
*/

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
        //   LatLng sydney = new LatLng(-34, 151);
        //    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10));
    }
}
