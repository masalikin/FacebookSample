package com.hsv.freeadblockerbrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.GameRequestDialog;
import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import bolts.AppLinks;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "fb_test";

    private static final String APP_LINK_URL = "https://fb.me/1301427246545229";
    private CallbackManager callbackManager = new CallbackManagerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
        }

        LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email", "user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: " + loginResult);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError: ", error);
            }
        });

        findViewById(R.id.send_invite_fb_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAppInvite();
            }
        });

        findViewById(R.id.send_game_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchInvitableFriends();
            }
        });

        findViewById(R.id.send_kiwi_quiet_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendKiwiRequest();
            }
        });
    }

    private void sendAppInvite() {
        Log.d(TAG, "sendAppInvite() called");
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(APP_LINK_URL)
                    .build();
            AppInviteDialog appInviteDialog = new AppInviteDialog(this);
            appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>() {
                @Override
                public void onSuccess(AppInviteDialog.Result result) {
                    Log.d(TAG, "onSuccess: " + result);
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "onCancel: ");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e(TAG, "onError: ", error);
                }
            });
            appInviteDialog.show(content);
        }
    }

    private void sendGameRequest(List<FacebookInvitableFriend> friends) {
        Log.d(TAG, "sendGameRequest() called");
        GameRequestDialog gameRequestDialog = new GameRequestDialog(this);
        gameRequestDialog.registerCallback(callbackManager, new FacebookCallback<GameRequestDialog.Result>() {
            @Override
            public void onSuccess(GameRequestDialog.Result result) {
                Log.d(TAG, "onSuccess: " + result);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError: ", error);
            }
        });

        List<String> ids = new ArrayList<>(friends.size());
        for (FacebookInvitableFriend friend : friends) {
            ids.add(String.valueOf(friend.getId()));
        }

        GameRequestContent content = new GameRequestContent.Builder()
                .setRecipients(ids)
                .setObjectId(getString(R.string.facebook_app_id))
                .setActionType(GameRequestContent.ActionType.SEND)
                .setMessage("hey check this")
                .build();
        gameRequestDialog.show(content);
    }

    private void sendKiwiRequest() {
       /* new FacebookInviteRequest(getString(R.string.facebook_app_id), AccessToken.getCurrentAccessToken().getToken(), null, toIds)
                .setListener(new ResponseListener<FacebookInviteRequest.Result>() {
                    @Override
                    public void onSuccess(FacebookInviteRequest.Result result) {
                        Log.d(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "onError: ", t);
                    }
                })
                .execute();*/

    }

    private void fetchInvitableFriends() {
        final List<FacebookInvitableFriend> friends = new ArrayList<>();
        Bundle parameters = new Bundle();
        parameters.putInt("limit", 5000);
        parameters.putString("fields", "id,name,picture.width(200).height(200)");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/friends",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null) {
                            try {
                                JSONArray array = response.getJSONObject().getJSONArray(ShareConstants.WEB_DIALOG_PARAM_DATA);
                                for (int i = 0; i < array.length(); i++) {
                                    FacebookInvitableFriend friend = FacebookInvitableFriend.fromJson(array.getJSONObject(i));
                                    if (!(friend == null || friend.getName() == null || friend.getName().isEmpty())) {
                                        friends.add(friend);
                                    }
                                }
                                sendGameRequest(friends);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

        ).executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
