package project.android.com.connect24;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private Button mLogin;
    private TextInputLayout mEmail,mPassword;
    private DatabaseReference mUserDatabaseRefrence;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoginProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth =  FirebaseAuth.getInstance();
        mLogin = findViewById(R.id.login_button);
        mToolbar  =  findViewById(R.id.login_toolbar);
        mEmail =  findViewById(R.id.login_email_name);
        mPassword =  findViewById(R.id.login_password);

        mUserDatabaseRefrence = FirebaseDatabase.getInstance().getReference().child("User");

        mLoginProgressDialog = new ProgressDialog(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //When login buttton is presed

        mLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
               String email =  mEmail.getEditText().getText().toString();
               String pass = mPassword.getEditText().getText().toString();

                if(email.isEmpty())
                {
                    mPassword.setErrorEnabled(false);
                    mEmail.setErrorEnabled(true);
                    mEmail.setError("Empty!");
                }
                else
                {
                    if(pass.isEmpty())
                    {
                        mEmail.setErrorEnabled(false);
                        mPassword.setErrorEnabled(true);
                        mPassword.setError("Empty!");

                    }
                    else
                    {
                        mPassword.getEditText().setText("");
                        mEmail.getEditText().setText("");


                        mLoginProgressDialog.setTitle("Logging In");
                        mLoginProgressDialog.setMessage("Please wait while we check your credentials.");
                        mLoginProgressDialog.setCancelable(false);

                        mLoginProgressDialog.show();

                        loginUser(email,pass);
                    }
                }
            }


        });
    }

    private void loginUser(String email, String pass)
    {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    //Saving Current login device details to the database(Known as Token)

                   String user_id = mAuth.getCurrentUser().getUid();
                   String mDeviceToken =  FirebaseInstanceId.getInstance().getToken();//getting current device token
                   mUserDatabaseRefrence.child(user_id).child("device_token").setValue(mDeviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid)
                       {
                           mLoginProgressDialog.dismiss();
                           Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                           finish();
                       }
                   });
                }
                else
                {
                    mLoginProgressDialog.hide();

                    Toast.makeText(LoginActivity.this, "Cannot Sign In. Please check your login credentials !", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
