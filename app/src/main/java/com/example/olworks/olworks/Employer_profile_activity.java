package com.example.olworks.olworks;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.RoundedTransformation;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

/**
 * Created by OLANG on 6/15/2017.
 */

public class Employer_profile_activity extends Fragment {

    private static final int REQUEST_PLACE_PICKER = 1;
    private GoogleApiClient mGoogleApiClient;

    ImageView profileImage, editProfile;
    TextView jobVicinity, description, names, txtProgressStatus, location;
    DatabaseReference databaseReference;
    String authUid;
    String JobVicinity, Description, FirstName, Othername, ProfileImage, AddressLocation;
    ProgressBar progressBar;
    int progressStatus, progressListener;
    ScrollView scrollView;
    MapView mMapView;
    private GoogleMap googleMap;
    String placeId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_profile_employer, container, false);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference getProgressStatus = databaseReference.child("Progress_Bar_Status").child(authUid);

        DatabaseReference getPlaceId = databaseReference.child("Users").child(authUid);
        getPlaceId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                placeId = map.get("placeId");
                if (placeId != null) {
                    Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, placeId)
                            .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                                   @Override
                                                   public void onResult(PlaceBuffer places) {
                                                       LatLng location = places.get(0).getLatLng();
                                                       googleMap.addMarker(new MarkerOptions().position(location));
                                                       CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(12).build();
                                                       googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                       places.release();
                                                   }
                                               }
                            );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMapView = (MapView) rootview.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocation();
            }
        });

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
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

        editProfile = (ImageView) rootview.findViewById(R.id.imageViewEditProfile);
        profileImage = (ImageView) rootview.findViewById(R.id.imageViewProfile);
        jobVicinity = (TextView) rootview.findViewById(R.id.textViewJobVicinity);
        description = (TextView) rootview.findViewById(R.id.textViewDescription);
        names = (TextView) rootview.findViewById(R.id.textViewNames);
        progressBar = (ProgressBar) rootview.findViewById(R.id.progressBar);
        txtProgressStatus = (TextView) rootview.findViewById(R.id.textViewProgressStatus);
        location = (TextView) rootview.findViewById(R.id.textViewLocation);
        scrollView = (ScrollView) rootview.findViewById(R.id.scrollView);

        DatabaseReference profileInfo = databaseReference.child("Users").child(authUid);
        profileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map) dataSnapshot.getValue();
                JobVicinity = map.get("jobVicinity");
                jobVicinity.setText(JobVicinity);
                Description = map.get("description");
                description.setText(Description);
                FirstName = map.get("firstName");
                Othername = map.get("otherName");
                names.setText(FirstName+" "+Othername);
                AddressLocation = map.get("addressLocation");
                ProfileImage = map.get("photoUrl");
                if (ProfileImage == null){
                    Picasso.with(getActivity()).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(profileImage);
                }else {
                    Picasso.with(getActivity()).load(ProfileImage).fit().transform(new RoundedTransformation(50, 4)).into(profileImage);
                }
                if (AddressLocation == null){
                    location.setText("Unidentified");
                }else {
                    location.setText(AddressLocation);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getProgressStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long value = (long) dataSnapshot.getValue();
                progressStatus = (int) value;
                progressListener = progressStatus;
                progressBar.setProgress(progressListener);
                txtProgressStatus.setText("Status: "+progressListener+"/"+progressBar.getMax());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfile = new Intent(getActivity(), Edit_profile_activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("firstName", FirstName);
                bundle.putString("otherName", Othername);;
                bundle.putString("desc", Description);;
                bundle.putString("photoUrl", ProfileImage);
                bundle.putString("jobVicinity", JobVicinity);
                editProfile.putExtras(bundle);
                startActivity(editProfile);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocation();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scrollView.setNestedScrollingEnabled(true);
        }

        return rootview;
    }

    private void changeLocation(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(),
                    REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Please install Google Play Services!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                Toast.makeText(getActivity(), ""+place.getLatLng()+" "+place.getAddress(), Toast.LENGTH_LONG).show();
                DatabaseReference Location = FirebaseDatabase.getInstance().getReference().child("Users").child(authUid);
                Map map = new HashMap();
                map.put("addressLocation", place.getAddress());
                map.put("placeId", place.getId());
                Location.updateChildren(map);
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(getActivity(), "Places API failure! Check that the API is enabled for your key",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
