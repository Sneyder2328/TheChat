package com.mysampleapp.demo.nosql;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.Set;

public class DemoNoSQLUserResult implements DemoNoSQLResult {
    private static final int KEY_TEXT_COLOR = 0xFF333333;
    private final UserDO result;

    DemoNoSQLUserResult(final UserDO result) {
        this.result = result;
    }
    @Override
    public void updateItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final double originalValue = result.getBirthday();
        result.setBirthday(DemoSampleDataGenerator.getRandomSampleNumber());
        try {
            mapper.save(result);
        } catch (final AmazonClientException ex) {
            // Restore original data if save fails, and re-throw.
            result.setBirthday(originalValue);
            throw ex;
        }
    }

    @Override
    public void deleteItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        mapper.delete(result);
    }

    private void setKeyTextViewStyle(final TextView textView) {
        textView.setTextColor(KEY_TEXT_COLOR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(5), dp(2), dp(5), 0);
        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param dp number of design pixels.
     * @return number of pixels corresponding to the desired design pixels.
     */
    private int dp(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setValueTextViewStyle(final TextView textView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(15), 0, dp(15), dp(2));
        textView.setLayoutParams(layoutParams);
    }

    private void setKeyAndValueTextViewStyles(final TextView keyTextView, final TextView valueTextView) {
        setKeyTextViewStyle(keyTextView);
        setValueTextViewStyle(valueTextView);
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", bytes[0]));
        for(int index = 1; index < bytes.length; index++) {
            builder.append(String.format(" %02X", bytes[index]));
        }
        return builder.toString();
    }

    private static String byteSetsToHexStrings(Set<byte[]> bytesSet) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        for (byte[] bytes : bytesSet) {
            builder.append(String.format("%d: ", ++index));
            builder.append(bytesToHexString(bytes));
            if (index < bytesSet.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public View getView(final Context context, final View convertView, int position) {
        final LinearLayout layout;
        final TextView resultNumberTextView;
        final TextView userIdKeyTextView;
        final TextView userIdValueTextView;
        final TextView birthdayKeyTextView;
        final TextView birthdayValueTextView;
        final TextView chatsCurrentKeyTextView;
        final TextView chatsCurrentValueTextView;
        final TextView chatsReportKeyTextView;
        final TextView chatsReportValueTextView;
        final TextView emailKeyTextView;
        final TextView emailValueTextView;
        final TextView genderKeyTextView;
        final TextView genderValueTextView;
        final TextView languageKeyTextView;
        final TextView languageValueTextView;
        final TextView latitudeKeyTextView;
        final TextView latitudeValueTextView;
        final TextView longitudeKeyTextView;
        final TextView longitudeValueTextView;
        final TextView nameKeyTextView;
        final TextView nameValueTextView;
        final TextView photoUrlKeyTextView;
        final TextView photoUrlValueTextView;
        final TextView pointsKeyTextView;
        final TextView pointsValueTextView;
        final TextView reportsKeyTextView;
        final TextView reportsValueTextView;
        final TextView statusKeyTextView;
        final TextView statusValueTextView;
        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            resultNumberTextView = new TextView(context);
            resultNumberTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(resultNumberTextView);


            userIdKeyTextView = new TextView(context);
            userIdValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(userIdKeyTextView, userIdValueTextView);
            layout.addView(userIdKeyTextView);
            layout.addView(userIdValueTextView);

            birthdayKeyTextView = new TextView(context);
            birthdayValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(birthdayKeyTextView, birthdayValueTextView);
            layout.addView(birthdayKeyTextView);
            layout.addView(birthdayValueTextView);

            chatsCurrentKeyTextView = new TextView(context);
            chatsCurrentValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(chatsCurrentKeyTextView, chatsCurrentValueTextView);
            layout.addView(chatsCurrentKeyTextView);
            layout.addView(chatsCurrentValueTextView);

            chatsReportKeyTextView = new TextView(context);
            chatsReportValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(chatsReportKeyTextView, chatsReportValueTextView);
            layout.addView(chatsReportKeyTextView);
            layout.addView(chatsReportValueTextView);

            emailKeyTextView = new TextView(context);
            emailValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(emailKeyTextView, emailValueTextView);
            layout.addView(emailKeyTextView);
            layout.addView(emailValueTextView);

            genderKeyTextView = new TextView(context);
            genderValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(genderKeyTextView, genderValueTextView);
            layout.addView(genderKeyTextView);
            layout.addView(genderValueTextView);

            languageKeyTextView = new TextView(context);
            languageValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(languageKeyTextView, languageValueTextView);
            layout.addView(languageKeyTextView);
            layout.addView(languageValueTextView);

            latitudeKeyTextView = new TextView(context);
            latitudeValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(latitudeKeyTextView, latitudeValueTextView);
            layout.addView(latitudeKeyTextView);
            layout.addView(latitudeValueTextView);

            longitudeKeyTextView = new TextView(context);
            longitudeValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(longitudeKeyTextView, longitudeValueTextView);
            layout.addView(longitudeKeyTextView);
            layout.addView(longitudeValueTextView);

            nameKeyTextView = new TextView(context);
            nameValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(nameKeyTextView, nameValueTextView);
            layout.addView(nameKeyTextView);
            layout.addView(nameValueTextView);

            photoUrlKeyTextView = new TextView(context);
            photoUrlValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(photoUrlKeyTextView, photoUrlValueTextView);
            layout.addView(photoUrlKeyTextView);
            layout.addView(photoUrlValueTextView);

            pointsKeyTextView = new TextView(context);
            pointsValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(pointsKeyTextView, pointsValueTextView);
            layout.addView(pointsKeyTextView);
            layout.addView(pointsValueTextView);

            reportsKeyTextView = new TextView(context);
            reportsValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(reportsKeyTextView, reportsValueTextView);
            layout.addView(reportsKeyTextView);
            layout.addView(reportsValueTextView);

            statusKeyTextView = new TextView(context);
            statusValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(statusKeyTextView, statusValueTextView);
            layout.addView(statusKeyTextView);
            layout.addView(statusValueTextView);
        } else {
            layout = (LinearLayout) convertView;
            resultNumberTextView = (TextView) layout.getChildAt(0);

            userIdKeyTextView = (TextView) layout.getChildAt(1);
            userIdValueTextView = (TextView) layout.getChildAt(2);

            birthdayKeyTextView = (TextView) layout.getChildAt(3);
            birthdayValueTextView = (TextView) layout.getChildAt(4);

            chatsCurrentKeyTextView = (TextView) layout.getChildAt(5);
            chatsCurrentValueTextView = (TextView) layout.getChildAt(6);

            chatsReportKeyTextView = (TextView) layout.getChildAt(7);
            chatsReportValueTextView = (TextView) layout.getChildAt(8);

            emailKeyTextView = (TextView) layout.getChildAt(9);
            emailValueTextView = (TextView) layout.getChildAt(10);

            genderKeyTextView = (TextView) layout.getChildAt(11);
            genderValueTextView = (TextView) layout.getChildAt(12);

            languageKeyTextView = (TextView) layout.getChildAt(13);
            languageValueTextView = (TextView) layout.getChildAt(14);

            latitudeKeyTextView = (TextView) layout.getChildAt(15);
            latitudeValueTextView = (TextView) layout.getChildAt(16);

            longitudeKeyTextView = (TextView) layout.getChildAt(17);
            longitudeValueTextView = (TextView) layout.getChildAt(18);

            nameKeyTextView = (TextView) layout.getChildAt(19);
            nameValueTextView = (TextView) layout.getChildAt(20);

            photoUrlKeyTextView = (TextView) layout.getChildAt(21);
            photoUrlValueTextView = (TextView) layout.getChildAt(22);

            pointsKeyTextView = (TextView) layout.getChildAt(23);
            pointsValueTextView = (TextView) layout.getChildAt(24);

            reportsKeyTextView = (TextView) layout.getChildAt(25);
            reportsValueTextView = (TextView) layout.getChildAt(26);

            statusKeyTextView = (TextView) layout.getChildAt(27);
            statusValueTextView = (TextView) layout.getChildAt(28);
        }

        resultNumberTextView.setText(String.format("#%d", + position+1));
        userIdKeyTextView.setText("userId");
        userIdValueTextView.setText(result.getUserId());
        birthdayKeyTextView.setText("birthday");
        birthdayValueTextView.setText("" + result.getBirthday().longValue());
        chatsCurrentKeyTextView.setText("chatsCurrent");
        chatsCurrentValueTextView.setText(result.getChatsCurrent().toString());
        chatsReportKeyTextView.setText("chatsReport");
        chatsReportValueTextView.setText(result.getChatsReport().toString());
        emailKeyTextView.setText("email");
        emailValueTextView.setText(result.getEmail());
        genderKeyTextView.setText("gender");
        genderValueTextView.setText(result.getGender());
        languageKeyTextView.setText("language");
        languageValueTextView.setText(result.getLanguage());
        latitudeKeyTextView.setText("latitude");
        latitudeValueTextView.setText("" + result.getLatitude().longValue());
        longitudeKeyTextView.setText("longitude");
        longitudeValueTextView.setText("" + result.getLongitude().longValue());
        nameKeyTextView.setText("name");
        nameValueTextView.setText(result.getName());
        photoUrlKeyTextView.setText("photoUrl");
        photoUrlValueTextView.setText(result.getPhotoUrl());
        pointsKeyTextView.setText("points");
        pointsValueTextView.setText("" + result.getPoints().longValue());
        reportsKeyTextView.setText("reports");
        reportsValueTextView.setText("" + result.getReports().longValue());
        statusKeyTextView.setText("status");
        statusValueTextView.setText(result.getStatus());
        return layout;
    }
}
