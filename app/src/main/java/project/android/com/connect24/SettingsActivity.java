package project.android.com.connect24;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity
{

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurentUser;
    private CircleImageView mCircleImageView;
    private TextView mDisplayname,mStatus;
    private Button change_image,change_status;
    private ProgressDialog mProgressDialog,mImageChangeProgreesDialog;
    public static final int GALLARY_PICK = 1;
    public Bitmap thumb_bitmap;

    private StorageReference mStorageRefrence;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Firebase Storage

        mStorageRefrence = FirebaseStorage.getInstance().getReference();


      mCircleImageView =   findViewById(R.id.profile_setting_image);
      mDisplayname = findViewById(R.id.profile_user_name);
      mStatus = findViewById(R.id.profile_status);

      change_image = findViewById(R.id.profile_change_image_button);
      change_status = findViewById(R.id.profile_change_status_button);


        mProgressDialog = new ProgressDialog(this);
        mImageChangeProgreesDialog = new ProgressDialog(this);


        mProgressDialog.setTitle("Loading Profile");
        mProgressDialog.setMessage("Please wait while we load your profile...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

       mCurentUser  =   FirebaseAuth.getInstance().getCurrentUser();

       String uid = mCurentUser.getUid();

      mUserDatabase =  FirebaseDatabase.getInstance().getReference().child("User").child(uid);

      mUserDatabase.keepSynced(true);//It will enable offline capability of firebase

      mUserDatabase.addValueEventListener(new ValueEventListener()
      {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot)
          {

             String name =  dataSnapshot.child("name").getValue().toString();
             String status =  dataSnapshot.child("status").getValue().toString();
             final String image =  dataSnapshot.child("image").getValue().toString();
             String thumb_image =  dataSnapshot.child("thumb_image").getValue().toString();


             mDisplayname.setText(name);
             mStatus.setText("Status : "+status);

             if(!image.equals("default"))
             {

                 //Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(mCircleImageView);

                 //Enabling offline image loading capability

                 Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                         .placeholder(R.drawable.profile_pic).into(mCircleImageView, new Callback() {
                     @Override
                     public void onSuccess()
                     {

                     }

                     @Override
                     public void onError(Exception e)
                     {
                         Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(mCircleImageView);

                     }
                 });




             }

             mProgressDialog.dismiss();

             change_status.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view)
                 {
                     String status_value = mStatus.getText().toString();
                     Intent i  = new Intent(SettingsActivity.this,StatusActivity.class);
                     i.putExtra("status_value",status_value);
                     startActivity(i);
                 }
             });
          }
          @Override
          public void onCancelled(DatabaseError databaseError)
          {
              mProgressDialog.dismiss();
              Log.e("-->Profile loading ","Error Occured while loading your Profile ! ");
          }
      });


        //Changing the profile image

        change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallaryIntent,"SEELCT IMAGE"),GALLARY_PICK);

                   /**

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);

                    */

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if(requestCode==GALLARY_PICK && resultCode==RESULT_OK)
        {
           Uri imagUri =  data.getData();
           CropImage.activity(imagUri).
                   setAspectRatio(1,1).
                   setMinCropWindowSize(500,500)
                   .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mImageChangeProgreesDialog.setTitle("Uploading Image");
                mImageChangeProgreesDialog.setMessage("Please wait while we upload your image...");
                mImageChangeProgreesDialog.setCanceledOnTouchOutside(false);
                mImageChangeProgreesDialog.show();

                Uri resultUri = result.getUri();//getting uri of selected image.

                //Converting Original image to thumbnail_image using Compressor

                File thumb_file = new File(resultUri.getPath());

                String UID =   mCurentUser.getUid();

                try
                {
                   thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_file);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

                final byte[] thumb_byte =  byteArrayOutputStream.toByteArray();

                //Storing Profile_image and thumb_image into database storage

                final StorageReference thumb_filepath = mStorageRefrence.child("profile_images").child("thumbs").child(UID+".jpg");

                StorageReference filePath =  mStorageRefrence.child("profile_images").child(UID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()//It will upload the croped image in the firebase storage
               {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) 
                   {
                       if(task.isSuccessful())
                       {

                          final String profile_pic_url =  task.getResult().getDownloadUrl().toString(); //Getting the download url of original quality uploaded profile picture

                           //Uploading  process for thumnail image using Bitmap

                          UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                          uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                              @Override
                              public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                              {
                                  String thumb_download_url =  thumb_task.getResult().getDownloadUrl().toString();

                                  if(thumb_task.isSuccessful())
                                  {
                                       //Updating url of both image and thumb_image into database

                                      Map update_hash_map = new HashMap();
                                      update_hash_map.put("image",profile_pic_url);
                                      update_hash_map.put("thumb_image",thumb_download_url);

                                      mUserDatabase.updateChildren(update_hash_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task)
                                          {
                                              if(task.isSuccessful())
                                              {
                                                  Picasso.get().load(profile_pic_url).placeholder(R.drawable.profile_pic).into(mCircleImageView);
                                                  mImageChangeProgreesDialog.dismiss();
                                                  Toast.makeText(SettingsActivity.this, "Profile Picture Sucessfully set", Toast.LENGTH_SHORT).show();
                                              }
                                              else
                                              {
                                                  mImageChangeProgreesDialog.dismiss();
                                                  Toast.makeText(SettingsActivity.this, "Error saving profile image url in database", Toast.LENGTH_SHORT).show();
                                              }

                                          }
                                      });


                                  }
                                  else
                                  {
                                      Log.e("-->>Thumbnail","Error uploading thumbnail ! ");

                                  }

                              }
                          });

                       }
                       else
                       {
                           mImageChangeProgreesDialog.dismiss();
                           Toast.makeText(SettingsActivity.this, "Error while uploading profile picture ! ", Toast.LENGTH_SHORT).show();

                       }
                   }
               });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mUserDatabase.child("online").setValue(""+true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mUserDatabase.child("online").setValue(""+false);
        mUserDatabase.child("last_seen").setValue(ServerValue.TIMESTAMP);

    }


}
