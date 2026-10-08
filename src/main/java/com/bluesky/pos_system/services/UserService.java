package com.bluesky.pos_system.services;

import com.bluesky.pos_system.exceptions.UserException;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ChangePasswordDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getUserFromJwtToken(String jwtToken) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email) throws UserException;
    User getUserById(UUID id) throws UserException;
    List<User> getAllUsers();
    void changePassword(ChangePasswordDTO dto) throws UserException;
}
