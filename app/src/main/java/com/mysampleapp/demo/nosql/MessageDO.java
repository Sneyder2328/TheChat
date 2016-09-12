package com.mysampleapp.demo.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "thechat-mobilehub-1444704093-Message")

public class MessageDO {
    private Double _date;
    private String _by;
    private String _content;
    private String _to;
    private String _type;
    private boolean _sendCheck = true;


    public MessageDO(){

    }
    public MessageDO(Double _date, String _by, String _to, String _content, String _type) {
        this._date = _date;
        this._by = _by;
        this._content = _content;
        this._to = _to;
        this._type = _type;
    }
    public MessageDO(Double _date, String _by, String _to, String _content, String _type, boolean _sendCheck) {
        this._date = _date;
        this._by = _by;
        this._content = _content;
        this._to = _to;
        this._type = _type;
        this._sendCheck = _sendCheck;
    }

    @DynamoDBHashKey(attributeName = "date")
    @DynamoDBAttribute(attributeName = "date")
    public Double getDate() {
        return _date;
    }

    public void setDate(final Double _date) {
        this._date = _date;
    }
    @DynamoDBIndexHashKey(attributeName = "by", globalSecondaryIndexName = "ByTo")
    public String getBy() {
        return _by;
    }

    public void setBy(final String _by) {
        this._by = _by;
    }
    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return _content;
    }

    public void setContent(final String _content) {
        this._content = _content;
    }
    @DynamoDBIndexRangeKey(attributeName = "to", globalSecondaryIndexName = "ByTo")
    public String getTo() {
        return _to;
    }

    public void setTo(final String _to) {
        this._to = _to;
    }
    @DynamoDBAttribute(attributeName = "type")
    public String getType() {
        return _type;
    }

    public void setType(final String _type) {
        this._type = _type;
    }

    public boolean is_sendCheck() {
        return _sendCheck;
    }

    public void set_sendCheck(boolean _sendCheck) {
        this._sendCheck = _sendCheck;
    }

    @Override
    public String toString() {
        return "date " + getDate() + " by " + getBy() + " to " + getTo() + " content " + getContent() + " type " + getType();
    }
}
