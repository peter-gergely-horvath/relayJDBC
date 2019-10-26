package com.github.relayjdbc.servlet;

/**
 * The RequestEnhancer interface must be implemented by classes that want to enhance
 * the Http-Requests that VJDBC sends to the VJDBC-Servlet. This way it's possible to
 * send connection specific data like authentication cookies.
 * @author Mike
 */
public interface RequestEnhancer {
    /**
     * Called before the initial connect request of VJDBC is sent.
     * @param requestModifier the RequestModifier to add
     */
    void enhanceConnectRequest(RequestModifier requestModifier);
    
    /**
     * Called before each processing request of VJDBC.
     * @param requestModifier the RequestModifier to add
     */
    void enhanceProcessRequest(RequestModifier requestModifier);
}
