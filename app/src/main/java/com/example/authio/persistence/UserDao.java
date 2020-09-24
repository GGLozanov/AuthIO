package com.example.authio.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

// DAOs define DB interactions (queries) with the RoomDatabase
@Dao // annotate just in case
public interface UserDao extends BaseDao<UserEntity> {

    // TODO: Future implemenation of pagination will come from here
    @Query("SELECT * FROM users WHERE id != :id") // annotation binding with method arguments (smart lil' thing!)
    LiveData<List<UserEntity>> getUsers(int id);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<UserEntity> getUser(int id);

    @Query("UPDATE users SET email = :email, username = :username, description = :description WHERE id = :id")
    void updateFields(String email, String username, String description, int id);

}
