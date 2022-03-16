package com.example.olworks.olworks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Login_activity extends AppCompatActivity {

    Button createAccount, login;
    String type;
    TextInputLayout email, password;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString("type");
        Toast.makeText(this, ""+type, Toast.LENGTH_SHORT).show();

        createAccount = (Button) findViewById(R.id.buttonCreateAccount);
        login = (Button) findViewById(R.id.buttonLogin);
        email = (TextInputLayout) findViewById(R.id.editTextEmail);
        password = (TextInputLayout) findViewById(R.id.editTextPassword);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email = email.getEditText().getText().toString();
                String Password = password.getEditText().getText().toString();

                if (Password.equals("") || Email.equals("")){
                    Toast.makeText(Login_activity.this, "Please Fill In All Details", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setMessage("Signing You In");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(Login_activity.this, "Unable To Sign You In", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                if (type.equals("Employer")){
                                    DatabaseReference changeType = FirebaseDatabase.getInstance().getReference().child("Users");
                                    changeType.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("type").setValue("Employer");
                                    Intent intent = new Intent(Login_activity.this, Employer_tab_activity.class);
                                    startActivity(intent);
                                    progressDialog.dismiss();
                                    finish();
                                }else {
                                    DatabaseReference changeType = FirebaseDatabase.getInstance().getReference().child("Users");
                                    changeType.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("type").setValue("Employee");
                                    Intent intent = new Intent(Login_activity.this, Employee_tab_activity.class);
                                    startActivity(intent);
                                    progressDialog.dismiss();
                                    finish();
                                }
                            }
                        }
                    });
                }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccount = new Intent(Login_activity.this, SignUp_activity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("type", type);
                createAccount.putExtras(bundle1);
                startActivity(createAccount);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent createAccount = new Intent(Login_activity.this, MainActivity.class);
        startActivity(createAccount);
        finish();
        super.onBackPressed();
    }
}
