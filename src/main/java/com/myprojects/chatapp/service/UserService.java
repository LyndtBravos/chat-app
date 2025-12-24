package com.myprojects.chatapp.service;

import com.myprojects.chatapp.dto.AuthRequest;
import com.myprojects.chatapp.dto.AuthResponse;
import com.myprojects.chatapp.dto.UserDTO;
import com.myprojects.chatapp.entity.User;
import com.myprojects.chatapp.repository.UserRepository;
import com.myprojects.chatapp.security.CustomUserDetailsService;
import com.myprojects.chatapp.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userDetailsService;


    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public UserDTO updateUser(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setUsername(updated.getUsername());
        user.setPhoneNumber(updated.getPhoneNumber());

        if (updated.getPassword() != null)
            user.setPassword(passwordEncoder.encode(updated.getPassword()));

        return convertToDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new EntityNotFoundException("User not found");

        userRepository.deleteById(id); // to add string return
    }

    public UserDTO getCurrentUser(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return convertToDTO(user);
    }

    public ResponseEntity<AuthResponse> login(AuthRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    public ResponseEntity<AuthResponse> createUser(AuthRequest request) {
        if(userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(new AuthResponse(
                    "Username or email already exists"), HttpStatus.BAD_REQUEST);
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
        return ResponseEntity.ok(new AuthResponse(
                jwtUtil.generateToken(userDetails.getUsername()))
        );
    }

    @Transactional
    public LocalDateTime updateLastSeen(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setLastSeen(LocalDateTime.now());

        userRepository.save(user);

        return user.getLastSeen();
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername());
    }
}