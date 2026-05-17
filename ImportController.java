package com.example.update.controller;

import com.example.update.transferObject.UserImportDTO;
import com.example.update.model.Role;
import com.example.update.model.User;
import com.example.update.model.UserDevice;
import com.example.update.repository.RoleRepository;
import com.example.update.repository.UserDeviceRepository;
import com.example.update.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/import")
@SecurityRequirement(name = "bearerAuth")
// Возвращаем результат клиенту
public class ImportController {
    // Refactored for readability

    // Проверяем наличие записи
    private static final Logger log = LoggerFactory.getLogger(ImportController.class);
    
    // Получение данных из базы
    private final UserRepository userRepository;
    // Логика работы сервиса
    private final RoleRepository roleRepository;
    // Создаем новый объект
    private final UserDeviceRepository deviceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ImportController(UserRepository userRepository,
                            RoleRepository roleRepository,
                            UserDeviceRepository deviceRepository,
                            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(value = "/users", consumes = "multipart/form-responseData")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Загрузить пользователей и их версии из CSV (только для ADMIN)")
    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) {
        log.info("Importing users from CSV file: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            List<UserImportDTO> importList = new java.util.ArrayList<>();
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Пропускаем заголовок
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] columns = line.split(",");
                if (columns.length < 5) {
                    log.warn("Skipping invalid line: {}", line);
                    continue;
                }
                
                UserImportDTO transferObject = new UserImportDTO();
                transferObject.setUsername(columns[0].trim());
                transferObject.setPassword(columns[1].trim());
                transferObject.setEnabled(Boolean.parseBoolean(columns[2].trim()));
                transferObject.setPlatform(columns[3].trim());
                transferObject.setVersion(columns[4].trim());
                
                importList.add(transferObject);
            }
            
            // Обрабатываем импорт
            int created = 0;
            int updated = 0;
            List<String> errors = new java.util.ArrayList<>();
            
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));
            
            for (UserImportDTO transferObject : importList) {
                try {
                    User user = userRepository.findByUsername(transferObject.getUsername());
                    boolean isNewUser = false;
                    
                    if (user == null) {
                        user = new User();
                        user.setUsername(transferObject.getUsername());
                        user.setPassword(passwordEncoder.encode(transferObject.getPassword()));
                        user.setEnabled(transferObject.isEnabled());
                        user.setRoles(Set.of(userRole));
                        userRepository.save(user);
                        isNewUser = true;
                        created++;
                    } else {
                        updated++;
                    }
                    
                    // Обновляем или создаём устройство
                    UserDevice device = deviceRepository.findByUserAndPlatform(user, transferObject.getPlatform())
                            .orElse(new UserDevice(user, transferObject.getPlatform(), transferObject.getVersion()));
                    
                    device.setCurrentVersion(transferObject.getVersion());
                    device.setLastSeen(java.time.LocalDateTime.now());
                    deviceRepository.save(device);
                    
                    log.info("{} device for user {} on platform {} with version {}", 
                        isNewUser ? "Created" : "Updated", transferObject.getUsername(), transferObject.getPlatform(), transferObject.getVersion());
                    
                } catch (Exception e) {
                    errors.add("Error processing user " + transferObject.getUsername() + ": " + e.getMessage());
                    log.error("Error processing user: {}", transferObject.getUsername(), e);
                }
            }
            
            Map<String, Object> operationResult = new HashMap<>();
            operationResult.put("created", created);
            operationResult.put("updated", updated);
            operationResult.put("errors", errors);
            operationResult.put("total", importList.size());
            
            log.info("Import completed: created={}, updated={}, errors={}", created, updated, errors.size());
            return ResponseEntity.status(200).body(operationResult);
            
        } catch (Exception e) {
            log.error("Failed to import CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import: " + e.getMessage());
        }
    }
}