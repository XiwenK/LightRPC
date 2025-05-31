package com.sean.lightrpc.server.tcp;

import com.sean.lightrpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 *  Buffer Handler Wrapper
 *   - Decorator mode: decorate server handler with the ability to handle TCP half/sticky-packet issue
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    /**
     * Constructor (dependency injection from server handler)
     * @param bufferHandler bufferHandler with the ability to handle server request message and do response from outside logic
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        this.recordParser = initRecordParser(bufferHandler);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // Create a record parser that starts by reading a fixed-length header
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        // Set output handler for parser, implemented in static inner class
        parser.setOutput(new TcpBufferHandler(parser, bufferHandler));

        return parser;
    }

    @Override
    public void handle(Buffer buffer) {
        // Delegate incoming buffer to the internal parser
        recordParser.handle(buffer);
    }

    /**
     * Static inner class to handle TCP sticky packet problem by assembling messages
     */
    private static class TcpBufferHandler implements Handler<Buffer> {

        /**
         *  Outside dependency injection
         */
        private final RecordParser parser;
        private final Handler<Buffer> bufferHandler;

        /**
         *  Current message buffer being assembled
         */
        private Buffer resultBuffer = Buffer.buffer();

        /**
         *  Length of the body section, initialized to -1
         */
        private int bodySize = -1;

        public TcpBufferHandler(RecordParser parser, Handler<Buffer> bufferHandler) {
            this.parser = parser;
            this.bufferHandler = bufferHandler;
        }

        @Override
        public void handle(Buffer buffer) {
            if (bodySize == -1) {
                // Step 1: Parse body length in int from header (from byte index 13)
                bodySize = buffer.getInt(13);

                // Switch parser to read the body size next
                parser.fixedSizeMode(bodySize);

                // Store header
                resultBuffer.appendBuffer(buffer);
            } else {
                // Step 2: Store body and forward complete message
                resultBuffer.appendBuffer(buffer);
                bufferHandler.handle(resultBuffer);

                // Step 3: Reset for next message
                parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                bodySize = -1;
                resultBuffer = Buffer.buffer();
            }
        }
    }
}

