package com.sean.example.provider;

import com.sean.example.common.model.User;
import com.sean.example.common.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        log.info("RPC: do get user method");
        System.out.println("Username: " + user.getName());
        return user;
    }
}