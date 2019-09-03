// Relay - Virtual JDBC
// Written by Michael Link
// Website: http://Relay.sourceforge.net

package com.github.relayjdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.servlet.RequestEnhancerFactory;
import com.github.relayjdbc.servlet.kryo.KryoServletCommandSinkJdkHttpClient;
import com.github.relayjdbc.util.ClientInfo;
import com.github.relayjdbc.util.SQLExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.relayjdbc.command.CallingContextFactory;
import com.github.relayjdbc.command.CommandSink;
import com.github.relayjdbc.command.DecoratedCommandSink;
import com.github.relayjdbc.command.NullCallingContextFactory;
import com.github.relayjdbc.command.StandardCallingContextFactory;

public final class RelayDriver implements Driver {
    private static Log _logger = LogFactory.getLog(RelayDriver.class);

    private static final String Relay_IDENTIFIER = "jdbc:relayjdbc:";
    private static final String EJB_IDENTIFIER = "ejb:";
    private static final String RMI_IDENTIFIER = "rmi:";
    private static final String SERVLET_IDENTIFIER = "servlet:";
//    private static SecureSocketFactory _sslSocketFactory;
    private static boolean _cacheEnabled = false;

//    private static final int MAJOR_VERSION = 1;
//    private static final int MINOR_VERSION = 7;

    static {
        try {
            DriverManager.registerDriver(new RelayDriver());
            _logger.info("RelayDriver JDBC-Driver successfully registered");
            try {
                Class.forName("org.hsqldb.jdbcDriver").newInstance();
                _logger.info("HSQL-Driver loaded, caching activated");
                _cacheEnabled = true;
            } catch(ClassNotFoundException e) {
                _logger.info("Couldn't load HSQL-Driver, caching deactivated");
                _cacheEnabled = false;
            } catch(Exception e) {
                _logger.error("Unexpected exception occured on loading the HSQL-Driver");
                _cacheEnabled = false;
            }
        } catch(Exception e) {
            _logger.fatal("Couldn't register RelayDriver JDBC-Driver !", e);
            throw new RuntimeException("Couldn't register RelayDriver JDBC-Driver !", e);
        }
    }

    public RelayDriver() {
    }

    public Connection connect(String urlstr, Properties props) throws SQLException {
        Connection result = null;

        if(acceptsURL(urlstr)) {
            String realUrl = urlstr.substring(Relay_IDENTIFIER.length());

            _logger.info("Relay-URL: " + realUrl);

            try {
                CommandSink sink = null;

                String[] urlparts;

//                // EJB-Connection
//                if(realUrl.startsWith(EJB_IDENTIFIER)) {
//                    urlparts = split(realUrl.substring(EJB_IDENTIFIER.length()));
//                    _logger.info("Relay in EJB-Mode, using object " + urlparts[0]);
//                    sink = createEjbCommandSink(urlparts[0]);
//                    // RMI-Connection
//                } else if(realUrl.startsWith(RMI_IDENTIFIER)) {
//                    urlparts = split(realUrl.substring(RMI_IDENTIFIER.length()));
//                    _logger.info("Relay in RMI-Mode, using object " + urlparts[0]);
//                    // Examine SSL property
//                    boolean useSSL = false;
//                    String propSSL = props.getProperty(RelayProperties.RMI_SSL);
//                    useSSL = (propSSL != null && propSSL.equalsIgnoreCase("true"));
//                    if(useSSL) {
//                        _logger.info("Using Secure Socket Layer (SSL)");
//                    }
//                    sink = createRmiCommandSink(urlparts[0], useSSL);
//                    // Servlet-Connection
//                } else 
                if(realUrl.startsWith(SERVLET_IDENTIFIER)) {
                    urlparts = split(realUrl.substring(SERVLET_IDENTIFIER.length()));
                    _logger.info("Relay in Servlet-Mode, using URL " + urlparts[0]);
                    sink = createServletCommandSink(urlparts[0], props);
                } else {
                    throw new SQLException("Unknown protocol identifier " + realUrl);
                }

                if(urlparts[1].length() > 0) {
                    _logger.info("Connecting to datasource " + urlparts[1]);
                } else {
                    _logger.info("Using default datasource");
                }

                // Connect with the sink
                UIDEx reg = sink.connect(
                        urlparts[1],
                        props,
                        ClientInfo.getProperties(props.getProperty(VJdbcProperties.CLIENTINFO_PROPERTIES)),
                        new CallingContext());

                CallingContextFactory ctxFactory;
                // The value 1 signals that every remote call shall provide a calling context. This should only
                // be used for debugging purposes as sending of these objects is quite expensive.
                if(reg.getValue1() == 1) {
                    ctxFactory = new StandardCallingContextFactory();
                }
                else {
                    ctxFactory = new NullCallingContextFactory();
                }
                // Decorate the sink
                DecoratedCommandSink decosink = new DecoratedCommandSink(reg, sink, ctxFactory);
                // return the new connection
                result = new VirtualConnection(reg, decosink, props, _cacheEnabled);
            } catch(Exception e) {
                _logger.error(e);
                throw SQLExceptionHelper.wrap(e);
            }
        }

        return result;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(Relay_IDENTIFIER);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return Version.majorIntVersion; //MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return Version.minorIntVersion; //MINOR_VERSION;
    }

    public boolean jdbcCompliant() {
        return true;
    }

//    private CommandSink createRmiCommandSink(String rminame, boolean useSSL) throws Exception {
//        if(useSSL) {
//            if(_sslSocketFactory == null) {
//                _sslSocketFactory = new SecureSocketFactory();
//                RMISocketFactory.setSocketFactory(_sslSocketFactory);
//            }
//        }
//        ConnectionBrokerRmi broker = (ConnectionBrokerRmi)Naming.lookup(rminame);
//        CommandSinkRmi rmiSink = broker.createCommandSink();
//        CommandSink proxy = new CommandSinkRmiProxy(rmiSink);
//        return proxy;
//    }

//    private CommandSink createEjbCommandSink(String ejbname) throws Exception {
//        Context ctx = new InitialContext();
//        _logger.info("Lookup " + ejbname);
//        Object ref = ctx.lookup(ejbname);
//        _logger.info("remote bean " + ref.getClass().getName());
//        return (EjbCommandSinkProxy)ref;
//    }

    private CommandSink createServletCommandSink(String url, Properties props) throws Exception {
        RequestEnhancer requestEnhancer = null;

        String requestEnhancerFactoryClassName = props.getProperty(VJdbcProperties.SERVLET_REQUEST_ENHANCER_FACTORY);

        if(requestEnhancerFactoryClassName != null) {
            _logger.debug("Found RequestEnhancerFactory class: " + requestEnhancerFactoryClassName);
            Class requestEnhancerFactoryClass = Class.forName(requestEnhancerFactoryClassName);
            RequestEnhancerFactory requestEnhancerFactory = (RequestEnhancerFactory)requestEnhancerFactoryClass.newInstance();
            _logger.debug("RequestEnhancerFactory successfully created");
            requestEnhancer = requestEnhancerFactory.create();
        }
        // for now support only Kryo serialization via JDK HTTP client
        return new KryoServletCommandSinkJdkHttpClient(url, requestEnhancer);
        // TODO Decide here if we should use Jakarta-HTTP-Client or Kryo Http Client
//        String useKryoHttpClient = props.getProperty(RelayProperties.SERVLET_USE_KRYO_HTTP_CLIENT);
//        String useJakartaHttpClient = props.getProperty(RelayProperties.SERVLET_USE_JAKARTA_HTTP_CLIENT);
//        if (useKryoHttpClient!=null && useKryoHttpClient.equals("true")) {
//        	return new KryoServletCommandSinkJdkHttpClient(url, requestEnhancer);
//        } 
//        else if(useJakartaHttpClient != null && useJakartaHttpClient.equals("true")) {
//            return new ServletCommandSinkJakartaHttpClient(url, requestEnhancer);
//        }
//        else {
//            return new ServletCommandSinkJdkHttpClient(url, requestEnhancer);
//        }
    }

    // Helper method (can't use the 1.4-Method because support for 1.3 is desired)
    private String[] split(String url) {
        char[] splitChars = { ',', ';', '#', '$' };

        for(int i = 0; i < splitChars.length; i++) {
            int charindex = url.indexOf(splitChars[i]);

            if(charindex >= 0) {
                return new String[] { url.substring(0, charindex), url.substring(charindex + 1) };
            }
        }

        return new String[] { url, "" };
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger");
    }
}
