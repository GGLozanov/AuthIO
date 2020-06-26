package com.example.authio.api;

public interface OnAuthStateChanged {
    void performAuthChange(String email, String username, String description);

    void performAuthReset();
}
