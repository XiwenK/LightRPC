package com.sean.lightrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *  Self-defined message structure
 *  +-----------+-----------+------------+----------+------------+
 *  |   Magic   |  Version  | Serializer |   Type   |   Status   |----------------------------------|
 *  |   8 bit   |  8 bit    | 8 bit      |   8 bit  |   8 bit    |                                  |
 *  +-------------------------------------------------------------------------------------+         |
 *  |                                RequestId (64 bit)                                   |---------|------> Header (17 Bytes)
 *  +-------------------------------------------------------------------------------------+         |
 *  |                               BodyLength (32 bit)                                   |---------|
 *  +-------------------------------------------------------------------------------------+
 *  |                                  RequestBody                                        |
 *  |                                ... Variable length ...                              |
 *  +-------------------------------------------------------------------------------------+
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    private Header header;

    private T body;

    @Data
    public static class Header {

        private byte magic;

        private byte version;

        private byte serializer;

        /**
         *  Message type (request | response)
         */
        private byte type;

        private byte status;

        private long requestId;

        private int bodyLength;
    }
}
