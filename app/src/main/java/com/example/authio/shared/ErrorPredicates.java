package com.example.authio.shared;

import android.util.Patterns;

import java.util.function.Predicate;

public class ErrorPredicates {
    public static Predicate<String> email = (email) ->
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    public static Predicate<String> password = (password) ->
            password.isEmpty() || password.length() < 4 || password.length() > 14;
    public static Predicate<String> username = (username) ->
            username.isEmpty() || username.length() < 2 || username.length() > 25;
    public static Predicate<String> description = (description) ->
            description.isEmpty() || description.length() < 5 || description.length() > 30;
}
