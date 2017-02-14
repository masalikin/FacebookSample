package com.hsv.freeadblockerbrowser.apache;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.facebook.appevents.AppEventsConstants;
import com.hsv.freeadblockerbrowser.BuildConfig;
import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFriends {

    private final Context appContext;
    private final String fbAppId;

    public GetFriends(Context appContext, String fbAppId) {
        this.appContext = appContext;
        this.fbAppId = fbAppId;
    }

    public static List<FacebookInvitableFriend> parseFirstDegreeIds(String html) {
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

    public String getFriendIDHTMLString(String token) {
        try {
            String[] cookieValues;
            String[] split;
            BasicClientCookie cookie;
            String url = "https://m.facebook.com/v2.2/dialog/apprequests?access_token=" + token + "&app_id=" + fbAppId + "&sdk=android-4.5.0&redirect_uri=fbconnect%3A%2F%2Fsuccess&message=Join%20me%20on%20Kiwi!&display=touch";
            DefaultHttpClient httpclient = new DefaultHttpClient();
            BasicCookieStore store = new BasicCookieStore();
            CookieSyncManager.createInstance(appContext);
            httpclient.addRequestInterceptor(new HttpRequestInterceptor() {

                public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                    httpRequest.addHeader(new C10101());
                    httpRequest.addHeader(new C10112());
                    httpRequest.addHeader(new C10123());
                    httpRequest.addHeader(new C10134());
                    httpRequest.addHeader(new C10145());
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.14.1 */
                class C10101 implements Header {
                    C10101() {
                    }

                    public String getName() {
                        return "X-Requested-With";
                    }

                    public String getValue() {
                        return BuildConfig.APPLICATION_ID;
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.14.2 */
                class C10112 implements Header {
                    C10112() {
                    }

                    public String getName() {
                        return "Upgrade-Insecure-Requests";
                    }

                    public String getValue() {
                        return AppEventsConstants.EVENT_PARAM_VALUE_YES;
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.14.3 */
                class C10123 implements Header {
                    C10123() {
                    }

                    public String getName() {
                        return "User-Agent";
                    }

                    public String getValue() {
                        return System.getProperty("http.agent");
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.14.4 */
                class C10134 implements Header {
                    C10134() {
                    }

                    public String getName() {
                        return "Accept-Language";
                    }

                    public String getValue() {
                        return "en-US";
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.14.5 */
                class C10145 implements Header {
                    C10145() {
                    }

                    public String getName() {
                        return "Accept";
                    }

                    public String getValue() {
                        return "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }
            });
            if (CookieManager.getInstance().getCookie("https://m.facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://m.facebook.com").split(";");
                for (String split2 : cookieValues) {
                    split = split2.split("=");
                    if (split.length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("m.facebook.com");
                    store.addCookie(cookie);
                }
            }
            if (CookieManager.getInstance().getCookie("https://facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://facebook.com").split(";");
                for (String split22 : cookieValues) {
                    split = split22.split("=");
                    if (split.length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("facebook.com");
                    store.addCookie(cookie);
                }
            }
            httpclient.setCookieStore(store);
            HttpResponse response = httpclient.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() == 200) {
                Matcher m = Pattern.compile("first_degree.php?[^\"]*").matcher(EntityUtils.toString(response.getEntity()));
                if (m.find()) {
                    return getFirstDegreeHTML(m.group(0));
                }
            }
            response.getEntity().getContent().close();
        } catch (Throwable e) {
        }
        return null;
    }

    private String getFirstDegreeHTML(String url) {
        try {
            String[] cookieValues;
            String[] split;
            BasicClientCookie cookie;
            url = ("http://m.facebook.com/ds/" + url).replaceAll("\\\\u0025", "%");
            DefaultHttpClient httpclient = new DefaultHttpClient();
            BasicCookieStore store = new BasicCookieStore();
            CookieSyncManager.createInstance(appContext);
            httpclient.addRequestInterceptor(new HttpRequestInterceptor() {

                public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                    httpRequest.addHeader(new C10151());
                    httpRequest.addHeader(new C10162());
                    httpRequest.addHeader(new C10173());
                    httpRequest.addHeader(new C10184());
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.15.1 */
                class C10151 implements Header {
                    C10151() {
                    }

                    public String getName() {
                        return "X-Requested-With";
                    }

                    public String getValue() {
                        return "XMLHttpRequest";
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.15.2 */
                class C10162 implements Header {
                    C10162() {
                    }

                    public String getName() {
                        return "User-Agent";
                    }

                    public String getValue() {
                        return System.getProperty("http.agent");
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.15.3 */
                class C10173 implements Header {
                    C10173() {
                    }

                    public String getName() {
                        return "Accept-Language";
                    }

                    public String getValue() {
                        return "en-US";
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }

                /* renamed from: com.chatous.pointblank.manager.FacebookManager.15.4 */
                class C10184 implements Header {
                    C10184() {
                    }

                    public String getName() {
                        return "Accept";
                    }

                    public String getValue() {
                        return "*/*";
                    }

                    public HeaderElement[] getElements() {
                        return new HeaderElement[0];
                    }
                }
            });
            if (CookieManager.getInstance().getCookie("https://m.facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://m.facebook.com").split(";");
                for (String split2 : cookieValues) {
                    split = split2.split("=");
                    if (split.length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("m.facebook.com");
                    store.addCookie(cookie);
                }
            }
            if (CookieManager.getInstance().getCookie("https://facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://facebook.com").split(";");
                for (String split22 : cookieValues) {
                    split = split22.split("=");
                    if (split.length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("facebook.com");
                    store.addCookie(cookie);
                }
            }
            httpclient.setCookieStore(store);
            HttpResponse response = httpclient.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            }
            response.getEntity().getContent().close();
            return null;
        } catch (Throwable e) {
            return null;
        }
    }


}
