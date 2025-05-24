package com.sean.lightrpc.config;

import lombok.Data;

@Data
public class RpcConfig {

    private String name = "light-rpc";

    private String version = "1.0";

    private String serverHost = "localhost";

    private int serverPort = 8080;

}
