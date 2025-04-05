package com.example.todosimple;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationViewModel extends ViewModel {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference categoriesReference;
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private GoogleSignInClient client;

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public RegistrationViewModel() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                user.setValue(firebaseAuth.getCurrentUser());
            }
        });

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("Users");
    }

    public void signUp(String email, String password, String name) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        return;
                    }
                    User newUser = new User(
                            firebaseUser.getUid(),
                            name,
                            firebaseUser.getUid()
                    );
                    usersReference.child(newUser.getId()).setValue(newUser);

                    categoriesReference = database.getReference("Categories");
                    String categoryId = categoriesReference.child(firebaseUser.getUid()).push().getKey();
                    Category category = new Category(
                            categoryId,
                            "All",
                            firebaseUser.getUid()
                    );

                    assert categoryId != null;
                    categoriesReference.child(newUser.getId()).child(categoryId).setValue(category);
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void signInWithGoogle() {

    }


    public interface OnGoogleSignInListener {
        void onError(String errorMessage);
    }
}
