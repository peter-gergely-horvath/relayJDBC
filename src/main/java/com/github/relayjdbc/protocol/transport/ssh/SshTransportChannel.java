package com.github.relayjdbc.protocol.transport.ssh;

import com.github.relayjdbc.protocol.transport.TransportChannel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class SshTransportChannel implements TransportChannel {

    private static final int LINE_FEED = 10;
    private static final int CARRIAGE_RETURN = 13;

    private static final int STREAM_END_FLAG = -1;

    private static final int READ_BUFFER_SIZE = 32 * 1024;
    private static final int READ_DELAY = 50;

    private static byte[] NEW_LINE_BYTES = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);


    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    // using MimeDecoder prevents choking on newline characters at the end of the line
    private static final Base64.Decoder BASE64_DECODER = Base64.getMimeDecoder();



    private final OutputStream requestSender;
    private final InputStream inputStream;

    private ByteArrayOutputStream requestPayloadCollectorByteArrayOutputStream;

    SshTransportChannel(InputStream inputStream, OutputStream requestSender) {
        this.requestSender = requestSender;
        this.inputStream = inputStream;
    }


    @Override
    public void open()  {
        requestPayloadCollectorByteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        if (requestPayloadCollectorByteArrayOutputStream == null) {
            throw new IllegalStateException("Not connected");
        }
        return requestPayloadCollectorByteArrayOutputStream;
    }

    @Override
    public InputStream sendAndWaitForResponse() throws IOException {
        if (requestPayloadCollectorByteArrayOutputStream == null) {
            throw new IllegalStateException("Not connected");
        }

        requestPayloadCollectorByteArrayOutputStream.flush();
        byte[] requestPayload = requestPayloadCollectorByteArrayOutputStream.toByteArray();

        byte[] base64EncodedRequestPayload = BASE64_ENCODER.encode(requestPayload);
        requestSender.write(base64EncodedRequestPayload);
        requestSender.write(NEW_LINE_BYTES);
        requestSender.flush();

        byte[] base64EncodedResponse = readNextLineBytes();
        if (base64EncodedResponse.length == 0) {
            throw new IOException("empty response received");
        }

        byte[] responsePayload = BASE64_DECODER.decode(base64EncodedResponse);

        return new ByteArrayInputStream(responsePayload);
    }

    // Apparently a Jsch-provided InputStream has some issues with BufferedReaders:
    // as a work-around we have implemented the logic to read a whole line
    private byte[] readNextLineBytes() throws IOException {
        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

            byte[] readBuffer = new byte[READ_BUFFER_SIZE];

            readRetries: while (true) {
                int numberOfBytesRead;

                while (inputStream.available() > 0
                        && (numberOfBytesRead = inputStream.read(readBuffer, 0, readBuffer.length)) != STREAM_END_FLAG) {

                    outputBuffer.write(readBuffer, 0, numberOfBytesRead);

                    if (numberOfBytesRead > 0) {
                        final int endIndexOfReadBuffer = numberOfBytesRead - 1;
                        final byte lastByteRead = readBuffer[endIndexOfReadBuffer];

                        if (lastByteRead == LINE_FEED || lastByteRead == CARRIAGE_RETURN) {
                            break readRetries;
                        }
                    }
                }
                Thread.sleep(READ_DELAY);
            }


            outputBuffer.flush();
            return outputBuffer.toByteArray();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new IOException("Read interrupted", e);
        }
    }

    @Override
    public void close() {

    }
}
