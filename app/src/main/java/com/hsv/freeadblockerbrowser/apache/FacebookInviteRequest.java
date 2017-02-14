package com.hsv.freeadblockerbrowser.apache;

import android.text.TextUtils;

import com.facebook.share.internal.ShareConstants;
import com.hsv.freeadblockerbrowser.networking.MyAsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FacebookInviteRequest extends MyAsyncTask<Boolean> {


    private final String fbAppId;
    private final String token;
    private final String message;
    private final Iterable toIds;

    public FacebookInviteRequest(String fbAppId, String token, String message, Iterable toIds) {
        super();

        this.fbAppId = fbAppId;
        this.token = token;
        this.message = message;
        this.toIds = toIds;
    }

    @Override
    protected Boolean doInBackground() {
        return sendInvites();
    }

    public boolean sendInvites() {
        int i;
        String url = "https://m.facebook.com/v2.2/dialog/apprequests?access_token=" + this.token
                + "&app_id=" + fbAppId
                + "&to=" + TextUtils.join("%2c", toIds)
                + "&type=user_agent"
                + "&title=SampleApp"
                + "&redirect_uri=fbconnect%3A%2F%2Fsuccess"
                + "&message=Join%20me%20on%20SampleApp!"
                + "&display=touch";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String[] cookieValues;
            int length;
            String[] split;
//            BasicClientCookie cookie;
//            BasicCookieStore store = new BasicCookieStore();
          /*  if (CookieManager.getInstance().getCookie("https://m.facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://m.facebook.com").split(";");
                i = 0;
                while (true) {
                    length = cookieValues.length;
                    if (i >= length) {
                        break;
                    }
                    split = cookieValues[i].split("=");
                    length = split.length;
                    if (length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("m.facebook.com");
                    store.addCookie(cookie);
                    i++;
                }
            }
            if (CookieManager.getInstance().getCookie("https://facebook.com") != null) {
                cookieValues = CookieManager.getInstance().getCookie("https://facebook.com").split(";");
                i = 0;
                while (true) {
                    length = cookieValues.length;
                    if (i >= length) {
                        break;
                    }
                    split = cookieValues[i].split("=");
                    length = split.length;
                    if (length == 2) {
                        cookie = new BasicClientCookie(split[0], split[1]);
                    } else {
                        cookie = new BasicClientCookie(split[0], null);
                    }
                    cookie.setDomain("facebook.com");
                    store.addCookie(cookie);
                    i++;
                }
            }
            httpclient.setCookieStore(store);*/
        } catch (Throwable e) {
        }
        try {
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            InputStream is = response.getEntity().getContent();
            if (statusLine.getStatusCode() == 200) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                NodeList inputs = document.getElementsByTagName("input");
                if (inputs == null || inputs.getLength() == 0) {
                    return false;
                }
                Map<String, String> postParams2 = new HashMap<>();
                List<NameValuePair> postParams = new ArrayList();
                for (i = 0; i < inputs.getLength(); i++) {
                    NamedNodeMap nodeMap = inputs.item(i).getAttributes();
                    String name = null;
                    String value = null;
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Node node = nodeMap.item(j);
                        String nodeName = node.getNodeName();
                        if (ShareConstants.WEB_DIALOG_PARAM_NAME.equals(nodeName)) {
                            name = node.getNodeValue();
                        } else {
                            if ("value".equals(nodeName)) {
                                value = node.getNodeValue();
                            }
                        }
                    }
                    if (!(value == null || name == null)) {
                        postParams.add(new BasicNameValuePair(name, value));
                    }
                    if (!(value == null || name == null)) {
                        postParams2.put(name, value);
                    }
                }
                HttpPost post = new HttpPost("http://m.facebook.com/v2.2/dialog/app_requests/submit");
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams);
                post.setEntity(entity);
                HttpResponse postResponse = httpclient.execute(post);
                StatusLine postStatusLine = postResponse.getStatusLine();
                String postResponseStr = EntityUtils.toString(postResponse.getEntity());
                if (postStatusLine.getStatusCode() == 200) {
                    if (postResponseStr.contains("fbconnect:\\/\\/success")) {
                        return true;
                    }
                }
            } else {
                is.close();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return false;
    }
}
