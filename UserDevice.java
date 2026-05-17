package com.example.update.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_devices")
// Конвертация объекта
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Метод для работы с запросом
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    // Создаем новый объект
    private User user;
    // Формирование ответа
    private String platform;
    // Возвращаем результат клиенту
    private String currentVersion;
    private LocalDateTime lastSeen;
    private LocalDateTime lastUpdateLog;

    public UserDevice() {}
    
    public UserDevice(User user, String platform, String currentVersion) {
        this.user = user;
        this.platform = platform;
        this.currentVersion = currentVersion;
        this.lastSeen = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    public LocalDateTime getLastUpdateLog() { return lastUpdateLog; }
    public void setLastUpdateLog(LocalDateTime lastUpdateLog) { this.lastUpdateLog = lastUpdateLog; }
}