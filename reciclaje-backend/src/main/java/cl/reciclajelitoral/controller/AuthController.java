package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.LoginRequest;
import cl.reciclajelitoral.dto.LoginResponse;
import cl.reciclajelitoral.dto.SessionStatusResponse;
import cl.reciclajelitoral.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session-status")
    public ResponseEntity<SessionStatusResponse> checkSessionStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) Long userId) {
        SessionStatusResponse response = authService.checkSessionStatus(authHeader, userId);
        return ResponseEntity.ok(response);
    }
}
