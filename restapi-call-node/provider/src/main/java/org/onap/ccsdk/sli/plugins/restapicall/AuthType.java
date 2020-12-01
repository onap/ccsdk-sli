package org.onap.ccsdk.sli.plugins.restapicall;

public enum AuthType {
    NONE, BASIC, DIGEST, OAUTH, Unspecified;

    public static AuthType fromString(String s) {
        if ("basic".equalsIgnoreCase(s))
            return BASIC;
        if ("digest".equalsIgnoreCase(s))
            return DIGEST;
        if ("oauth".equalsIgnoreCase(s))
            return OAUTH;
        if ("none".equalsIgnoreCase(s))
            return NONE;
        if ("unspecified".equalsIgnoreCase(s))
            return Unspecified;
        throw new IllegalArgumentException("Invalid value for format: " + s);
    }
}
