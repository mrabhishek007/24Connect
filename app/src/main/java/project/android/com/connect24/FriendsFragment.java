package project.android.com.connect24;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FriendsFragment extends Fragment
{
    private RecyclerView mFriendList;
    private View mMainView;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private String mCurrentUserID;
    private FirebaseUser mFirebaseUser;
    private FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter;



    public FriendsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment

       mMainView  = inflater.inflate(R.layout.fragment_friends, container, false);

       mFriendList =  mMainView.findViewById(R.id.friends_list);

       mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

       mCurrentUserID = mFirebaseUser.getUid();

       mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserID);

      mFriendsDatabase.keepSynced(true); //Enabling offline feature of firebase

       mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");

      mUserDatabase.keepSynced(true);  //Enabling offline feature of firebase

       mFriendList.setHasFixedSize(true);

       mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

       //Inflate the layout for this fragment

        return mMainView;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        //Fetching the list of All Friends and showing it into Recycler View..........


        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendsDatabase,Friends.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull final Friends model)
            {

               final String user_id =  getRef(position).getKey(); //retreiving the key of selected user in layout

               mUserDatabase.child(user_id).addValueEventListener(new ValueEventListener()
               {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot)
                   {
                     final String name =  dataSnapshot.child("name").getValue().toString();

                     final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                     String status = dataSnapshot.child("status").getValue().toString();

                       holder.setName(name);
                      // holder.setDate(model.getDate());
                       holder.setImage(thumb_image);
                       holder.setStatus(status);
                       if(dataSnapshot.hasChild("online"))
                       {
                           String online =  dataSnapshot.child("online").getValue().toString();
                           holder.setOnline(online);
                       }

                       holder.user_profile_pic.setOnClickListener(new View.OnClickListener() //accessing the profile of the user_profile_circle
                       {
                           @Override
                           public void onClick(View view)
                           {
                               Intent profile_intent = new Intent(getContext(),ProfileActivty.class);
                               profile_intent.putExtra("user_id",user_id);
                               startActivity(profile_intent);
                           }
                       });

                       holder.mLinearlayout.setOnClickListener(new View.OnClickListener() //accessing the whole linear layout except imageview
                       {
                           @Override
                           public void onClick(View view)
                           {
                               Intent chat_intent = new Intent(getContext(),ChatActivity.class);
                               chat_intent.putExtra("user_id",user_id);
                               chat_intent.putExtra("user_name",name);
                               chat_intent.putExtra("thumb_image",thumb_image);
                               startActivity(chat_intent);
                           }
                       });

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

                // View detailView =  holder.getFriendsDetailsView(); It will provide accessability for user_single_layout


            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
                return new FriendsViewHolder(view);
            }
        };

        mFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}
