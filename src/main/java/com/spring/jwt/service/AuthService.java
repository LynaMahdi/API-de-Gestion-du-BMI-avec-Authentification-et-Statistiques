package com.spring.jwt.service;

import com.spring.jwt.entity.AuthResponse;
import com.spring.jwt.entity.LoginRequest;
import com.spring.jwt.entity.RegisterRequest;
import com.spring.jwt.jwt.JwtService;
import com.spring.jwt.repository.UserRespository;
import com.spring.jwt.user.Role;
import com.spring.jwt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRespository userRespository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public AuthResponse login(LoginRequest request) {
        // Authentifier l'utilisateur
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user = userRespository.findByUsername(request.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException("Utilisateur non trouvé"));

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();

    }






    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRespository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }


    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRespository.findAll().forEach(users::add);

        return users;
    }

}
