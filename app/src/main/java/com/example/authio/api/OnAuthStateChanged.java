package com.example.authio.api;

import com.example.authio.models.User;

public interface OnAuthStateChanged {
    void performAuthChange(User user);
}
