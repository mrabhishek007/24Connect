package project.android.com.connect24;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity
{
    private TextInputLayout mDisplayName,mEmail,mPassword;
    private Button mCreateAccount;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    public String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

       mToolbar =  findViewById(R.id.register_toolbar);
       setSupportActionBar(mToolbar);
       getSupportActionBar().setTitle(R.string.create_account);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);//providing back button functionality in top of the app bar

        mProgressDialog = new ProgressDialog(this);

       mDisplayName =  findViewById(R.id.reg_display_name);
       mEmail = findViewById(R.id.reg_email);
       mPassword = findViewById(R.id.reg_password);

       mCreateAccount = findViewById(R.id.create_account_button);

       mCreateAccount.setOnClickListener(new View.OnClickListener()
       {
           @Override
           public void onClick(View view)
           {

               String disp_name = mDisplayName.getEditText().getText().toString();
               String email = mEmail.getEditText().getText().toString();
               String pass = mPassword.getEditText().getText().toString();

               if(disp_name.isEmpty())
               {
                   mEmail.setErrorEnabled(false);
                   mPassword.setErrorEnabled(false);
                   mDisplayName.setErrorEnabled(true);
                   mDisplayName.setError("Empty!");
                   mDisplayName.getEditText().requestFocus();
               }
               else
                   if(email.isEmpty())
                   {
                       mPassword.setErrorEnabled(false);
                       mDisplayName.setErrorEnabled(false);
                       mEmail.setErrorEnabled(true);
                       mEmail.setError("Empty!");
                       mEmail.getEditText().requestFocus();

                   }
                   else
                       if(pass.isEmpty())
                       {
                           mPassword.setErrorEnabled(true);
                           mDisplayName.setErrorEnabled(false);
                           mEmail.setErrorEnabled(true);
                           mPassword.setError("Empty!");
                           mPassword.getEditText().requestFocus();
                       }
                       else
                       {
                           if(validateEmail(email))
                           {
                                //When fields are not empty!

                                   mProgressDialog.setTitle("Registering User...");
                                   mProgressDialog.setMessage("Please wait while we create your account !");
                                   mProgressDialog.setCancelable(false);
                                   mProgressDialog.show();
                                   registerNewUser(disp_name, email, pass);
                           }
                           else
                           {
                               mPassword.setErrorEnabled(false);
                               mDisplayName.setErrorEnabled(false);
                               mEmail.setErrorEnabled(true);
                               mEmail.setError("Invalid Email");
                               mEmail.getEditText().requestFocus();
                           }


                       }
           }

           private void registerNewUser(final String disp_name, final String email, String pass)
           {
               mAuth =  FirebaseAuth.getInstance();
               mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   //When Registration is successfull
                   public void onComplete(@NonNull Task<AuthResult> task)
                   {
                       if(task.isComplete())
                       {
                             //Getting the UID and storing UID and related fields in the database if registration is sucessfull

                         FirebaseUser mFirebaseUser =  mAuth.getCurrentUser();

                         try {

                             uid = mFirebaseUser.getUid();


                             mFirebaseDatabase = FirebaseDatabase.getInstance();
                             DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference().child("User").child(uid);

                             //Saving Current login device details to the database(Known as Token)

                             String mDeviceToken =  FirebaseInstanceId.getInstance().getToken();//getting current device token

                             HashMap<String, String> hashMap = new HashMap<>();
                             hashMap.put("name", disp_name);
                             hashMap.put("status", "Hi there,I'm using 24Connect App.");
                             hashMap.put("image", "default");
                             hashMap.put("thumb_image", "default");
                             hashMap.put("device_token",mDeviceToken);

                             mDatabaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         //Transferring to Mainactivity after successfull signup/login

                                         mProgressDialog.dismiss();
                                         Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//removes all the previous activity
                                         startActivity(intent);
                                         finish();
                                     }
                                 }
                             });

                         }
                         catch(Exception e)
                         {
                             mProgressDialog.dismiss();
                             Toast.makeText(RegisterActivity.this, "error! Please check your credentails", Toast.LENGTH_SHORT).show();
                         }
                       }

                       else
                       {
                           mProgressDialog.hide();
                           mPassword.getEditText().setText("");
                           mEmail.getEditText().setText("");
                           mDisplayName.getEditText().setText("");
                           Snackbar.make(findViewById(R.id.ll_create_account_activity),"Cannot Sign In.Please check your credentials again! ",Snackbar.LENGTH_SHORT).show();
                       }
                   }
               });
           }
       });

    }

    private boolean validateEmail(String email)
    {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+[a-zA-Z0-9._]*@[a-zA-z]+([.][a-zA-z]+)+");//[a-zA-Z0-9]+ represents 1st character,[a-zA-Z0-9._]* represents remaining character
        Matcher matcher = pattern.matcher(email);

        if(matcher.find()&&matcher.group().equals(email))
        {
            return  true;
        }
           return  false;
    }
    }

