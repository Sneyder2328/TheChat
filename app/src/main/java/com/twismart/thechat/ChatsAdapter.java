package com.twismart.thechat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.mysampleapp.demo.nosql.UserDO;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sneyd on 9/2/2016.
 **/
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> implements View.OnClickListener {

    private View.OnClickListener listener;
    List<UserDO> users;
    private Context context;
    private UserDO myUser;
    private AmazonS3 s3 = new AmazonS3Client(AWSMobileClient.defaultMobileClient().getIdentityManager().getCredentialsProvider());

    public ChatsAdapter(Context context, List<UserDO> users, UserDO myUser, View.OnClickListener listener){
        this.context = context;
        this.users = users;
        this.myUser = myUser;
        this.listener = listener;
    }

    public void setListUsers(List<UserDO> users){
        this.users = users;
    }

    @Override
    public ChatsAdapter.ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        itemView.setOnClickListener(this);
        return new ChatsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatsAdapter.ChatsViewHolder holder, int position) {
        holder.nameUser.setText(users.get(position).getName());
        String status;
        if(users.get(position).getStatus().equals(Constantes.Status.OFFLINE.name())){
            status = context.getString(R.string.chats_text_status_offline);
            holder.statusUser.setTextColor(Color.RED);
        }
        else {
            status = context.getString(R.string.chats_text_status_online);
            holder.statusUser.setTextColor(Color.GREEN);
        }
        holder.statusUser.setText(status);
        holder.distanceToUser.setText(String.valueOf(Util.distanceBetweenUsers(myUser, users.get(position))));
        holder.distanceToUser.append("Km");
        String url = users.get(position).getPhotoUrl();
        if(!url.contains("http")) {
            url = Util.generateURL(s3, url);
        }
        Log.e("URL", url);
        Picasso.with(context).load(url).into(holder.imgUser);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        protected TextView nameUser, distanceToUser, statusUser;
        protected CircleImageView imgUser;

        public ChatsViewHolder(View v) {
            super(v);
            nameUser = (TextView) v.findViewById(R.id.nameUser);
            statusUser = (TextView) v.findViewById(R.id.statusUser);
            distanceToUser = (TextView) v.findViewById(R.id.distanceToUser);
            imgUser = (CircleImageView) v.findViewById(R.id.imgUser);
        }
    }

    @Override
    public void onClick(View view) {
        if(listener != null)
            listener.onClick(view);
    }
}
