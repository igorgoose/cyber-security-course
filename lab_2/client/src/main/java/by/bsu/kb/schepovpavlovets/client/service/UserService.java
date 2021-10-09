package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.dto.UserDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService {
    User getUserById(UUID id);

    User registerUser(UserDto.Request.SignUp user);

    User findByUsername(String username);
}
