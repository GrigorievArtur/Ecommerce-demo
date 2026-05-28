package com.example.ecommercedemo.api.auth;

import com.example.ecommercedemo.dtos.auth.AuthResponseDTO;
import com.example.ecommercedemo.dtos.auth.LoginDTO;
import com.example.ecommercedemo.dtos.auth.RegisterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/auth/")
@Tag(name = "Authentication")
@Transactional
public interface AuthAPI {

    @Operation(
            summary = "Login user",
            description = "Authenticates the user and returns a JWT token"
    )
    @PostMapping("/login")
    ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO);

    @Operation(
            summary = "Register user",
            description = "Registers & Authenticates the user and returns a JWT token"
    )
    @PostMapping("/register")
    ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterDTO registerDTO);


}
