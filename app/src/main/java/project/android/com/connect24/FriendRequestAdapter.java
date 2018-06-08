package project.android.com.connect24;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestViewHolder>
{
    private ArrayList mList;
    public Context parent_context;
    private DatabaseReference mUserDatabase,mRootRef;
    private String mCurrentUser;

    public FriendRequestAdapter(ArrayList mList)
    {
        this.mList = mList;
      mUserDatabase =   FirebaseDatabase.getInstance().getReference().child("User");
      mUserDatabase.keepSynced(true);
      mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
      mRootRef = FirebaseDatabase.getInstance().getReference();

    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
       View mRequestLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_single_layout,parent,false);

       parent_context =  parent.getContext();

        return new FriendRequestViewHolder(mRequestLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, final int position)
    {
       final String reqUser =  (String)mList.get(position);

       mUserDatabase.child(reqUser).addValueEventListener(new ValueEventListener()
       {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot)
           {
               final String name =  dataSnapshot.child("name").getValue().toString();
               String thumb_url = dataSnapshot.child("thumb_image").getValue().toString();

               holder.setName(name);
               holder.setProfileImage(thumb_url);

               //When Profile Image is clicked transferring to ProfileActivity

               holder.mProfileView.setOnClickListener(new View.OnClickListener()
               {
                   @Override
                   public void onClick(View view)
                   {
                       Intent intent = new Intent(parent_context,ProfileActivty.class);
                       intent.putExtra("user_id",reqUser);
                       parent_context.startActivity(intent);
                   }
               });

               ////////////// Accept a friend request  ////////////////

               holder.mAccept.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view)
                   {
                       holder.mDeny.setVisibility(View.INVISIBLE);
                       holder.mAccept.setVisibility(View.INVISIBLE);

                       mRootRef.child("Friend_req").child(mCurrentUser).child(reqUser).addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot)
                           {
                                    if(dataSnapshot.hasChild("request_type"))
                                    {
                                        // String reqType =   dataSnapshot.child("request_type").getValue().toString(); //It must be of type "received" in database

                                        String current_date = DateFormat.getDateTimeInstance().format(new Date());

                                        HashMap friendAcceptMap = new HashMap();

                                        friendAcceptMap.put("Friends/" + mCurrentUser + "/" + reqUser + "/date",current_date);
                                        friendAcceptMap.put("Friends/" + reqUser + "/" + mCurrentUser + "/date",current_date);

                                        //ACCEPTING FRIEND WILL DELETE FRIEND REQUEST QUERY FROM DATABASE

                                        friendAcceptMap.put("Friend_req/" + mCurrentUser + "/" + reqUser , null);
                                        friendAcceptMap.put("Friend_req/" + reqUser + "/" + mCurrentUser , null);

                                        mRootRef.updateChildren(friendAcceptMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                            {
                                                  if(databaseError!=null)
                                                  {
                                                      Log.e("RequestFragmentAccept",databaseError.getMessage().toString());

                                                  }
                                                  else
                                                  {         //Friend Accepting sucessfull

                                                      Toast.makeText(parent_context, "You are now a friend of "+ name  , Toast.LENGTH_SHORT).show();
                                                      mList.remove(position);
                                                      notifyItemRemoved(position);//It will reload the fragment
                                                  }
                                            }
                                        });
                                    } // if valid request
                                    else
                                    {
                                        mList.remove(position);
                                        notifyItemRemoved(position);//It will remove the current rv item reload the fragment
                                    }
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {
                           }
                       });
                   }
               });

               //////////// Deny a friend Request ///////////////

               holder.mDeny.setOnClickListener(new View.OnClickListener()
               {
                   @Override
                   public void onClick(View view)
                   {
                       holder.mDeny.setVisibility(View.INVISIBLE);
                       holder.mAccept.setVisibility(View.INVISIBLE);

                       mRootRef.child("Friend_req").child(mCurrentUser).child(reqUser).addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot)
                           {
                               if(dataSnapshot.hasChild("request_type"))
                               {
                                   HashMap denyMap = new HashMap();
                                   denyMap.put("Friend_req/" + mCurrentUser + "/" + reqUser , null);
                                   denyMap.put("Friend_req/" + reqUser + "/" + mCurrentUser , null);

                                   mRootRef.updateChildren(denyMap, new DatabaseReference.CompletionListener() {
                                       @Override
                                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                       {
                                           if(databaseError!=null)
                                           {
                                               Log.e("REQFRAGMENT_DenyRequest",databaseError.getMessage().toString());

                                           }
                                           else
                                           {         //Friend Deny successfull

                                               Toast.makeText(parent_context, "Cancelled" , Toast.LENGTH_SHORT).show();
                                               mList.remove(position);
                                               notifyItemRemoved(position);//It will reload the fragment
                                           }

                                       }
                                   });

                               }//if valid request

                               //If a user cancel the request after sending friend request,in that case we are just removing the relative layout

                               else
                               {
                                   mList.remove(position);
                                   notifyItemRemoved(position);//It will remove the current rv item reload the fragment
                               }
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {
                           }
                       });
                   }
               });

           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }
}
