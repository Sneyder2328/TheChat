package com.twismart.thechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mysampleapp.demo.nosql.MessageDO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sneyd on 9/4/2016.
 **/
public class LocalDataBase extends SQLiteOpenHelper {

    public static final String TAG = "LocalDataBase", DATABASE_NAME = "TheChatDB";

    public LocalDataBase(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + Message.TABLE_NAME + "(" + Message.COLUMN_NAME_DATE + " INTEGER, " + Message.COLUMN_NAME_BY + " TEXT, " + Message.COLUMN_NAME_TO + " TEXT, " + Message.COLUMN_NAME_CONTENT + " TEXT, " + Message.COLUMN_NAME_TYPE + " TEXT)");
    }

    public void addMessages(List<MessageDO> listMessages){
        SQLiteDatabase database = getWritableDatabase();
        for(MessageDO messageDO : listMessages){
            ContentValues contentValues = new ContentValues();
            contentValues.put(Message.COLUMN_NAME_DATE, (long)(double)messageDO.getDate());
            contentValues.put(Message.COLUMN_NAME_BY, messageDO.getBy());
            contentValues.put(Message.COLUMN_NAME_TO, messageDO.getTo());
            contentValues.put(Message.COLUMN_NAME_CONTENT, messageDO.getContent());
            contentValues.put(Message.COLUMN_NAME_TYPE, messageDO.getType());
            database.insert(Message.TABLE_NAME, null, contentValues);
        }
        database.close();
        Log.d(TAG, "added to SQLite " + listMessages.toString());
    }

    public List<MessageDO> getMessagesWithUser(String myId, String interlocutorId){
        List<MessageDO> listMessages = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();

        Cursor messagesReceived = database.rawQuery("SELECT  * FROM " + Message.TABLE_NAME + " WHERE " + Message.COLUMN_NAME_TO + " = '" + myId + "' AND " + Message.COLUMN_NAME_BY + " = '" + interlocutorId + "'", null);
        while (messagesReceived.moveToNext()){
            Log.d(TAG, "date "  + messagesReceived.getLong(0) + "  by " + messagesReceived.getString(1) + "  to "  +messagesReceived.getString(2) + "  content " + messagesReceived.getString(3) + "  type " + messagesReceived.getString(4));
            listMessages.add(new MessageDO((double)messagesReceived.getLong(0), messagesReceived.getString(1), messagesReceived.getString(2), messagesReceived.getString(3), messagesReceived.getString(4)));
        }
        Log.d(TAG, "received " + messagesReceived.getCount());
        messagesReceived.close();

        Cursor messagesSent = database.rawQuery("SELECT  * FROM " + Message.TABLE_NAME + " WHERE " + Message.COLUMN_NAME_TO + " = '" + interlocutorId + "'", null);
        while (messagesSent.moveToNext()){
            Log.d(TAG, "date "  + messagesSent.getLong(0) + "  by " +messagesSent.getString(1) + "  to "  +messagesSent.getString(2) + "  content " + messagesSent.getString(3) + "  type " + messagesSent.getString(4));
            listMessages.add(new MessageDO((double)messagesSent.getLong(0), messagesSent.getString(1), messagesSent.getString(2), messagesSent.getString(3), messagesSent.getString(4)));
        }
        Log.d(TAG, "sent " + messagesSent.getCount());
        messagesSent.close();

        database.close();

        if(!listMessages.isEmpty()){
            Collections.sort(listMessages, new Comparator<MessageDO>() {
                @Override
                public int compare(MessageDO m1, MessageDO m2) {
                    return m1.getDate().compareTo(m2.getDate());
                }
            });
        }
        return listMessages;
    }

    public void clear(){
        SQLiteDatabase database = getWritableDatabase();
        Log.d(TAG, "delete MessageTable Result: " + database.delete(Message.TABLE_NAME, null, null));
        database.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
class Message {
    public static final String TABLE_NAME = "messages", COLUMN_NAME_DATE = "date", COLUMN_NAME_BY = "byUser", COLUMN_NAME_TO = "toUser", COLUMN_NAME_CONTENT = "content", COLUMN_NAME_TYPE = "type";
}
class ChatsCurrent {//lo hago despues
    public static final String TABLE_NAME = "chatssCurrent", COLUMN_NAME_TOKEN = "tokenId", COLUMN_NAME_NAME = "name", COLUMN_NAME_ID = "id", COLUMN_NAME_STATUS = "status", COLUMN_NAME_LAT = "lat", COLUMN_NAME_LONG = "long";
}