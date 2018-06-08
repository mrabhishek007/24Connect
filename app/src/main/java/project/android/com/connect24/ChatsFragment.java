package project.android.com.connect24;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mChatsView;
    private FirebaseUser mFirebaseUser;
    private String current_user;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUserDatabase;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<ConvModel,ConvViewHolder> firebaseRecyclerAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);



        mConvList = mChatsView.findViewById(R.id.conv_list);

         mFirebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
         current_user =  mFirebaseUser.getUid();

         mConvDatabase =  FirebaseDatabase.getInstance().getReference().child("chat").child(current_user);
         mConvDatabase.keepSynced(true);

         mMessageDatabase =  FirebaseDatabase.getInstance().getReference().child("messages").child(current_user);
         mMessageDatabase.keepSynced(true);

         mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        //Inflate the layout for this fragment
        return  mChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

       Query convQuery =  mConvDatabase.orderByChild("timestamp");   //It will order Chat list by timestamp (Recent message sent by which user will be on top)

       //Fetching all the list of last chats between users

        FirebaseRecyclerOptions<ConvModel> options = new FirebaseRecyclerOptions.Builder<ConvModel>().setQuery(convQuery,ConvModel.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ConvModel,ConvViewHolder>(options)
       {

           @NonNull
           @Override
           public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
           {
               View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);

               return new ConvViewHolder(view);
           }

           @Override
           protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final ConvModel model)
           {
               final String list_User_Id = getRef(position).getKey();

              Query lastMessageQuery =  mMessageDatabase.child(list_User_Id).limitToLast(1);//It will retreive last message send of all users

              lastMessageQuery.addChildEventListener(new ChildEventListener() {
                  @Override
                  public void onChildAdded(DataSnapshot dataSnapshot, String s)
                  {
                      String msg_from = dataSnapshot.child("from").getValue().toString();
                      String data =   dataSnapshot.child("message").getValue().toString();
                      String msg_Type = dataSnapshot.child("type").getValue().toString();
                      boolean isSeen = (boolean)dataSnapshot.child("seen").getValue();
                      long timestamp = (long)dataSnapshot.child("time").getValue();
                      String mTimestamp = new SimpleDateFormat(".   d MMMM 'at' h:m a").format(timestamp);
                      holder.setMessage(data,msg_Type,mTimestamp,isSeen,msg_from,current_user);
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
                  public void onCancelled(DatabaseError databaseError)
                  {
                      Log.e("ChatFragment",databaseError.getMessage().toString());
                  }
              }) ;

               mUserDatabase.child(list_User_Id).addValueEventListener(new ValueEventListener()
               {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot)
                   {
                      final String chatuser =    dataSnapshot.child("name").getValue().toString();
                      final String user_thumb =  dataSnapshot.child("thumb_image").getValue().toString();

                      if(dataSnapshot.hasChild("online"))
                      {
                        String online_status =   dataSnapshot.child("online").getValue().toString();
                        holder.setUserOnline(online_status);
                      }
                      holder.setName(chatuser);
                      holder.setProfilePic(user_thumb);

                       holder.getUserDetailsView().setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view)
                           {
                               Intent chat_intent = new Intent(getContext(),ChatActivity.class);
                               chat_intent.putExtra("user_id",list_User_Id);
                               chat_intent.putExtra("user_name",chatuser);
                               chat_intent.putExtra("thumb_image",user_thumb);
                               startActivity(chat_intent);
                           }
                       });
                   }
                   @Override
                   public void onCancelled(DatabaseError databaseError)
                   {Log.e("ChatFragment",databaseError.getMessage().toString());
                   }
               });
           }
       };
        mConvList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}
