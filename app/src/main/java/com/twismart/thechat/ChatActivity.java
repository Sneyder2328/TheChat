package com.twismart.thechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.mysampleapp.demo.nosql.UserDO;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "ChatActivity";
    private static int TIME_WAIT = 3000;//3 second of time wait
    private NetworkInteractor networkInteractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        networkInteractor = new NetworkInteractor(this);

        String chatId = getIntent().getStringExtra(Constantes.ID);
        if(chatId == null){
            startNewRandomChat();
        }
        else{
            startChatWithId(chatId);
        }
    }

    private boolean chatFound;
    private void startNewRandomChat(){
        networkInteractor.writeStatusSearching(new NetworkInteractor.IWriteStatusSearchingListener() {
            @Override
            public void onSucces() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            do {
                                Thread.sleep(TIME_WAIT);
                                Log.d(TAG, "luego de Thread.sleep(TIME_WAIT);");
                                networkInteractor.getStatus(new NetworkInteractor.IGetStatus() {
                                    @Override
                                    public void onSucces(String status) {
                                        if(status.equals(Constantes.Status.FOUND.name())){
                                            //start  chat
                                            chatFound = true;
                                            startChatWithId("orita busco la id en setchats");
                                        }
                                        if(status.equals(Constantes.Status.SEARCHING.name())){
                                            //search interlocutor and start chat
                                            Log.d(TAG, "luego de search interlocutor and start chat");
                                            networkInteractor.searchUserOnline(new NetworkInteractor.ISearchUserOnline() {
                                                @Override
                                                public void onSucces(String idUser) {
                                                    chatFound = true;
                                                    Log.d(TAG, "luego de  public void onSucces(String idUser) {");
                                                    startChatWithId(idUser);
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    Log.d(TAG, "luego de public void onFailure(String error) { " + error);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            } while(!chatFound);{

                            }
                        }
                        catch (Exception e){
                            Log.e(TAG, "catch in startNewRandomChat --> onSucces " + e.getMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void startChatWithId(String id){
        Log.d(TAG, "startChatWithId " + id);
    }
}
