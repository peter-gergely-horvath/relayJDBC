package com.github.relayjdbc.test.junit.http;

import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.servlet.RequestEnhancerFactory;

public class TestRequestEnhancerFactory implements RequestEnhancerFactory {
    public RequestEnhancer create() {
        return new TestRequestEnhancer();
    }
}
