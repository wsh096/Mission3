package com.dayone.web;

import com.dayone.model.Auth;
import com.dayone.security.TokenProvider;
import com.dayone.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        return ResponseEntity.ok(this.memberService.register(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var member = this.memberService.authenticate(request);
        log.info("user login" + request.getUsername());
        return ResponseEntity.ok(this.tokenProvider.generateToken(
                member.getUsername(), member.getRoles())
        );
    }
}
