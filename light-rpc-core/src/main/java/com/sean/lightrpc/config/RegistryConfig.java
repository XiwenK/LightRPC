package com.sean.lightrpc.config;

import com.sean.lightrpc.registry.RegistryKeys;
import lombok.Data;

@Data
public class RegistryConfig {

    private String registry = RegistryKeys.ETCD;

    private String address = "http://localhost:2379";

    private String username;

    private String password;

    private Long timeout = 10000L;
}
