package project.android.com.connect24;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivty extends AppCompatActivity
{

    private TextView mDispName,mDispStatus,mTotalFriends;
    private Button sendFriendReq,declineFriendReq;
    private ImageView userProfilePic;
    public ProgressDialog mProgressDialog;
    private DatabaseReference userDatabaseRefrence;
    private DatabaseReference mFriendReqRefrence;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private  DatabaseReference mFriendDatabase;
    private  String mCurrentReqStatus;
    public String user_name;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    public String u_id;
    public long mFriendCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_activty);

        userProfilePic = findViewById(R.id.single_user_profile_img_view);

        mDispStatus = findViewById(R.id.prof_act_disp_status);

        mDispName = findViewById(R.id.prof_act_disp_name);

        mTotalFriends = findViewById(R.id.prof_act_disp_total_friends);

        sendFriendReq = findViewById(R.id.send_frnd_req);

        declineFriendReq = findViewById(R.id.decline_frnd_req);




        //Retreiving usename from AllUserName Activity



        final String data = getIntent().getStringExtra("user_id");

        if(data==null)
        {

         u_id = getIntent().getStringExtra("from_user_id"); //Getting the intent data directly from firebase server while in this case we are getting notification in background

        }

        else
        {
            u_id = getIntent().getStringExtra("user_id"); //Getting the intent data directly when the app is running in foreground (i.e. from FirebaseMessaging Service and other Activity's )
        }

        mCurrentReqStatus = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading Profile");
        mProgressDialog.setMessage("Please wait while we load User profile...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mFriendReqRefrence = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendReqRefrence.keepSynced(true);

        userDatabaseRefrence = FirebaseDatabase.getInstance().getReference().child("User").child(u_id);
        userDatabaseRefrence.keepSynced(true);

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mFriendDatabase.child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                    mFriendCount =  dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        userDatabaseRefrence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("name").getValue().toString();
                String user_status = dataSnapshot.child("status").getValue().toString();
                String profile_img_url = dataSnapshot.child("image").getValue().toString();
                String total_friend = "Total Friends : "+String.valueOf(mFriendCount);

                mDispName.setText(user_name);
                mTotalFriends.setText(total_friend);
                mDispStatus.setText(user_status);

                Picasso.get().load(profile_img_url).placeholder(R.drawable.profile_pic).into(userProfilePic);
                // userProfilePic.setScaleType(ImageView.ScaleType.FIT_XY);


                //-----------------------FRIEND LIST REQUEST FEATURE----------------------------------

                //WE ARE CHECKING 4 STATUS ON ONE BUTTON SEND FRIEND REQUEST,CANCEL FRIEND REQUEST,ACCEPT FRIEND REQUEST,UNFRIEND.
                //DECLINE FRIEND REQUEST WILL BE ENABLED IF USER GET FRIEND REQUEST TO ACCEPT

                mFriendReqRefrence.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(u_id))
                        {
                            String friend_request_status = dataSnapshot.child(u_id).child("request_type").getValue().toString();
                            if (friend_request_status.equals("received"))
                            {
                                mCurrentReqStatus = "pending_request";
                                sendFriendReq.setText("Accept Friend Request");
                                declineFriendReq.setVisibility(View.VISIBLE);
                                sendFriendReq.setVisibility(View.VISIBLE);
                            } else if (friend_request_status.equals("sent")) {
                                mCurrentReqStatus = "friends";
                                sendFriendReq.setText("Cancel Friend Request");
                                sendFriendReq.setVisibility(View.VISIBLE);
                            }

                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(u_id)) {
                                        mCurrentReqStatus = "already_friends";
                                        sendFriendReq.setText("Unfriend " + user_name);
                                        sendFriendReq.setVisibility(View.VISIBLE);
                                    } else {
                                        //When user has no child that means they are not friends

                                        sendFriendReq.setVisibility(View.VISIBLE);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });
                        }
                        mProgressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        mProgressDialog.dismiss();
                        //Toast.makeText(ProfileActivty.this, "Unable to fetch friend request status ! ", Toast.LENGTH_SHORT).show();
                        final Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.profile_activity_mainll), "Unable to fetch friend request status !", Snackbar.LENGTH_LONG);
                        // Changing message text color
                        snackbar.setActionTextColor(Color.WHITE);
                        // Changing action button text color
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.RED);
                        snackbar.show();
                        snackbar.setAction("Close", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });

        //WHEN 1st BUTTON IS PRESSED

        sendFriendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //---------------------------------NOT FRIEND--------------------------------------------

                if (mCurrentReqStatus.equals("not_friends"))
                {
                    sendFriendReq.setVisibility(View.GONE);

                    final String current_loggedin_user_id = mCurrentUser.getUid();

                    //Saving friend request to sender in db as whom  he is sending the friend request

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(u_id).push();
                    String notif_id =  newNotificationRef.getKey();

                    HashMap requestMap  = new HashMap();

                    requestMap.put("Friend_req" + "/" + current_loggedin_user_id + "/" + u_id + "/" + "request_type" ,"sent");
                    requestMap.put("Friend_req" + "/" + u_id + "/" + current_loggedin_user_id + "/" + "request_type" , "received");

                      //Saving notification details in DB

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", current_loggedin_user_id);
                    notificationData.put("type", "request");

                    requestMap.put("notifications/" + u_id + "/" + notif_id,notificationData);

                    //It will update the exisiting query

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError!=null)
                            {
                                Toast.makeText(ProfileActivty.this, "There was some error occured in sending request ", Toast.LENGTH_SHORT).show();
                                sendFriendReq.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                //Toast.makeText(ProfileActivty.this, "Request sent ", Toast.LENGTH_SHORT).show();

                                final Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.profile_activity_mainll), "Request Sent Successfully. ", Snackbar.LENGTH_SHORT);
                                // Changing message text color
                                snackbar.setActionTextColor(Color.YELLOW);
                                // Changing action button text color
                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.GREEN);
                                snackbar.show();
                                snackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });

                                mCurrentReqStatus = "friends";
                                sendFriendReq.setText("Cancel Friend Request");
                                sendFriendReq.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                //-----------------------------CANCEL FRIEND REQUEST---------------------------------------------------

                if (mCurrentReqStatus.equals("friends")) {

                    sendFriendReq.setVisibility(View.GONE);

                    mFriendReqRefrence.child(mCurrentUser.getUid()).child(u_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqRefrence.child(u_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid)
                                {

                                    mCurrentReqStatus = "not_friends";

                                    sendFriendReq.setText("Send friend request");

                                    sendFriendReq.setVisibility(View.VISIBLE);

                                    final Snackbar snackbar = Snackbar
                                            .make(findViewById(R.id.profile_activity_mainll), "Friend request cancelled", Snackbar.LENGTH_SHORT);
                                    // Changing message text color
                                    snackbar.setActionTextColor(Color.YELLOW);
                                    // Changing action button text color
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(Color.RED);
                                    snackbar.show();
                                    snackbar.setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            snackbar.dismiss();
                                        }
                                    });

                                }
                            });
                        }
                    });

                }

                //-----------------------ACCEPT A FRIEND REQUEST-----------------------------------------------------

                if (mCurrentReqStatus.equals("pending_request"))
                {
                    sendFriendReq.setVisibility(View.GONE);
                    declineFriendReq.setVisibility(View.GONE);

                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                    HashMap friendAcceptMap = new HashMap();

                    friendAcceptMap.put("Friends/" + mCurrentUser.getUid() + "/" + u_id + "/date",current_date);
                    friendAcceptMap.put("Friends/" + u_id + "/" + mCurrentUser.getUid() + "/date",current_date);

                           //ACCEPTING FRIEND WILL DELETE FRIEND REQUEST QUERY FROM DATABASE

                    friendAcceptMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + u_id, null);
                    friendAcceptMap.put("Friend_req/" + u_id + "/" + mCurrentUser.getUid(), null);


                    mRootRef.updateChildren(friendAcceptMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {

                            if(databaseError!=null)
                            {
                                Toast.makeText(ProfileActivty.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                sendFriendReq.setVisibility(View.VISIBLE);
                                declineFriendReq.setVisibility(View.VISIBLE);

                            }
                            else
                            {
                                //IF ACCEPTING FRIEND IS SUCCESSFULL
                                mCurrentReqStatus = "already_friends";
                                sendFriendReq.setText("Unfriend " + user_name);
                                sendFriendReq.setVisibility(View.VISIBLE);
                                sendFriendReq.setEnabled(true);
                               // Toast.makeText(ProfileActivty.this, "Your are now a friend of " + user_name, Toast.LENGTH_SHORT).show();

                                final Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.profile_activity_mainll), "Your are now a friend of " + user_name, Snackbar.LENGTH_SHORT);
                                // Changing message text color
                                snackbar.setActionTextColor(Color.YELLOW);
                                // Changing action button text color
                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.GREEN);
                                snackbar.show();
                                snackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });


                            }
                        }
                    });

                }

                //---------------------UNFRIEND AN EXISTING FRIEND---------------------------------------

                if (mCurrentReqStatus.equals("already_friends")) {
                    sendFriendReq.setEnabled(false);
                    String c_uid = mCurrentUser.getUid();

                    HashMap unfriendMap = new HashMap();

                    unfriendMap.put("Friends/" + c_uid + "/" + u_id, null);
                    unfriendMap.put("Friends/" + u_id + "/" + c_uid, null);
                    unfriendMap.put("chat/" + c_uid + "/" + u_id , null);
                    unfriendMap.put("chat/" + u_id + "/" + c_uid, null);
                    unfriendMap.put("messages/" + c_uid + "/" + u_id, null);
                    unfriendMap.put("messages/" + u_id + "/" + c_uid, null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError!=null)
                            {
                                Log.e("Unfriend error",databaseError.getMessage().toString());

                            }
                            else
                            {
                                //When unfriend is successfull
                                mCurrentReqStatus = "not_friends";
                                sendFriendReq.setEnabled(true);
                                sendFriendReq.setText("Send friend request");

                                final Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.profile_activity_mainll), "Friend Sucessfully Removed ", Snackbar.LENGTH_SHORT);
                                // Changing message text color
                                snackbar.setActionTextColor(Color.YELLOW);
                                // Changing action button text color
                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.RED);
                                snackbar.show();
                                snackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });



                            }

                        }
                    });
                }
            }
        });


        declineFriendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineFriendReq.setVisibility(View.GONE);
                sendFriendReq.setVisibility(View.GONE);
                mFriendReqRefrence.child(mCurrentUser.getUid()).child(u_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendReqRefrence.child(u_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                mCurrentReqStatus = "not_friends";
                                sendFriendReq.setText("Send friend request");
                                sendFriendReq.setVisibility(View.VISIBLE);
                                //Toast.makeText(ProfileActivty.this, "Friend request Declined ", Toast.LENGTH_SHORT).show();

                                final Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.profile_activity_mainll), "Friend request Declined" ,Snackbar.LENGTH_SHORT);
                                // Changing message text color
                                snackbar.setActionTextColor(Color.YELLOW);
                                // Changing action button text color
                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.RED);
                                snackbar.show();
                                snackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });

                            }
                        });
                    }
                });
            }
        });


        mTotalFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent friendListIntent =  new Intent(ProfileActivty.this,UserListFriendActivity.class);
                friendListIntent.putExtra("from_user_id",u_id);
                startActivity(friendListIntent);
            }
        });


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
