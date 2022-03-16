package com.example.olworks.olworks;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.Getters;
import com.example.olworks.olworks.model.RoundedTransformation;
import com.firebase.ui.database.FirebaseListAdapter;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class View_employer_my_jobs_activity extends AppCompatActivity {

    ListView listView, listViewHeader;
    String ImageUrl, Desc, Timestamp, PushKey, Duration, authUid, Type, AddressLocation, PlaceId;
    ImageView image, cancel, accept;
    Button closeJob;
    TextView desc, timestamp, duration, type;
    FirebaseListAdapter<Getters> gettersFirebaseListAdapter, getGettersFirebaseListAdapterShortListedApplicants;
    DatabaseReference databaseReference;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private GoogleApiClient mGoogleApiClient;
    MapView mMapView;
    private GoogleMap googleMap;
    String FirstName, OtherName, PhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employer_my_jobs_layout);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        View header = View.inflate(View_employer_my_jobs_activity.this, R.layout.activity_view_employer_my_jobs_header_layout, null);

        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        ImageUrl = bundle.getString("imageUrl");
        Desc = bundle.getString("desc");
        Timestamp = bundle.getString("timestamp");
        PushKey = bundle.getString("pushKey");
        Duration = bundle.getString("duration");
        Type = bundle.getString("type");
        PlaceId = bundle.getString("placeId");
        AddressLocation = bundle.getString("addressLocation");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Applicants").child(authUid)
                .child(PushKey);

        listView = (ListView) findViewById(R.id.listView);

        desc = (TextView) header.findViewById(R.id.textViewDescription);
        desc.setText(Desc);
        TextView location = (TextView) header.findViewById(R.id.textViewLocation);
        location.setText(AddressLocation);
        duration = (TextView) header.findViewById(R.id.textViewDuration);
        duration.setText("Duration of "+Duration);
        ImageView image = (ImageView) header.findViewById(R.id.imageView);
        Picasso.with(View_employer_my_jobs_activity.this).load(ImageUrl).fit().into(image);
        closeJob = (Button) header.findViewById(R.id.buttonCloseOpenJob);
        listViewHeader = (ListView) header.findViewById(R.id.listView);

        //TIME*********************************
        timestamp = (TextView) header.findViewById(R.id.textViewTimeStamp);
        long time = Long.parseLong(Timestamp);
        final long now = System.currentTimeMillis() / 1000;
        long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            timestamp.setText(" just now");
        } else if (diff < 2 * MINUTE_MILLIS) {
            timestamp.setText(" a minute ago");
        } else if (diff < 50 * MINUTE_MILLIS) {
            timestamp.setText(" "+diff / MINUTE_MILLIS + " minutes ago");
        } else if (diff < 90 * MINUTE_MILLIS) {
            timestamp.setText(" an hour ago");
        } else if (diff < 24 * HOUR_MILLIS) {
            timestamp.setText(" "+diff / HOUR_MILLIS + " hours ago");
        } else if (diff < 48 * HOUR_MILLIS) {
            timestamp.setText(" yesterday");
        } else {
            timestamp.setText(" "+diff / DAY_MILLIS + " days ago");
        }
        //**************************************

        //GET SHORTLISTD APPLICANTS
        DatabaseReference getShortListedApplicants = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(authUid)
                .child(PushKey);
        getGettersFirebaseListAdapterShortListedApplicants = new FirebaseListAdapter<Getters>(
                this,
                Getters.class,
                R.layout.employer_applicants_layout,
                getShortListedApplicants
        ) {
            @Override
            protected void populateView(View v, final Getters model, int position) {

                TextView names = (TextView) v.findViewById(R.id.textViewNames);
                names.setText(model.getFirstName()+" "+model.getOtherName());
                ImageView image = (ImageView) v.findViewById(R.id.imageViewProfileImage);
                if (model.getPhotoUrl() == null){
                    Picasso.with(View_employer_my_jobs_activity.this).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(image);
                }else {
                    Picasso.with(View_employer_my_jobs_activity.this).load(model.getPhotoUrl()).fit().transform(new RoundedTransformation(50, 4)).into(image);
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(View_employer_my_jobs_activity.this, Applicants_employer_activity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("uid", model.getUid());
                        bundle1.putString("key", PushKey);
                        intent1.putExtras(bundle1);
                        startActivity(intent1);
                       // Toast.makeText(View_employer_my_jobs_activity.this, ""+model.getUid(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        listViewHeader.setAdapter(getGettersFirebaseListAdapterShortListedApplicants);

        //CHECK IF JOB IS CLOSED
        DatabaseReference checkIfJobIsActive = FirebaseDatabase.getInstance().getReference().child("All_Posts").child(Type);
        checkIfJobIsActive.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(PushKey)){
                    closeJob.setEnabled(false);
                    listView.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gettersFirebaseListAdapter = new FirebaseListAdapter<Getters>(
                this,
                Getters.class,
                R.layout.employer_applicants_layout,
                databaseReference
        ) {
            @Override
            protected void populateView(View v, final Getters model, int position) {

                final String key = getRef(position).getKey();

                TextView names = (TextView) v.findViewById(R.id.textViewNames);
                names.setText(model.getFirstName()+" "+model.getOtherName());
                ImageView image = (ImageView) v.findViewById(R.id.imageViewProfileImage);
                if (model.getPhotoUrl() == null){
                    Picasso.with(View_employer_my_jobs_activity.this).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(image);
                }else {
                    Picasso.with(View_employer_my_jobs_activity.this).load(model.getPhotoUrl()).fit().transform(new RoundedTransformation(50, 4)).into(image);
                }
                //TIME
                TextView timeStamp = (TextView) v.findViewById(R.id.textViewTimestamp);
                long time = model.getTimestamp();
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

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(View_employer_my_jobs_activity.this, Applicants_employer_activity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("uid", model.getUid());
                        bundle1.putString("key", PushKey);
                        intent1.putExtras(bundle1);
                        startActivity(intent1);
                        //Toast.makeText(View_employer_my_jobs_activity.this, ""+model.getUid(), Toast.LENGTH_SHORT).show();
                    }
                });

                final ImageView cancel = (ImageView) v.findViewById(R.id.imageViewUnList);
                final ImageView accept = (ImageView) v.findViewById(R.id.imageViewShortList);

                DatabaseReference chechIfApplicantExists = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(authUid);
                chechIfApplicantExists.child(PushKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(model.getUid())){
                            accept.setEnabled(false);
                            cancel.setEnabled(true);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference cancelApplicant = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(authUid);
                                    cancelApplicant.child(PushKey).child(model.getUid()).removeValue();
                                    Toast.makeText(View_employer_my_jobs_activity.this, "Applicant removed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            accept.setEnabled(true);
                            cancel.setEnabled(false);
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference acceptApplicant = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(authUid);
                                    Map map = new HashMap();
                                    map.put("firstName", model.getFirstName());
                                    map.put("otherName", model.getOtherName());
                                    map.put("photoUrl", model.getPhotoUrl());
                                    map.put("uid", model.getUid());
                                    acceptApplicant.child(PushKey).child(model.getUid()).setValue(map);
                                    Toast.makeText(View_employer_my_jobs_activity.this, "Short Listed This Applicant", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        //CLOSE THE JOB
        closeJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(View_employer_my_jobs_activity.this).create();
                alertDialog.setTitle("Close Job?");
                alertDialog.setMessage("Are you through short listing applicants");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int which) {
                                listView.setAdapter(null);
                                StorageReference deleteImage =FirebaseStorage.getInstance().getReferenceFromUrl(ImageUrl);
                                deleteImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ImageUrl = "";
                                        DatabaseReference closeJobRef = FirebaseDatabase.getInstance().getReference();
                                        closeJobRef.child("All_Posts").child(Type).child(PushKey).removeValue();
                                        Toast.makeText(View_employer_my_jobs_activity.this, "Job Closed", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        listView.addHeaderView(header, null, false);
        listView.setAdapter(gettersFirebaseListAdapter);


        if (PlaceId != null) {
            Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, PlaceId)
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
}
