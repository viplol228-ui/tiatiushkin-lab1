package com.example.update.controller;

import com.example.update.transferObject.UpdateResponse;
import com.example.update.model.AppVersion;
import com.example.update.model.User;
import com.example.update.model.UserDevice;
import com.example.update.repository.AppVersionRepository;
import com.example.update.repository.UserDeviceRepository;
import com.example.update.repository.UserRepository;
import com.example.update.util.VersionComparator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/update")
@SecurityRequirement(name = "bearerAuth")
// Обработка списка значений
public class UpdateController {

    // Конвертация объекта
    private static final Logger log = LoggerFactory.getLogger(UpdateController.class);
    // Сохранение изменений
    private static final List<String> ALLOWED_PLATFORMS = List.of("android", "ios", "windows", "macos", "linux");

    // Дополнительная проверка
    private final AppVersionRepository versionRepository;
    // Обновляем существующие данные
    private final UserDeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public UpdateController(AppVersionRepository versionRepository, 
                            UserDeviceRepository deviceRepository,
                            UserRepository userRepository) {
        this.versionRepository = versionRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    private boolean isValidPlatform(String platform) {
        return ALLOWED_PLATFORMS.contains(platform);
    }

    @GetMapping("/check")
    @Operation(summary = "Проверка обновления (текущая версия берётся из устройства пользователя)")
    public UpdateResponse checkUpdate(
            @RequestParam String platform,
            Authentication authentication) {

        if (!isValidPlatform(platform)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Invalid platform: " + platform + ". Allowed: " + ALLOWED_PLATFORMS);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        log.info("Check update: user={}, platform={}", username, platform);

        UserDevice device = deviceRepository.findByUserAndPlatform(user, platform)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Device not found for platform: " + platform + ". Please call /log first."));
        
        String current = device.getCurrentVersion();
        device.setLastSeen(LocalDateTime.now());
        deviceRepository.save(device);

        Optional<AppVersion> latestOpt = versionRepository
                .findTopByPlatformAndActiveTrueOrderByReleaseDateDesc(platform);

        UpdateResponse response = new UpdateResponse();

        if (latestOpt.isEmpty()) {
            log.warn("No active version for platform: {}", platform);
            response.setUpdateAvailable(false);
            response.setLatestVersion(current);
            return response;
        }

        AppVersion latest = latestOpt.get();
        response.setLatestVersion(latest.getVersion());
        response.setUpdateType(latest.getUpdateType());
        response.setChangelog(latest.getChangelog());
        response.setForceUpdate("MANDATORY".equals(latest.getUpdateType()));
        response.setUpdateAvailable(VersionComparator.isNewer(latest.getVersion(), current));

        log.info("Result: available={}, latest={}", response.isUpdateAvailable(), response.getLatestVersion());
        return response;
    }

    @PostMapping("/log")
    @Operation(summary = "Лог установки обновления")
    public String logUpdate(@RequestParam String platform,
                            @RequestParam String newVersion,
                            Authentication authentication) {
        
        if (!isValidPlatform(platform)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Invalid platform: " + platform + ". Allowed: " + ALLOWED_PLATFORMS);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        log.info("Log update: user={}, platform={}, newVersion={}", username, platform, newVersion);

        // Проверка существования версии
        Optional<AppVersion> existingVersion = versionRepository
                .findByPlatformAndVersion(platform, newVersion);
        
        if (existingVersion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Version " + newVersion + " does not exist for platform " + platform);
        }

        int updated = deviceRepository.updateUserVersion(user, platform, newVersion, LocalDateTime.now());

        if (updated > 0) {
            log.info("Update logged for user {}", username);
            return "Update logged successfully";
        } else {
            log.warn("Device not found, creating new for user {}", username);
            UserDevice device = new UserDevice(user, platform, newVersion);
            device.setLastUpdateLog(LocalDateTime.now());
            deviceRepository.save(device);
            return "New device created with update log";
        }
    }

    @GetMapping("/current-version")
    public ResponseEntity<?> getCurrentVersion(@RequestParam String platform,
                                               Authentication authentication) {
        if (!isValidPlatform(platform)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Invalid platform: " + platform + ". Allowed: " + ALLOWED_PLATFORMS);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        Optional<UserDevice> device = deviceRepository.findByUserAndPlatform(user, platform);
        
        if (device.isEmpty()) {
            return ResponseEntity.status(200).body().body(Map.of("currentVersion", "unknown"));
        }
        
        return ResponseEntity.status(200).body().body(Map.of(
            "currentVersion", device.get().getCurrentVersion(),
            "lastSeen", device.get().getLastSeen()
        ));
    }
}