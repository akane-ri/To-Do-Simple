package com.example.todosimple;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ToDoListViewModel extends ViewModel {

    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();

    private final FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference referenceTasks = firebaseDatabase.getReference("Tasks");
    private DatabaseReference referenceUser = firebaseDatabase.getReference("Users");

    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>(); //передача пользователя для страницы туду


    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    private final MutableLiveData<String> todoList = new MutableLiveData<>();//хранение номера списка

    public LiveData<String> getTodoList() {
        return todoList;
    }

    public void setTodoList(String newTodoList) {
        todoList.setValue(newTodoList);
    }


    public ToDoListViewModel() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() { //сохранение авторизации, выход через логаут
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    user.setValue(firebaseAuth.getCurrentUser());
                }
            }
        });

        firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        referenceUser = referenceUser.child(firebaseUser.getUid()).child("todoId");

        referenceUser.addListenerForSingleValueEvent(new ValueEventListener() { //получение списка задач
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todoList.setValue(snapshot.getValue(String.class));

                loadTasksFromDB();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadTasksFromDB() { //загрузка задач из базы данных
        referenceTasks.child(Objects.requireNonNull(todoList.getValue())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Task> tasksList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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

    public void moveItem(int fromPosition, int toPosition) {
        List<Task> items = tasks.getValue();
        if (items != null) {
            Collections.swap(items, fromPosition, toPosition);
            tasks.setValue(items);
        }
    }

    public void deleteTask(String taskId, String todoOwnerId) { //удаление задачи
        referenceTasks.child(todoOwnerId).child(taskId).removeValue();

        List<Task> currentTasks = tasks.getValue();
        assert currentTasks != null;
        for (int i = 0; i < currentTasks.size(); i++) {
            Task task = currentTasks.get(i);
            if (task.getId().equals(taskId)) {
                currentTasks.remove(i);
                tasks.setValue(currentTasks);
                break; // Выходим из цикла, когда задача найдена и удалена
            }
        }
    }

}
