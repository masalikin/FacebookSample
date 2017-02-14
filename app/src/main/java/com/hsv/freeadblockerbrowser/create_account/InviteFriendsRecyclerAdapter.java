package com.hsv.freeadblockerbrowser.create_account;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsv.freeadblockerbrowser.BuildConfig;
import com.hsv.freeadblockerbrowser.R;
import com.hsv.freeadblockerbrowser.model.FacebookInvitableFriend;

import org.freemp.malevich.ImageCache;
import org.freemp.malevich.Malevich;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class InviteFriendsRecyclerAdapter extends RecyclerView.Adapter<InviteFriendsRecyclerAdapter.FriendsItemViewHolder> {

    private static final String TAG = InviteFriendsRecyclerAdapter.class.getSimpleName();

    private final List<FacebookInvitableFriend> friends = new ArrayList<>();
    private Malevich malevich;

    @Override
    public FriendsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (malevich == null) {
            ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(context, "fb_image_cache");
            cacheParams.memoryCacheEnabled = false;

            malevich = new Malevich.Builder(context)
                    .debug(BuildConfig.DEBUG)
                    .CacheParams(cacheParams)
                    .build();

            malevich = new Malevich.Builder(context).build();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_invite_friend_item, parent, false);
        return new FriendsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendsItemViewHolder holder, int position) {
        final FacebookInvitableFriend friend = friends.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.toggle();
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                friend.setCheckedForInvite(isChecked);
            }
        });
        holder.textName.setText(friend.getName());
        holder.checkBox.setChecked(friend.isCheckedForInvite());
        String imageUrl = friend.getImageUrl();
        malevich.load(imageUrl)
                .imageDecodedListener(new Malevich.ImageDecodedListener() {
                    @Override
                    public Bitmap onImageDecoded(String data, int reqWidth, int reqHeight, Bitmap bitmap) {
                        Log.d(TAG, "onImageDecoded() called with: data = [" + data + "], reqWidth = [" + reqWidth + "], reqHeight = [" + reqHeight + "], bitmap = [" + bitmap + "]");
                        return Malevich.Utils.getCircleBitmap(bitmap);
                    }
                }).into(holder.image);
    }

    public List<FacebookInvitableFriend> getFriends() {
        return friends;
    }

    public void setFriends(Collection<FacebookInvitableFriend> friends) {
        if (friends == null) {
            this.friends.clear();
        } else {
            this.friends.addAll(friends);
        }
        notifyDataSetChanged();
    }

    public void selectAllFriends(boolean select) {
        for (FacebookInvitableFriend friend : friends) {
            friend.setCheckedForInvite(select);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendsItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView textName;
        private final CheckBox checkBox;

        public FriendsItemViewHolder(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.invite_friend_photo);
            this.textName = (TextView) itemView.findViewById(R.id.invite_friend_name);
            this.checkBox = (CheckBox) itemView.findViewById(R.id.invite_friend_checkbox);
        }
    }
}
