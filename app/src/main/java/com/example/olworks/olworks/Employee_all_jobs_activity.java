package com.example.olworks.olworks;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.Getters;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by OLANG on 6/18/2017.
 */

public class Employee_all_jobs_activity extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    public Employee_all_jobs_activity() {
    }

    ListView listView;
    ArrayList<Getters> getterses;
    Spinner spinnerCategory;
    DatabaseReference databaseReference;
    GeoFire geoFire;
    GeoQuery geoQuery;
    FirebaseListAdapter<Getters> gettersFirebaseListAdapter;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    double longtitude, latitude;
    GoogleApiClient mGoogleApiClient;
    String categorychange = "";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        //Toast.makeText(getActivity(), ""+longtitude, Toast.LENGTH_SHORT).show();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_employee_all_jobs, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        spinnerCategory = (Spinner) rootview.findViewById(R.id.spinnerCategory);

        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(getActivity(),
                R.array.category, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String Category = parent.getItemAtPosition(position).toString();

                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users_Jobs")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference1.child(categorychange).removeValue();

                categorychange = Category;

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users_Jobs").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Category);
                DatabaseReference referencegeo = FirebaseDatabase.getInstance().getReference().child("geofire").child(Category);
                GeoFire geoFire = new GeoFire(referencegeo);
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longtitude), 10);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, GeoLocation location) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("All_Posts").child(Category).child(key);
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users_Jobs")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                reference1.child(Category).child(key).setValue(dataSnapshot.getValue());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Toast.makeText(getActivity(), "entered", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onKeyExited(String key) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users_Jobs")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        reference1.child(Category).child(key).removeValue();

                        Toast.makeText(getActivity(), "Exited", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Toast.makeText(getActivity(), "Key is moving", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onGeoQueryReady() {
                        Toast.makeText(getActivity(), latitude+"/ "+longtitude, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Toast.makeText(getActivity(), "There was an error with this query:   " + error, Toast.LENGTH_SHORT).show();
                    }
                });

                gettersFirebaseListAdapter = new FirebaseListAdapter<Getters>(
                        getActivity(),
                        Getters.class,
                        R.layout.all_jobs_list_layout,
                        databaseReference
                ) {
                    @Override
                    protected void populateView(View v, final Getters model, int position) {

                        final String pushKey = getRef(position).getKey();

                        TextView description = (TextView) v.findViewById(R.id.textViewDescription);
                        description.setText(model.getDescription());
                        TextView location = (TextView) v.findViewById(R.id.textViewLocation);
                        location.setText(model.getAddressLocation());
                        TextView category = (TextView) v.findViewById(R.id.textViewCategory);
                        category.setText(Category);
                        ImageView image = (ImageView) v.findViewById(R.id.imageViewImage);
                        Picasso.with(getActivity()).load(model.getImageUrl()).fit().into(image);

                        //TIME*********************************
                        TextView date = (TextView) v.findViewById(R.id.textViewTimestamp);
                        long time = model.getTimestamp();
                        long now = System.currentTimeMillis() / 1000;
                        long diff = now - time;
                        if (diff < MINUTE_MILLIS) {
                            date.setText(" just now");
                        } else if (diff < 2 * MINUTE_MILLIS) {
                            date.setText(" a minute ago");
                        } else if (diff < 50 * MINUTE_MILLIS) {
                            date.setText(" "+diff / MINUTE_MILLIS + " minutes ago");
                        } else if (diff < 90 * MINUTE_MILLIS) {
                            date.setText(" an hour ago");
                        } else if (diff < 24 * HOUR_MILLIS) {
                            date.setText(" "+diff / HOUR_MILLIS + " hours ago");
                        } else if (diff < 48 * HOUR_MILLIS) {
                            date.setText(" yesterday");
                        } else {
                            date.setText(" "+diff / DAY_MILLIS + " days ago");
                        }
                        //**********************************

                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent view = new Intent(getActivity(), View_employee_all_jobs_activity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("imageUrl", model.getImageUrl());
                                bundle.putString("timestamp", model.getTimestamp()+"");
                                bundle.putString("desc", model.getDescription());
                                bundle.putString("pushKey", pushKey);
                                bundle.putString("type", Category);
                                bundle.putString("uid", model.getUid());
                                bundle.putString("placeId", model.getPlaceId());
                                bundle.putString("addressLocation", model.getAddressLocation());
                                view.putExtras(bundle);
                                startActivity(view);
                            }
                        });
                    }
                };

                listView.setAdapter(gettersFirebaseListAdapter);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    listView.setNestedScrollingEnabled(true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return rootview;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            longtitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
            //Toast.makeText(getActivity(), ""+longtitude, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
