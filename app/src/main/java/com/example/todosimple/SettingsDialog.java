package com.example.todosimple;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class SettingsDialog extends BottomSheetDialogFragment {

    private static final String EXTRA_CURRENT_USER_ID = "current_id";

    private SettingViewModel viewModel;
    private ToDoListViewModel toDoListViewModel;
    private String currentUserId;

    private RelativeLayout buttonExit;
    private RelativeLayout buttonConnectTodolist;
    private ImageView buttonThemePurpless;
    private ImageView buttonThemeDark;
    private ImageView buttonThemeGreen;
    private ImageView buttonThemeBlue;
    private TextView buttonLanguageRu;
    private TextView buttonLanguageEn;


    public static SettingsDialog newInstance(String currentUserId) {
        SettingsDialog fragment = new SettingsDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_CURRENT_USER_ID, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_settings, container, false);
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

        setupClickListener();
    }

    @Override
    public void onStart() {
      super.onStart();
    }

    private void setupClickListener() {

        buttonConnectTodolist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ConnectingDialog connectingDialog = ConnectingDialog.newInstance(currentUserId);
                assert getFragmentManager() != null;
                connectingDialog.show(getFragmentManager(), "ConnectionDialog");
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.logout();
                dismiss();
            }
        });

        buttonThemePurpless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectedTheme(buttonThemePurpless, buttonThemeBlue, buttonThemeDark, buttonThemeGreen);
            }
        });

        buttonThemeBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectedTheme(buttonThemeBlue, buttonThemePurpless, buttonThemeDark, buttonThemeGreen);
            }
        });

        buttonThemeGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectedTheme(buttonThemeGreen, buttonThemeBlue, buttonThemeDark, buttonThemePurpless);
            }
        });

        buttonThemeDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectedTheme(buttonThemeDark, buttonThemePurpless, buttonThemeBlue, buttonThemeGreen);
            }
        });

        buttonLanguageRu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewLocale("ru");
                dismiss();
            }
        });

        buttonLanguageEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewLocale("en");
                dismiss();
            }
        });
    }

    private void setNewLocale(String language) {
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);
        Configuration configuration = new Configuration();
        configuration.setLocale(newLocale);
        Resources resources = getResources();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        getActivity().getSharedPreferences("Settings", getActivity().MODE_PRIVATE)
                .edit()
                .putString("App_Language", language)
                .apply();

    }

    private void changeSelectedTheme(ImageView selectedTheme, ImageView otherTheme1, ImageView otherTheme2, ImageView otherTheme3) {
        selectedTheme.setImageResource(R.drawable.baseline_done_24);

        otherTheme1.setImageDrawable(null);
        otherTheme2.setImageDrawable(null);
        otherTheme3.setImageDrawable(null);
    }

    private void initViews(View view) {
        buttonExit = view.findViewById(R.id.buttonExit);
        buttonConnectTodolist = view.findViewById(R.id.buttonConnectTodolist);

        buttonThemePurpless = view.findViewById(R.id.buttonThemePurpless);
        buttonThemeDark = view.findViewById(R.id.buttonThemeDark);
        buttonThemeGreen = view.findViewById(R.id.buttonThemeGreen);
        buttonThemeBlue = view.findViewById(R.id.buttonThemeBlue);
        buttonLanguageRu = view.findViewById(R.id.buttonLanguageRu);
        buttonLanguageEn = view.findViewById(R.id.buttonLanguageEn);
    }
}