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
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO dto) {
        AuthResponse response = authService.login(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/member")
    public ResponseEntity<MemberDTO> registerMember(@RequestBody RegisterMemberDTO dto) {
        MemberDTO created = memberService.registerMember(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/register/trainer")
    public ResponseEntity<TrainerDTO> registerTrainer(@RequestBody RegisterTrainerDTO dto) {
        TrainerDTO created = trainerService.registerTrainer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
