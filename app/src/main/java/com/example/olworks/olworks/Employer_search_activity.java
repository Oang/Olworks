package com.example.olworks.olworks;

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
import com.example.olworks.olworks.model.RoundedTransformation;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

/**
 * Created by OLANG on 6/15/2017.
 */

public class Employer_search_activity extends Fragment {

    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter<Getters> gettersFirebaseDatabase;
    Query query;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.fragment_search_employer, container, false);

        listView = (ListView) rootview.findViewById(R.id.listView);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        query = databaseReference.orderByChild("type").equalTo("Employee");

        gettersFirebaseDatabase = new FirebaseListAdapter<Getters>(
                getActivity(),
                Getters.class,
                R.layout.employer_search_layout,
                query
        ) {
            @Override
            protected void populateView(View v, Getters model, int position) {
                TextView name = (TextView) v.findViewById(R.id.textViewName);
                name.setText(model.getFirstName()+" "+ model.getOtherName());
                TextView location = (TextView) v.findViewById(R.id.textViewLocation);
                location.setText(model.getAddressLocation());
                ImageView imageView = (ImageView) v.findViewById(R.id.imageViewProfileImage);
                if (model.getPhotoUrl() == null){
                    Picasso.with(getActivity()).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(imageView);
                }else {
                    Picasso.with(getActivity()).load(model.getPhotoUrl()).fit().transform(new RoundedTransformation(50, 4)).into(imageView);
                }
            }
        };

        listView.setAdapter(gettersFirebaseDatabase);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }
        return rootview;
    }
}
