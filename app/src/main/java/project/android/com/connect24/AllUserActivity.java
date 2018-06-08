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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class AllUserActivity extends AppCompatActivity
{

    private FirebaseRecyclerAdapter<UserAdapter,UsersViewHolder> firebaseRecyclerAdapter;
    private Toolbar mToolbar;
    private ProgressDialog mPrgressDialog;
    private RecyclerView mUSerList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDatabase1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        mToolbar =  findViewById(R.id.app_bar_allusers);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrgressDialog = new ProgressDialog(this);
        mPrgressDialog.setTitle("Fetching Users");
        mPrgressDialog.setMessage("Please wait while we are retreiving users...");
        mPrgressDialog.setCancelable(false);
        mPrgressDialog.show();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUSerList = findViewById(R.id.recyclerview_alluser);
        mUSerList.setHasFixedSize(true);
        mUSerList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart()
    {
        super.onStart();

       checkOnlineStatus();

                 //Fetching the list of All users and showing it into Recycler View..........

        FirebaseRecyclerOptions<UserAdapter> options = new FirebaseRecyclerOptions.Builder<UserAdapter>().setQuery(mUserDatabase,UserAdapter.class).build();


           firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserAdapter, UsersViewHolder>(options)
           {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, final int position, @NonNull UserAdapter model)
            {
                final String user_id =  getRef(position).getKey();//retreiving the key of selected user in layout
                 holder.setName(model.getName());
                 holder.setStatus(model.getStatus());
                 holder.setProfileImage(model.getThumb_image());
                 holder.setOnlineStatus(model.getOnline_status());
                 mPrgressDialog.dismiss();

                 holder.getUserDetailsView().setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view)
                     {
                         Intent intent = new Intent(AllUserActivity.this,ProfileActivty.class);
                         intent.putExtra("user_id",user_id);
                         startActivity(intent);
                     }
                 });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
               View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);

                return new UsersViewHolder(view);
            }
        };

        mUSerList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkOnlineStatus()
    {
        mUserDatabase1 = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mUserDatabase1.child("online").setValue(""+true);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabase1.child("online").setValue(""+false);
        mUserDatabase1.child("last_seen").setValue(ServerValue.TIMESTAMP);
    }
}
