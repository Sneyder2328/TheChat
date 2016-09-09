package com.twismart.thechat;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mysampleapp.demo.nosql.MessageDO;
import com.mysampleapp.demo.nosql.UserDO;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sneyd on 8/17/2016.
 **/
public class NetworkInteractor {
    private static final String TAG = "NetworkInteractor";
    public DynamoDBMapper mapper;
    private Activity activity;
    private PreferencesProfile preferencesProfile;
    private PreferencesFind preferencesFind;

    public NetworkInteractor(Activity activity) {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        this.activity = activity;
        preferencesProfile = new PreferencesProfile(activity);
        preferencesFind = new PreferencesFind(activity);
    }

    public NetworkInteractor(Context context) {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        preferencesProfile = new PreferencesProfile(context);
        preferencesFind = new PreferencesFind(context);
    }

    public void writeProfile(final boolean isNewUser, final IWriteProfileListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserDO userDO;
                    if (isNewUser) {
                        //create a new user with all the data
                        userDO = new UserDO();
                        userDO.setUserId(preferencesProfile.getId());
                        userDO.setName(preferencesProfile.getName());
                        userDO.setEmail(preferencesProfile.getEmail());
                        userDO.setStatus(Constantes.Status.ONLINE.name());
                        userDO.setPhotoUrl(preferencesProfile.getPhotoUrl());
                        userDO.setGender(preferencesProfile.getGender());
                        userDO.setBirthday(preferencesProfile.getBirthday());
                        userDO.setPoints(10d);//por defecto todos los usuarios tendran 10 points
                        userDO.setLanguage(preferencesProfile.getLanguage());
                    } else {
                        //uptade only the data visible of profile
                        userDO = mapper.load(UserDO.class, preferencesProfile.getId());
                        userDO.setName(preferencesProfile.getName());
                        userDO.setStatus(Constantes.Status.ONLINE.name());
                        userDO.setPhotoUrl(preferencesProfile.getPhotoUrl());
                        userDO.setGender(preferencesProfile.getGender());
                        userDO.setBirthday(preferencesProfile.getBirthday());
                        userDO.setLanguage(preferencesProfile.getLanguage());
                    }
                    String token = FirebaseInstanceId.getInstance().getToken();
                    Log.d(TAG, "token write: " + token);
                    userDO.setTokenId(token);

                    final UserDO newUser = userDO;

                    mapper.save(newUser);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces();
                        }
                    });
                } catch (final AmazonClientException ex) {
                    Log.e(TAG, "catch in writeProfile " + ex.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(ex.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IWriteProfileListener {
        void onSucces();

        void onFailure(String error);
    }


    private boolean chatFound;
    private static int TIME_WAIT = 3000;//3 second of time wait

    public void findInterlocutor(final IFindInterLocutorListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userSetStatus = mapper.load(UserDO.class, preferencesProfile.getId());
                    userSetStatus.setStatus(Constantes.Status.SEARCHING.name());
                    userSetStatus.setGenderInterlocutor(preferencesFind.getGender());
                    userSetStatus.setAgeMaxInterlocutor((double) preferencesFind.getAgeMax());
                    userSetStatus.setAgeMinInterlocutor((double) preferencesFind.getAgeMin());
                    userSetStatus.setDistanceMaxInterlocutor((double) preferencesFind.getDistanceMax());
                    if (preferencesFind.getInMyLanguage()) {
                        userSetStatus.setLanguageInterlocutor(preferencesProfile.getLanguage());
                    } else {
                        userSetStatus.setLanguageInterlocutor(Arrays.toString(activity.getResources().getStringArray(R.array.register_list_languages)));
                    }
                    mapper.save(userSetStatus);

                    chatFound = false;
                    while (!chatFound) {
                        Thread.sleep(TIME_WAIT);
                        Log.d(TAG, "luego de Thread.sleep(TIME_WAIT);");
                        if (chatFound) {
                            return;
                        }
                        final UserDO userGetStatus = mapper.load(UserDO.class, preferencesProfile.getId());
                        String status = userGetStatus.getStatus();

                        if (status.equals(Constantes.Status.FOUND.name())) {
                            //try start  chat
                            List<String> myChatsCurrent = new ArrayList<>(Arrays.asList(userGetStatus.getChatsCurrent().substring(1, userGetStatus.getChatsCurrent().length() - 1).split("\\s*,\\s*")));
                            if (myChatsCurrent.size() > 0) {
                                Log.d(TAG, "luego de if(myChatsCurrent.size() > 0){");
                                final String idInterlocutor = myChatsCurrent.get(0);
                                Log.d(TAG, "luego de " + idInterlocutor);

                                final UserDO myInterlocutor = mapper.load(UserDO.class, idInterlocutor);

                                chatFound = true;
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onSucces(myInterlocutor);
                                    }
                                });
                            }
                        }
                        if (status.equals(Constantes.Status.SEARCHING.name())) {
                            //search interlocutor and start chat
                            UserDO userToFind = new UserDO();
                            userToFind.setStatus(Constantes.Status.SEARCHING.name());

                            final Condition conditionGender = new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                                    .withAttributeValueList(new AttributeValue().withS(preferencesFind.getGender()));

                            final Condition conditionMyGender = new Condition()
                                    .withComparisonOperator(ComparisonOperator.CONTAINS.toString())
                                    .withAttributeValueList(new AttributeValue().withS(userGetStatus.getGender()));

                            final Condition conditionInMyLanguage = new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                                    .withAttributeValueList(new AttributeValue().withS(preferencesProfile.getLanguage()));

                            final Condition conditionInHisLanguage = new Condition()
                                    .withComparisonOperator(ComparisonOperator.CONTAINS.toString())
                                    .withAttributeValueList(new AttributeValue().withS(preferencesProfile.getLanguage()));

                            final Condition conditionAge = new Condition()
                                    .withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                                    .withAttributeValueList(new AttributeValue[]{new AttributeValue().withN(String.valueOf(preferencesFind.getAgeMax())), new AttributeValue().withN(String.valueOf(preferencesFind.getAgeMin()))});

                            final Condition conditionHisAgeMax = new Condition()
                                    .withComparisonOperator(ComparisonOperator.LE.toString())
                                    .withAttributeValueList(new AttributeValue().withN(String.valueOf(userGetStatus.getBirthday())));

                            final Condition conditionHisAgeMin = new Condition()
                                    .withComparisonOperator(ComparisonOperator.GE.toString())
                                    .withAttributeValueList(new AttributeValue().withN(String.valueOf(userGetStatus.getBirthday())));

                            final DynamoDBQueryExpression<UserDO> queryExpression = new DynamoDBQueryExpression<>();
                            queryExpression.withHashKeyValues(userToFind);
                            queryExpression.withConsistentRead(false);

                            if (!preferencesFind.getGender().contains(Constantes.SEPARATOR)) {
                                queryExpression.withQueryFilterEntry("gender", conditionGender);
                            }
                            queryExpression.withQueryFilterEntry("genderInterlocutor", conditionMyGender);

                            if (preferencesFind.getInMyLanguage()) {
                                queryExpression.withQueryFilterEntry("language", conditionInMyLanguage);
                            }
                            queryExpression.withQueryFilterEntry("languageInterlocutor", conditionInHisLanguage);

                            queryExpression.withQueryFilterEntry("birthday", conditionAge);

                            queryExpression.withQueryFilterEntry("ageMaxInterlocutor", conditionHisAgeMax);
                            queryExpression.withQueryFilterEntry("ageMinInterlocutor", conditionHisAgeMin);

                            final PaginatedQueryList<UserDO> results = mapper.query(UserDO.class, queryExpression);

                            final UserDO userFound;

                            if (results.size() != 0) {
                                Log.d(TAG, "results ");
                                UserDO user = null;
                                for (UserDO userDo : results) {
                                    if (!userDo.getUserId().equals(preferencesProfile.getId()) && Util.distanceBetweenUsers(userGetStatus, userDo) <= preferencesFind.getDistanceMax() && Util.distanceBetweenUsers(userGetStatus, userDo) <= userDo.getDistanceMaxInterlocutor()) {
                                        user = userDo;
                                    }
                                }
                                userFound = user;

                                if (userFound != null) {
                                    List<String> userFoundChatsCurrent;
                                    if (userFound.getChatsCurrent() != null) {
                                        userFoundChatsCurrent = new ArrayList<>(Arrays.asList(userFound.getChatsCurrent().substring(1, userFound.getChatsCurrent().length() - 1).split("\\s*,\\s*")));
                                        Log.d("lista encontrada", userFoundChatsCurrent.toString());
                                        userFoundChatsCurrent.remove(userGetStatus.getUserId());
                                        userFoundChatsCurrent.add(0, userGetStatus.getUserId());
                                        Log.d("lista encontrada2", userFoundChatsCurrent.toString());
                                    } else {
                                        userFoundChatsCurrent = new ArrayList<>();
                                        userFoundChatsCurrent.add(userGetStatus.getUserId());
                                        Log.d("lista hecha", userFoundChatsCurrent.toString());
                                    }
                                    userFound.setChatsCurrent(userFoundChatsCurrent.toString());
                                    userFound.setStatus(Constantes.Status.FOUND.name());
                                    mapper.save(userFound);

                                    List<String> myChatsCurrent;
                                    if (userGetStatus.getChatsCurrent() != null) {
                                        myChatsCurrent = new ArrayList<>(Arrays.asList(userGetStatus.getChatsCurrent().substring(1, userGetStatus.getChatsCurrent().length() - 1).split("\\s*,\\s*")));
                                        Log.d("milista encontrada", myChatsCurrent.toString());
                                        myChatsCurrent.remove(userFound.getUserId());
                                        myChatsCurrent.add(0, userFound.getUserId());
                                        Log.d("milista encontrada2", myChatsCurrent.toString());
                                    } else {
                                        myChatsCurrent = new ArrayList<>();
                                        myChatsCurrent.add(userFound.getUserId());
                                    }
                                    userGetStatus.setChatsCurrent(myChatsCurrent.toString());
                                    userGetStatus.setStatus(Constantes.Status.FOUND.name());
                                    mapper.save(userGetStatus);


                                    chatFound = true;
                                }
                            } else {
                                userFound = null;
                            }

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userFound != null) {
                                        listener.onSucces(userFound);
                                    }
                                }
                            });
                        }
                    }

                } catch (final Exception e) {
                    Log.e(TAG, "catch in findInterlocutor " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IFindInterLocutorListener {
        void onSucces(UserDO interlocutor);

        void onFailure(String error);
    }

    public void stopFindInterlocutor() {
        chatFound = true;
    }


    public void writeLocationInPerfil(final double latitude, final double longitude) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, preferencesProfile.getId());
                    userDO.setLatitude(latitude);
                    userDO.setLongitude(longitude);
                    if (userDO.getStatus().equals(Constantes.Status.OFFLINE.name())) {
                        userDO.setStatus(Constantes.Status.ONLINE.name());
                    }
                    mapper.save(userDO);
                } catch (Exception e) {
                    Log.d(TAG, "catch in writeLocationInPerfil " + e.getMessage());
                }
            }
        }).start();
    }

    public void writeStatus(final String status) {
        final String userId = preferencesProfile.getId();//adelantarme al preferences.clear
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, userId);
                    userDO.setStatus(status);
                    mapper.save(userDO);
                    Log.d(TAG, " writeStatus " + status);
                } catch (final Exception e) {
                    Log.e(TAG, "catch in writeStatus " + e.getMessage());
                }
            }
        }).start();
    }


    public void writePhotoUrl(final String photoUrl) {
        final String userId = preferencesProfile.getId();//adelantarme al preferences.clear
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, userId);
                    userDO.setPhotoUrl(photoUrl);
                    mapper.save(userDO);
                    Log.d(TAG, " writePhotoUrl " + photoUrl);
                } catch (final Exception e) {
                    Log.e(TAG, "catch in writeStatus " + e.getMessage());
                }
            }
        }).start();
    }

    public void writeTokenIdFirebase(final String tokenId, final IWriteTokenIdFirebase listener) {
        final String userId = preferencesProfile.getId();//adelantarme al preferences.clear
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, userId);
                    userDO.setTokenId(tokenId);
                    mapper.save(userDO);
                    if (listener != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSucces();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "catch in writeTokenIdFirebase " + e.getMessage());
                    if (listener != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure(e.getMessage());
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public interface IWriteTokenIdFirebase {
        void onSucces();

        void onFailure(String error);
    }


    public void newMessageToId(final String idInterlocutor, final String messageToSend, final String type, final INewMessageToIdListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessageDO newMessage = new MessageDO();
                newMessage.setBy(preferencesProfile.getId());
                newMessage.setTo(idInterlocutor);
                newMessage.setContent(messageToSend);
                newMessage.setType(type);

                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                double date = (double) cal.getTimeInMillis();
                newMessage.setDate(date);

                try {
                    mapper.save(newMessage);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSendSucces();
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSendFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface INewMessageToIdListener {
        void onSendSucces();

        void onSendFailure(String error);
    }


    public void getMessagesWithUser(final String idInterlocutor, final double minDate, final IGetMessagesWithUser listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageDO myMessagesToFind = new MessageDO();
                    myMessagesToFind.setBy(preferencesProfile.getId());

                    final Condition conditionDate = new Condition()
                            .withComparisonOperator(ComparisonOperator.GT.toString())
                            .withAttributeValueList(new AttributeValue().withN(String.valueOf(minDate)));

                    DynamoDBQueryExpression<MessageDO> myQueryExpression = new DynamoDBQueryExpression<MessageDO>()
                            .withHashKeyValues(myMessagesToFind)
                            .withRangeKeyCondition("to", new Condition().withComparisonOperator(ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(idInterlocutor)))
                            .withQueryFilterEntry("date", conditionDate)
                            .withConsistentRead(false);

                    PaginatedQueryList<MessageDO> listMyMessages = mapper.query(MessageDO.class, myQueryExpression);


                    MessageDO hisMessagesToFind = new MessageDO();
                    hisMessagesToFind.setBy(idInterlocutor);

                    DynamoDBQueryExpression<MessageDO> hisQueryExpression = new DynamoDBQueryExpression<MessageDO>()
                            .withHashKeyValues(hisMessagesToFind)
                            .withRangeKeyCondition("to", new Condition().withComparisonOperator(ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(preferencesProfile.getId())))
                            .withQueryFilterEntry("date", conditionDate)
                            .withConsistentRead(false);

                    PaginatedQueryList<MessageDO> listHisMessages = mapper.query(MessageDO.class, hisQueryExpression);

                    final List<MessageDO> listMessages = new ArrayList<>();

                    for (MessageDO myMessage : listMyMessages) {
                        listMessages.add(myMessage);
                    }

                    for (MessageDO hisMessage : listHisMessages) {
                        listMessages.add(hisMessage);
                    }

                    if (!listMessages.isEmpty()) {
                        Collections.sort(listMessages, new Comparator<MessageDO>() {
                            @Override
                            public int compare(MessageDO m1, MessageDO m2) {
                                return m1.getDate().compareTo(m2.getDate());
                            }
                        });
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces(listMessages);
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IGetMessagesWithUser {
        void onSucces(List<MessageDO> listMessages);

        void onFailure(String error);
    }

    public void getUserById(final String userId, final IGetUserById listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, userId);
                    if (userDO != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSucces(userDO);
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure("");
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.d(TAG, "catch in existUserById " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IGetUserById {
        void onSucces(UserDO userDO);

        void onFailure(String error);
    }

    public void getUsersByIds(final List<String> listIds, final IGetUsersByIds listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<UserDO> listUsers = new ArrayList<>();
                    for (String userId : listIds) {
                        UserDO user = mapper.load(UserDO.class, userId);
                        if (user != null) {
                            listUsers.add(user);
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces(listUsers);
                        }
                    });
                } catch (final Exception e) {
                    Log.d(TAG, "catch in existUserById " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IGetUsersByIds {
        void onSucces(List<UserDO> listUsers);

        void onFailure(String error);
    }

    private UserFileManager userFileManagerClient;

    public void createUserFileManager() {
        AWSMobileClient.defaultMobileClient().createUserFileManager(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/", AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET_REGION,
                new UserFileManager.BuilderResultHandler() {
                    @Override
                    public void onComplete(final UserFileManager userFileManager) {
                        userFileManagerClient = userFileManager;
                    }
                });
    }

    public void uploadFile(File file, final IUploadFile listener) {
        Log.d(TAG, "uploadFile = " + file.getPath());

        try {
            userFileManagerClient.uploadContent(file, file.getName(), new ContentProgressListener() {

                @Override
                public void onSuccess(final ContentItem contentItem) {
                    Log.d(TAG, "uploadContent onSuccess ");

                    final AmazonS3 s3 = new AmazonS3Client(AWSMobileClient.defaultMobileClient().getIdentityManager().getCredentialsProvider());
                    final URL presignedUrl = s3.generatePresignedUrl(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/" + contentItem.getFilePath(), new Date(new Date().getTime() * 2));
                    listener.onSuccess(presignedUrl.toString());
                }

                @Override
                public void onProgressUpdate(final String fileName, final boolean isWaiting, final long bytesCurrent, final long bytesTotal) {
                    Log.d(TAG, "uploadContent onProgressUpdate");
                    listener.onProgress(fileName, isWaiting, bytesCurrent, bytesTotal);
                }

                @Override
                public void onError(final String fileName, final Exception ex) {
                    Log.d(TAG, "uploadContent onError");
                    listener.onError(fileName, ex);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "catch uploadFile " + e.getMessage());
        }
    }

    public interface IUploadFile {
        void onSuccess(String url);
        void onProgress(String fileName, boolean isWaiting, long bytesCurrent, long bytesTotal);
        void onError(String fileName, Exception ex);
    }
}