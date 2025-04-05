package com.example.todosimple;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ResetPasswordViewModel extends ViewModel {
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> success = new MutableLiveData<>();

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isSuccess() {
        return success;
    }

    public void resetPassword(String email) {
        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                success.setValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() { //обработка ошибок при сбросе пароля
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    error.setValue(String.valueOf(R.string.invalid_credentials_error));
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    error.setValue(String.valueOf(R.string.invalid_user_error));
                } else {
                    error.setValue(String.valueOf(R.string.unknown_error));
                }
            }
        });
    }
}
