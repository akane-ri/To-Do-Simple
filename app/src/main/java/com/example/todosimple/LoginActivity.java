package com.example.todosimple;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText textInputEmail;
    private TextInputEditText textInputPassword;
    private RelativeLayout buttonLogin;
    private LinearLayout buttonGoogleSignIn;
    private TextView textViewForgotPassword;
    private TextView textViewRegistration;

    private LoginViewModel loginViewModel;
    private RegistrationViewModel registrationViewModel;
    private AuthViewModel authViewModel;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        FirebaseApp.initializeApp(this);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        registrationViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        observeViewModel();
        setupClickListener();
    }

    private void signInWithGoogle() {
        authViewModel.signOutAndSignIn();
        Intent signInIntent = authViewModel.getGoogleSignInIntent();
        activityResultLauncher.launch(signInIntent);
        Log.d(TAG, "Google Sign-In initiated");
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        authViewModel.handleSignInResult(result.getData());
                    } else {
                        Log.d(TAG, "Google Sign-In Result NOT OK" + result);
                    }
                }
            }
    );

    private void observeViewModel() {
        authViewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    updateUI(firebaseUser);
                }
            }
        });

        authViewModel.getErrorLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                int messageResId;
                if (errorMessage != null) {
                    switch (errorMessage) {
                        case "error_authentication_failed":
                            messageResId = R.string.error_authentication_failed;
                            break;
                        case "error_google_sign_in_failed":
                            messageResId = R.string.error_google_sign_in_failed;
                            break;
                        case "error_sign_out_failed":
                            messageResId = R.string.error_sign_out_failed;
                            break;
                        default:
                            messageResId = R.string.error_authentication_failed;
                    }
                    Toast.makeText(LoginActivity.this, getString(messageResId), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginViewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
        });

        loginViewModel.getUser().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Intent intent = ToDoListActivity.newIntent(LoginActivity.this, firebaseUser.getUid());
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = ToDoListActivity.newIntent(LoginActivity.this, user.getUid());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListener() {
        buttonLogin.setOnClickListener(v -> {
            String email = textInputEmail.getText().toString().trim();
            String password = textInputPassword.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                loginViewModel.login(email, password);
            } else {
                Toast.makeText(LoginActivity.this, getText(R.string.error_fieldinput), Toast.LENGTH_SHORT).show();
            }
        });

        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = ResetPasswordActivity.newIntent(LoginActivity.this, textInputEmail.getText().toString().trim());
            startActivity(intent);
        });

        textViewRegistration.setOnClickListener(v -> {
            Intent intent = RegistrationActivity.newIntent(LoginActivity.this);
            startActivity(intent);
        });
    }

    private void initViews() {
        textInputEmail = findViewById(R.id.textInputEmail);
        textInputPassword = findViewById(R.id.textInputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
        textViewRegistration = findViewById(R.id.textViewRegistration);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}
