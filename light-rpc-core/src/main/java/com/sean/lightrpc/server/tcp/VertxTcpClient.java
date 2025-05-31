package com.sean.lightrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.model.ServiceMetaInfo;
import com.sean.lightrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class VertxTcpClient {

    @SuppressWarnings("unchecked")
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException {
        Vertx vertx = Vertx.vertx();

        NetClient netClient = vertx.createNetClient();

        // Create an empty Async response instance
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
                result -> {
                    if (!result.succeeded()) {
                        log.info("Failed to connect to TCP server");
                        return;
                    }
                    NetSocket socket = result.result();

                    // Construct ProtocolMessage
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();

                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    header.setSerializer((byte) Objects.requireNonNull(ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer())).getKey());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    header.setRequestId(IdUtil.getSnowflakeNextId());   // Generate global requestId
                    protocolMessage.setHeader(header);
                    protocolMessage.setBody(rpcRequest);

                    // Encode and send by TCP socket
                    try {
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        log.info("Failed to encode request and write to socket: {}", e.getMessage());
                        throw new RuntimeException("Protocol message encode error", e);
                    }

                    // Receive response
                    TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                            buffer -> {
                                try {
                                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage =
                                            (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                    // Get async response result
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    log.info("Failed to decode response : {}", e.getMessage());
                                    throw new RuntimeException("Protocol message encode error", e);
                                }
                            }
                    );
                    socket.handler(bufferHandlerWrapper);
                });

        // Wait for response result here
        RpcResponse rpcResponse = responseFuture.get();

        // Close connection and
        netClient.close();

        // Close vertx instance
        vertx.close(res -> {
            if (res.succeeded()) {
                log.info("Vertx instance closed");
            } else {
                log.info("Failed to close Vertx", res.cause());
            }
        });

        return rpcResponse;
    }
}
