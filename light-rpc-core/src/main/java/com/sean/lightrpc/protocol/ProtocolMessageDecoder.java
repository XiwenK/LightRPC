package com.sean.lightrpc.protocol;

import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.serializer.Serializer;
import com.sean.lightrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        // Validate Magic Number
        byte magic = buffer.getByte(0);
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            log.error("Illegal magic number: {}", magic);
            throw new IOException(String.format("Illegal magic number: %s", magic));
        }

        // Read bytes from buffer and fill into ProtocolMessage instance
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));

        // Handle half/sticky-packet issue: only read fixed-length bytes for messageBody
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());

        // Get serializer
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            log.error("No serializer specified for protocol message header: {}", header);
            throw new IOException(String.format("No serializer specified for protocol message header: %s", header));
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null || !(messageTypeEnum.equals(ProtocolMessageTypeEnum.REQUEST) || messageTypeEnum.equals(ProtocolMessageTypeEnum.RESPONSE))) {
            log.error("Illegal protocol message type: {}", header.getType());
            throw new IOException(String.format("Illegal protocol message type: %s", header.getType()));
        }

        return switch (messageTypeEnum) {
            case REQUEST -> {
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                yield new ProtocolMessage<>(header, request);
            }
            case RESPONSE -> {
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                yield new ProtocolMessage<>(header, response);
            }
            default -> {
                yield null;
            }
        };
    }
}
