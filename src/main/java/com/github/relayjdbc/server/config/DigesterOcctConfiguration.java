package com.github.relayjdbc.server.config;

public class DigesterOcctConfiguration extends OcctConfiguration {
    public void setCheckingPeriod(String checkingPeriod) {
        setCheckingPeriodInMillis(ConfigurationUtil.getMillisFromString(checkingPeriod));
    }
    
    public void setTimeout(String timeout) {
        setTimeoutInMillis(ConfigurationUtil.getMillisFromString(timeout));
    }
}
