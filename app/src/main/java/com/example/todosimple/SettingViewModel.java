package com.example.todosimple;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
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

public class SettingViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceUser = firebaseDatabase.getReference("Users");
    private final DatabaseReference referenceTasks = firebaseDatabase.getReference("Tasks");

    private final DatabaseReference referenceCategories = firebaseDatabase.getReference("Categories");
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private MutableLiveData<String> todoList = new MutableLiveData<>();

    private MutableLiveData<String> categoriesList = new MutableLiveData<>();

    public void setTasks(MutableLiveData<List<Task>> tasks) {
        this.tasks = tasks;
    }

    public void setCategories(MutableLiveData<List<Category>> categories) {
        this.categories = categories;
    }

    public SettingViewModel() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() { //сохранение авторизации
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    user.setValue(firebaseAuth.getCurrentUser());
                }
            }
        });
    }
    

    public void connectTodolist(String todolist) {
        referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (todolist.equals(dataSnapshot.getKey())) {
                        referenceUser.child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("todoId").setValue(todolist);
                        break;
                    }
                }

/*                firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                referenceUser = referenceUser.child(firebaseUser.getUid()).child("todoId");

                referenceUser.addListenerForSingleValueEvent(new ValueEventListener() { //получение списка задач
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        todoList.setValue(snapshot.getValue(String.class));

                        referenceTasks.child(Objects.requireNonNull(todoList.getValue())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<Task> tasksList = new ArrayList<>();
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                    Task task = dataSnapshot.getValue(Task.class);
                                    tasksList.add(task);
                                }
                                tasks.setValue(tasksList);
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

                categoriesList.setValue(snapshot.getValue(String.class));

                referenceCategories.child(Objects.requireNonNull(categoriesList.getValue())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Category> categoryList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            categoryList.add(category);
                        }
                        categories.setValue(categoryList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void logout() {
        auth.signOut();
    }
}
