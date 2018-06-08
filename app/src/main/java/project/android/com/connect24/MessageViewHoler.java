package project.android.com.connect24;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHoler extends RecyclerView.ViewHolder
{


    public TextView mMessageText,mSender,mTime;
    public ImageView mImageMsg;
    private CircleImageView mprofileImage;
    private View mView;
    private String current_looged_in_user;
    private DatabaseReference mUserDatabase;


    public MessageViewHoler(View itemView)
    {
        super(itemView);

        mView = itemView;

       mMessageText =   itemView.findViewById(R.id.message_text_layout);

       mprofileImage =   itemView.findViewById(R.id.message_profile_layout);

       mSender =  itemView.findViewById(R.id.name_text_layout);

       mTime =  itemView.findViewById(R.id.time_text_layout);

       current_looged_in_user =  FirebaseAuth.getInstance().getCurrentUser().getUid();

       mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
       mUserDatabase.keepSynced(true);

       mImageMsg = itemView.findViewById(R.id.message_image_layout);
    }


   public View getMessageView()
   {
       return mView;
   }

   public void setMessage(String message)
   {
       mMessageText.setText(message);
   }


   public void setMessageSender(String sender)
   {
        mUserDatabase.child(sender).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String sender_username = dataSnapshot.child("name").getValue().toString();
                final String url = dataSnapshot.child("thumb_image").getValue().toString();

                mSender.setText(sender_username);
                if (!url.equals("default"))
                {
                    Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile_pic).into(mprofileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e)
                        {
                            Picasso.get().load(url).placeholder(R.drawable.profile_pic).into(mprofileImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
   }


    public void setTimeChat(String time)
    {
        mTime.setText(time);
    }
}
