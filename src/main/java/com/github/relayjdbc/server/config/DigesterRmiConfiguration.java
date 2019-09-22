package com.github.relayjdbc.server.config;

public class DigesterRmiConfiguration extends RmiConfiguration {
    public void setCreateRegistry(String createRegistry) {
        _createRegistry = ConfigurationUtil.getBooleanFromString(createRegistry);
    }
}
