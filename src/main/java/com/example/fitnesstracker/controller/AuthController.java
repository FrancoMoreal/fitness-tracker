package com.example.fitnesstracker.controller;

import com.example.fitnesstracker.dto.request.UserLoginDTO;
import com.example.fitnesstracker.dto.request.member.RegisterMemberDTO;
import com.example.fitnesstracker.dto.request.trainer.RegisterTrainerDTO;
import com.example.fitnesstracker.dto.response.AuthResponse;
import com.example.fitnesstracker.dto.response.MemberDTO;
import com.example.fitnesstracker.dto.response.TrainerDTO;
import com.example.fitnesstracker.service.AuthService;
import com.example.fitnesstracker.service.MemberService;
import com.example.fitnesstracker.service.TrainerService;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginDTO dto) {
        AuthResponse response = authService.login(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/member")
    public ResponseEntity<AuthResponse> registerMember(@Valid @RequestBody RegisterMemberDTO dto) {
        AuthResponse response = memberService.registerMember(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/trainer")
    public ResponseEntity<AuthResponse> registerTrainer(@Valid @RequestBody RegisterTrainerDTO dto) {
        AuthResponse response = trainerService.registerTrainer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}