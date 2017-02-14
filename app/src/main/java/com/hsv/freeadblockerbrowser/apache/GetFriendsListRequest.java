package com.hsv.freeadblockerbrowser.apache;

import com.facebook.appevents.AppEventsConstants;
import com.hsv.freeadblockerbrowser.BuildConfig;
import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;
import com.hsv.freeadblockerbrowser.networking.MyAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFriendsListRequest extends MyAsyncTask<List<FacebookInvitableFriend>> {

    private final String fbAppId;
    private final String token;

    public GetFriendsListRequest(String fbAppId, String token) {
        this.fbAppId = fbAppId;
        this.token = token;
    }

    private static List<FacebookInvitableFriend> parseFirstDegreeIds(String html) {
        List<FacebookInvitableFriend> friends = new ArrayList<>();
        JSONArray payload = null;
        try {
            String[] spString = html.split("for \\(;;\\);");
            if (spString.length > 1) {
                JSONObject obj = new JSONObject(spString[1]);
                payload = obj.getJSONArray("payload");

            }
        } catch (Throwable e) {
            return null;
        }

        if (payload == null) {
            return null;
        }
        for (int i = 0; i < payload.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = payload.getJSONObject(i);
                String uid = jsonObject.optString("uid");
                String text = jsonObject.optString("text");
                String photo = jsonObject.optString("photo");
                FacebookInvitableFriend friend = new FacebookInvitableFriend(uid, text, photo);
                friends.add(friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return friends;
    }

    @Override
    protected List<FacebookInvitableFriend> doInBackground() {
        String friendIDHTMLString = getFriendIDHTMLString(token);
        return parseFirstDegreeIds(friendIDHTMLString);
    }

    public String getFriendIDHTMLString(String token) {
        try {
            String url = "https://m.facebook.com/v2.2/dialog/apprequests?access_token=" + token
                    + "&app_id=" + fbAppId
                    + "&sdk=android-4.5.0"
                    + "&redirect_uri=fbconnect%3A%2F%2Fsuccess"
                    + "&message=Join%20me!"
                    + "&display=touch";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Requested-With", BuildConfig.APPLICATION_ID);
            connection.addRequestProperty("Upgrade-Insecure-Requests", AppEventsConstants.EVENT_PARAM_VALUE_YES);
            connection.addRequestProperty("User-Agent", System.getProperty("http.agent"));
            connection.addRequestProperty("Accept-Language", "en-US");
            connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1024);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                String html = sb.toString();

                Matcher m = Pattern.compile("first_degree.php?[^\"]*").matcher(html);
                if (m.find()) {
                    return getFirstDegreeHTML(m.group(0));
                }
            }
        } catch (Throwable e) {
        }
        return null;
    }


    private String getFirstDegreeHTML(String url) {
        try {
            url = ("https://m.facebook.com/ds/" + url).replaceAll("\\\\u0025", "%");

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.addRequestProperty("User-Agent", "http.agent");
            connection.addRequestProperty("Accept-Language", "en-US");
            connection.addRequestProperty("Accept", "*/*");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1024);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                return sb.toString();
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }
}
