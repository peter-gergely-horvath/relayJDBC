package com.github.relayjdbc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

public class ClientInfo {
    public static final String RELAY_CLIENT_NAME = "relay-client.name";
	public static final String RELAY_CLIENT_ADDRESS = "relay-client.address";
	public static final String RELAY_FAST_UPDATE = "relay-client.fast.update";
	
	private static Log _logger = LogFactory.getLog(ClientInfo.class);

    private static final AtomicReference<Properties> propertiesRef = new AtomicReference<>();

    public static Properties getProperties(String propertiesToTransfer) {
        // Initialize the properties with the first access
        Properties properties = propertiesRef.get();
        if (properties == null) {
            properties = initProperties(propertiesToTransfer);
            final boolean wasNull = propertiesRef.compareAndSet(null, properties);
            if (!wasNull) {
                properties = propertiesRef.get();
            }
        }
        return Objects.requireNonNull(properties);
    }

    private static Properties initProperties(String propertiesToTransfer) {
        Properties properties = new Properties();;

        try {
            // Deliver local host information
            tryAddHostInfoToProperties(properties);

            // Split the passed string into pieces and put all system properties
            // into the Properties object
            if(propertiesToTransfer != null) {
                // Use StringTokenizer here, split-Method is only available in JDK 1.4
                StringTokenizer tokenizer = new StringTokenizer(propertiesToTransfer, ";");
                while(tokenizer.hasMoreTokens()) {
                    String key = tokenizer.nextToken();
                    if (!"".equals(key.trim())) {
                        String value = System.getProperty(key);
                        if(value != null) {
                            properties.put(key, value);
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            _logger.info("Access-Exception, System-Properties can't be delivered to the server");
        }

        return properties;
    }

    private static void tryAddHostInfoToProperties(Properties properties) {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            properties.put(RELAY_CLIENT_ADDRESS, localAddress.getHostAddress());
            properties.put(RELAY_CLIENT_NAME, localAddress.getHostName());
        } catch (UnknownHostException e) {
            _logger.info("UnknownHostException: host information can't be delivered to the server");
        }
    }
}
