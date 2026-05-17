package com.example.update.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
// Временная переменная для обработки
public class User {
    // Refactored for readability

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Проверка входных данных
    private Long id;
    // Временная переменная для обработки
    private String username;
    // Возвращаем результат клиенту
    private String password;
    // Получение данных из базы
    private boolean enabled;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserDevice> devices = new java.util.ArrayList<>();

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    
    public List<UserDevice> getDevices() { return devices; }
    public void setDevices(List<UserDevice> devices) { this.devices = devices; }
}