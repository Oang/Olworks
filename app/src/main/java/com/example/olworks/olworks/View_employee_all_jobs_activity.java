package com.example.olworks.olworks;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.RoundedTransformation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class View_employee_all_jobs_activity extends AppCompatActivity {

    String ImageUrl, Desc, Timestamp, PushKey, Uid, authUid, Category, PlaceId, AddressLocation;
    TextView category, timeStamp, description, location;
    TextView eCompany, eNames;
    Button apply;
    ImageView image, eProfileImage;
    DatabaseReference databaseReference;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    long Date;
    boolean isApplied;
    String FirstName, OtherName, PhotoUrl;
    String EFirstName, ECompany, EOtherName, EPhotoUrl;
    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee_all_jobs_activity);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ImageUrl = bundle.getString("imageUrl");
        Desc = bundle.getString("desc");
        Timestamp = bundle.getString("timestamp");
        PushKey = bundle.getString("pushKey");
        Category = bundle.getString("type");
        Uid = bundle.getString("uid");
        PlaceId = bundle.getString("placeId", PlaceId);
        AddressLocation = bundle.getString("addressLocation");

        Date = System.currentTimeMillis() / 1000;
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        apply = (Button) findViewById(R.id.buttonApply);
        category = (TextView) findViewById(R.id.textViewCategory);
        timeStamp = (TextView) findViewById(R.id.textViewTimestamp);
        description = (TextView) findViewById(R.id.textViewDescription);
        location = (TextView) findViewById(R.id.textViewLocation);
        image = (ImageView) findViewById(R.id.imageViewImage);
        eCompany = (TextView) findViewById(R.id.textViewCompany);
        eNames = (TextView) findViewById(R.id.textViewNames);
        eProfileImage = (ImageView) findViewById(R.id.imageViewProfileImage);

        DatabaseReference employerProfileInfo = databaseReference.child("Users").child(Uid);
        employerProfileInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                EFirstName = map.get("firstName");
                EOtherName = map.get("otherName");
                eNames.setText(EFirstName+" "+EOtherName);
                EPhotoUrl = map.get("photoUrl");
                ECompany = map.get("jobVicinity");
                eCompany.setText(ECompany);
                Picasso.with(View_employee_all_jobs_activity.this).load(EPhotoUrl).fit().transform(new RoundedTransformation(50, 4)).into(eProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference profileInfo = databaseReference.child("Users").child(authUid);
        profileInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                FirstName = map.get("firstName");
                OtherName = map.get("otherName");
                PhotoUrl = map.get("photoUrl");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Picasso.with(this).load(ImageUrl).fit().into(image);
        location.setText(AddressLocation);
        category.setText(Category);
        description.setText(Desc);

        //TIME
        long time = Long.parseLong(Timestamp);
        long now = System.currentTimeMillis() / 1000;
        long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            timeStamp.setText(" just now");
        } else if (diff < 2 * MINUTE_MILLIS) {
            timeStamp.setText(" a minute ago");
        } else if (diff < 50 * MINUTE_MILLIS) {
            timeStamp.setText(" "+diff / MINUTE_MILLIS + " minutes ago");
        } else if (diff < 90 * MINUTE_MILLIS) {
            timeStamp.setText(" an hour ago");
        } else if (diff < 24 * HOUR_MILLIS) {
            timeStamp.setText(" "+diff / HOUR_MILLIS + " hours ago");
        } else if (diff < 48 * HOUR_MILLIS) {
            timeStamp.setText(" yesterday");
        } else {
            timeStamp.setText(" "+diff / DAY_MILLIS + " days ago");
        }
        //**************************************
        DatabaseReference checkIfApplied = FirebaseDatabase.getInstance().getReference().child("Applied_Jobs").child(authUid);
        checkIfApplied.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(PushKey)){
                    apply.setText("Cancel Application");
                    isApplied = true;
                }else {
                    apply.setText("Apply");
                    isApplied = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference applyJob = FirebaseDatabase.getInstance().getReference().child("Applied_Jobs");
                DatabaseReference applicants = FirebaseDatabase.getInstance().getReference().child("Applicants");
                if (isApplied == false){
                    Map map = new HashMap();
                    map.put("timestamp", Date);
                    map.put("imageUrl", ImageUrl);
                    map.put("description", Desc);
                    map.put("type", Category);
                    map.put("uid", Uid);
                    map.put("placeId", PlaceId);
                    map.put("pushKey", PushKey);
                    map.put("addressLocation", AddressLocation);
                    applyJob.child(authUid).child(PushKey).setValue(map);

                    Map map1 = new HashMap();
                    map1.put("firstName", FirstName);
                    map1.put("otherName", OtherName);
                    map1.put("photoUrl", PhotoUrl);
                    map1.put("uid", authUid);
                    map1.put("timestamp", Date);
                    applicants.child(Uid).child(PushKey).child(authUid).setValue(map1);
                }else {
                    applyJob.child(authUid).child(PushKey).removeValue();
                    applicants.child(Uid).child(PushKey).child(authUid).removeValue();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        if (PlaceId != null) {
            Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, PlaceId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                           @Override
                                           public void onResult(PlaceBuffer places) {
                                               LatLng location = places.get(0).getLatLng();
                                               googleMap.addMarker(new MarkerOptions().position(location));
                                               CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(15).build();
                                               googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                               places.release();
                                           }
                                       }
                    );
        }

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(false);

                // For dropping a marker at a point on the Map
                /*LatLng sydney = new LatLng(-34, 151);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
