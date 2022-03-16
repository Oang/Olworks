package com.example.olworks.olworks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button hiring, jobSeeker;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            progressDialog.setMessage("Loading ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            DatabaseReference checkTypeOfUser = FirebaseDatabase.getInstance().getReference().child("Users");
            checkTypeOfUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map <String, String> map = (Map)dataSnapshot.getValue();
                    String type = map.get("type");

                    if (type.equals("Employer")){
                        Intent intent = new Intent(MainActivity.this, Employer_tab_activity.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Intent intent = new Intent(MainActivity.this, Employee_tab_activity.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // No user is signed in
        }

        hiring = (Button) findViewById(R.id.buttonHiring);
        jobSeeker = (Button) findViewById(R.id.buttonJobSeeker);

        hiring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login_activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "Employer");
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        jobSeeker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login_activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "Employee");
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }
}
