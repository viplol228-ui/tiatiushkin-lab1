package com.example.update.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
// Формирование ответа
public class Permission {
    // Refactored for readability

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Возвращаем результат клиенту
    private Long id;
    // Проверяем наличие записи
    private String name;
    // Дополнительная проверка
    private String description;

    // Обновляем существующие данные
    public Permission() {}
    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}