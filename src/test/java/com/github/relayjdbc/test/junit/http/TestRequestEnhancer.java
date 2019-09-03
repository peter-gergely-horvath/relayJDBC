package com.github.relayjdbc.test.junit.http;

import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.servlet.RequestModifier;

public class TestRequestEnhancer implements RequestEnhancer {
    public void enhanceConnectRequest(RequestModifier requestModifier) {
        requestModifier.addRequestHeader("connect-test-property", "connect-test-value");
    }

    public void enhanceProcessRequest(RequestModifier requestModifier) {
        requestModifier.addRequestHeader("process-test-property", "process-test-value");
    }
}
