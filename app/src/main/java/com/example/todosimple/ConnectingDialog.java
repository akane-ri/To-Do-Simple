package com.example.todosimple;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConnectingDialog extends BottomSheetDialogFragment {

    private static final String EXTRA_CURRENT_USER_ID = "current_id";

    private SettingViewModel viewModel;
    private ToDoListViewModel toDoListViewModel;
    private String currentUserId;

    private TextView textViewListConnect;
    private TextView textViewReturnList;

    private LinearLayout linearLayoutSetPassword;
    private TextInputEditText textInputSetPassword;
    private RelativeLayout buttonSavePassword;

    private LinearLayout linearLayoutCopy;
    private TextView textViewChangePassword;
    private TextView buttonCopyTodo;

    private TextInputEditText textInputNumberList;
    private TextInputEditText textInputPasswordList;
    private RelativeLayout buttonConnectTodolist;

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceUser = firebaseDatabase.getReference("Users");

    public static ConnectingDialog newInstance(String currentUserId) {
        ConnectingDialog fragment = new ConnectingDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_CURRENT_USER_ID, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_connecting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentUserId = getArguments().getString(EXTRA_CURRENT_USER_ID);
        }

        initViews(view);
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        toDoListViewModel = new ViewModelProvider(requireActivity()).get(ToDoListViewModel.class);

        fetchUserData();

        setupClickListener();
    }

    private void fetchUserData() {
        referenceUser.child(currentUserId).child("todoId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handleTodoIdResponse(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ConnectingDialog", "Ошибка при получении todoId: ", error.toException());
            }
        });

        referenceUser.child(currentUserId).child("passwordList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handlePasswordListResponse(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ConnectingDialog", "Ошибка при получении passwordList: ", error.toException());
            }
        });
    }

    private void handleTodoIdResponse(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue().toString().equals(currentUserId)) {
            textViewListConnect.setText(R.string.connect_list_your);
            textViewReturnList.setVisibility(View.GONE);
            linearLayoutCopy.setVisibility(View.VISIBLE);
            linearLayoutSetPassword.setVisibility(View.VISIBLE);
        } else {
            referenceUser.child(dataSnapshot.getValue().toString()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    String name = dataSnapshot1.getValue().toString();
                    textViewListConnect.setText(getString(R.string.connect_list) + name);
                    linearLayoutCopy.setVisibility(View.GONE);
                    linearLayoutSetPassword.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ConnectingDialog", "Ошибка при получении имени: ", error.toException());
                }
            });

            textViewReturnList.setVisibility(View.VISIBLE);
            linearLayoutSetPassword.setVisibility(View.GONE);
            linearLayoutCopy.setVisibility(View.GONE);

            Toast.makeText(requireContext(), R.string.connect_list + dataSnapshot.getValue().toString().substring(0, dataSnapshot.getValue().toString().length() / 2) + "...", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePasswordListResponse(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
            linearLayoutSetPassword.setVisibility(View.GONE);
            linearLayoutCopy.setVisibility(View.VISIBLE);
        } else {
            linearLayoutSetPassword.setVisibility(View.VISIBLE);
            linearLayoutCopy.setVisibility(View.GONE);
        }
    }

    private void setupClickListener() {
        buttonConnectTodolist.setOnClickListener(v -> connectTodoList());

        buttonSavePassword.setOnClickListener(v -> savePassword());

        textViewChangePassword.setOnClickListener(v -> {
            linearLayoutCopy.setVisibility(View.GONE);
            linearLayoutSetPassword.setVisibility(View.VISIBLE);
        });

        buttonCopyTodo.setOnClickListener(v -> copyTodoId());

        textViewReturnList.setOnClickListener(v -> returnToOwnList());
    }

    private void connectTodoList() {
        String list = textInputNumberList.getText().toString();
        String password = textInputPasswordList.getText().toString();

        referenceUser.child(list).child("passwordList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getValue().equals(password)) {
                        referenceUser.child(currentUserId).child("todoId").setValue(list);

                        toDoListViewModel.setTodoList(list);
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), R.string.error_password, Toast.LENGTH_SHORT).show();
                    }

                } else if (dataSnapshot.exists()) {
                    Toast.makeText(requireContext(), R.string.error_connect_user, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.error_connect_list, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ConnectingDialog", "Ошибка при подключении списка дел: ", error.toException());
            }
        });
    }

    private void savePassword() {
        referenceUser.child(currentUserId).child("passwordList").setValue(textInputSetPassword.getText().toString());
        Toast.makeText(requireContext(), R.string.passwordSet, Toast.LENGTH_SHORT).show();
        linearLayoutSetPassword.setVisibility(View.GONE);
        linearLayoutCopy.setVisibility(View.VISIBLE);
        textInputSetPassword.setHint(R.string.passwordSet);
        hideKeyboard();
    }

    private void copyTodoId() {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", currentUserId);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), getString(R.string.success_copy), Toast.LENGTH_SHORT).show();
    }

    private void returnToOwnList() {
        referenceUser.child(currentUserId).child("todoId").setValue(currentUserId);
        toDoListViewModel.setTodoList(currentUserId);
        textViewReturnList.setVisibility(View.GONE);
        dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(textInputSetPassword.getWindowToken(), 0);
        }
    }

    private void initViews(View view) {
        textViewListConnect = view.findViewById(R.id.textViewListConnect);
        textViewReturnList = view.findViewById(R.id.textViewReturnList);

        linearLayoutSetPassword = view.findViewById(R.id.linearLayoutSetPassword);
        textInputSetPassword = view.findViewById(R.id.textInputSetPassword);
        buttonSavePassword = view.findViewById(R.id.buttonSavePassword);

        linearLayoutCopy = view.findViewById(R.id.linearLayoutCopy);
        textViewChangePassword = view.findViewById(R.id.textViewChangePassword);
        buttonCopyTodo = view.findViewById(R.id.buttonCopyTodo);

        textInputNumberList = view.findViewById(R.id.textInputNumberList);
        textInputPasswordList = view.findViewById(R.id.textInputPasswordList);
        buttonConnectTodolist = view.findViewById(R.id.buttonConnectTodolist);
    }
}
