package com.hsv.freeadblockerbrowser.networking;

import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FacebookInviteRequest extends MyAsyncTask<Boolean> {

    private final String fbAppId;
    private final String token;
    private final Iterable toIds;

    public FacebookInviteRequest(String fbAppId, String token, Iterable toIds) {
        super();

        this.fbAppId = fbAppId;
        this.token = token;
        this.toIds = toIds;
    }

    @Override
    protected Boolean doInBackground() {

        String url = "https://m.facebook.com/v2.2/dialog/apprequests?access_token=" + this.token
                + "&app_id=" + fbAppId
                + "&to=" + TextUtils.join("%2c", toIds)
                + "&type=user_agent"
                + "&title=SampleApp"
                + "&redirect_uri=fbconnect%3A%2F%2Fsuccess"
                + "&message=Join%20me%20on%20SampleApp!"
                + "&display=touch";
        Map<String, String> postParams = null;
        try {
            HttpURLConnection get = (HttpURLConnection) new URL(url).openConnection();
            get.setConnectTimeout(100000);
            get.setReadTimeout(100000);
            get.setRequestMethod("GET");
            int responseCode = get.getResponseCode();
            if (responseCode == 200) {
                InputStream is = get.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1024);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                String json = sb.toString();

                json = json.replaceAll("&", "&amp;");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(json));

                while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("input")) {
                        String name = parser.getAttributeValue(null, "name");
                        String value = parser.getAttributeValue(null, "value");
                        if (!(value == null || name == null)) {
                            if (postParams == null) {
                                postParams = new HashMap<>();
                            }
                            postParams.put(name, value);
                        }
                    }
                    parser.next();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        try {
            if (postParams != null) {
                StringBuilder params = new StringBuilder();
                Set<String> keys = postParams.keySet();
                for (String key : keys) {
                    String value = postParams.get(key);
                    String endcodedKey = URLEncoder.encode(key, "UTF-8");
                    String encodedValue = URLEncoder.encode(value, "UTF-8");
                    params.append(endcodedKey).append("=").append(encodedValue).append("&");
                }

                String charset = "UTF-8";
                HttpURLConnection post = (HttpURLConnection) new URL("http://m.facebook.com/v2.2/dialog/app_requests/submit").openConnection();
                post.setConnectTimeout(100000);
                post.setReadTimeout(100000);
                post.setRequestMethod("POST");
                post.setDoOutput(true);
                post.setDoInput(true);
                post.setRequestProperty("Accept-Charset", charset);
                post.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

                DataOutputStream dataout = new DataOutputStream(post.getOutputStream());
                dataout.writeBytes(params.toString());
                int responseCode = post.getResponseCode();
                if (responseCode == 200) {
                    InputStream is1 = post.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is1), 1024);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    reader.close();
                    String s1 = sb.toString();

                    if (!TextUtils.isEmpty(s1)) {
                        return s1.contains("fbconnect:\\/\\/success");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
