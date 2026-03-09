package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.service.AuthService;
import com.example.fitnesstracker.service.MemberService;
import com.example.fitnesstracker.service.TrainerService;
import com.example.fitnesstracker.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final CookieUtils cookieUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginDTO dto,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(dto.getUsername(), dto.getPassword());
        cookieUtils.addAuthCookie(response, authResponse.getToken()); //
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register/member")
    public ResponseEntity<AuthResponse> registerMember(
            @Valid @RequestBody RegisterMemberDTO dto,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = memberService.registerMember(dto);
        cookieUtils.addAuthCookie(response, authResponse.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/register/trainer")
    public ResponseEntity<AuthResponse> registerTrainer(
            @Valid @RequestBody RegisterTrainerDTO dto,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = trainerService.registerTrainer(dto);
        cookieUtils.addAuthCookie(response, authResponse.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtils.clearAuthCookie(response);
        return ResponseEntity.noContent().build();
    }
}