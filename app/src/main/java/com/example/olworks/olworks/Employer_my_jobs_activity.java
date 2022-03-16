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
 * Created by OLANG on 6/15/2017.
 */

public class Employer_my_jobs_activity extends Fragment {

    public Employer_my_jobs_activity() {
    }

    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter<Getters> listAdapter;
    String authUid;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_my_job_employer, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference  = FirebaseDatabase.getInstance().getReference().child("My_Job_Posts").child(authUid);

        listAdapter = new FirebaseListAdapter<Getters>(
                getActivity(),
                Getters.class,
                R.layout.my_jobs_list_layout,
                databaseReference
        ) {
            @Override
            protected void populateView(final View v, final Getters model, int position) {

                final String pushKey = getRef(position).getKey();

                final TextView applicants = (TextView) v.findViewById(R.id.textViewNoApplicants);
                TextView description = (TextView) v.findViewById(R.id.textViewDescription);
                description.setText(model.getDescription());
                final ImageView image = (ImageView) v.findViewById(R.id.imageViewImage);
                Picasso.with(getActivity()).load(model.getImageUrl()).fit().into(image);
                TextView category = (TextView) v.findViewById(R.id.textViewCategory);
                category.setText(model.getType());
                TextView duration = (TextView) v.findViewById(R.id.textViewDuration);
                duration.setText(model.getDuration());
                TextView location = (TextView) v.findViewById(R.id.textViewLocation);
                location.setText(model.getAddressLocation());

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
                //**************************************

                //Get no of applicants
                DatabaseReference referenceApplicants = FirebaseDatabase.getInstance().getReference().child("Applicants").child(authUid);
                referenceApplicants.child(pushKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long s = dataSnapshot.getChildrenCount();
                        applicants.setText(s+" applicants");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent view = new Intent(getActivity(), View_employer_my_jobs_activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imageUrl", model.getImageUrl());
                        bundle.putString("timestamp", model.getTimestamp()+"");
                        bundle.putString("desc", model.getDescription());
                        bundle.putString("duration", model.getDuration());
                        bundle.putString("pushKey", pushKey);
                        bundle.putString("type", model.getType());
                        bundle.putString("addressLocation", model.getAddressLocation());
                        bundle.putString("placeId", model.getPlaceId());
                        view.putExtras(bundle);
                        startActivity(view);
                    }
                });

                //CHECK IF JOB IS ACTIVE
                DatabaseReference checkIfJobIsActive = FirebaseDatabase.getInstance().getReference().child("All_Posts").child(model.getType());
                checkIfJobIsActive.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(pushKey)){
                            TextView textView = (TextView) v.findViewById(R.id.textViewJobActive);
                            textView.setText("Job is active");
                        }else {
                            TextView textView = (TextView) v.findViewById(R.id.textViewJobActive);
                            textView.setVisibility(View.GONE);
                            Picasso.with(getActivity()).load(R.color.cardview_dark_background).fit().into(image);
                            TextView textView1 = (TextView) v.findViewById(R.id.textView9);
                            textView1.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        listView.setAdapter(listAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }

        return rootview;
    }
}
