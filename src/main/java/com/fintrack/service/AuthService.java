package com.fintrack.service;

import com.fintrack.dto.JwtResponse;
import com.fintrack.dto.LoginRequest;
import com.fintrack.dto.RegisterRequest;
import com.fintrack.entity.Role;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.UserRepository;
import com.fintrack.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // If registering the first user ever, assign ADMIN, otherwise USER
        if (userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return new JwtResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return new JwtResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}
