package project.android.com.connect24;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHoler>
{
    public Context parent_context;

    public ImageView msgImage;

    public List<MessagesModel> mMessageList;

    public MessageAdapter(List<MessagesModel> mMessageList)
    {
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

     View v =   LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_single_layout,parent,false);

    parent_context =  parent.getContext();

        return new MessageViewHoler(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHoler holder, int position)

    {
        msgImage = holder.mImageMsg;
        MessagesModel messagesModel =  mMessageList.get(position);
        String msg_type =  messagesModel.getType();
        final String msg =  messagesModel.getMessage();
        long msg_timestamp =   messagesModel.getTime();
        String msg_from = messagesModel.getFrom();
        String mTimestamp = new SimpleDateFormat("MMMM d 'at' h:m a").format(msg_timestamp);

       if(msg_type.equals("text"))
       {
           msgImage.setVisibility(View.GONE);
           holder.setMessageSender(msg_from);
           holder.setTimeChat(mTimestamp);
           holder.setMessage(msg);
       }
       //If msg is an image
       else if(msg_type.equals("image"))
       {
           /**
           Dynamically adding imageview to recyclerview
            ImageView iv = new ImageView(parent_context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(350,350);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,R.id.time_text_layout);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            iv.setLayoutParams(lp);
            holder.rl.addView(iv);
            */

          // imgView.setLayoutParams(new RelativeLayout.LayoutParams(300,300));
           holder.setTimeChat(mTimestamp);
           holder.setMessageSender(msg_from);
           holder.setMessage("");
           msgImage.setVisibility(View.VISIBLE);
           Picasso.get().load(msg).networkPolicy(NetworkPolicy.OFFLINE) //Enabliing offline image capability
                   .into(msgImage, new Callback() {
                       @Override
                       public void onSuccess() {
                       }
                       @Override
                       public void onError(Exception e)
                       {
                           Picasso.get().load(msg).into(msgImage);
                       }
                   });
       }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }
}
