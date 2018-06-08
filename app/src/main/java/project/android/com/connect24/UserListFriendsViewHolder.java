package project.android.com.connect24;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListFriendsViewHolder extends RecyclerView.ViewHolder
{
    public View userFriendListView;
    private TextView single_user_name,friend_since;
    private CircleImageView user_profile_pic;
    private ImageView status_icon;

    public UserListFriendsViewHolder(View itemView)
    {
        super(itemView);
        userFriendListView =  itemView;
        single_user_name =  itemView.findViewById(R.id.single_user_name_tv);

        friend_since =  itemView.findViewById(R.id.single_user_status_tv);

        user_profile_pic = itemView.findViewById(R.id.single_user_circular_layout);

        status_icon = itemView.findViewById(R.id.online_status);

    }

    public void setFriendUserDetails(String mFriendname, final String url, String online, String mFriendSince)
    {
        single_user_name.setText(mFriendname);

        if(online.equals("true"))
        {
            status_icon.setVisibility(View.VISIBLE);
        }
        else
        {
            status_icon.setVisibility(View.INVISIBLE);
        }

        String date[] = mFriendSince.split(" ");
        String newdate = "Friend since :  "+date[0];
        friend_since.setText(newdate);

        if (!url.equals("default"))
        {
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_pic).into(user_profile_pic, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e)
                {
                    Picasso.get().load(url).placeholder(R.drawable.profile_pic).into(user_profile_pic);
                }
            });
        }
    }
}
