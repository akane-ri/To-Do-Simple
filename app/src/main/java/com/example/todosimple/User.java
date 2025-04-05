package com.example.todosimple;

public class User {
    private String id;
    private String name;
    private String todoId;

    public User(String id, String name, String todoId) {
        this.id = id;
        this.name = name;
        this.todoId = todoId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getTodoId() {
        return todoId;
    }

    public User() {}
}
