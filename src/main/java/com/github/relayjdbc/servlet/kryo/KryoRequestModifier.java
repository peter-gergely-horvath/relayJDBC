package com.github.relayjdbc.servlet.kryo;

import java.net.URLConnection;

import com.github.relayjdbc.servlet.RequestModifier;

public class KryoRequestModifier implements RequestModifier {
    private final URLConnection _urlConnection;
    
    /**
     * Package visibility, doesn't make sense for other packages.
     * @param urlConnection Wrapped URLConnection
     */
    public KryoRequestModifier(URLConnection urlConnection) {
        _urlConnection = urlConnection;
    }
    
    /* (non-Javadoc)
     * @see com.github.relayjdbc.servlet.RequestModifier#addRequestProperty(java.lang.String, java.lang.String)
     */
    public void addRequestHeader(String key, String value) {
        _urlConnection.addRequestProperty(key, value);
    }

}
