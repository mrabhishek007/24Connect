package project.android.com.connect24;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity
{
    private CircleImageView appbar_pic;
    TextView chatUname,lastSeen;
    private String m_chatUser,m_chat_Username,m_chat_thumbimage;
    private DatabaseReference mUserDatabase,mRootRef,mFriends_Database;
    private DatabaseReference mChatDb;
    private FirebaseUser mUser;
    private Toolbar mToolbar;
    private String current_loggedin_user;
    private ImageButton mChatAddButton,mChatSendButton;
    private EditText mChatMessage;
    private RecyclerView mchatView;
    private  List<MessagesModel> mList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    public static  final int GALLARY_PICK = 1;
    private SwipeRefreshLayout mRefreshLayout;
    private StorageReference mStorageRefrence ;
    private File mCompressedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");

        mStorageRefrence = FirebaseStorage.getInstance().getReference();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        current_loggedin_user = mUser.getUid();

        m_chatUser = getIntent().getStringExtra("user_id"); //Getting the intent data from FriendsFragmentActivty
        m_chat_Username = getIntent().getStringExtra("user_name");
        m_chat_thumbimage = getIntent().getStringExtra("thumb_image");
        mToolbar =  findViewById(R.id.app_bar_chatsuser);

        mFriends_Database = FirebaseDatabase.getInstance().getReference().child("User").child(m_chatUser);

        mChatDb = mRootRef.child("messages").child(current_loggedin_user).child(m_chatUser);

        mChatDb.keepSynced(true);

        setSupportActionBar(mToolbar);

       ActionBar mActionBar =  getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        //mActionBar.setTitle(m_chat_Username);  not work bcz we are using custom action bar

        //Setting the custom action bar

        LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cust_appbar_view = inflater.inflate(R.layout.chat_custom_appbar,null);
        mActionBar.setCustomView(cust_appbar_view);

        //Getting the custom appbar icons and showing in custom app bar

       appbar_pic =  findViewById(R.id.custom_bar_profile_pic);
       chatUname =   findViewById(R.id.custom_bar_title);
        lastSeen =   findViewById(R.id.custom_bar_seen);
        mRefreshLayout = findViewById(R.id.swipe_to_refresh);
        mchatView =       findViewById(R.id.chat_list_rv);
        mChatAddButton =  findViewById(R.id.chat_add_btn);
        mChatSendButton = findViewById(R.id.chat_send_btn);
        mChatMessage =    findViewById(R.id.chat_message_view);

        chatUname.setText(m_chat_Username);

        if(!m_chat_thumbimage.equals("default"))
        {
            Picasso.get().load(m_chat_thumbimage).placeholder(R.drawable.profile_pic).into(appbar_pic);
        }

        mFriends_Database.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
              String timeStringStamp = dataSnapshot.child("last_seen").getValue().toString();
              String online_status =  dataSnapshot.child("online").getValue().toString();

               if(online_status.equals("true"))
               {
                   lastSeen.setText("Online");
               }
               else
               {
                   Long timeStamp =  Long.parseLong(timeStringStamp);
                   lastSeen.setText(""+timeStamp);
                   String latseen_status = GetTimeAgo.getTimeAgo(timeStamp,ChatActivity.this);//Getting the status of the time
                   lastSeen.setText(latseen_status);
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



         // Updating the chat root directory of the database to know when user was logged in

        HashMap chatAddMap = new HashMap();
        chatAddMap.put("seen",false);
        chatAddMap.put("timestamp",ServerValue.TIMESTAMP);
        HashMap  chatUserMap = new HashMap();
        chatUserMap.put("chat/" + current_loggedin_user + "/" +  m_chatUser ,chatAddMap);
        chatUserMap.put("chat/" + m_chatUser + "/" +  current_loggedin_user ,chatAddMap);

        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {

                if(databaseError!=null)
                {
                    Log.e("CHAT_LOG",databaseError.getMessage().toString());

                }
            }
        });

        mChatMessage.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

                if(charSequence.toString().equals(""))
                {
                    mChatSendButton.setClickable(false);
                }
                else
                {
                    mChatSendButton.setClickable(true);
                    //mChatSendButton.setImageAlpha(1000);
                }

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
            }
        });

        //When ChatSend button is pressed

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                 sendMessage();
            }
        });

        mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryIntent,"SELECT IMAGE"),GALLARY_PICK);

            }
        });
        retreiveChats();

        mRefreshLayout.setEnabled(false);

    }


    private void sendMessage()
    {
       String chat_msg =  mChatMessage.getText().toString();

       if(!chat_msg.isEmpty())
       {
           mChatMessage.setText("");
           String current_user_ref = "messages/" + current_loggedin_user + "/" + m_chatUser ;
           String chat_user_ref =  "messages/" + m_chatUser + "/" + current_loggedin_user;

           DatabaseReference user_msg_push = mRootRef.child("messages").child(current_loggedin_user).child(m_chatUser).push();
           String push_id =  user_msg_push.getKey();

           HashMap messageMap = new HashMap();

           messageMap.put( "message" ,chat_msg);
           messageMap.put("seen" ,false);
           messageMap.put("type" ,"text");
           messageMap.put("time" ,ServerValue.TIMESTAMP);
           messageMap.put("from",current_loggedin_user);

           HashMap messageUserMap = new HashMap();
           messageUserMap.put(current_user_ref + "/" + push_id ,messageMap);
           messageUserMap.put(chat_user_ref+ "/" + push_id ,messageMap);

           mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
               @Override
               public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
               {
                   if(databaseError!=null)
                   {
                       Log.e("CHAT_LOG", databaseError.getMessage().toString());
                   }
               }
           });
       }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        setlastMessageSeen();
        mUserDatabase.child(current_loggedin_user).child("online").setValue(""+true);
    }

    private void setlastMessageSeen()
    {
        // Retreiving the last message and setting seen status of the message true

        final Query last_msg =   mChatDb.limitToLast(1);

        last_msg.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
              String last_msg_push_key =   dataSnapshot.getKey();

              mChatDb.child(last_msg_push_key).child("seen").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid)
                  { }
              });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mUserDatabase.child(current_loggedin_user).child("online").setValue(""+false);
        mUserDatabase.child(current_loggedin_user).child("last_seen").setValue(ServerValue.TIMESTAMP);
    }


    private void retreiveChats()
    {
        //Initaializing the Recycler view so it can show list of messages

        messageAdapter = new MessageAdapter(mList);

        mchatView.setLayoutManager(new LinearLayoutManager(this));

        mchatView.setHasFixedSize(true);

        mchatView.setAdapter(messageAdapter);

        //Loading the list of messages

       // Query messageQuery = mRootRef.child("messages").child(current_loggedin_user).child(m_chatUser).limitToLast(TOTAL_ITEMS_TO_LOAD); //It will load last 10 messages only


        mChatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                MessagesModel model = dataSnapshot.getValue(MessagesModel.class);
                mList.add(model);
                messageAdapter.notifyDataSetChanged();
                mchatView.scrollToPosition(mList.size()-1);

              //  mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLARY_PICK && resultCode== RESULT_OK )
        {

            Uri mUri = data.getData();
            CropImage.activity(mUri)
                    .start(this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK)
                {
                    Uri uri = result.getUri();

                    String mpath =  uri.getPath();

                    File thumb_file = new File(mpath);

                    try
                    {
                        mCompressedImg =   new Compressor(this).setMaxWidth(600).setMaxHeight(600).setQuality(75).compressToFile(thumb_file);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    final String current_user_ref = "messages/" + current_loggedin_user + "/" + m_chatUser ;
                    final String chat_user_ref =  "messages/" + m_chatUser + "/" + current_loggedin_user;

                    DatabaseReference user_msg_push = mRootRef.child("messages").child(current_loggedin_user).child(m_chatUser).push();

                    final String push_id =  user_msg_push.getKey();

                    StorageReference filepath = mStorageRefrence.child("message_images").child(push_id+".jpg");

                    Uri muri =  Uri.fromFile(mCompressedImg);

                    filepath.putFile(muri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                String download_url =   task.getResult().getDownloadUrl().toString();//Getting the url of selected image
                                HashMap messageMap = new HashMap();

                                messageMap.put( "message" ,download_url);
                                messageMap.put("seen" ,false);
                                messageMap.put("type" ,"image");
                                messageMap.put("time" ,ServerValue.TIMESTAMP);
                                messageMap.put("from",current_loggedin_user);

                                HashMap messageUserMap = new HashMap();

                                messageUserMap.put(current_user_ref + "/" + push_id ,messageMap);
                                messageUserMap.put(chat_user_ref+ "/" + push_id ,messageMap);

                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                    {
                                        if(databaseError!=null)
                                        {
                                            Log.e("IMAGE_MSG_SENDING", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Error occured while sending image ! ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

    }

}

