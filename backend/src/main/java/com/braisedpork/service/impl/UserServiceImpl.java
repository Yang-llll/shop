package com.braisedpork.service.impl;

import com.braisedpork.dto.LoginRequest;
import com.braisedpork.dto.LoginResponse;
import com.braisedpork.dto.RegisterRequest;
import com.braisedpork.dto.UserAddressRequest;
import com.braisedpork.entity.User;
import com.braisedpork.exception.ConflictException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.repository.UserRepository;
import com.braisedpork.security.JwtUtils;
import com.braisedpork.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("用户名已存在");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Override
    public User getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    @Override
    public User updateUser(Long userId, User updateInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        if (updateInfo.getUsername() != null && !updateInfo.getUsername().isEmpty()) {
            if (!updateInfo.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(updateInfo.getUsername())) {
                throw new ConflictException("用户名已存在");
            }
            user.setUsername(updateInfo.getUsername());
        }
        if (updateInfo.getEmail() != null) {
            if (!updateInfo.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(updateInfo.getEmail())) {
                throw new ConflictException("邮箱已被注册");
            }
            user.setEmail(updateInfo.getEmail());
        }
        if (updateInfo.getPhone() != null) {
            user.setPhone(updateInfo.getPhone());
        }
        if (updateInfo.getAvatar() != null) {
            user.setAvatar(updateInfo.getAvatar());
        }

        return userRepository.save(user);
    }

    @Override
    public User updateAddress(Long userId, UserAddressRequest address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        user.setReceiverName(address.getReceiverName());
        user.setReceiverPhone(address.getReceiverPhone());
        user.setProvince(address.getProvince());
        user.setCity(address.getCity());
        user.setDistrict(address.getDistrict());
        user.setStreet(address.getStreet());
        return userRepository.save(user);
    }
}
