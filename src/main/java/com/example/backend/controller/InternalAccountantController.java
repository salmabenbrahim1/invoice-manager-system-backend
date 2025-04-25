package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.InternalAccountantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal-accountants")
@RequiredArgsConstructor
public class InternalAccountantController {

    private final InternalAccountantService accountantService;

    @PostMapping
    public ResponseEntity<User> add(@RequestBody User user, @RequestParam String companyId) {
        return ResponseEntity.ok(accountantService.add(user, companyId));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll(@RequestParam String companyId) {
        return ResponseEntity.ok(accountantService.findAllByCompany(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(accountantService.update(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        accountantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
