package com.twismart.thechat;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.mysampleapp.demo.nosql.MessageDO;
import com.mysampleapp.demo.nosql.UserDO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "ChatActivity";
    private NetworkInteractor networkInteractor;

    private ProgressDialog mProgressDialog;
    private LinearLayout layoutMessages;
    private EditText inputNewMessage;

    private String idInterlocutor = null;
    private UserDO interlocutor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        networkInteractor = new NetworkInteractor(this);

        layoutMessages = (LinearLayout) findViewById(R.id.layoutMessages);

        inputNewMessage = (EditText) findViewById(R.id.inputNewMessage);

        String chatId = getIntent().getStringExtra(Constantes.ID);
        if(chatId == null){
            showProgressDialog();
            startNewRandomChat();
        }
        else{
            startChatWithId(chatId);
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
                if(idInterlocutor == null){
                    finish();
                }
            }
        });
    }

    private void startNewRandomChat(){
        networkInteractor.findInterlocutor(new NetworkInteractor.IFindInterLocutorListener() {
            @Override
            public void onSucces(UserDO interlocutor) {
                idInterlocutor = interlocutor.getUserId();
                startChatWithId(idInterlocutor);
                if(getSupportActionBar() != null){
                    getSupportActionBar().setTitle(interlocutor.getName());
                }
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

    private void startChatWithId(String id){
        Log.d(TAG, "startChatWithId " + id);
        idInterlocutor = id;
        networkInteractor.getMessagesWithUser(idInterlocutor, new NetworkInteractor.IGetMessagesWithUser() {
            @Override
            public void onSucces(List<MessageDO> listMessages) {
                //if(!listMessages.isEmpty()){
                    for(MessageDO message : listMessages){
                        Log.d(TAG, "onSucces list " + message.getContent());
                    }
                //}
            }
            @Override
            public void onFailure(String error) {
                Log.d(TAG, "startChatWithId onFailure" + error);
            }
        });
    }

    private void addMyMessage(){

    }

    private void addInterlocutorMessage(){

    }

    public static final String URL_SEND_MSG = "http://54.162.88.89/sendmsg.php";

    public void send(View v){
        String messageToSend = inputNewMessage.getText().toString().trim();
        if(idInterlocutor != null && !messageToSend.isEmpty()){
            networkInteractor.newMessageToId(idInterlocutor, messageToSend, new NetworkInteractor.INewMessageToIdListener() {
                @Override
                public void onSendSucces() {
                    Log.d(TAG, "onSendSucces");

                    final Map<String, String> params = new HashMap<>();
                    params.put("senderId", "id");
                    params.put("title", "Titulo");
                    params.put("from", "de");

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
        networkInteractor.stopFindInterlocutor();
    }
}
