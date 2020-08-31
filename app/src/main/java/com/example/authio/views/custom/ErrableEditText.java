package com.example.authio.views.custom;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import java.util.Objects;
import java.util.function.Predicate;

public class ErrableEditText extends androidx.appcompat.widget.AppCompatEditText {
    private boolean isInvalid = false;
    private Predicate<String> invalidityPredicate; // type arg -> input arg type for checking if whether edittext is invalid

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
     * Tests the current text value with the given predicate and reinitialises the isInvalid property
     * @return - boolean result of the validity check by the predicate from the isInvalid method call
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isInvalid() {
        return isInvalid = invalidityPredicate.test(Objects.requireNonNull(getText()).toString());
    }

    /**
     * Retrieves the boolean result of the last validity check by the predicate from the isInvalid method call
     * @return - boolean result of the last validity check by the predicate from the isInvalid method call
     */
    public boolean wasInvalid() {
        return isInvalid;
    }

    public ErrableEditText withErrorPredicate(Predicate<String> invalidPredicate) {
        this.invalidityPredicate = invalidPredicate;
        return this;
    }
}
