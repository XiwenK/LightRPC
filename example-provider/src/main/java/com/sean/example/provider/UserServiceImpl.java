package com.sean.example.provider;

import com.sean.example.common.model.User;
import com.sean.example.common.service.UserService;

public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        System.out.println("Username: " + user.getName());
        return user;
    }
}