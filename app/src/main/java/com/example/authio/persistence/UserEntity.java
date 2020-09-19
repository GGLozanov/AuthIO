package com.example.authio.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.authio.models.CacheModel;
import com.example.authio.models.Model;
import com.example.authio.models.User;
import com.example.authio.shared.Constants;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
public class UserEntity extends BaseEntity implements Parcelable {

    @ColumnInfo(name = Constants.USERNAME)
    @SerializedName(Constants.USERNAME)
    private String username;

    @ColumnInfo(name = Constants.DESCRIPTION)
    @SerializedName(Constants.DESCRIPTION)
    private String description;

    @ColumnInfo(name = Constants.EMAIL)
    @SerializedName(Constants.EMAIL)
    private String email;


    @ColumnInfo(name = Constants.PHOTO_URL)
    @SerializedName(Constants.PHOTO_URL)
    private String photoUrl;

    public UserEntity() {}

    public UserEntity(int id, String username, String description, String email, String photoUrl) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public UserEntity(Parcel in) {
        this.id = in.readInt();
        this.username = in.readString();
        this.description = in.readString();
        this.email = in.readString();
        this.photoUrl = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { // creator used to convert parcelables to entity
        public UserEntity createFromParcel(Parcel in) {
            return new UserEntity(in);
        }

        public UserEntity[] newArray(int size) {
            return new UserEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(id > -1) { // not an invalid row. . .
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

        if(photoUrl != null) {
            dest.writeString(photoUrl);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + id + ", " + username + ", " + description + ", " + email + ", " + photoUrl + "]";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
