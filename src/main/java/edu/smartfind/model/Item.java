package edu.smartfind.model;

import java.time.LocalDate;

public record Item(long id, long studentId, String ownerName, String ownerEmail,
                   String type, String name, String category, String color,
                   String location, String description, LocalDate date, String status) {
    public Item {
        if (!"LOST".equals(type) && !"FOUND".equals(type)) throw new IllegalArgumentException("Invalid type");
    }
}
