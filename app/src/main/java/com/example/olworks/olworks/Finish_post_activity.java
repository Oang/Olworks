package com.example.olworks.olworks;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Finish_post_activity extends AppCompatActivity {

    String Desc, SpinnerDuration, SpinnerType, ImageUrl, ProgressStatus, AddressLocation, PlaceId;
    ProgressBar progressBar;
    CheckBox checkBox;
    TextView status;
    int currentProgressbarStatus;
    long Date;
    String authUid;
    boolean isUrgent = false;
    Double Longt, Lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_post_activity);

        Date  = System.currentTimeMillis()/1000;
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Desc = bundle.getString("description");
        SpinnerDuration = bundle.getString("duration");
        SpinnerType = bundle.getString("category");
        ImageUrl = bundle.getString("imageUrl");
        ProgressStatus = bundle.getString("progressStatus");
        PlaceId = bundle.getString("placeId");
        AddressLocation = bundle.getString("addressLocation");
        Longt = bundle.getDouble("longt");
        Lat = bundle.getDouble("lat");

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        status = (TextView) findViewById(R.id.textViewStatus);

        status.setText(ProgressStatus+"/"+progressBar.getMax());
        currentProgressbarStatus = Integer.parseInt(ProgressStatus);
        Toast.makeText(this, ""+currentProgressbarStatus, Toast.LENGTH_SHORT).show();

        progressBar.setProgress(currentProgressbarStatus);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()){
                    isUrgent = true;
                    currentProgressbarStatus = currentProgressbarStatus - 1;
                    progressBar.setProgress(currentProgressbarStatus);
                    status.setText(currentProgressbarStatus+"/"+progressBar.getMax());
                }else {
                    isUrgent = false;
                    currentProgressbarStatus = currentProgressbarStatus + 1;
                    progressBar.setProgress(currentProgressbarStatus);
                    status.setText(currentProgressbarStatus+"/"+progressBar.getMax());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_finish_post_activity, menu);
        menu.findItem(R.id.action_finish_post).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DatabaseReference allPosts = FirebaseDatabase.getInstance().getReference().child("All_Posts");
                DatabaseReference myJobPosts = FirebaseDatabase.getInstance().getReference().child("My_Job_Posts");

                String postKey = myJobPosts.child(authUid).push().getKey();

                DatabaseReference progressStatus = FirebaseDatabase.getInstance().getReference().child("Progress_Bar_Status");
                progressStatus.child(authUid).setValue(currentProgressbarStatus);
                //SEND TO ALL POSTS
                Map map = new HashMap();
                map.put("description", Desc);
                map.put("duration", SpinnerDuration);
                map.put("imageUrl", ImageUrl);
                map.put("urgent", isUrgent);
                map.put("uid", authUid);
                map.put("timestamp", Date);
                map.put("addressLocation", AddressLocation);
                map.put("placeId", PlaceId);
                allPosts.child(SpinnerType).child(postKey).setValue(map);

                //SEND TO OWNER POST
                Map map1 = new HashMap();
                map1.put("description", Desc);
                map1.put("duration", SpinnerDuration);
                map1.put("imageUrl", ImageUrl);
                map1.put("urgent", isUrgent);
                map1.put("uid", authUid);
                map1.put("timestamp", Date);
                map1.put("type", SpinnerType);
                map1.put("addressLocation", AddressLocation);
                map1.put("placeId", PlaceId);
                myJobPosts.child(authUid).child(postKey).setValue(map1);
                Toast.makeText(Finish_post_activity.this, "Your Job Has Been Posted", Toast.LENGTH_SHORT).show();



                //set geofire location
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geofire");
                GeoFire geoFire = new GeoFire(ref);

                geoFire.setLocation(SpinnerType+"/"+postKey, new GeoLocation(Lat, Longt));

                Post_jobs_activity.fa.finish();
                finish();
                return false;
            }
        });
        return true;
    }
}
