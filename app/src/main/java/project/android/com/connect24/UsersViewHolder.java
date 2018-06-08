package project.android.com.connect24;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public  class UsersViewHolder extends RecyclerView.ViewHolder
{

    private TextView single_user_name,user_status;
    private CircleImageView user_profile_pic;
    private ImageView status_icon;

    public  View view;

    public UsersViewHolder(View itemView)
    {
        super(itemView);

        single_user_name =  itemView.findViewById(R.id.single_user_name_tv);

        user_status =  itemView.findViewById(R.id.single_user_status_tv);

        user_profile_pic = itemView.findViewById(R.id.single_user_circular_layout);

        status_icon = itemView.findViewById(R.id.online_status);

        view = itemView;

    }

    public   View  getUserDetailsView()
    {
        return view;
    }


    public void setName(String name)
    {

       single_user_name.setText(name);

    }

    public void setStatus(String status)
    {

        user_status.setText("Status : "+status);
    }


    public void setOnlineStatus(String onlineStatus)
    {
        if(onlineStatus.equals("true"))
        {
            status_icon.setVisibility(View.VISIBLE);
        }
        else
        {
            status_icon.setVisibility(View.INVISIBLE);
        }
    }


    public void setProfileImage(String url)
    {
        if(!url.equals("default"))
        {
            Picasso.get().load(url).placeholder(R.drawable.profile_pic).into(user_profile_pic);
        }
    }



}
