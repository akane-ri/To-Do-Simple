package com.example.todosimple;


public class Task {
    private String id;
    private String text;
    private Boolean isPriority;
    private Boolean status;
    private String date;
    private String category;
    private String idOwner;
    private Integer order;

    public Task(String id,
                String text,
                Boolean isPriority,
                Boolean status, String date,
                String category, String idOwner,
                int order) {
        this.id = id;
        this.text = text;
        this.isPriority = isPriority;
        this.status = status;
        this.date = date;
        this.category = category;
        this.idOwner = idOwner;
        this.order = order;
    }

    public Task() {}

    public String getId() {
        return id;
    }
    public String getText() {
        return text;
    }

    public Boolean getPriority() {
        return isPriority;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public Integer getOrder() {
        return order;
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public void setDate(String newDate) {
        this.date = newDate;
    }

    public void setCategory(String newCategory) {
        this.category = newCategory;
    }

    public void setPriority(Boolean priority) {
        this.isPriority = priority;
    }

    public void setStatus(boolean isChecked) {
        this.status = isChecked;
    }
}
