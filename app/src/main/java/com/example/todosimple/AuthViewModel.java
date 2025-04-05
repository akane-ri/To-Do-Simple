package com.example.todosimple;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";
    private final FirebaseAuth auth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usersReference = database.getReference("Users");
    private DatabaseReference categoriesReference;
    private final GoogleSignInClient googleSignInClient;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(application.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(application, options);
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signOutAndSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                userLiveData.setValue(null);  // Clear the user before new sign in
            } else {
                Log.w(TAG, "signOut:failure", task.getException());
                errorLiveData.setValue("Sign out failed. Please try again.");
            }
        });
    }

    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            usersReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        User newUser = new User(
                                                firebaseUser.getUid(),
                                                firebaseUser.getDisplayName(),
                                                firebaseUser.getUid()
                                        );
                                        usersReference.child(newUser.getId()).setValue(newUser);
                                    }

                                    // Ensure default category "All" exists
                                    categoriesReference = database.getReference("Categories").child(firebaseUser.getUid());
                                    categoriesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            boolean categoryExists = false;
                                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                                Category existingCategory = categorySnapshot.getValue(Category.class);
                                                if (existingCategory != null && "All".equals(existingCategory.getName())) {
                                                    categoryExists = true;
                                                    break;
                                                }
                                            }

                                            if (!categoryExists) {
                                                String categoryId = categoriesReference.push().getKey();
                                                if (categoryId != null) {
                                                    Category category = new Category(
                                                            categoryId,
                                                            "All",
                                                            firebaseUser.getUid()
                                                    );
                                                    categoriesReference.child(categoryId).setValue(category);
                                                }
                                            }

                                            userLiveData.setValue(firebaseUser);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.w(TAG, "loadCategory:onCancelled", databaseError.toException());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error checking user existence", error.toException());
                                }
                            });
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        errorLiveData.setValue("Authentication Failed.");
                    }
                }
            });
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            errorLiveData.setValue("Google sign in failed: " + e.getMessage());
        }
    }
}
