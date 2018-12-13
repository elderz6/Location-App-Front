package com.todo.elder.aaaaaaaaaaaaaa;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView textView;
    private LocationManager location;
    private LocationListener listener;
    private Button button;
    private String locationProvider;

    private MarkerOptions userMarker = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textView = findViewById(R.id.textView2);
        button = findViewById(R.id.locBtn);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        location = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            location.requestLocationUpdates(locationProvider, 0, 0, listener);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("------------------------------------------------------------------------");
                final double latitude = location.getLastKnownLocation(locationProvider).getLatitude();
                final double longitude = location.getLastKnownLocation(locationProvider).getLongitude();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(""+latitude+ "\n" +longitude);
                    }
                });
                LatLng userLocal = new LatLng(latitude, longitude);
                mMap.clear();
                mMap.addMarker(userMarker.position(userLocal).title("Your Position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocal));
                mMap.moveCamera(CameraUpdateFactory.zoomBy(mMap.getMaxZoomLevel() - 7));
                SendRequest(latitude, longitude);
            }
        });
    }
    public void getPosition(View view){
        String requestUrl = "http://192.168.137.1:3000/api/user";
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                Random ran = new Random();
                //convert response json to java usable data
                String[] coordenadas = response.split(":");
                JSONObject stuff = new JSONObject();
                LatLng friendsLocal = new LatLng(Double.parseDouble(coordenadas[0]),
                        Double.parseDouble(coordenadas[1]));
                mMap.addMarker(new MarkerOptions().position(friendsLocal)
                        .title("Friend position")
                        .icon(BitmapDescriptorFactory.defaultMarker(ran.nextInt(350)+ 1)));
                final TextView tv = findViewById(R.id.textView3);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("response");
                    }
                });
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
    public void SendRequest(final double latitude, final double longitude){
        String requestUrl = "http://192.168.137.1:3000/api/user";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Volley Result", ""+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("latitude", ""+latitude);
                postMap.put("longitude", ""+longitude);
                postMap.put("name", "tester1");
                return postMap;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
    public void BackAct(View view){
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}