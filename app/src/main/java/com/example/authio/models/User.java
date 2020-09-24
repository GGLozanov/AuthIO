package com.example.authio.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// a transcribed model for an HTTP response from the Web service
// contains 'response' (status), 'username', 'description', and 'email' (at most) in a JSON format
// implements Parcelable in order to be sent through intents using Bundle args
public class User extends CacheModel<UserEntity> implements Parcelable {

    /**
     * Constructor made for when user has an image (photoUrl)
     * @param id - Given user's id
     * @param response - Custom API status message received from call
     * @param username - Given user's username
     * @param description - Given user's description
     * @param email - Given user's email
     * @param photoUrl - Given user's photoUrl
     */
    public User(@Nullable Integer id, String response, @Nullable String username,
                @Nullable String description, @Nullable String email, @Nullable String photoUrl) {
        super(response, new UserEntity(id, username, description, email, photoUrl));
    }

    /**
     * Constructor made for when user has no image (no photoUrl)
     * @param id - Given user's id
     * @param response - Custom API status message received from call
     * @param username - Given user's username
     * @param description - Given user's description
     * @param email - Given user's email
     */
    public User(@Nullable Integer id, String response, @Nullable String username,
                @Nullable String description, @Nullable String email) {
        super(response, new UserEntity(id, username, description, email, null));
    }

    /**
     * Constructor made for when User is represented as a Room DB entity
     * @param id - Given user's id
     * @param username - Given user's username
     * @param description - Given user's description
     * @param email - Given user's email
     */
    public User(Integer id, @Nullable String username,
                @Nullable String description, @Nullable String email) {
        this(id, id == null || username == null || description == null || email == null ?
                Constants.FAILED_RESPONSE : Constants.SUCCESS_RESPONSE, username, description, email);
        // can't invoke invalid method here before object init
    }

    /**
     * Utility constructor for representing User as a Room DB entity
     * @param entity
     * @param response
     */
    public User(UserEntity entity, String response) {
        super(response, entity);
    }

    /**
     * Utility constructor with a default response recorded based on the entity given
     * @param entity
     */
    public User(UserEntity entity) {
        super(entity != null && (entity.getId() <= 0 || entity.getUsername() == null || entity.getDescription() == null || entity.getEmail() == null) ?
                Constants.FAILED_RESPONSE : Constants.SUCCESS_RESPONSE, entity);
    }

    /**
     *
     * @param response - Custom API status message received from call
     */
    private User(String response) {
        super(response);
    }

    public static User asFailed(String response) { // couldn't use base class method due to casting issues; FIXME optimise this repetition for all models
        return new User(response);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { // creator used to convert parcelables to model
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(Parcel in) {
        super(in.readString(), in.readParcelable(UserEntity.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(response != null) {
            dest.writeString(response);
        }

        if(entity != null) {
            dest.writeParcelable(entity, 0);
        }
    }

    public boolean isInvalid() {
        return entity == null || entity.getId() <= 0 || entity.getUsername() == null
                || entity.getDescription() == null || entity.getEmail() == null;
    }
}
