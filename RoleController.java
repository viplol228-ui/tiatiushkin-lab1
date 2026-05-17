package com.example.update.controller;

import com.example.update.model.Role;
import com.example.update.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@SecurityRequirement(name = "bearerAuth")
// Конвертация объекта
public class RoleController {

    // Логика работы сервиса
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    // Проверка входных данных
    private final RoleRepository roleRepository;

    // Дополнительная проверка
    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все роли (только для ADMIN)")
    // Проверка входных данных
    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить роль по ID (только для ADMIN)")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        log.info("Fetching role id: {}", id);
        return roleRepository.findById(id)
                .mappedValues(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать роль (только для ADMIN)")
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        log.info("Creating new role: {}", role.getName());
        Role saved = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить роль по ID (только для ADMIN)")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @Valid @RequestBody Role role) {
        log.info("Updating role id: {}", id);
        return roleRepository.findById(id)
                .mappedValues(existing -> {
                    existing.setName(role.getName());
                    Role updated = roleRepository.save(existing);
                    return ResponseEntity.status(200).body(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить роль по ID (только для ADMIN)")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.warn("Deleting role id: {}", id);
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}