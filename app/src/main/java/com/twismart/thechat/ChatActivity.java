package com.twismart.thechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.util.ImageSelectorUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mysampleapp.demo.nosql.MessageDO;
import com.mysampleapp.demo.nosql.UserDO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "ChatActivity";
    private static final int PICK_PHOTO = 100, TAKE_PHOTO = 101;
    public static ChatActivity instance = null;

    private NetworkInteractor networkInteractor;

    private ProgressDialog mProgressDialog;

    private LinearLayout layoutMessages;
    private ScrollView scrollMessages;
    private EditText inputNewMessage;
    private ImageButton btnAction;

    private boolean btnActionSend = false;

    private static UserDO interlocutor = null;
    private PreferencesProfile preferencesProfile;
    private LocalDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        networkInteractor = new NetworkInteractor(this);

        layoutMessages = (LinearLayout) findViewById(R.id.layoutMessages);

        scrollMessages = (ScrollView) findViewById(R.id.scrollMessages);

        inputNewMessage = (EditText) findViewById(R.id.inputNewMessage);
        inputNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downScrollView();
                    }
                }, 200);
            }
        });
        inputNewMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnActionSend = charSequence.length() > 0;
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnAction = (ImageButton) findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnActionSend) {
                    send(inputNewMessage.getText().toString().trim(), true);
                    inputNewMessage.setText("");
                }
                else {
                    selectImg();
                }
            }
        });

        preferencesProfile = new PreferencesProfile(getApplicationContext());

        dataBase = new LocalDataBase(getApplicationContext());

        instance = this;

        String chatId = getIntent().getStringExtra(Constantes.ID);
        if (chatId == null) {
            showProgressDialog();
            startNewRandomChat();
        } else {
            UserDO userDO = new UserDO();
            userDO.setUserId(chatId);
            userDO.setTokenId(getIntent().getStringExtra(Constantes.TOKEN_ID));
            userDO.setName(getIntent().getStringExtra(Constantes.NAME));
            startChat(userDO);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void selectImg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.register_options_avatar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, TAKE_PHOTO);
                    }
                }
                else{
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        return;
                    }
                    // We have permission, so show the image selector.
                    final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                    startActivityForResult(intent, PICK_PHOTO);
                }
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                startActivityForResult(intent, PICK_PHOTO);
            } else {
                // Inform the user they won't be able to upload without permission.
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.content_permission_failure_title_text));
                dialogBuilder.setMessage(getString(R.string.content_permission_failure_text));
                dialogBuilder.setNegativeButton(getString(android.R.string.ok), null);
                dialogBuilder.show();
            }
        }
    }

    private File fileImg = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_PHOTO:
                    Uri selectedImage = data.getData();

                    final String path = ImageSelectorUtils.getFilePathFromUri(this, selectedImage);

                    fileImg = new File(path);
                    break;
                case TAKE_PHOTO:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    fileImg = Util.saveToInternalStorage(this, imageBitmap);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    networkInteractor.uploadFile(fileImg, new NetworkInteractor.IUploadFile() {
                        @Override
                        public void onSuccess(final String url) {
                            send(url, false);
                        }
                        @Override
                        public void onProgress(String fileName, boolean isWaiting, long bytesCurrent, long bytesTotal) {
                        }
                        @Override
                        public void onError(String fileName, Exception ex) {
                        }
                    });
                }
            }).start();
        }
    }



    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage(getString(R.string.activity_chat_message_loadchat));
        mProgressDialog.show();
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (interlocutor == null) {
                    finish();
                }
            }
        });
    }

    private void startNewRandomChat() {
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

    private void startChat(UserDO interlocutor) {
        this.interlocutor = interlocutor;
        Log.d(TAG, "startChat " + interlocutor.getUserId());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(interlocutor.getName());
        }
        getMessages(true);
    }

    private void getMessages(boolean showMessagesSaved) {
        List<MessageDO> messagesSaved = dataBase.getMessagesWithUser(preferencesProfile.getId(), interlocutor.getUserId());
        if (messagesSaved.isEmpty()) {//if the list of Messages in local is Empty
            //get all the messages from the server
            getMessagesFromServer(0);
        } else {
            if (showMessagesSaved) {
                for (MessageDO message : messagesSaved) {
                    Log.d(TAG, "messagesSaved list " + message.getContent());
                    addMessage(message);
                    downScrollView();
                }
            }
            double lastMessageDate = messagesSaved.get(messagesSaved.size() - 1).getDate();
            Log.d(TAG, "last Message " + lastMessageDate);
            getMessagesFromServer(lastMessageDate);
        }
    }

    private void downScrollView() {
        Log.d(TAG, "downScrollView()");
        scrollMessages.post(new Runnable() {
            public void run() {
                scrollMessages.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void getMessagesFromServer(double dateMin) {
        networkInteractor.getMessagesWithUser(interlocutor.getUserId(), dateMin, new NetworkInteractor.IGetMessagesWithUser() {
            @Override
            public void onSucces(List<MessageDO> listMessages) {
                if (!listMessages.isEmpty()) {
                    for (MessageDO message : listMessages) {
                        Log.d(TAG, "onSucces listMessages " + message.getContent());
                        addMessage(message);
                    }
                    downScrollView();
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
            if (remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("fromName") && remoteMessage.getData().containsKey("fromTokenId") && remoteMessage.getData().containsKey("fromId")) {

                String title = remoteMessage.getData().get("title");
                String fromName = remoteMessage.getData().get("fromName");
                String fromTokenId = remoteMessage.getData().get("fromTokenId");
                String fromId = remoteMessage.getData().get("fromId");

                //if(interlocutor != null && interlocutor.getName().equals(from)){
                if (instance != null && interlocutor.getName().equals(fromName)) {
                    Log.d(TAG, "if(interlocutor != null) {");

                    instance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            instance.getMessages(false);
                            MediaPlayer mp = MediaPlayer.create(instance, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            //  mp.prepare();
                            mp.start();
                        }
                    });
                } else {
                    Log.d(TAG, "else {");
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra(Constantes.ID, fromId);
                    intent.putExtra(Constantes.TOKEN_ID, fromTokenId);
                    intent.putExtra(Constantes.NAME, fromName);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(fromName)
                            .setAutoCancel(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
            } else {
                Log.d(TAG, "No tiene los datos suficientes");
            }
        }
    }

    private void addMessage(MessageDO messageDO) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        int layout;
        if (messageDO.getBy().equals(preferencesProfile.getId())) {
            layout = R.layout.message_out;
        } else {
            layout = R.layout.message_in;
        }
        ViewGroup message = (ViewGroup) layoutInflater.inflate(layout, null, false);

        TextView contentMessage = (TextView) message.findViewById(R.id.contentMessage);
        contentMessage.setText(messageDO.getContent().trim());

        TextView date = (TextView) message.findViewById(R.id.date);
        date.setText(getDateFromMilli(messageDO.getDate()));

        layoutMessages.addView(message);
    }

    private String getDateFromMilli(double timeMilli) {
        Date date = new Date();
        date.setTime((long) timeMilli);

        SimpleDateFormat df = new SimpleDateFormat("d MMM h:mm a");
        return df.format(date);
    }

    public static final String URL_SEND_MSG = "http://54.162.88.89/sendmsg.php";

    public void send(String content, boolean isText) {
        if (interlocutor != null && !content.isEmpty()) {
            String type;
            if(isText) {
                type = Constantes.TypeMessage.TEXT.name();
            }
            else {
                type = Constantes.TypeMessage.IMAGE.name();
            }
            networkInteractor.newMessageToId(interlocutor.getUserId(), content, type, new NetworkInteractor.INewMessageToIdListener() {
                @Override
                public void onSendSucces() {
                    Log.d(TAG, "onSendSucces");
                    getMessages(false);

                    final Map<String, String> params = new HashMap<>();
                    params.put("senderId", interlocutor.getTokenId());
                    params.put("fromTokenId", FirebaseInstanceId.getInstance().getToken());
                    params.put("fromName", preferencesProfile.getName());
                    params.put("fromId", preferencesProfile.getId());
                    params.put("title", getString(R.string.activity_chat_text_has_message));

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
                    }) {
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
