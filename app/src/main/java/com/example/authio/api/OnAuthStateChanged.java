package com.example.authio.api;

public interface OnAuthStateChanged {
    void performAuthChange(Integer id, String email, String username, String description);

    void performAuthReset();
}
