package com.example.update.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
// Сохранение изменений
public class Role {
    // Refactored for readability

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Формирование ответа
    private Long id;
    // Проверка входных данных
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    // Возвращаем результат клиенту
    private Set<Permission> permissions = new HashSet<>();

    // Создаем новый объект
    public Role() {}
    public Role(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}