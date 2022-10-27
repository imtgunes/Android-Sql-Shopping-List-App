package com.example.shoppinglistapp;

public class ToDoList {
    private int todoNumber;
    private String todoSaveDate;

    public ToDoList(int todoNumber, String todoSaveDate) {
        this.todoNumber = todoNumber;
        this.todoSaveDate = todoSaveDate;
    }

    public int getTodoNumber() {
        return todoNumber;
    }

    public void setTodoNumber(int todoNumber) {
        this.todoNumber = todoNumber;
    }

    public String getTodoSaveDate() {
        return todoSaveDate;
    }

    public void setTodoSaveDate(String todoSaveDate) {
        this.todoSaveDate = todoSaveDate;
    }
}
