package com.example.olworks.olworks;

import android.content.Intent;
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
import com.example.olworks.olworks.model.RoundedTransformation;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

/**
 * Created by OLANG on 6/19/2017.
 */

public class Employee_chat_activity extends Fragment {

    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter<Getters> gettersFirebaseListAdapter;
    String authUid;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public Employee_chat_activity() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_chat_employee, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference chats = databaseReference.child("Chats").child(authUid);

        gettersFirebaseListAdapter = new FirebaseListAdapter<Getters>(
                getActivity(),
                Getters.class,
                R.layout.chats_layout,
                chats
        ) {
            @Override
            protected void populateView(View v, Getters model, int position) {

                final String key = getRef(position).getKey();

                TextView names = (TextView) v.findViewById(R.id.textViewNames);
                names.setText(model.getFirstName());
                TextView message = (TextView) v.findViewById(R.id.textViewMessage);
                message.setText(model.getMessage());
                TextView date = (TextView) v.findViewById(R.id.textViewDate);
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

                ImageView imageViewProfile = (ImageView) v.findViewById(R.id.imageViewProfileImage);
                if (model.getPhotoUrl() == null){
                    Picasso.with(getActivity()).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(imageViewProfile);
                }else {
                    Picasso.with(getActivity()).load(model.getPhotoUrl()).fit().transform(new RoundedTransformation(50, 4)).into(imageViewProfile);
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), Messages_activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", key);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
        };

        listView.setAdapter(gettersFirebaseListAdapter);

        return rootview;
    }
}
