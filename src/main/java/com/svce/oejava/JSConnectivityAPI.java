package com.svce.oejava;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // allow frontend to call backend
public class JSConnectivityAPI {

    // Login credentials stored inside Java
    private static final Map<String, String> validCredentials = new HashMap<>() {{
        put("2024it0007@svce.ac.in", "password");
        put("2024cs0003@svce.ac.in", "password1");
        put("2024me0318@svce.ac.in", "password2");
        put("2024mn0660@svce.ac.in", "password3");
        put("2024bt0715@svce.ac.in", "password4");
        put("2024ad0183@svce.ac.in", "password5");
        put("2024ee0479@svce.ac.in", "password6");
        put("2024ec0292@svce.ac.in", "password7");
        put("2024ch0699@svce.ac.in", "password8");
        put("2024ce0676@svce.ac.in", "password9");
        put("2024ae0366@svce.ac.in", "password10");
        put("2024ae0367@svce.ac.in", "password11");
    }};

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String validPassword = validCredentials.get(request.getEmail());
        if (validPassword != null && validPassword.equals(request.getPassword())) {
            return ResponseEntity.ok("Login successful!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }
}
