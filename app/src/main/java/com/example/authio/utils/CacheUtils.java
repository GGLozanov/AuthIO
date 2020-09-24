package com.example.authio.utils;

import com.example.authio.executors.AppExecutors;
import com.example.authio.persistence.UserDao;
import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Constants;

import java.util.Map;

public class CacheUtils {
    public static void updateCacheUserFromRequestMap(UserDao dao, int authId, Map<String, String> userMap) {
       dao.getUser(authId).observeForever(userEntity -> {
           if(userMap.containsKey(Constants.USERNAME)) {
               userEntity.setUsername(userMap.get(Constants.USERNAME));
           }

           if(userMap.containsKey(Constants.DESCRIPTION)) {
               userEntity.setDescription(userMap.get(Constants.DESCRIPTION));
           }

           if(userMap.containsKey(Constants.EMAIL)) {
               userEntity.setEmail(userMap.get(Constants.EMAIL));
           }

           // we don't care about the password for the cache (because there is no such field)
           AppExecutors.getInstance().executeOnDiskIO(() -> dao.updateFields(
                   userEntity.getEmail(),
                   userEntity.getUsername(),
                   userEntity.getDescription(), authId)); // TODO: Fix small overhead with resetting already set information
       }); // get user to receive already cached information and use it if necessary
    }

}
