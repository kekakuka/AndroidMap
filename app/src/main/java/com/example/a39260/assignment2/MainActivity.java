package com.example.a39260.assignment2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    RequestQueue queue;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(getApplicationContext());




        final TextView txtLatitude = (TextView)findViewById(R.id.txtLatitude);
        final TextView txtLongitude = (TextView)findViewById(R.id.txtLongitude);
        final TextView textView = (TextView) findViewById(R.id.txtView);
        final Button request = (Button) findViewById(R.id.btnOpen);
        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
       /*       try {

                    String provider;
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    List<String> providerList=lm.getProviders(true);
                    if(providerList.contains(LocationManager.GPS_PROVIDER)){
                        provider=LocationManager.GPS_PROVIDER;
                    }
                    else if(providerList.contains(LocationManager.NETWORK_PROVIDER)){
                        provider=LocationManager.NETWORK_PROVIDER;
                    }
                    else{

                        return;
                    }
                    Location location1=lm.getLastKnownLocation(provider);
                    txtLatitude.setText(Double.toString(location1.getLatitude()));
                    txtLongitude.setText(Double.toString(location1.getLongitude()));
                }
                catch(SecurityException ex)
                {
                    ex.printStackTrace();
                }*/
          try {
                    Task<Location> location =client.getLastLocation();

                    location.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.getResult() != null) {
                                txtLatitude.setText(String.valueOf(task.getResult().getLatitude()));
                                txtLongitude.setText(String.valueOf(task.getResult().getLongitude()));
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

/*
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
*/



    }
}
