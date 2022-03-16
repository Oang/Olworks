package com.example.olworks.olworks;

import android.*;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.olworks.olworks.model.RoundedTransformation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Edit_employee_profile_activity extends AppCompatActivity {

    ImageView profileImage;
    TextInputLayout firstName, otherName, experience, aboutYou, education;
    int CAMERA_REQUEST_CODE = 1;
    int CAMERA_PERMISSION = 2;
    private Uri downloadUri, uri;
    ProgressDialog progressDialog;
    String authUid;
    String Experience, AboutYou, FirstName, Othername, PhotoUrl, Education;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee_profile_activity);

        progressDialog = new ProgressDialog(this);
        authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Education = bundle.getString("education");
        AboutYou = bundle.getString("desc");
        FirstName = bundle.getString("firstName");
        Othername = bundle.getString("otherName");
        PhotoUrl = bundle.getString("photoUrl");
        Experience = bundle.getString("experience");

        profileImage = (ImageView) findViewById(R.id.imageViewProfile);
        firstName = (TextInputLayout) findViewById(R.id.editTextFirstName);
        otherName = (TextInputLayout) findViewById(R.id.editTextOtherNames);
        aboutYou = (TextInputLayout) findViewById(R.id.editTextAboutYou);
        education = (TextInputLayout) findViewById(R.id.editTextEducation);
        experience = (TextInputLayout) findViewById(R.id.editTextWorkExperience);

        if (PhotoUrl == null){
            Picasso.with(Edit_employee_profile_activity.this).load(R.drawable.null_profile_image).fit().transform(new RoundedTransformation(50, 4)).into(profileImage);
        }else {
            Picasso.with(Edit_employee_profile_activity.this).load(PhotoUrl).fit().transform(new RoundedTransformation(50, 4)).into(profileImage);
        }
        firstName.getEditText().setText(FirstName);
        otherName.getEditText().setText(Othername);
        aboutYou.getEditText().setText(AboutYou);
        education.getEditText().setText(Education);
        experience.getEditText().setText(Experience);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfiePhoto();
            }
        });

    }

    //CHANGE PROFILE IMAGE METHOD
    private void changeProfiePhoto(){
        String [] items = new String[]{"Take Photo", "Choose From Gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Edit_employee_profile_activity.this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(Edit_employee_profile_activity.this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //REQUEST USER TO ALLOW CAMERA PERMMISSION
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Edit_employee_profile_activity.this.checkSelfPermission(android.Manifest.permission.CAMERA)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setMessage("Processing Image...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference uploadImage = FirebaseStorage.getInstance().getReference().child("Users").child(authUid)
                        .child("Images").child("Profile Photo").child(UUID.randomUUID() + uri.getLastPathSegment());
                uploadImage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadUri = taskSnapshot.getDownloadUrl();
                        DatabaseReference profileUrlLink = FirebaseDatabase.getInstance().getReference().child("Users");
                        profileUrlLink.child(authUid).child("photoUrl").setValue(downloadUri.toString());
                        Toast.makeText(Edit_employee_profile_activity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        Picasso.with(Edit_employee_profile_activity.this).load(downloadUri.toString()).fit().transform(new RoundedTransformation(50, 4)).into(profileImage);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Edit_employee_profile_activity.this, "Image Was Not Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        }
    }

    //SAVE CHANGES AFTER PROFILE IS UPDATED
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile_activity, menu);
        menu.findItem(R.id.action_save_changes).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                firstName = (TextInputLayout) findViewById(R.id.editTextFirstName);
                otherName = (TextInputLayout) findViewById(R.id.editTextOtherNames);
                education = (TextInputLayout) findViewById(R.id.editTextEducation);
                aboutYou = (TextInputLayout) findViewById(R.id.editTextAboutYou);
                experience = (TextInputLayout) findViewById(R.id.editTextWorkExperience);

                Education = education.getEditText().getText().toString();
                FirstName = firstName.getEditText().getText().toString();
                Othername = otherName.getEditText().getText().toString();
                AboutYou = aboutYou.getEditText().getText().toString();
                Experience = experience.getEditText().getText().toString();

                if (Education.equals("") || FirstName.equals("") || Othername.equals("")){
                    Toast.makeText(Edit_employee_profile_activity.this, "Please Fill All Details", Toast.LENGTH_SHORT).show();
                }else {
                    DatabaseReference saveChanges = FirebaseDatabase.getInstance().getReference().child("Users");
                    Map map = new HashMap();
                    map.put("education", Education);
                    map.put("firstName", FirstName);
                    map.put("otherName", Othername);
                    map.put("description", AboutYou);
                    map.put("experience", Experience);
                    saveChanges.child(authUid).updateChildren(map);
                    Toast.makeText(Edit_employee_profile_activity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }

                return false;
            }
        });
        return true;
    }

}
