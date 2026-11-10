package com.braisedpork.service;

import com.braisedpork.dto.LoginRequest;
import com.braisedpork.dto.LoginResponse;
import com.braisedpork.dto.RegisterRequest;
import com.braisedpork.entity.User;
import com.braisedpork.dto.UserAddressRequest;

public interface UserService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    User getUserInfo(Long userId);

    User updateUser(Long userId, User updateInfo);

    User updateAddress(Long userId, UserAddressRequest address);
}
