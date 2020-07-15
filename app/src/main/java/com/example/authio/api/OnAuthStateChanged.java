package com.example.authio.api;

public interface OnAuthStateChanged {
    void performAuthChange(UserModel user);

    void performAuthReset();
}
