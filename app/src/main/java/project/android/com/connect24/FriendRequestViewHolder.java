package project.android.com.connect24;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder
{
    private View mHolderView;
    public CircleImageView mProfileView;
    public ImageButton mAccept,mDeny;
    private TextView mProfileUserName;
    public LinearLayout mLinearLayout;

    public FriendRequestViewHolder(View itemView)
    {
        super(itemView);
        mHolderView = itemView;
        mLinearLayout = itemView.findViewById(R.id.fr_main_ll);
        mProfileView =  itemView.findViewById(R.id.fr_user_circular_layout);
        mAccept = itemView.findViewById(R.id.fr_accept);
        mDeny = itemView.findViewById(R.id.fr_deny);
        mProfileUserName = itemView.findViewById(R.id.username_fr);

    }

    public  void setProfileImage(final String url)
    {
        if(!url.equals("default"))
        {
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_pic).into(mProfileView, new Callback() {
                @Override
                public void onSuccess()
                {

                }
                @Override
                public void onError(Exception e)
                {
                    Picasso.get().load(url).placeholder(R.drawable.profile_pic).into(mProfileView);
                }
            });
        }
    }

    public void setName(String user_name)
    {
          mProfileUserName.setText(user_name);
    }


}
