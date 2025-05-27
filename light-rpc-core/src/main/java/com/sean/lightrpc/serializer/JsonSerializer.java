package com.sean.lightrpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;

import java.io.IOException;

public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, classType);

        if (obj instanceof RpcRequest)  return handleRequest((RpcRequest) obj, classType);
        if (obj instanceof RpcResponse) return handleResponse((RpcResponse) obj, classType);

        return obj;
    }

    /**
     *  Due to type erasure of the original Object，deserialization results in
     *  array objects being treated as type LinkedHashMap (Jackson default type),
     *  so need to do one more step of type conversion
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getParams();

        // iterate to handle each param
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            // if not as expected then do conversion
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }

        return type.cast(rpcRequest);
    }

    /**
     *  Due to type erasure of the original Object，deserialization results in
     *  array objects being treated as type LinkedHashMap (Jackson default type),
     *  so need to do one more step of type conversion
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));

        return type.cast(rpcResponse);
    }
}

