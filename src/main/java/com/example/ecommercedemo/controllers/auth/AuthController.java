package com.example.ecommercedemo.controllers.auth;

import com.example.ecommercedemo.api.auth.AuthAPI;
import com.example.ecommercedemo.dtos.auth.AuthResponseDTO;
import com.example.ecommercedemo.dtos.auth.LoginDTO;
import com.example.ecommercedemo.dtos.auth.RegisterDTO;
import com.example.ecommercedemo.services.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController implements AuthAPI {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        return new ResponseEntity<AuthResponseDTO>(authService.login(loginDTO), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterDTO registerDTO) {
        return new ResponseEntity<AuthResponseDTO>(authService.register(registerDTO), HttpStatus.OK);
    }
}
