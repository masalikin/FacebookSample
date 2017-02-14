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
import com.hsv.freeadblockerbrowser.R;
import com.hsv.freeadblockerbrowser.apache.GetFriendsListRequest;
import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;
import com.hsv.freeadblockerbrowser.networking.FacebookInviteRequest;
import com.hsv.freeadblockerbrowser.networking.ResponseListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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

    private void getFriends() {
        final String fbAppId = getString(R.string.facebook_app_id);
        final String token = AccessToken.getCurrentAccessToken().getToken();
        new GetFriendsListRequest(fbAppId, token).setListener(new ResponseListener<List<FacebookInvitableFriend>>() {
            @Override
            public void onSuccess(List<FacebookInvitableFriend> result) {
                if (result != null) {
                    populateFriendsList(result);
                }
            }

            @Override
            public void onError(Throwable t) {

            }
        }).execute();
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
