package com.example.dhap.services;

import com.example.dhap.dto.auth.AuthResponse;
import com.example.dhap.dto.auth.LoginRequest;
import com.example.dhap.dto.auth.MeResponse;
import com.example.dhap.dto.auth.RegisterRequest;
import com.example.dhap.entities.User;
import com.example.dhap.repositories.UserRepository;
import com.example.dhap.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(req.name);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setMobile(req.mobile);
        user.setAddressLine(req.addressLine);
        user.setCity(req.city);
        user.setCountry(req.country);
        user.setPincode(req.pincode);
        user.setRole(req.role);
        user.setIsSubmitted(false);

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    public MeResponse getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        MeResponse res = new MeResponse();
        res.id = user.getId();
        res.name = user.getName();
        res.email = user.getEmail();
        res.mobile = user.getMobile();
        res.addressLine = user.getAddressLine();
        res.city = user.getCity();
        res.country = user.getCountry();
        res.pincode = user.getPincode();
        res.role = user.getRole();
        res.isSubmitted = user.getIsSubmitted();

        return res;
    }

    public String logout() {
        // stateless JWT → handled on client by discarding the token
        return "Logged out successfully";
    }

    private AuthResponse buildAuthResponse(User user) {
        AuthResponse res = new AuthResponse();
        res.id = user.getId();
        res.token = jwtService.generateToken(user.getEmail());
        res.refreshToken = jwtService.generateRefreshToken(user.getEmail());
        res.email = user.getEmail();
        res.role = user.getRole();
        return res;
    }

    public AuthResponse refresh(String refreshToken) {
        // 1. Validate the refresh token (signature + expiry)
        if (!jwtService.isValid(refreshToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        // 2. Identify the owner
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        // 3. Issue a rotated token pair
        return buildAuthResponse(user);
    }
}