package com.example.authio.persistence;

import androidx.room.Database;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { UserEntity.class }, version = 1)
public abstract class UserDatabase extends RoomDatabase {
    private static UserDatabase instance;

    public static UserDatabase getInstance() {
        if(instance == null) {
            instance = Room.databaseBuilder(getApplicationContext(),
                    )
        }
        return instance;
    }

    abstract public UserDao getUserDao();

}
