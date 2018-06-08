package project.android.com.connect24;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConvViewHolder extends RecyclerView.ViewHolder
{
    private TextView friend_user_name,last_msg;
    private CircleImageView user_profile_pic;
    private ImageView status_icon;
    public View convView;

    public ConvViewHolder(View itemView)
    {
        super(itemView);

        friend_user_name =  itemView.findViewById(R.id.single_user_name_tv);

        last_msg =  itemView.findViewById(R.id.single_user_status_tv);

        user_profile_pic = itemView.findViewById(R.id.single_user_circular_layout);

        status_icon = itemView.findViewById(R.id.online_status);

        convView = itemView;
    }

    public   View  getUserDetailsView()
    {
        return convView;
    }

    public void setMessage(String data, String msg_type , String timestamp , boolean seen, String from,String current_loggedin_user)
    {
        if(msg_type.equals("text"))
        {
            if(data.length()>16)
            {
               data = data.substring(0,16)+".";
            }
            last_msg.setText(data+""+timestamp);
        }
        else
        {
            last_msg.setText("\uD83D\uDCF7"+" Photo "+timestamp);
        }


        if(!seen && !from.equals(current_loggedin_user) )
         {
           last_msg.setTypeface(last_msg.getTypeface(),Typeface.BOLD);
         }

    }

    public void setUserOnline(String userOnline)
    {
        if(userOnline.equals("true"))
        {
            status_icon.setVisibility(View.VISIBLE);
        }
        else
        {
            status_icon.setVisibility(View.INVISIBLE);
        }
    }

    public void setProfilePic(final String url)
    {
        if(!url.equals("default"))
        {
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_pic).into(user_profile_pic, new Callback() {
                @Override
                public void onSuccess()
                {

                }
                @Override
                public void onError(Exception e) {
                    Picasso.get().load(url).placeholder(R.drawable.profile_pic).into(user_profile_pic);
                }
            });
        }
    }
    public void setName(String name)
    {
        friend_user_name.setText(name);
    }
}
