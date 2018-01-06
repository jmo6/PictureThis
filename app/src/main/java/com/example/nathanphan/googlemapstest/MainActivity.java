package com.example.nathanphan.googlemapstest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.bloder.magic.view.MagicButton;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    static final public String MY_LOCATION = "location";

    AppInfo appLocation;

    //GoogleMap Object
    GoogleMap mGoogleMap;

    //Database Object
    DatabaseReference databaseReferencePin;

    //Pin class Object
    Pin mPin;

    //Last used Marker Object
    Marker mMarker;

    //Location in Database
    String mId;

    //Pin uploaded switch
    boolean save = false;

    //Database Pins
    private List<Pin> pinList;

    MagicButton button2;
    MagicButton button3;
    MagicButton button4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReferencePin = FirebaseDatabase.getInstance().getReference("pin");
        pinList = new ArrayList<>();
        appLocation = AppInfo.getInstance(this);
        appLocation.setLocationName("");


        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            initMap();
        }

        button2 = (MagicButton) findViewById(R.id.button2);

            button2.setMagicButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        geoLocate(v);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        button3 = (MagicButton) findViewById(R.id.button3);


            button3.setMagicButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadPin(v);
                }
            });

        button4 = (MagicButton) findViewById(R.id.button4);
        button4.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadMap(v);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        //Grab database of pins and uploads them into pinList arraylist
        databaseReferencePin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pinList.clear();
                for(DataSnapshot pinSnapshot : dataSnapshot.getChildren()){
                    Pin pin = pinSnapshot.getValue(Pin.class);
                    pinList.add(pin);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //mGoogleMap.setOnInfoWindowClickListener((GoogleMap.OnInfoWindowClickListener) this);
        goToLocationZoom(37.580126, -122.082823, 16);

    }


    //Changes Map Location
    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }


    //Function to locate Lat and Lng based on name
    public void geoLocate(View view) throws IOException  {

        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();
        if(location.isEmpty()){
            //do nothing
        }
        else {
            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocationName(location, 1);
            Address address = list.get(0);
            String temp = "N/A";
            String locality = "";
            if(address.getLocality() == null){
                locality = temp;
            }
            else {
                locality = address.getLocality();
            }

            Toast.makeText(this, "Located", Toast.LENGTH_LONG).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            goToLocationZoom(lat, lng, 16);

            mId = databaseReferencePin.push().getKey();
            mPin = new Pin(mId, lat, lng, locality);

            //if marker not uploaded, delete last marker
            if (mMarker != null && save == false) {
                mMarker.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(locality)
                    .snippet("Click Upload then Marker to upload")
                    .position(new LatLng(lat, lng))
                    .draggable(false);
            mMarker = mGoogleMap.addMarker(options);
            mMarker.showInfoWindow();
            save = false;
        }

    }


    //Uploads pin to database
    public void uploadPin(View view) {
        if(mMarker != null) {
            save = true;
            databaseReferencePin.child(mId).setValue(mPin);
            Toast.makeText(this, "Pin Uploaded", Toast.LENGTH_LONG).show();

            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String location = marker.getTitle();
                    options(location,marker);
                    return false;
                }
            });
        }
    }


    //Retrieve pins from database
    //Refreshes map with pins uploaded to database
    public void reloadMap(View view) {
        for(int i = 0; i<pinList.size(); i++) {
            MarkerOptions options = new MarkerOptions()
                    .title(pinList.get(i).getTitle())
                    .position(new LatLng(pinList.get(i).getLat(), pinList.get(i).getLng()))
                    .draggable(true);
            mGoogleMap.addMarker(options);

            Toast.makeText(this, "Pin retrieved", Toast.LENGTH_LONG).show();


            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String location = marker.getTitle();
                    options(location, marker);
                    return false;
                }
            });
        }
    }


    private static final String TAG = "MyActivity";

    //helper function for popup window
    //Delete or upload photos to marker
    private void options(final String map, final Marker marker){
        final CharSequence options[] = new CharSequence[] {"Upload", "Delete Marker"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(map);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which] == "Upload"){
                    appLocation.setLocationName(map);
                    Intent intent = new Intent(getApplicationContext(), MainPictureActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                if(options[which] == "Delete Marker"){
                    final LatLng loc = marker.getPosition();
                    String temp = String.valueOf(loc);
                    String temp2 = temp.substring(0,temp.indexOf(","));
                    final String lat = temp2.substring(10,temp2.length());
                    final double location = Double.parseDouble(lat);
                    Log.v(TAG, String.valueOf(location));
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    Query query = databaseReference.child("pin").orderByChild("lat").equalTo(location);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    marker.remove();
                }
            }
        });
        builder.show();
    }

    //Check if GooglePlay Services are up to date
    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) return true;
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

}

