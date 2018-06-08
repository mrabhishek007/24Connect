package project.android.com.connect24;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private Button status_change_button;

    private TextInputLayout mTextInputLayout;

    private FirebaseUser mFirebaseUser;



    private DatabaseReference mDatabaseReference;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

      mToolbar =   findViewById(R.id.status_activity_appbar);



      setSupportActionBar(mToolbar);

      getSupportActionBar().setTitle("Account Status");

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

     status_change_button =  findViewById(R.id.change_status_btn);


     mTextInputLayout =  findViewById(R.id.change_status_til);


    String status_value =  getIntent().getStringExtra("status_value");

     mTextInputLayout.getEditText().setText(status_value.substring(9));//Remove Starting 9 character

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mFirebaseUser.getUid();
        mDatabaseReference =  FirebaseDatabase.getInstance().getReference().child("User").child(uid);


     status_change_button.setOnClickListener(new View.OnClickListener()
     {

         @Override
         public void onClick(View view)
         {
            changeStatus();

         }
     });

    }

    private void changeStatus()
    {
        String status_msg =  mTextInputLayout.getEditText().getText().toString();

        if(status_msg.isEmpty())
        {
            mTextInputLayout.setErrorEnabled(true);
            mTextInputLayout.setError("Empty!");
        }
        else
        {
            mTextInputLayout.setErrorEnabled(false);

            mProgressDialog = new ProgressDialog(StatusActivity.this);
            mProgressDialog.setTitle("Saving Changes ");
            mProgressDialog.setMessage("Please wait while we change your status... ");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            mDatabaseReference.child("status").setValue(status_msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        mProgressDialog.dismiss();
                        Toast.makeText(StatusActivity.this, "Status Changed Successfullly.", Toast.LENGTH_SHORT).show();

                        mTextInputLayout.getEditText().setText("");
                    }
                    else
                    {
                        mProgressDialog.hide();
                        Toast.makeText(StatusActivity.this, "Cannot Change Status !", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        mDatabaseReference.child("online").setValue(""+true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mDatabaseReference.child("online").setValue(""+false);
        mDatabaseReference.child("last_seen").setValue(ServerValue.TIMESTAMP);

    }
}
