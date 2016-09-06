package com.twismart.thechat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mysampleapp.demo.nosql.MessageDO;
import com.mysampleapp.demo.nosql.UserDO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "ChatActivity";
    private NetworkInteractor networkInteractor;

    private ProgressDialog mProgressDialog;
    private LinearLayout layoutMessages;
    private EditText inputNewMessage;
    private UserDO interlocutor = null;
    private PreferencesProfile  preferencesProfile;
    private LocalDataBase dataBase;
    public static ChatActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        networkInteractor = new NetworkInteractor(this);

        layoutMessages = (LinearLayout) findViewById(R.id.layoutMessages);

        inputNewMessage = (EditText) findViewById(R.id.inputNewMessage);

        preferencesProfile = new PreferencesProfile(getApplicationContext());

        dataBase = new LocalDataBase(getApplicationContext());

        instance = this;

        String chatId = getIntent().getStringExtra(Constantes.ID);
        if(chatId == null){
            showProgressDialog();
            startNewRandomChat();
        }
        else{
            networkInteractor.getUserById(chatId, new NetworkInteractor.IGetUserById() {
                @Override
                public void onSucces(UserDO userDO) {
                    startChat(userDO);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "getUserById onFailure " + error);
                    finish();
                }
            });
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage(getString(R.string.activity_chat_message_loadchat));
        mProgressDialog.show();
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(interlocutor == null){
                    finish();
                }
            }
        });
    }

    private void startNewRandomChat(){
        networkInteractor.findInterlocutor(new NetworkInteractor.IFindInterLocutorListener() {
            @Override
            public void onSucces(UserDO interlocutor) {
                startChat(interlocutor);
                mProgressDialog.cancel();
            }

            @Override
            public void onFailure(String error) {
                mProgressDialog.cancel();
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void startChat(UserDO interlocutor){
        this.interlocutor = interlocutor;
        Log.d(TAG, "startChat " + interlocutor.getUserId());
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(interlocutor.getName());
        }
        getMessages();
    }

    private void getMessages(){
        List<MessageDO> messagesSaved = dataBase.getMessagesWithUser(preferencesProfile.getId(), interlocutor.getUserId());
        if(messagesSaved.isEmpty()) {//if the list of Messages in local is Empty
            //get all the messages from the server
            getMessagesFromServer(0);
        }
        else {
            for(MessageDO message : messagesSaved){
                Log.d(TAG, "messagesSaved list " + message.getContent());
                addMessage(message);
            }
            double lastMessageDate = messagesSaved.get(messagesSaved.size()-1).getDate();
            Log.d(TAG, "last Message " +lastMessageDate);
            getMessagesFromServer(lastMessageDate);
        }
    }

    private void getMessagesFromServer(double dateMin){
        networkInteractor.getMessagesWithUser(interlocutor.getUserId(), dateMin, new NetworkInteractor.IGetMessagesWithUser() {
            @Override
            public void onSucces(List<MessageDO> listMessages) {
                if(!listMessages.isEmpty()){
                    for(MessageDO message : listMessages){
                        Log.d(TAG, "onSucces listMessages " + message.getContent());
                        addMessage(message);
                    }
                    dataBase.addMessages(listMessages);
                }
            }
            @Override
            public void onFailure(String error) {
                Log.d(TAG, "startChatWithId onFailure" + error);
            }
        });
    }

    public static class NotificationService extends FirebaseMessagingService {

        public static final String TAG = "NotificationService";

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            Log.d(TAG, "From: ");
            //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            if (remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("fromName")) {

                String title = remoteMessage.getData().get("title");
                String from = remoteMessage.getData().get("fromName");

                //if(interlocutor != null && interlocutor.getName().equals(from)){
                if(instance != null) {
                    Log.d(TAG, "if(interlocutor != null) {");
                    instance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            instance.getMessages();
                        }
                    });
                }
                else {
                    Log.d(TAG, "else {");
                    Intent intent = new Intent(this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(from)
                            .setAutoCancel(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
            }
            else {
                Log.d(TAG, "No tiene los datos suficientes");
            }
        }
    }

    private void addMessage(MessageDO messageDO){
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        int layout;
        if(messageDO.getBy().equals(preferencesProfile.getId())){
            layout  = R.layout.message_out;
        }
        else{
            layout  = R.layout.message_in;
        }
        ViewGroup message = (ViewGroup) layoutInflater.inflate(layout, null, false);

        TextView contentMessage = (TextView) message.findViewById(R.id.contentMessage);
        contentMessage.setText(messageDO.getContent().trim());

        TextView date = (TextView) message.findViewById(R.id.date);
        date.setText(getDateFromMilli(messageDO.getDate()));

        layoutMessages.addView(message);
    }

    private String getDateFromMilli(double timeMilli){
        Date date = new Date();
        date.setTime((long)timeMilli);

        SimpleDateFormat df = new SimpleDateFormat("d MMM h:mm a");
        return df.format(date);
    }

    public static final String URL_SEND_MSG = "http://54.162.88.89/sendmsg.php";

    public void send(View v){
        String messageToSend = inputNewMessage.getText().toString().trim();
        if(interlocutor != null && !messageToSend.isEmpty()){
            networkInteractor.newMessageToId(interlocutor.getUserId(), messageToSend, new NetworkInteractor.INewMessageToIdListener() {
                @Override
                public void onSendSucces() {
                    Log.d(TAG, "onSendSucces");
                    inputNewMessage.setText("");

                    final Map<String, String> params = new HashMap<>();
                    params.put("senderId", interlocutor.getTokenId());
                    params.put("title", "Tienes un nuevo mensaje");
                    params.put("fromName", preferencesProfile.getName());

                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, URL_SEND_MSG, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse post " + response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error: post " + error.getClass());
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            return params;
                        }
                    };
                    com.mysampleapp.Application.getInstance().addToRequestQueue(jsonObjectRequest);
                }
                @Override
                public void onSendFailure(String error) {
                    Log.d(TAG, "onSendFailure " + error);
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        networkInteractor.stopFindInterlocutor();
    }
}
