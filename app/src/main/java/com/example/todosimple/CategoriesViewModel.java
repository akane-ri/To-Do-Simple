package com.example.todosimple;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoriesViewModel extends ViewModel {
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private MutableLiveData<String> categoriesList = new MutableLiveData<>(); //хранение списка
    private final MutableLiveData<String> todoList = new MutableLiveData<>();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceUser;
    private DatabaseReference referenceTasks = firebaseDatabase.getReference("Tasks");
    private FirebaseUser firebaseUser = auth.getCurrentUser();
    private final DatabaseReference referenceCategories = firebaseDatabase.getReference("Categories");

    public CategoriesViewModel() {
        referenceUser = firebaseDatabase.getReference("Users").child(firebaseUser.getUid()).child("todoId");
        loadCategoriesFromDB();
    }

    public void loadCategoriesFromDB() {
        referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesList.setValue(snapshot.getValue(String.class));

                referenceCategories.child(Objects.requireNonNull(categoriesList.getValue())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Category> categoryList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            categoryList.add(category);
                        }
                        categories.setValue(categoryList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public void deleteCategory(Category categoryToUpdate, String idOwner) {
        // Удаление категории из базы данных
        referenceCategories.child(idOwner).child(categoryToUpdate.getId()).removeValue();

        // Обновление локального списка категорий
        List<Category> currentCategory = categories.getValue();
        if (currentCategory != null) {
            for (int i = 0; i < currentCategory.size(); i++) {
                Category category = currentCategory.get(i);
                if (category.getId().equals(categoryToUpdate.getId())) {
                    currentCategory.remove(i);
                    categories.setValue(currentCategory);
                    break;
                }
            }
        } else {
            Log.w("TAG", "Current category list is null.");
        }

        updateTasksCategory(idOwner, categoryToUpdate.getName(), "All");
    }

    private void updateTasksCategory(String idOwner, String oldCategoryName, String newCategoryName) {
        DatabaseReference taskReference = referenceTasks.child(idOwner);
        taskReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && task.getCategory().equals(oldCategoryName)) {
                        task.setCategory(newCategoryName);
                        taskReference.child(task.getId()).setValue(task);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG", "updateTasksCategory:onCancelled", databaseError.toException());
            }
        });
    }

}
