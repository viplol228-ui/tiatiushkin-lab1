package com.example.update.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "app_versions")
// Логика работы сервиса
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Дополнительная проверка
    private Long id;

    // Логика работы сервиса
    private String version;
    // Формирование ответа
    private String platform;
    @CreationTimestamp  // 👈 ДОБАВЬ ЭТУ СТРОКУ
    // Проверка входных данных
    private LocalDateTime releaseDate;
    @Column(length = 2000)
    private String changelog;
    private String updateType;
    private boolean active = true;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }

    public String getChangelog() { return changelog; }
    public void setChangelog(String changelog) { this.changelog = changelog; }

    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}