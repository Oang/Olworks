package com.example.olworks.olworks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.olworks.olworks.model.Getters;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by OLANG on 6/19/2017.
 */

public class Employee_applied_jobs_activity extends Fragment {

    DatabaseReference databaseReference;
    ListView listView;
    FirebaseListAdapter<Getters> gettersFirebaseListAdapter;
    String authUid;
    //String Category = "Cleaning";
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public Employee_applied_jobs_activity() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_employee_applied_job, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference appliedJobs = databaseReference.child("Applied_Jobs").child(authUid);

        gettersFirebaseListAdapter = new FirebaseListAdapter<Getters>(
                getActivity(),
                Getters.class,
                R.layout.employee_applied_jobs_latout,
                appliedJobs
        ) {
            @Override
            protected void populateView(final View v, final Getters model, int position) {

                final String pushKey = getRef(position).getKey();

                TextView description = (TextView) v.findViewById(R.id.textViewDescription);
                description.setText(model.getDescription());
                TextView category = (TextView) v.findViewById(R.id.textViewCategory);
                category.setText(model.getType());
                TextView location = (TextView) v.findViewById(R.id.textViewLocation);
                location.setText(model.getAddressLocation());
                final ImageView image = (ImageView) v.findViewById(R.id.imageViewImage);
                Picasso.with(getActivity()).load(model.getImageUrl()).fit().centerCrop().into(image);

                TextView date = (TextView) v.findViewById(R.id.textViewTimeStamp);
                long time = model.getTimestamp();
                long now  = System.currentTimeMillis()/1000;
                long diff = now-time;
                if (diff < MINUTE_MILLIS) {
                    date.setText("just now");
                } else if (diff < 2 * MINUTE_MILLIS) {
                    date.setText("a minute ago");
                } else if (diff < 50 * MINUTE_MILLIS) {
                    date.setText(diff / MINUTE_MILLIS + " minutes ago");
                } else if (diff < 90 * MINUTE_MILLIS) {
                    date.setText("an hour ago");
                } else if (diff < 24 * HOUR_MILLIS) {
                    date.setText(diff / HOUR_MILLIS + " hours ago");
                } else if (diff < 48 * HOUR_MILLIS) {
                    date.setText("yesterday");
                } else {
                    date.setText(diff / DAY_MILLIS + " days ago");
                }
                //**************************************

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent view = new Intent(getActivity(), View_employee_all_jobs_activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imageUrl", model.getImageUrl());
                        bundle.putString("timestamp", model.getTimestamp()+"");
                        bundle.putString("desc", model.getDescription());
                        bundle.putString("pushKey", pushKey);
                        bundle.putString("type", model.getType());
                        bundle.putString("uid", model.getUid());
                        bundle.putString("placeId", model.getPlaceId());
                        bundle.putString("addressLocation", model.getAddressLocation());
                        view.putExtras(bundle);
                        startActivity(view);
                    }
                });

                //CHECK IF JOB IS ACTIVE
                DatabaseReference checkIfShortListed = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(model.getUid());
                checkIfShortListed.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child(model.getPushKey()).hasChild(authUid)){
                            TextView textView = (TextView) v.findViewById(R.id.textViewShortList);
                            textView.setText("Not Short Listed");
                        }else {
                            TextView textView = (TextView) v.findViewById(R.id.textViewShortList);
                            textView.setText("Short Listed");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        listView.setAdapter(gettersFirebaseListAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }

        return rootview;

    }
}
