package com.twismart.thechat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.*;
import com.mysampleapp.demo.nosql.UserDO;

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

    public ChatsAdapter(Context context, List<UserDO> users, UserDO myUser, View.OnClickListener listener){
        this.context = context;
        this.users = users;
        this.myUser = myUser;
        this.listener = listener;
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
        }
        holder.statusUser.setText(status);
        holder.distanceToUser.setText(String.valueOf(Util.distanceBetweenUsers(myUser, users.get(position))));
        holder.distanceToUser.append("Km");
        Glide.with(context).load(users.get(position).getPhotoUrl()).into(holder.imgUser);
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
