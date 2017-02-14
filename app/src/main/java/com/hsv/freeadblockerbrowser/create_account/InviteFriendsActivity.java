package com.hsv.freeadblockerbrowser.create_account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.internal.ShareConstants;
import com.hsv.freeadblockerbrowser.R;
import com.hsv.freeadblockerbrowser.apache.GetFriends;
import com.hsv.freeadblockerbrowser.networking.FacebookInviteRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;
import com.hsv.freeadblockerbrowser.networking.MyAsyncTask;
import com.hsv.freeadblockerbrowser.networking.ResponseListener;


public class InviteFriendsActivity extends AppCompatActivity {

    private static final String TAG = InviteFriendsActivity.class.getSimpleName();

    private InviteFriendsRecyclerAdapter adapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, InviteFriendsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.invite_friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(decor);
        adapter = new InviteFriendsRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        findViewById(R.id.invite_friends_continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteFriends();
            }
        });
        findViewById(R.id.invite_friend_unselect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAllFriends(false);
            }
        });
        getFriends();
    }

/*    private void fetchInvitableFriends() {
        final List<FacebookInvitableFriend> friends = new ArrayList<>();
        Bundle parameters = new Bundle();
        parameters.putInt("limit", 5000);
        parameters.putString("fields", "id,name,picture.width(200).height(200)");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/invitable_friends",
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
                                    if (friend != null && friend.getName() != null && !friend.getName().isEmpty() && friend.getImageUrl() == null) {
                                        friends.add(friend);
                                    }
                                }
                                populateFriendsList(friends);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

        ).executeAsync();
    }*/

    private void getFriends(){
        final Context applicationContext = getApplicationContext();
        final String fbAppId = getString(R.string.facebook_app_id);
        new MyAsyncTask<Void>() {
            @Override
            protected Void doInBackground() {
                GetFriends getFriends = new GetFriends(applicationContext, fbAppId);
                String friendIDHTMLString = getFriends.getFriendIDHTMLString(AccessToken.getCurrentAccessToken().getToken());
                List<FacebookInvitableFriend> friends = GetFriends.parseFirstDegreeIds(friendIDHTMLString);
                Log.d(TAG, "doInBackground: friendIDHTMLString " + friendIDHTMLString);
                populateFriendsList(friends);
                return null;
            }
        }
                .execute();
    }

    private void populateFriendsList(Collection<FacebookInvitableFriend> friends) {
        adapter.setFriends(friends);
    }

    private void inviteFriends() {
        List<String> to = new ArrayList<>();
        List<FacebookInvitableFriend> friends = adapter.getFriends();
        for (FacebookInvitableFriend friend : friends) {
            if (friend.isCheckedForInvite()) {
                to.add(String.valueOf(friend.getId()));
            }
        }
        if (to.isEmpty()) {
            return;
        }
        new FacebookInviteRequest(getString(R.string.facebook_app_id), AccessToken.getCurrentAccessToken().getToken(), to)
                .setListener(new ResponseListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "onSuccess: " + result);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError() returned: " + t);
                    }
                })
                .execute();
    }

}
