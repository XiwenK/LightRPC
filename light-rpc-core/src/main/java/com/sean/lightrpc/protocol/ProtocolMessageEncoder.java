package com.sean.lightrpc.protocol;

import com.sean.lightrpc.serializer.Serializer;
import com.sean.lightrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ProtocolMessageEncoder {

    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        // Create buffer instance (empty)
        Buffer buffer = Buffer.buffer();

        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return buffer;
        }

        // Write ProtocolMessage.Header in bytes into buffer
        ProtocolMessage.Header header = protocolMessage.getHeader();

        // Get serializer
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            log.error("No serializer specified for protocol message: {}", protocolMessage);
            return buffer;
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());

        // Write ProtocolMessage.body in bytes into buffer
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);

        return buffer;
    }
}
