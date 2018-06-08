package project.android.com.connect24;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

//It will show liist of friends of selected user

public class UserListFriendActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<UserListFriendsModel,UserListFriendsViewHolder> firebaseRecyclerAdapter;
    private String currentUID;
    private DatabaseReference mUserDatabase,mFriendDatabase;
    private Toolbar mToolbar;
    private RecyclerView mFriendList;
    private ProgressDialog mPrgressDialog;
    private String userUID,fromUserName ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_friend);

        userUID =  getIntent().getStringExtra("from_user_id");

        currentUID =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(userUID);

        mUserDatabase.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                fromUserName =  dataSnapshot.child("name").getValue().toString();
                setSupportActionBar(mToolbar);
                getSupportActionBar().setTitle(fromUserName+" \uD83D\uDC65");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mToolbar =  findViewById(R.id.app_bar_userfriendlist);
        mFriendList = findViewById(R.id.recyclerview_alluserfriendlist);

        mPrgressDialog = new ProgressDialog(this);
        mPrgressDialog.setTitle("Fetching Users");
        mPrgressDialog.setMessage("Please wait while we are retreiving users...");
        mPrgressDialog.setCanceledOnTouchOutside(false);
        mPrgressDialog.show();

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(this));

    }

    private void retreiveFriend()
    {
        //Fetching the list of All users and showing it into Recycler View..........

        FirebaseRecyclerOptions<UserListFriendsModel> options = new FirebaseRecyclerOptions.Builder<UserListFriendsModel>().setQuery(mFriendDatabase,UserListFriendsModel.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserListFriendsModel, UserListFriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final UserListFriendsViewHolder holder, final int position, @NonNull UserListFriendsModel model)
            {
                    final String mFriendsUID =  getRef(position).getKey();
                    final String mFriendSince =  model.getDate();


                    mUserDatabase.child(mFriendsUID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                           String mFriendname   =   dataSnapshot.child("name").getValue().toString();
                           String mFriendthumb  =  dataSnapshot.child("thumb_image").getValue().toString();
                           String mFriendonline =  dataSnapshot.child("online").getValue().toString();
                           holder.setFriendUserDetails(mFriendname,mFriendthumb,mFriendonline,mFriendSince);
                           mPrgressDialog.dismiss();

                           //When the users are clicked

                           holder.userFriendListView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view)
                               {
                                   Intent profileIntent =  new Intent(UserListFriendActivity.this,ProfileActivty.class);
                                   profileIntent.putExtra("user_id",mFriendsUID);
                                   startActivity(profileIntent);

                               }
                           });

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                   });
            }

            @NonNull
            @Override
            public UserListFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);

                return new UserListFriendsViewHolder(view);
            }
        };

        mFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mUserDatabase.child(currentUID).child("online").setValue(""+true);
        retreiveFriend();

    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mUserDatabase.child(currentUID).child("online").setValue(""+false);
        mUserDatabase.child(currentUID).child("last_seen").setValue(ServerValue.TIMESTAMP);

    }
}
