package com.example.olworks.olworks;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.Getters;
import com.example.olworks.olworks.model.RoundedTransformation;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Messages_activity extends AppCompatActivity {

    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter<Getters> gettersFirebaseListAdapter;
    String Uid;
    FloatingActionButton floatingActionButton;
    EditText editTextMessage;
    boolean scroll = true;
    String authUid, FirstName, PhotoUrl, OtherPhotoUrl, OtherFirstName;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_activity);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uid = bundle.getString("uid");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference readMessages = databaseReference.child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Uid);
        readMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                scroll = true;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference getProfileInfo = databaseReference.child("Users").child(authUid);
        DatabaseReference getOtherProfileInfo = databaseReference.child("Users").child(Uid);
        getProfileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map) dataSnapshot.getValue();
                FirstName = map.get("firstName");
                PhotoUrl = map.get("photoUrl");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getOtherProfileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map) dataSnapshot.getValue();
                OtherFirstName = map.get("firstName");
                OtherPhotoUrl = map.get("photoUrl");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView = (ListView) findViewById(R.id.listView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);

        editTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scroll = true;
            }
        });

        gettersFirebaseListAdapter = new FirebaseListAdapter<Getters>(
                this,
                Getters.class,
                R.layout.messaging_layout,
                readMessages
        ) {
            @Override
            protected void populateView(View v, Getters model, int position) {
                if (model.getFirstName().equals(FirstName)){
                    TextView names = (TextView) v.findViewById(R.id.textViewName1);
                    names.setText(model.getFirstName());
                    TextView message = (TextView) v.findViewById(R.id.textViewMessage1);
                    message.setText(model.getMessage());

                    if (scroll){
                        listView.smoothScrollToPosition(gettersFirebaseListAdapter.getCount() - 1);
                        scroll = false;
                    }else {
                        scroll = false;
                    }

                    TextView date = (TextView) v.findViewById(R.id.textViewDate1);
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

                    RelativeLayout owner = (RelativeLayout) v.findViewById(R.id.relativeLayoutOwner);
                    RelativeLayout other = (RelativeLayout) v.findViewById(R.id.relativeLayoutOther);
                    owner.setVisibility(View.VISIBLE);
                    other.setVisibility(View.GONE);
                }else {
                    TextView names = (TextView) v.findViewById(R.id.textViewName);
                    names.setText(model.getFirstName());
                    TextView message = (TextView) v.findViewById(R.id.textViewMessage);
                    message.setText(model.getMessage());
                    ImageView imageViewProfileImage = (ImageView) v.findViewById(R.id.imageViewProfile);
                    Picasso.with(Messages_activity.this).load(model.getPhotoUrl()).transform(new RoundedTransformation(50, 0)).fit().into(imageViewProfileImage);

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

                    RelativeLayout owner = (RelativeLayout) v.findViewById(R.id.relativeLayoutOwner);
                    RelativeLayout other = (RelativeLayout) v.findViewById(R.id.relativeLayoutOther);
                    owner.setVisibility(View.GONE);
                    other.setVisibility(View.VISIBLE);
                }

            }
        };

        listView.setAdapter(gettersFirebaseListAdapter);

        final DatabaseReference chatsOwner = databaseReference.child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Uid);

        final DatabaseReference chatsOther = databaseReference.child("Chats").child(Uid)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final DatabaseReference owner = databaseReference.child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final DatabaseReference otherOwner = databaseReference.child("Messages").child(Uid);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();

                long date  = System.currentTimeMillis()/1000;
                if (message.equals("")) {
                    Toast.makeText(Messages_activity.this, "Type In A Message To Send", Toast.LENGTH_SHORT).show();
                } else {
                    //THE MESSAGES
                    Map map = new HashMap();
                    map.put("firstName", FirstName);
                    map.put("photoUrl", PhotoUrl);
                    map.put("timestamp", date);
                    map.put("message", message);
                    owner.child(Uid).push().setValue(map);
                    otherOwner.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push()
                            .setValue(map);

                    Map map2 = new HashMap();
                    map2.put("photoUrl", OtherPhotoUrl);
                    map2.put("firstName", OtherFirstName);
                    chatsOwner.updateChildren(map2);

                    Map map3 = new HashMap();
                    map3.put("photoUrl", PhotoUrl);
                    map3.put("firstName", FirstName);
                    chatsOther.updateChildren(map3);

                    //LAST CHATS MESSAGES SENT
                    Map map1 = new HashMap();
                    map1.put("sender", FirstName);
                    map1.put("timestamp", date);
                    map1.put("message", message);
                    chatsOwner.updateChildren(map1);
                    chatsOther.updateChildren(map1);
                    editTextMessage.setText("");
                }
            }
        });

    }
}
