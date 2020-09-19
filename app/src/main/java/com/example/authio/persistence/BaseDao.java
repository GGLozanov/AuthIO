package com.example.authio.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<T> entities);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(T user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(List<T> users);

    @Delete
    void deleteUser(T user);

    @Delete
    void deleteUsers(List<T> users);
}
