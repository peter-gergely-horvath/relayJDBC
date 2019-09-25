package com.github.relayjdbc.server.config;

import com.github.relayjdbc.command.ConnectionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NamedQueryConfiguration {
    private static Log _logger = LogFactory.getLog(NamedQueryConfiguration.class);
    private Map<String, String> _queryMap = new HashMap();

    public String getSqlForId(ConnectionContext ctx, String id) throws SQLException {
        String result = _queryMap.get(id);
        if(result != null) {
            return result;
        }
        else {
            String msg = "Named-Query for key '" + id + "' not found";
            _logger.error(msg);
            throw new SQLException(msg);
        }
    }

    void log() {
        _logger.info("  Named Query-Configuration:");

        for (Map.Entry<String, String> entry : _queryMap.entrySet()) {
            String id = entry.getKey();
            String value = entry.getValue();
            _logger.info("    [" + id + "] = [" + value + "]");
        }
    }
}
