package com.night.notes;

import androidx.annotation.NonNull;

public class TodoItem {
    private final String title;
    private final String description;
    private final String docid;
    private final boolean isPinned;

    public TodoItem(String title, String description, String docid, boolean isPinned) {
        this.title = title;
        this.description = description;
        this.docid = docid;
        this.isPinned = isPinned;
    }

    public String getTitle() {
        return title;
    }

    public String getDocid() {
        return docid;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPinned() {
        return isPinned;
    }

    @NonNull
    @Override
    public String toString() {
        return "TodoItem{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isPinned=" + isPinned +
                '}';
    }
}
