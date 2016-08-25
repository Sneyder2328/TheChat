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

@DynamoDBTable(tableName = "thechat-mobilehub-1444704093-User")

public class UserDO {
    private String _userId;
    private Double _ageMaxInterlocutor;
    private Double _ageMinInterlocutor;
    private Double _birthday;
    private String _chatsCurrent;
    private String _chatsReport;
    private Double _distanceMaxInterlocutor;
    private String _email;
    private String _gender;
    private String _genderInterlocutor;
    private String _language;
    private String _languageInterlocutor;
    private Double _latitude;
    private Double _longitude;
    private String _name;
    private String _photoUrl;
    private Double _points;
    private Double _reports;
    private String _status;
    private String _tokenId;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "ageMaxInterlocutor")
    public Double getAgeMaxInterlocutor() {
        return _ageMaxInterlocutor;
    }

    public void setAgeMaxInterlocutor(final Double _ageMaxInterlocutor) {
        this._ageMaxInterlocutor = _ageMaxInterlocutor;
    }
    @DynamoDBAttribute(attributeName = "ageMinInterlocutor")
    public Double getAgeMinInterlocutor() {
        return _ageMinInterlocutor;
    }

    public void setAgeMinInterlocutor(final Double _ageMinInterlocutor) {
        this._ageMinInterlocutor = _ageMinInterlocutor;
    }
    @DynamoDBAttribute(attributeName = "birthday")
    public Double getBirthday() {
        return _birthday;
    }

    public void setBirthday(final Double _birthday) {
        this._birthday = _birthday;
    }
    @DynamoDBAttribute(attributeName = "chatsCurrent")
    public String getChatsCurrent() {
        return _chatsCurrent;
    }

    public void setChatsCurrent(final String _chatsCurrent) {
        this._chatsCurrent = _chatsCurrent;
    }
    @DynamoDBAttribute(attributeName = "chatsReport")
    public String getChatsReport() {
        return _chatsReport;
    }

    public void setChatsReport(final String _chatsReport) {
        this._chatsReport = _chatsReport;
    }
    @DynamoDBAttribute(attributeName = "distanceMaxInterlocutor")
    public Double getDistanceMaxInterlocutor() {
        return _distanceMaxInterlocutor;
    }

    public void setDistanceMaxInterlocutor(final Double _distanceMaxInterlocutor) {
        this._distanceMaxInterlocutor = _distanceMaxInterlocutor;
    }
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return _email;
    }

    public void setEmail(final String _email) {
        this._email = _email;
    }
    @DynamoDBAttribute(attributeName = "gender")
    public String getGender() {
        return _gender;
    }

    public void setGender(final String _gender) {
        this._gender = _gender;
    }
    @DynamoDBAttribute(attributeName = "genderInterlocutor")
    public String getGenderInterlocutor() {
        return _genderInterlocutor;
    }

    public void setGenderInterlocutor(final String _genderInterlocutor) {
        this._genderInterlocutor = _genderInterlocutor;
    }
    @DynamoDBAttribute(attributeName = "language")
    public String getLanguage() {
        return _language;
    }

    public void setLanguage(final String _language) {
        this._language = _language;
    }
    @DynamoDBAttribute(attributeName = "languageInterlocutor")
    public String getLanguageInterlocutor() {
        return _languageInterlocutor;
    }

    public void setLanguageInterlocutor(final String _languageInterlocutor) {
        this._languageInterlocutor = _languageInterlocutor;
    }
    @DynamoDBAttribute(attributeName = "latitude")
    public Double getLatitude() {
        return _latitude;
    }

    public void setLatitude(final Double _latitude) {
        this._latitude = _latitude;
    }
    @DynamoDBAttribute(attributeName = "longitude")
    public Double getLongitude() {
        return _longitude;
    }

    public void setLongitude(final Double _longitude) {
        this._longitude = _longitude;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "photoUrl")
    public String getPhotoUrl() {
        return _photoUrl;
    }

    public void setPhotoUrl(final String _photoUrl) {
        this._photoUrl = _photoUrl;
    }
    @DynamoDBAttribute(attributeName = "points")
    public Double getPoints() {
        return _points;
    }

    public void setPoints(final Double _points) {
        this._points = _points;
    }
    @DynamoDBAttribute(attributeName = "reports")
    public Double getReports() {
        return _reports;
    }

    public void setReports(final Double _reports) {
        this._reports = _reports;
    }
    @DynamoDBIndexHashKey(attributeName = "status", globalSecondaryIndexName = "Status")
    public String getStatus() {
        return _status;
    }

    public void setStatus(final String _status) {
        this._status = _status;
    }
    @DynamoDBAttribute(attributeName = "tokenId")
    public String getTokenId() {
        return _tokenId;
    }

    public void setTokenId(final String _tokenId) {
        this._tokenId = _tokenId;
    }
}