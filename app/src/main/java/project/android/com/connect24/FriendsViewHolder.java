package project.android.com.connect24;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder
{

    private   View view;
    public TextView single_user_name,friend_date;
    public CircleImageView user_profile_pic;
    private ImageView online_status ;
    public LinearLayout mLinearlayout;

    public FriendsViewHolder(View itemView)
    {

        super(itemView);

        single_user_name =  itemView.findViewById(R.id.single_user_name_tv);

        friend_date =  itemView.findViewById(R.id.single_user_status_tv);

        user_profile_pic = itemView.findViewById(R.id.single_user_circular_layout);

        online_status = itemView.findViewById(R.id.online_status);

        mLinearlayout = itemView.findViewById(R.id.ll_except_iv);

        view = itemView;

    }


   public View getFriendsDetailsView()
   {
       return  view;
   }


  public void setDate(String date)
   {
       friend_date.setText(date);
   }

   public void setStatus(String status)
   {
       if(status.length()>40)
       {
           status = status.substring(0,38)+"..";
       }
       friend_date.setText("Status : "+status);
   }

   public void setName(String name)
   {
       single_user_name.setText(name);
   }

   public void setImage(String thumb_url)
   {
       if(!thumb_url.equals("default"))
       {
           Picasso.get().load(thumb_url).placeholder(R.drawable.profile_pic).into(user_profile_pic);
       }
   }


    public void setOnline(String online)
    {
        if(online.equals("true"))
        {
            online_status.setVisibility(View.VISIBLE);
        }
        else
        {
            online_status.setVisibility(View.INVISIBLE);
        }

    }
}
