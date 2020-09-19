package com.example.authio.utils;

import com.example.authio.persistence.UserDao;
import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Constants;

import java.util.Map;

public class Converters {
    public static int updateCacheUserFromRequestMap(UserDao dao, Map<String, String> userMap) {
        UserEntity userEntity = new UserEntity();

        if(userMap.containsKey(Constants.USERNAME)) {
            userEntity.setUsername(userMap.get(Constants.USERNAME));
        }

        if(userMap.containsKey(Constants.DESCRIPTION)) {
            userEntity.setDescription(userMap.get(Constants.DESCRIPTION));
        }

        if(userMap.containsKey(Constants.EMAIL)) {
            userEntity.setEmail(userMap.get(Constants.EMAIL));
        }

        // we don't care about the password for the cache. . .

        return dao.update(userEntity);
    }

}
