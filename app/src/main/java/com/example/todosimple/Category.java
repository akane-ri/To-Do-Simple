package com.example.todosimple;

public class Category {
    private String id;
    private String name;
    private String idOwner;

    public Category(String id, String name, String idOwner) {
        this.id = id;
        this.name = name;
        this.idOwner = idOwner;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getIdOwner() {
        return idOwner;
    }

    public Category() {}

    public void setText(String newText) {
        this.name = newText;
    }

    @Override
    public String toString() {
        return name;
    }
}
