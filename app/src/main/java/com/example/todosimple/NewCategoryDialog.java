package com.example.todosimple;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class NewCategoryDialog extends BottomSheetDialogFragment {

    public static final String TAG = "NewCategoryDialog";
    private TextInputEditText editTextCategory;
    private RelativeLayout buttonSaveCategory;
    private TextView buttonSaveCategoryText;
    private ImageView buttonDeleteCategory;

    private CategoriesViewModel viewModel;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceCategories = firebaseDatabase.getReference("Categories");
    private DatabaseReference referenceUser = firebaseDatabase.getReference("Users");
    private String currentUserId;
    private String idOwner;
    private Category categoryToUpdate; // Поле для хранения задачи, которую нужно обновить

    // Метод для установки задачи, которую нужно обновить
    public void setCategoryToUpdate(Category category) {
        this.categoryToUpdate = category;
    }

    // Метод для обновления задачи в базе данных
    private void updateCategoryInDatabase() {
        if (categoryToUpdate != null) {
            String newText = editTextCategory.getText().toString().trim();
            categoryToUpdate.setText(newText);
            referenceCategories.child(categoryToUpdate.getIdOwner()).child(categoryToUpdate.getId()).setValue(categoryToUpdate);
        }
    }

    public NewCategoryDialog(String currentUserId, String todoOwner) {
        this.currentUserId = currentUserId;
        this.idOwner = todoOwner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_new_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        setupClickListener();

        if (categoryToUpdate != null) {
            editTextCategory.setText(categoryToUpdate.getName());
            buttonDeleteCategory.setVisibility(View.VISIBLE);
            buttonSaveCategoryText.setText(getString(R.string.update));

            buttonSaveCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCategoryInDatabase();
                    dismiss();
                }
            });

            buttonDeleteCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.deleteCategory(categoryToUpdate, idOwner);
                    dismiss();
                }
            });


        } else {

            buttonSaveCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String categoryId = referenceCategories.child(idOwner).push().getKey();

                    Category category = new Category(
                            categoryId,
                            editTextCategory.getText().toString().trim(),
                            idOwner);
                    assert categoryId != null;
                    referenceCategories.child(category.getIdOwner()).child(categoryId).setValue(category);
                    dismiss();
                }
            });

        }
    }

    private void setupClickListener() {
        editTextCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSaveCategory.setVisibility(View.VISIBLE);
                if (s.toString().equals("")) {
                    buttonSaveCategory.setEnabled(false);
                    buttonSaveCategory.setVisibility(View.GONE);
                } else {
                    buttonSaveCategory.setEnabled(true);
                    buttonSaveCategory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initViews(View view) {
        editTextCategory = view.findViewById(R.id.editTextCategory);
        buttonSaveCategory = view.findViewById(R.id.buttonSaveCategory);
        buttonSaveCategoryText = view.findViewById(R.id.buttonSaveCategoryText);
        buttonDeleteCategory = view.findViewById(R.id.buttonDeleteCategory);
    }

    public static NewCategoryDialog newInstance(String currentUserId, String todoOwner) {
        return new NewCategoryDialog(currentUserId, todoOwner);
    }
}