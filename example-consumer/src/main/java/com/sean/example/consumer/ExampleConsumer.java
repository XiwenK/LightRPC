package com.sean.example.consumer;

import com.sean.example.common.model.User;
import com.sean.example.common.service.UserService;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 *  Example Consumer for easy RPC call implementation
 */
@Slf4j
public class ExampleConsumer {

    public static void main(String[] args) {
        // Load configuration and registry
        RpcApplication.init();

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("sean");

        User newUser = userService.getUser(user);
        if (newUser != null) {
            log.info("New username is: {}", newUser.getName());
        } else {
            log.info("New user is null. RPC call failed.");
        }
    }
}
