package com.example.authio.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// a transcribed model for an HTTP response from the Web service
// contains 'response' (status), 'username', 'description', and 'email' (at most) in a JSON format
// implements Parcelable in order to be sent through intents using Bundle args
public class User extends Model implements Parcelable {

    @SerializedName("id")
    @Expose
    @Nullable
    private Integer id;

    @SerializedName("username")
    @Expose
    @Nullable
    private String username;

    @SerializedName("description")
    @Expose
    @Nullable
    private String description;

    @SerializedName("email")
    @Expose
    @Nullable
    private String email;

    /**
     *
     * @param id - Given user's id
     * @param response - Custom API status message received from call
     * @param username - Given user's username
     * @param description - Given user's description
     * @param email - Given user's email
     */
    public User(@Nullable Integer id, String response, @Nullable String username, @Nullable String description, @Nullable String email) {
        super(response);
        this.id = id;
        this.username = username;
        this.description = description;
        this.email = email;
    }

    public User(Parcel in) {
        super(in.readString());
        this.id = in.readInt();
        this.username = in.readString();
        this.description = in.readString();
        this.email = in.readString();
    }

    public Integer getId() { return id; }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { // creator used to convert parcelables to model
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(response != null) {
            dest.writeString(response);
        }

        if(id != null) {
            dest.writeInt(id);
        }

        if(username != null) {
            dest.writeString(username);
        }

        if(description != null) {
            dest.writeString(description);
        }

        if(email != null) {
            dest.writeString(email);
        }
    }
}
