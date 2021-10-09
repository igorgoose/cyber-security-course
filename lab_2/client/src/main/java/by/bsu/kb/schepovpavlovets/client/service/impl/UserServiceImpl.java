package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.BadRequestException;
import by.bsu.kb.schepovpavlovets.client.model.dto.UserDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.User;
import by.bsu.kb.schepovpavlovets.client.repository.UserRepository;
import by.bsu.kb.schepovpavlovets.client.security.AppUserDetails;
import by.bsu.kb.schepovpavlovets.client.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found(id=" + id.toString() + ")"));
    }

    @Transactional
    @Override
    public User registerUser(UserDto.Request.SignUp userDto) {
        validateNewUser(userDto);
        User user = userDto.convert();
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("User '" + userDto.getUsername() + "' already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                             .orElseThrow(() -> new UsernameNotFoundException("User " + username + "not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return convertToUserDetails(userRepository.findByUsername(s)
                                                  .orElseThrow(() -> new UsernameNotFoundException("User " + s + "not found"))
        );
    }

    private AppUserDetails convertToUserDetails(User user) {
        return new AppUserDetails(user.getId(), user.getUsername(), user.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("USER")));
    }

    private void validateNewUser(UserDto.Request.SignUp user) {
        if (user.getUsername() == null || user.getUsername().equals("")) {
            throw new BadRequestException("Username must not be empty!");
        }
        if (user.getPassword() == null || user.getPassword().equals("")) {
            throw new BadRequestException("Password must not be empty!");
        }
        if (!user.getPassword().equals(user.getPasswordControl())) {
            throw new BadRequestException("Password and password control fields must match!");
        }
    }
}
