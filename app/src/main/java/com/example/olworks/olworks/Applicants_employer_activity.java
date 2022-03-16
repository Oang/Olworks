package com.example.olworks.olworks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.RoundedTransformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class  Applicants_employer_activity extends AppCompatActivity {

    TextView desc, names, location, type, aboutYou, experience, education;
    ImageView imageViewProfile, cancel, accept;
    String Uid, auth, Key;
    String FirstName, OtherName, Location, Type, PhotoUrl, Desc, Experience, AboutYou, Education;
    ProgressDialog progressDialog;
    Button buttonMessage;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicants_employer_activity);

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uid = bundle.getString("uid");
        Key = bundle.getString("key");

        auth = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //desc = (TextView) findViewById(R.id.textViewDesc);
        names = (TextView) findViewById(R.id.textViewNames);
        cancel = (ImageView) findViewById(R.id.imageViewCancelApplicant);
        accept = (ImageView) findViewById(R.id.imageViewAcceptApplicant);
        buttonMessage = (Button) findViewById(R.id.buttonMessage);
        location = (TextView) findViewById(R.id.textViewLocation);
        type = (TextView) findViewById(R.id.textViewType);
        imageViewProfile = (ImageView) findViewById(R.id.imageViewProfileImage);
        education = (TextView) findViewById(R.id.textViewEducation);
        experience = (TextView) findViewById(R.id.textViewWorkExp);
        aboutYou = (TextView) findViewById(R.id.textViewDescription);

        DatabaseReference checkIfApplicantExists = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(auth);
        checkIfApplicantExists.child(Key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Uid)){
                    accept.setEnabled(false);
                    cancel.setEnabled(true);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseReference cancelApplicant = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(auth);
                            cancelApplicant.child(Key).child(Uid).removeValue();
                            Toast.makeText(Applicants_employer_activity.this, "Applicant removed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    accept.setEnabled(true);
                    cancel.setEnabled(false);
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseReference acceptApplicant = FirebaseDatabase.getInstance().getReference().child("Short_Listed").child(auth);
                            Map map = new HashMap();
                            map.put("firstName", FirstName);
                            map.put("otherName", OtherName);
                            map.put("photoUrl", PhotoUrl);
                            map.put("uid", Uid);
                            acceptApplicant.child(Key).child(Uid).setValue(map);
                            Toast.makeText(Applicants_employer_activity.this, "Short Listed This Applicant", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference getProfileInfo = databaseReference.child("Users").child(Uid);
        getProfileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                OtherName = map.get("otherName");
                FirstName = map.get("firstName");
                names.setText(FirstName+" "+OtherName);
                PhotoUrl = map.get("photoUrl");
                Picasso.with(Applicants_employer_activity.this).load(PhotoUrl).fit().transform(new RoundedTransformation(50, 4)).into(imageViewProfile);
                Type = map.get("category");
                type.setText(Type);
                /*Desc = map.get("description");
                desc.setText(Desc);*/
                /*Location = map.get("location");*/
                Education = map.get("education");
                if (Education == null){
                    Education = "No education stated";
                }
                education.setText(Education);
                Experience = map.get("experience");
                if (Experience == null){
                    Experience = "No job experience stated";
                }
                experience.setText(Experience);
                AboutYou = map.get("description");
                if (AboutYou == null){
                    AboutYou = "No description";
                }
                aboutYou.setText(AboutYou);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        buttonMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Applicants_employer_activity.this, Messages_activity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("uid", Uid);
                intent1.putExtras(bundle1);
                startActivity(intent1);
            }
        });
    }
}
