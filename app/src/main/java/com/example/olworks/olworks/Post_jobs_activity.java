package com.example.olworks.olworks;

import android.*;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olworks.olworks.model.RoundedTransformation;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Post_jobs_activity extends AppCompatActivity {

    Button next, addPost;
    TextInputLayout description;
    Spinner spinnerType, spinnerDurationPost;
    String SpinnerDuration, SpinnerType;
    ImageView image;
    TextView txtProgressStatus, txtlocation;
    int CAMERA_REQUEST_CODE = 1;
    int CAMERA_PERMISSION = 2;
    private Uri downloadUri, uri;
    boolean isImagePosted = false, isLocationSet = false;
    ProgressBar progressBar;
    String authUid;
    int progressStatus, progressListener;
    int alterProgressStatus, currentProgressStatus = 0;
    DatabaseReference databaseReference;
    MapView mMapView;
    private GoogleMap googleMap;
    private static final int REQUEST_PLACE_PICKER = 3;
    private GoogleApiClient mGoogleApiClient;
    String addressLocation, placeId;
    Double longt, lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_jobs_activity);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtlocation = (TextView) findViewById(R.id.textViewLocation);
        txtProgressStatus = (TextView) findViewById(R.id.textViewProgressStatus);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        spinnerDurationPost = (Spinner) findViewById(R.id.spinnerDurationPost);
        description = (TextInputLayout) findViewById(R.id.editTextDescription);
        addPost = (Button) findViewById(R.id.buttonPostImage);
        image = (ImageView) findViewById(R.id.imageViewPost);

        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference getProgressStatus = databaseReference.child("Progress_Bar_Status").child(authUid);

        getProgressStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long value = (long) dataSnapshot.getValue();
                progressStatus = (int) value;
                progressListener = progressStatus;
                progressBar.setProgress(progressListener);
                txtProgressStatus.setText("Status: "+progressListener+"/"+progressBar.getMax());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterCategory);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapterDuration = ArrayAdapter.createFromResource(this,
                R.array.duration, android.R.layout.simple_spinner_item);
        adapterDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDurationPost.setAdapter(adapterDuration);

        spinnerDurationPost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerDuration = parent.getItemAtPosition(position).toString();
                if (position == (0)){
                    progressBar.setProgress(progressListener);
                    txtProgressStatus.setText("Status: "+progressListener+"/"+progressBar.getMax());
                }else if (position == (1)){
                    progressStatus = progressStatus - 1;
                    alterProgressStatus = progressStatus;
                    progressStatus = alterProgressStatus + currentProgressStatus;
                    progressBar.setProgress(progressStatus);
                    currentProgressStatus = 1;
                    txtProgressStatus.setText("Status: "+progressStatus+"/"+progressBar.getMax());
                }else if (position == (2)){
                    progressStatus = progressStatus - 2;
                    alterProgressStatus = progressStatus;
                    progressStatus = alterProgressStatus + currentProgressStatus;
                    progressBar.setProgress(progressStatus);
                    currentProgressStatus = 2;
                    txtProgressStatus.setText("Status: "+progressStatus+"/"+progressBar.getMax());
                }else if (position == (3)) {
                    progressStatus = progressStatus - 3;
                    alterProgressStatus = progressStatus;
                    progressStatus = alterProgressStatus + currentProgressStatus;
                    progressBar.setProgress(progressStatus);
                    currentProgressStatus = 3;
                    txtProgressStatus.setText("Status: "+progressStatus+"/"+progressBar.getMax());
                }else {
                    progressStatus = progressStatus - 4;
                    alterProgressStatus = progressStatus;
                    progressStatus = alterProgressStatus + currentProgressStatus;
                    progressBar.setProgress(progressStatus);
                    currentProgressStatus = 4;
                    txtProgressStatus.setText("Status: "+progressStatus+"/"+progressBar.getMax());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CALL THE METHOD TO ADD PHOTO
                addPhoto();
            }
        });
//*****************************************************************************************************8
        //SETUP JOB LOCATION

        txtlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocation();
            }
        });

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
            }
        });
//***************************************************************************************************************

        //FINISH UP POST
        next = (Button) findViewById(R.id.buttonNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImagePosted == false){
                    Toast.makeText(Post_jobs_activity.this, "Please Add An Image For Your Job", Toast.LENGTH_SHORT).show();
                }else if (isLocationSet == false){
                    Toast.makeText(Post_jobs_activity.this, "Please Add A Location For Your Job", Toast.LENGTH_SHORT).show();
                }else {
                    String desc = description.getEditText().getText().toString();
                    if (SpinnerType.equals("Choose Category")){
                        Toast.makeText(Post_jobs_activity.this, "Please Choose A Category", Toast.LENGTH_SHORT).show();
                    }else if (SpinnerDuration.equals("Choose Duration")){
                        Toast.makeText(Post_jobs_activity.this, "Please Choose A Duration", Toast.LENGTH_SHORT).show();
                    }else if (desc.length() < 30 ){
                        Toast.makeText(Post_jobs_activity.this, "Description Is Too Short", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent next = new Intent(Post_jobs_activity.this, Finish_post_activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("progressStatus", progressBar.getProgress()+"");
                        bundle.putString("imageUrl", downloadUri.toString());
                        bundle.putString("duration", SpinnerDuration);;
                        bundle.putString("category", SpinnerType);
                        bundle.putString("description", desc);
                        bundle.putString("addressLocation", addressLocation);
                        bundle.putString("placeId", placeId);
                        bundle.putDouble("longt", longt);
                        bundle.putDouble("lat", lat);
                        next.putExtras(bundle);
                        startActivity(next);
                    }
                }
                Toast.makeText(Post_jobs_activity.this, ""+progressBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //USER ADDS JOB PHOTO
    private void addPhoto(){
        String [] items = new String[]{"Take Photo", "Choose From Gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Post_jobs_activity.this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(Post_jobs_activity.this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //REQUEST USER TO ALLOW CAMERA PERMMISSION
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Post_jobs_activity.this.checkSelfPermission(android.Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                                    CAMERA_PERMISSION);
                            dialog.cancel();
                        }
                        else {
                            //IF NO PERMISSION REQUIRED TAKE PHOTO
                            takePhoto();
                            dialog.cancel();
                        }
                    }
                }else {
                    //CHOOSE PHOTO FROM GALERY
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, CAMERA_REQUEST_CODE);
                    dialog.cancel();
                }
            }
        });
        builder.show();
    }

    //TAKE PHOTO METHOD
    private void takePhoto() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }

    //METHOD USED TO POST IMAGE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);

                Toast.makeText(fa, ""+place.getLatLng(), Toast.LENGTH_SHORT).show();

                placeId = place.getId();
                longt = place.getLatLng().longitude;
                lat = place.getLatLng().latitude;
                addressLocation = String.valueOf(place.getAddress());

                if (placeId != null) {
                    Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, placeId)
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
                    isLocationSet = true;
                }

                Toast.makeText(this, ""+place.getAddress(), Toast.LENGTH_LONG).show();
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(this, "Places API failure! Check that the API is enabled for your key",
                        Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                StorageReference uploadImage = FirebaseStorage.getInstance().getReference().child("Users").child(authUid)
                        .child("Images").child("Job Post Photos").child(UUID.randomUUID() + uri.getLastPathSegment());
                uploadImage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadUri = taskSnapshot.getDownloadUrl();
                        isImagePosted = true;
                        //downLoadUrlStringHolder = downloadUri.toString();
                        Toast.makeText(Post_jobs_activity.this, "Image Posted", Toast.LENGTH_SHORT).show();
                        Picasso.with(Post_jobs_activity.this).load(downloadUri.toString()).fit().into(image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Post_jobs_activity.this, "Image Not Posted, Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    //REQUEST USER TO ALLOW PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        }
    }

    //IF THERE IS ANY IMAGE POSTED, THIS METHOD DISCARDS FROM THE DATABASE
    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(Post_jobs_activity.this).create();
        alertDialog.setTitle("Discard Job?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DISCARD",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        if (isImagePosted == false){
                            finish();
                        }else {
                            StorageReference deleteDiscardedpPost = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUri.toString());
                            deleteDiscardedpPost.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Post_jobs_activity.this, "Post Discarded", Toast.LENGTH_SHORT).show();
                                    finish();
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Post_jobs_activity.this, "Error Discarding Post", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Post_jobs_activity.this, Post_jobs_activity.class);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static Activity fa;
    Post_jobs_activity()
    {
        fa = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void changeLocation(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Please install Google Play Services!", Toast.LENGTH_LONG).show();
        }
    }
}
