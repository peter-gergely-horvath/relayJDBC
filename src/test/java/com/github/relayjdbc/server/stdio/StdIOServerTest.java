package com.github.relayjdbc.server.stdio;

import com.github.relayjdbc.protocol.Decoder;
import com.github.relayjdbc.protocol.Encoder;
import com.github.relayjdbc.protocol.kryo.KryoProtocol;
import com.github.relayjdbc.protocol.kryo.KryoProtocolConstants;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.util.ClientInfo;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StdIOServerTest {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @AfterClass
    public static void afterClass() {
        List<Runnable> runnables = executorService.shutdownNow();
        if (!runnables.isEmpty()) {
            throw new RuntimeException("Executor had non-commenced tasks");
        }
    }

    @Test(timeout = 15000)
    public void testServer() throws IOException, ConfigurationException, InterruptedException {

        Pipe stdInPipe = Pipe.open();
        Pipe stdOutPipe = Pipe.open();

        InputStream serverStdIn = Channels.newInputStream(stdInPipe.source());
        OutputStream serverStdOut = Channels.newOutputStream(stdOutPipe.sink());

        OutputStream clientWriter = Base64.getEncoder().wrap(Channels.newOutputStream(stdInPipe.sink()));
        InputStream clientReader = Base64.getDecoder().wrap(Channels.newInputStream(stdOutPipe.source()));

        StdIOServer stdIOServer = new StdIOServer() {
            @Override
            protected InputStream getConfigFileStream(String fileToLoad) throws FileNotFoundException {
                InputStream resourceAsStream = StdIOServerTest.class.getResourceAsStream(fileToLoad);
                Objects.requireNonNull(resourceAsStream, "config is not found");

                return resourceAsStream;
            }

            @Override
            protected InputStream getStdIn() {
                return serverStdIn;
            }

            @Override
            protected OutputStream getStdOut() {
                return serverStdOut;
            }
        };

        executorService.execute(() -> {
            try {
                stdIOServer.runApp(new String[] {"config.xml"});
            } catch (ConfigurationException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        KryoProtocol protocol =
                new KryoProtocol(KryoProtocolConstants.DEFAULT_COMPRESSION_MODE,
                        KryoProtocolConstants.DEFAULT_COMPRESSION_THRESHOLD);

        Encoder protocolEncoder = protocol.getProtocolEncoder();

        Decoder protocolDecoder = protocol.getProtocolDecoder();

        protocolEncoder.writeConnect(clientWriter,
                "h2db",
                new Properties(),
                ClientInfo.getProperties("user.name"),
                new CallingContext());

        Thread.sleep(5000);

        Object resultObject = protocolDecoder.readObject(clientReader);

        Assert.assertNotNull(resultObject);
        Assert.assertFalse(resultObject instanceof SQLException);


    }


}
