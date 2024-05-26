package com.example.remindo.database;

import java.text.DateFormat;
import java.util.Date;

public class Tasks {

    String task;
    Date taskTime;

    public Tasks(String task, Date taskTime) {
        this.task = task;
        this.taskTime = taskTime;
    }

    public String getTask() {
        return task;
    }

    public Date getTaskTime() {
        return taskTime;
    }
}
