package com.example.authio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.authio.R;
import com.example.authio.databinding.SingleUserBinding;
import com.example.authio.models.User;

import java.util.List;

public class UserListViewAdapter extends ArrayAdapter<User> {
    private List<User> users;

    public UserListViewAdapter(Context context, int resource, List<User> users) {
        super(context, resource, users);
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public long getItemId(int position) {
        return users.get(position).hashCode();
    }

    private static class UserHolder {
        SingleUserBinding singleUserBinding;
        ImageView profileImage;
    }

    /**
     * Credit to @sergi from https://stackoverflow.com/questions/33943717/android-data-binding-with-custom-adapter
     * for the implementation of data-binding within this method using convertView's tag to assign data binding instance
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserHolder userHolder;
        View userCard = convertView;

        if(users.size() > 0 && position < users.size()) {
            User user = getItem(position);

            if(userCard == null)  {
                userHolder = new UserHolder();
                userHolder.singleUserBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(getContext()), // has access to context. . . somehow?
                        R.layout.single_user,
                        parent,
                        false
                );

            /* SingleUserBinding.inflate(
                    layoutInflater,
                    parent,
                    false); // other optional inflation */

                userCard = userHolder.singleUserBinding.getRoot();

                userHolder.profileImage = userCard.findViewById(R.id.profile_image);

                userCard.setTag(userHolder);
            } else {
                userHolder = (UserHolder) convertView.getTag();
            }

            String photoUrl;
            if(user != null && (photoUrl = user.getPhotoUrl()) != null)  {
                Glide.with(getContext())
                        .load(photoUrl)
                        .into(userHolder.profileImage);
            } else {
                userHolder.profileImage.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.default_img)
                );
            }

            userHolder.singleUserBinding.setImmutableUser(user);
        }

        return userCard;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

}
