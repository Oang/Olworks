package com.example.olworks.olworks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUp_activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText firstName, otherName, email, password, confirmPassword, phone;
    Spinner spinnerCategory;
    Button signUp;
    String SpinnerCategory, type;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString("type");
        Toast.makeText(this, ""+type, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        signUp = (Button) findViewById(R.id.buttonSignUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tabs = new Intent(SignUp_activity.this, Employer_tab_activity.class);
                startActivity(tabs);
            }
        });

        firstName = (EditText) findViewById(R.id.editTextFirstname);
        otherName = (EditText) findViewById(R.id.edittextOthername);
        email = (EditText) findViewById(R.id.editTextEmail);
        password = (EditText) findViewById(R.id.editTestPassword);
        confirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        phone = (EditText) findViewById(R.id.editTextPhone);
        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(this);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String FirstName = firstName.getText().toString();
                final String Phone = phone.getText().toString();
                final String OtherName = otherName.getText().toString();
                final String Email = email.getText().toString();
                String Password = password.getText().toString();
                String ConfirmPassword = confirmPassword.getText().toString();

                if (FirstName.equals("") || OtherName.equals("") || Password.equals("") ||
                        Phone.equals("") || Email.equals("")){
                    Toast.makeText(SignUp_activity.this, "Please Fill In All Details", Toast.LENGTH_SHORT).show();
                }else if (Password.length() < 6){
                    Toast.makeText(SignUp_activity.this, "Password Too Short", Toast.LENGTH_SHORT).show();
                }else if (!ConfirmPassword.equals(Password)){

                }else if (SpinnerCategory.equals("Choose Category")){
                    Toast.makeText(SignUp_activity.this, "Please Choose A Category", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setMessage("Creating Your Account");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(SignUp_activity.this, "Unable To Sign You Up", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                DatabaseReference registerUser = FirebaseDatabase.getInstance().getReference().child("Users");
                                Map map = new HashMap();
                                map.put("firstName", FirstName);
                                map.put("otherName", OtherName);
                                map.put("phone", Phone);
                                map.put("category", SpinnerCategory);
                                map.put("email", Email);
                                map.put("type", type);
                                registerUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(map);
                                DatabaseReference progressBar = FirebaseDatabase.getInstance().getReference().child("Progress_Bar_Status");
                                progressBar.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(5);
                                Intent intent1 = new Intent(SignUp_activity.this, MainActivity.class);
                                startActivity(intent1);
                                progressDialog.dismiss();
                                finish();
                            }
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SpinnerCategory = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        Intent createAccount = new Intent(SignUp_activity.this, Login_activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        createAccount.putExtras(bundle);
        startActivity(createAccount);
        finish();
        super.onBackPressed();
    }
}

