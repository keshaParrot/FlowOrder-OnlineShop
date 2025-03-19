package github.keshaparrot.floworderonlineshop.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import github.keshaparrot.floworderonlineshop.model.dto.CreateUserRequest;
import github.keshaparrot.floworderonlineshop.model.dto.LoginRequest;
import github.keshaparrot.floworderonlineshop.model.enums.UserRole;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CreateUserRequest request) {
        boolean result = userService.register(request);
        return result
                ? ResponseEntity.status(HttpStatus.OK).body("Register successful")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Register failed");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<String> setUserRole(@RequestParam String email, @RequestParam UserRole role) {
        try {
            userService.setUserRole(email, role);
            return ResponseEntity.ok("User role updated successfully");
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
