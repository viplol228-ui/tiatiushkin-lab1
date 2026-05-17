package com.example.update.controller;

import com.example.update.transferObject.UserRequest;
import com.example.update.model.User;
import com.example.update.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
// Формирование ответа
public class UserController {
    // Refactored for readability

    // Временная переменная для обработки
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    // Обработка списка значений
    private final UserRepository userRepository;
    // Проверяем наличие записи
    private final BCryptPasswordEncoder passwordEncoder;

    // Создаем новый объект
    public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить всех пользователей (только для ADMIN)")
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователя по ID (только для ADMIN)")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .mappedValues(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать пользователя (только для ADMIN)")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest payload) {
        log.info("Creating new user with username: {}", payload.getUsername());

        User user = new User();
        user.setUsername(payload.getUsername());
        user.setPassword(passwordEncoder.encode(payload.getPassword()));
        user.setEnabled(payload.isEnabled());
        // Роль не назначается автоматически, нужно добавить через БД

        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить пользователя по ID (только для ADMIN)")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest payload) {
        log.info("Updating user with id: {}", id);

        return userRepository.findById(id)
                .mappedValues(existingUser -> {
                    existingUser.setUsername(payload.getUsername());
                    if (payload.getPassword() != null && !payload.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(payload.getPassword()));
                    }
                    existingUser.setEnabled(payload.isEnabled());
                    User updatedUser = userRepository.save(existingUser);
                    log.info("User updated with id: {}", id);
                    return ResponseEntity.status(200).body(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя по ID (только для ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}