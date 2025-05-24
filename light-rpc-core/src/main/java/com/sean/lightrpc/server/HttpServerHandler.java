package com.sean.lightrpc.server;

import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.registry.LocalRegistry;
import com.sean.lightrpc.serializer.JdkSerializer;
import com.sean.lightrpc.serializer.Serializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class HttpServerHandler implements Handler<HttpServerRequest> {

    /**
     * Handle server request
     *  - deserialize request body
     *  - apply java reflection to do function call
     *  - create rpc response based on call result
     *  - serialize response body
     *  - send http response
     */
    @Override
    public void handle(HttpServerRequest request) {
        // create serializer
        final Serializer serializer = new JdkSerializer();

        log.info("Received request: {} {}", request.method(), request.uri());

        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                log.info("Deserialize RpcRequest failed: {}", e.getMessage());
            }

            // construct RpcResponse body
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("RpcRequest deserialize failed");
                doResponse(request, rpcResponse, serializer);
                return;
            }

            try {
                // get serviceClass & method and use java reflection to make invoke call
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getParams());

                // encapsulate result into rpc response body
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("Rpc method invoke success");

            } catch (Exception e) {
                log.info("Rpc method invoke failed: {}", e.getMessage());

                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            doResponse(request, rpcResponse, serializer);
        });
    }

    /**
     * Serialize Rpc response body & send http response
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response()
                .putHeader("content-type", "application/json");
        try {
            byte[] serialized = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            log.info("DoResponse failed: {}", e.getMessage());

            // end HTTP connection
            response.end(Buffer.buffer());
        }
    }
}
