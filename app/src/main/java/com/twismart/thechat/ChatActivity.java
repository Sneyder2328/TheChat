package com.twismart.thechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.util.ImageSelectorUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mysampleapp.demo.nosql.MessageDO;
import com.mysampleapp.demo.nosql.UserDO;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements AdListener {

    private static final int PICK_PHOTO = 100, TAKE_PHOTO = 101, PICK_BACKGROUND = 102;
    private static final String TAG = "ChatActivity";
    private static final int NUM_MESSAGES_SHOW = 15;
    public static ChatActivity instance = null;

    private NetworkInteractor networkInteractor;

    private ProgressDialog mProgressDialog;

    private LinearLayout layoutMessages;
    private ScrollView scrollMessages;
    private EditText inputNewMessage;
    private ImageButton btnAction;
    private Button btnLoadMore;
    private ImageView viewBackground;

    private boolean btnActionSend = false;

    private static UserDO interlocutor = null;
    private PreferencesProfile preferencesProfile;
    private LocalDataBase dataBase;

    private AmazonS3 s3 = new AmazonS3Client(AWSMobileClient.defaultMobileClient().getIdentityManager().getCredentialsProvider());

    private com.amazon.device.ads.AdLayout amazonAdView;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        com.amazon.device.ads.AdRegistration.setAppKey(Constantes.ADS_AMAZON);
        MobileAds.initialize(getApplicationContext(), Constantes.ID_ADMOB);

        showBanners();

        interlocutor = null;

        networkInteractor = new NetworkInteractor(this);

        viewBackground = (ImageView) findViewById(R.id.background);
        loadBackground();

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

        btnAction = (ImageButton) findViewById(R.id.btnAction);
        btnAction.setColorFilter(Color.parseColor("#757575"));
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnActionSend) {
                    send(inputNewMessage.getText().toString().trim(), true);
                    inputNewMessage.setText("");
                } else {
                    selectImageToUpload();
                }
            }
        });

        btnLoadMore = (Button) findViewById(R.id.btnLoadMore);

        inputNewMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    btnActionSend = true;
                    btnAction.setImageResource(R.drawable.ic_send_black_24dp);
                } else {
                    btnActionSend = false;
                    btnAction.setImageResource(R.drawable.ic_camera_alt_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
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

    private void showBanners() {
        try {
            amazonAdView = (com.amazon.device.ads.AdLayout) findViewById(R.id.amazonAd);
            amazonAdView.setListener(this);
            amazonAdView.loadAd();

            mAdView = (AdView) findViewById(R.id.admobAd);
        }
        catch (Exception e) {
            Log.d("Error ", "en registrarAdsAmazon");
        }
    }

    //listener od ads amazon
    @Override
    public void onAdLoaded(Ad ad, AdProperties adProperties) {
        Log.d(TAG, "onAdLoadddddd");
    }

    @Override
    public void onAdFailedToLoad(Ad ad, AdError adError) {
        Log.e(TAG, "onAdFailedToloaddd " + adError.getMessage());
        amazonAdView.destroy();
        amazonAdView.setVisibility(View.GONE);

        mAdView.setVisibility(View.VISIBLE);
        mAdView.loadAd(Constantes.getAdRequest());
        mAdView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "adgoogle error " + errorCode);
                mAdView.setVisibility(View.GONE);
            }
        });
    }
    @Override
    public void onAdExpanded(Ad ad) {
    }
    @Override
    public void onAdCollapsed(Ad ad) {
    }
    @Override
    public void onAdDismissed(Ad ad) {
    }

    private void loadBackground() {
        SharedPreferences preferences = getSharedPreferences("background", MODE_PRIVATE);
        String typeBackground = preferences.getString(Constantes.TYPE_BACKGROUND, Constantes.TypeBackground.DEFAULT.name());

        if (typeBackground.equals(Constantes.TypeBackground.DEFAULT.name())) {
            viewBackground.setImageDrawable(getResources().getDrawable(R.drawable.background_hd));
        } else if (typeBackground.equals(Constantes.TypeBackground.IMAGE.name())) {
            Glide.with(this).load(preferences.getString(Constantes.BACKGROUND, "")).placeholder(R.drawable.background_hd).into(viewBackground);
        } else if (typeBackground.equals(Constantes.TypeBackground.COLOR.name())) {
            try {
                Drawable drawable = new ColorDrawable(Integer.parseInt(preferences.getString(Constantes.BACKGROUND, "")));
                viewBackground.setImageDrawable(drawable);
            } catch (Exception e) {
                Log.e(TAG, "catch in: else if(typeBackground.equals(Constantes.TypeBackground.COLOR.name())) { " + e.getMessage());
            }
        }
    }

    private void setBackground(String typeBackground, String contentBackground) {
        SharedPreferences preferences = getSharedPreferences("background", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constantes.TYPE_BACKGROUND, typeBackground);
        editor.putString(Constantes.BACKGROUND, contentBackground);
        editor.apply();
    }

    private void selectImageToUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.register_options_avatar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, TAKE_PHOTO);
                    }
                } else {
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
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                startActivityForResult(intent, PICK_BACKGROUND);
            }
        }
    }

    private File fileImg = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PHOTO || requestCode == TAKE_PHOTO) {
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
                send(fileImg.getAbsolutePath(), false);
            } else if (requestCode == PICK_BACKGROUND) {
                Uri selectedImage = data.getData();

                final String path = ImageSelectorUtils.getFilePathFromUri(this, selectedImage);

                File file = new File(path);
                setBackground(Constantes.TypeBackground.IMAGE.name(), file.getAbsolutePath());
                loadBackground();
            }
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
                try {
                    Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "catch onFailure startNewRandomChat");
                }
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
                for (int i = 0; i < messagesSaved.size(); i++) {

                    MessageDO message = messagesSaved.get(i);
                    Log.d(TAG, "messagesSaved list " + message.getContent());

                    if (message.is_sendCheck()) {

                        if (i + NUM_MESSAGES_SHOW >= messagesSaved.size()) {
                            addMessage(message, true);
                        } else {
                            addMessage(message, false);
                        }
                    } else {
                        addMessageSending(message);
                    }
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

                    for (int i = 0; i < listMessages.size(); i++) {

                        MessageDO message = listMessages.get(i);
                        Log.d(TAG, "onSucces listMessages " + message.getContent());
                        if (i + NUM_MESSAGES_SHOW >= listMessages.size()) {
                            addMessage(message, true);
                        } else {
                            addMessage(message, false);
                        }

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


    private int count = 0, indexLastMessage = -1;

    private void addMessage(final MessageDO messageDO, final boolean showNow) {
        final int myIndex = count;

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        int layout;
        if (messageDO.getBy().equals(preferencesProfile.getId())) {
            layout = R.layout.message_out;
        } else {
            layout = R.layout.message_in;
        }
        final ViewGroup message = (ViewGroup) layoutInflater.inflate(layout, null, false);

        if (showNow) {
            indexesMessagesShown.add(myIndex);
            showMessage(message, messageDO);
        } else {
            message.setVisibility(View.GONE);
        }
        message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!indexesMessagesShown.contains(0)) {
                    showAndHideBtnMore();
                }
                return false;
            }
        });
        observers.add(new Observer() {
            @Override
            public void update() {
                if (myIndex + 10 >= indexLastMessage) {
                    if (!indexesMessagesShown.contains(myIndex)) {
                        indexesMessagesShown.add(myIndex);
                        showMessage(message, messageDO);
                    }
                }
            }
        });

        count++;
        layoutMessages.addView(message);
    }

    List<Observer> observers = new ArrayList<>();

    private interface Observer {
        void update();
    }

    private void notifyObservers() {
        int numMin = indexesMessagesShown.get(indexesMessagesShown.size() - 1);
        for (int num : indexesMessagesShown) {
            numMin = Math.min(num, numMin);
        }
        indexLastMessage = numMin;
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void showAndHideBtnMore() {
        if (btnLoadMore.getVisibility() == View.INVISIBLE) {
            btnLoadMore.setVisibility(View.VISIBLE);
            btnLoadMore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.appear));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnLoadMore.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.disappear));
                    btnLoadMore.setVisibility(View.INVISIBLE);
                }
            }, 2500);
        }
    }

    public void loadMoreMessages(View v) {
        notifyObservers();
    }

    List<Integer> indexesMessagesShown = new ArrayList<>();

    private void showMessage(ViewGroup message, MessageDO messageDO) {
        final ProgressBar progressLoadImg = (ProgressBar) message.findViewById(R.id.progressImgLoad);

        TextView contentMessage = (TextView) message.findViewById(R.id.contentMessage);
        ImageView imgMessage = (ImageView) message.findViewById(R.id.imgMessage);

        if (messageDO.getType().equals(Constantes.TypeMessage.TEXT.name())) {
            contentMessage.setText(messageDO.getContent());
            imgMessage.setVisibility(View.GONE);
        } else if (messageDO.getType().equals(Constantes.TypeMessage.IMAGE.name())) {
            contentMessage.setVisibility(View.GONE);
            String url = Util.generateURL(s3, messageDO.getContent());
            progressLoadImg.setVisibility(View.VISIBLE);
            Picasso.with(ChatActivity.this).load(url).into(imgMessage, new Callback() {
                @Override
                public void onSuccess() {
                    progressLoadImg.setVisibility(View.GONE);
                    if(indexLastMessage == -1) {
                        downScrollView();
                    }
                }

                @Override
                public void onError() {

                }
            });
            //Glide.with(this).load(url).placeholder(R.drawable.loading_animation).into(imgMessage);
        }
        TextView date = (TextView) message.findViewById(R.id.date);
        date.setText(getDateFromMilli(messageDO.getDate()));
        if (messageDO.getBy().equals(preferencesProfile.getId())) {
            date.append("✓✓");
        }
        message.setVisibility(View.VISIBLE);
    }

    private static final String URL_SEND_MSG = "http://54.162.88.89/sendmsg.php";

    private void send(String content, boolean isText) {
        if (interlocutor != null && !content.isEmpty()) {
            String type;
            if (isText) {
                type = Constantes.TypeMessage.TEXT.name();
            } else {
                type = Constantes.TypeMessage.IMAGE.name();
            }

            Calendar cal = Calendar.getInstance();
            double date = (double) cal.getTimeInMillis();
            Log.e(TAG, "datenew " + date);


            MessageDO messageSending = new MessageDO();
            messageSending.setDate(date);
            messageSending.setBy(preferencesProfile.getId());
            messageSending.setTo(interlocutor.getUserId());
            messageSending.setType(type);
            messageSending.setContent(content);
            messageSending.set_sendCheck(false);

            List<MessageDO> listMessage = new ArrayList<>();
            listMessage.add(messageSending);
            dataBase.addMessages(listMessage);

            addMessageSending(messageSending);
        }
    }

    private void addMessageSending(final MessageDO messageDO) {
        Log.d(TAG, "addMessageSending");
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        int layout;
        if (messageDO.getBy().equals(preferencesProfile.getId())) {
            layout = R.layout.message_out;
        } else {
            layout = R.layout.message_in;
        }
        ViewGroup message = (ViewGroup) layoutInflater.inflate(layout, null, false);

        final ProgressBar progressBar = (ProgressBar) message.findViewById(R.id.progressImgUpload);

        TextView contentMessage = (TextView) message.findViewById(R.id.contentMessage);
        ImageView imgMessage = (ImageView) message.findViewById(R.id.imgMessage);

        final TextView date = (TextView) message.findViewById(R.id.date);
        date.setText(getDateFromMilli(messageDO.getDate()));

        if (messageDO.getType().equals(Constantes.TypeMessage.TEXT.name())) {
            contentMessage.setText(messageDO.getContent());
            imgMessage.setVisibility(View.GONE);
            sendToServer(messageDO, date);
        } else if (messageDO.getType().equals(Constantes.TypeMessage.IMAGE.name())) {
            contentMessage.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this).load(messageDO.getContent()).into(imgMessage);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    networkInteractor.uploadFile(new File(messageDO.getContent()), new NetworkInteractor.IUploadFile() {
                        @Override
                        public void onSuccess(final String filePath) {
                            Log.d(TAG, "iisenndSucces");
                            messageDO.setContent(filePath);
                            progressBar.setVisibility(View.GONE);
                            sendToServer(messageDO, date);
                        }

                        @Override
                        public void onProgress(String fileName, boolean isWaiting, long bytesCurrent, long bytesTotal) {
                            try {
                                long percentage = bytesCurrent * 100 / bytesTotal;
                                Log.d(TAG, bytesCurrent + " iisenbdonProgress " + percentage + "    " + bytesTotal);
                                progressBar.setProgress((int) percentage);
                            } catch (Exception e) {
                                Log.e(TAG, "catch onProgress " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(String fileName, Exception ex) {
                            Log.d(TAG, "iisendonError " + fileName);
                        }
                    });
                }
            }).start();
        }
        count++;
        layoutMessages.addView(message);
        downScrollView();
    }

    private void sendToServer(final MessageDO messageDO, final TextView textCheck) {
        networkInteractor.newMessageToId(interlocutor.getUserId(), messageDO.getContent(), messageDO.getType(), messageDO.getDate(), new NetworkInteractor.INewMessageToIdListener() {
            @Override
            public void onSendSucces() {
                Log.d(TAG, "onSendSucces");
                textCheck.append("✓");
                messageDO.set_sendCheck(true);
                dataBase.updateMessage(messageDO);

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
                        textCheck.append("✓");
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


    private String getDateFromMilli(double timeMilli) {
        Date date = new Date();
        date.setTime((long) timeMilli);

        return new SimpleDateFormat("d MMM h:mm a").format(date);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    int nR = 0, nG = 0, nB = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.menu_background) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(R.array.activity_chat_options_background, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            if (ContextCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                                return;
                            }
                            // We have permission, so show the image selector.
                            final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                            startActivityForResult(intent, PICK_BACKGROUND);
                            break;
                        case 1:
                            nR = 0;
                            nG = 0;
                            nB = 0;
                            LayoutInflater li = LayoutInflater.from(ChatActivity.this);
                            View v = li.inflate(R.layout.activity_chat_pick_color, null);

                            final View colorPick = v.findViewById(R.id.colorPick);
                            colorPick.setBackgroundColor(getHexColor());

                            SeekBar seekBarR = (SeekBar) v.findViewById(R.id.seekBarR);
                            seekBarR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    nR = i;
                                    colorPick.setBackgroundColor(getHexColor());
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });

                            SeekBar seekBarG = (SeekBar) v.findViewById(R.id.seekBarG);
                            seekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    nG = i;
                                    colorPick.setBackgroundColor(getHexColor());
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });


                            SeekBar seekBarB = (SeekBar) v.findViewById(R.id.seekBarB);
                            seekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    nB = i;
                                    colorPick.setBackgroundColor(getHexColor());
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });

                            AlertDialog.Builder dialogo = new AlertDialog.Builder(ChatActivity.this);
                            dialogo.setView(v)
                                    .setCancelable(false)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {
                                            setBackground(Constantes.TypeBackground.COLOR.name(), String.valueOf(getHexColor()));
                                            loadBackground();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            dialogo.show();
                            break;
                        case 2:
                            setBackground(Constantes.TypeBackground.DEFAULT.name(), "");
                            loadBackground();
                            break;
                    }
                }
            });
            builder.create();
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public int getHexColor() {
        return android.graphics.Color.rgb(nR, nG, nB);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        networkInteractor.stopFindInterlocutor();
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
}
