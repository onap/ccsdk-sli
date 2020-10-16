package org.onap.ccsdk.sli.core.sli;

import java.util.regex.Pattern;

public class PathValidator {
    public static boolean isValidXmlPath(String path) {
        Pattern allowList = Pattern.compile("[-\\w/\\/]+\\.xml$");
        return (allowList.matcher(path).matches());
    }
    public static boolean isValidPropertiesPath(String path) {
        Pattern allowList = Pattern.compile("[-\\w/\\/]+\\.properties$");
        return (allowList.matcher(path).matches());
    }
    public static boolean isValidFilePath(String path) {
        Pattern allowList = Pattern.compile("[-\\w/\\/]+");
        return (allowList.matcher(path).matches());
    }
}
