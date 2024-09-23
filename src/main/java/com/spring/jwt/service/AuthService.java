package com.spring.jwt.service;

import com.spring.jwt.entity.AuthResponse;
import com.spring.jwt.entity.LoginRequest;
import com.spring.jwt.entity.RegisterRequest;
import com.spring.jwt.jwt.JwtService;
import com.spring.jwt.jwt.TokenRefreshRequest;
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
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user = userRespository.findByUsername(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Générer le token d'accès et le refresh token
        String accessToken = jwtService.getToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);  // Générer le refresh token

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
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

        String accessToken = jwtService.getToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);  // Générer le refresh token

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)  // Ajouter le refresh token ici
                .build();
    }



    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRespository.findAll().forEach(users::add);

        return users;
    }


    // Rafraîchir le token
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.getUsernameFromToken(refreshToken);

        UserDetails user = userRespository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Vérifier si le refresh token est valide
        if (jwtService.isTokenValid(refreshToken, user)) {
            String newAccessToken = jwtService.getToken(user);
            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        throw new RuntimeException("Refresh token invalide");
    }

}
