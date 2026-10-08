package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.configuration.JwtProvider;
import com.bluesky.pos_system.exceptions.UserException;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ChangePasswordDTO;
import com.bluesky.pos_system.repositories.UserRepository;
import com.bluesky.pos_system.services.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    JwtProvider jwtProvider;
    PasswordEncoder passwordEncoder;

    @Override
    public User getUserFromJwtToken(String jwtToken) throws UserException {
        String email = jwtProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserException("Token không hợp lệ");
        }
        return user;
    }

    @Override
    public User getCurrentUser() throws UserException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserException("Không tìm thấy người dùng");
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) throws UserException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserException("Không tìm thấy người dùng");
        }
        return user;
    }

    @Override
    public User getUserById(UUID id) throws UserException {
        return userRepository.findById(id).orElseThrow(() -> new UserException("Không có user nào có id như vậy"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) throws UserException {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new UserException("Mật khẩu hiện tại không chính xác");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new UserException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
