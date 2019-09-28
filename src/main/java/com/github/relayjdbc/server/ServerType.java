package com.github.relayjdbc.server;

public enum ServerType {

    HTTP,
    BASE64_PIPE;

    static ServerType fromString(String requestedName) {
        for(ServerType st : ServerType.values()) {
            String originalName = st.name();
            String simpleName = originalName.replaceAll("_", "");

            if (simpleName.equalsIgnoreCase(requestedName)) {
                return st;
            }
        }
        throw new IllegalArgumentException("Invalid Server Type: " + requestedName);
    }
}
