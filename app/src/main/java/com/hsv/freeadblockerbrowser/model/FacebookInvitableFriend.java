package com.hsv.freeadblockerbrowser.model;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookInvitableFriend {

    private final String id;
    private final String name;
    private final String imageUrl;
    private boolean checkedForInvite = false;

    public FacebookInvitableFriend(long id, String name, String imageUrl) {
        this(String.valueOf(id), name, imageUrl);
    }

    public FacebookInvitableFriend(String id, String name, String imageUrl) {
        this.id = String.valueOf(id);
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static FacebookInvitableFriend fromJson(JSONObject o) throws JSONException {
        String name = o.getString("name");
        long id = o.getLong("id");
        String imageUrl = o.getJSONObject("picture").getJSONObject("data").getString("url");
        return new FacebookInvitableFriend(id, name, imageUrl);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isCheckedForInvite() {
        return checkedForInvite;
    }

    public void setCheckedForInvite(boolean checkedForInvite) {
        this.checkedForInvite = checkedForInvite;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
