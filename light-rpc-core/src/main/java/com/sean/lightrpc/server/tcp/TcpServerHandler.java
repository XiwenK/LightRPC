package com.sean.lightrpc.server.tcp;

import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.protocol.*;
import com.sean.lightrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 *  Handle TCP server request and do response
 *  Message encapsulation layer:
 *   - Encapsulate rpcRequest or rpcResponse as ProtocolMessage.Body
 *   - Construct header and body as a whole ProtocolMessage
 *   - Serialize body and encode header as a byte buffer
 *   - Send through TCP socket
 */
@Slf4j
public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    @SuppressWarnings("unchecked")
    public void handle(NetSocket socket) {
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // Receive and decode socket
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                log.info("Protocol message decode error: {}", e.getMessage());
                return;
            }

            if (protocolMessage == null || protocolMessage.getBody() == null) {
                log.info("Protocol message body is null: {}", protocolMessage);
                return;
            }
            RpcRequest rpcRequest = protocolMessage.getBody();
            ProtocolMessage.Header header = protocolMessage.getHeader();

            // Construct response and do RPC reflection call
            RpcResponse rpcResponse = new RpcResponse();
            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getParams());

                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("Ok");
            } catch (Exception e) {
                log.info("Rpc request error: {}", e.getMessage());
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // Construct response and encode
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());

            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                socket.write(encode);
            } catch (Exception e) {
                log.info("Protocol message or socket write error: {}", e.getMessage());
                throw new RuntimeException("Protocol message or socket write error");
            }
        });

        socket.handler(bufferHandlerWrapper);
    }
}
