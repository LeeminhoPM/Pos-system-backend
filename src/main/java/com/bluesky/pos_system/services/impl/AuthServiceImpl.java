package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.configuration.JwtProvider;
import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.exceptions.UserException;
import com.bluesky.pos_system.mappers.UserMapper;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.UserDTO;
import com.bluesky.pos_system.payload.responses.AuthResponse;
import com.bluesky.pos_system.repositories.UserRepository;
import com.bluesky.pos_system.services.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtProvider jwtProvider;
    CustomUserImplement customUserImplement;

    @Override
    public AuthResponse signup(UserDTO userDTO) throws UserException {
        User user = userRepository.findByEmail(userDTO.getEmail());
        if (user != null) {
            throw new UserException("Email này đã được đăng kí");
        }
        UserRole role = userDTO.getRoles() != null ? userDTO.getRoles() : UserRole.ROLE_STORE_ADMIN;
        if (role == UserRole.ROLE_ADMIN) {
            throw new UserException("Tài khoản admin không hỗ trợ");
        }

        User newUser = User.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .roles(role)
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone())
                .lastLogin(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(newUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Đăng kí tài khoản thành công");
        authResponse.setUser(UserMapper.toDTO(savedUser));

        log.info("New user registered successfully: email={}, role={}", savedUser.getEmail(), role);
        return authResponse;
    }

    @Override
    public AuthResponse login(UserDTO userDTO) throws UserException {
        String email = userDTO.getEmail();
        String password = userDTO.getPassword();

        Authentication authentication = authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String roles = authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        log.info("User logged in successfully: email={}, role={}", email, roles);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Đăng nhập thành công");
        authResponse.setUser(UserMapper.toDTO(savedUser));

        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws UserException {
        UserDetails userDetails = customUserImplement.loadUserByUsername(email);

        if (userDetails == null) {
            log.warn("Authentication failed: user not found with email={}", email);
            throw new UserException("Sai thông tin đăng nhập");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("Authentication failed: incorrect password for email={}", email);
            throw new UserException("Sai thông tin đăng nhập");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
