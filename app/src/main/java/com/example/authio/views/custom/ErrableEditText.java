package com.example.authio.views.custom;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import com.example.authio.shared.ErrorPredicates;

import java.util.Objects;
import java.util.function.Predicate;

public class ErrableEditText extends androidx.appcompat.widget.AppCompatEditText {
    private boolean hasError = false;
    private Predicate<String> errorPredicate; // type arg -> input arg type for checking if whether edittext is invalid
    private String errorText;

    public ErrableEditText(Context context) {
        super(context);
    }

    public ErrableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ErrableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // TODO: Fix naming here; too ambiguous (one gets a property, the other tests the predicate as well)

    /**
     * Tests the current text value with the given predicate and reinitialises the hasError property. Sets the error text property if true.
     * @return - boolean result of the validity check by the predicate from the isInvalid method call
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean setErrorTextIfError() {
        if(hasError = errorPredicate.test(Objects.requireNonNull(getText()).toString())) {
            setError(errorText);
        }
        return hasError;
    }

    /**
     * Retrieves the boolean result of the last validity check by the predicate from the setErrorTextIfError method call
     * @return - boolean result of the last validity check by the predicate from the isInvalid method call
     */
    public boolean hadError() {
        return hasError;
    }

    public void setErrorPredicate(Predicate<String> errorPredicate) {
        this.errorPredicate = errorPredicate;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public ErrableEditText withErrorPredicate(Predicate<String> predicate) {
        this.errorPredicate = predicate;
        return this;
    }

    public ErrableEditText withErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    public ErrableEditText asEmail() {
        return withErrorPredicate(ErrorPredicates.email).withErrorText("Enter a valid E-mail");
    }

    public ErrableEditText asPassword() {
        return withErrorPredicate(ErrorPredicates.password).withErrorText("Enter a valid password (3 to 15 characters)");
    }

    public ErrableEditText asUsername() {
        return withErrorPredicate(ErrorPredicates.username).withErrorText("Enter a valid username (2 to 25 characters)");
    }

    public ErrableEditText asDescription() {
        return withErrorPredicate(ErrorPredicates.description).withErrorText("Enter a valid description (3 to 30 characters)");
    }

}
