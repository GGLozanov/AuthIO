package com.example.authio.adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.authio.R;
import com.example.authio.databinding.SingleUserBinding;
import com.example.authio.models.User;
import com.example.authio.viewmodels.ProfileFragmentViewModel;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /**
         * Credit to @sergi from https://stackoverflow.com/questions/33943717/android-data-binding-with-custom-adapter
         * for the implementation of data-binding within this method using convertView's tag to assign data binding instance
         */
        SingleUserBinding singleUserBinding;

        if(convertView == null)  {
            singleUserBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()), // has access to context. . . somehow?
                    R.layout.single_user,
                    parent,
                    false
            );

            /* SingleUserBinding.inflate(
                    layoutInflater,
                    parent,
                    false); // other optional inflation */

            convertView = singleUserBinding.getRoot();
        } else {
            singleUserBinding = (SingleUserBinding) convertView.getTag();
        }

        User user = getItem(position);

        singleUserBinding.setImmutableUser(user);

        convertView.setTag(singleUserBinding);

        return convertView;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
}
