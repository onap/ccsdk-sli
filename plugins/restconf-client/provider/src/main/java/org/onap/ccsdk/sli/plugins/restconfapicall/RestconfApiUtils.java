/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.plugins.restconfapicall;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.restapicall.HttpMethod;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.YangParameters;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

import static org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode.getParameters;
import static org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode.parseParam;
import static org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode.DEFAULT_MODE;
import static org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource.forFile;
import static org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors.defaultReactor;
import static org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource.create;

/**
 * Utilities for restconf api call node.
 */
public final class RestconfApiUtils {

    static final String RES_CODE = "response-code";

    static final String HTTP_REQ ="httpRequest";

    static final String RES_PRE = "responsePrefix";

    static final String RES_MSG = "response-message";

    static final String HEADER = "header.";

    static final String COMMA = ",";

    static final String COLON = ":";

    static final String HTTP_RES = "httpResponse";

    static final String REST_API_URL = "restapiUrl";

    static final String UPDATED_URL = "URL was set to";

    static final String COMM_FAIL = "Failed to communicate with host %s." +
            "Request will be re-attempted using the host %s.";

    static final String RETRY_COUNT = "This is retry attempt %d out of %d";

    static final String RETRY_FAIL = "Retry attempt has failed. No further " +
            "retry shall be attempted, calling setFailureResponseStatus";

    static final String NO_MORE_RETRY = "Could not attempt retry";

    static final String MAX_RETRY_ERR = "Maximum retries reached, calling " +
            "setFailureResponseStatus";

    static final String ATTEMPTS_MSG = "%d attempts were made out of %d " +
            "maximum retries";

    static final String REQ_ERR = "Error sending the request: ";

    private static final String SLASH = "/";

    private static final String DIR_PATH = "dirPath";

    private static final String URL_SYNTAX = "The following URL cannot be " +
            "parsed into URI : ";

    private static final String YANG = ".yang";

    private static final String YANG_FILE_ERR = "Unable to parse the YANG " +
            "file provided";

    //No instantiation.
    private RestconfApiUtils() {
    }

    /**
     * Returns the YANG parameters after parsing it from the map.
     *
     * @param paramMap parameters map
     * @return YANG parameters
     * @throws SvcLogicException when parsing of parameters map fail
     */
    static YangParameters getYangParameters(Map<String, String> paramMap)
            throws SvcLogicException {
        YangParameters param = (YangParameters) getParameters(
                paramMap, new YangParameters());
        param.dirPath = parseParam(paramMap, DIR_PATH, false, null);
        return param;
    }

    /**
     * Parses the restconf URL and gives the YANG path from it, which can be
     * used to get schema node. If it is a PUT operation, then a node must be
     * reduced from the url to make it always point to the parent.
     *
     * @param url    restconf URL
     * @param method HTTP operation
     * @return YANG path pointing to parent
     * @throws SvcLogicException when parsing the URL fails
     */
    public static String parseUrl(String url, HttpMethod method)
            throws SvcLogicException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new SvcLogicException(URL_SYNTAX + url, e);
        }

        String path = uri.getPath();
        path = getParsedPath(path);
        return path;
    }

    /**
     * Returns the path which contains only the schema nodes.
     *
     * @param path path
     * @return path representing schema
     */
    private static String getParsedPath(String path) {
        String firstHalf;
        String secondHalf;
        if (path.contains(COLON)) {
            String[] p = path.split(COLON);
            if (p[0].contains(SLASH)) {
                int slash = p[0].lastIndexOf(SLASH);
                firstHalf = p[0].substring(slash + 1);
            } else {
                firstHalf = p[0];
            }
            secondHalf = path.substring(p[0].length() + 1);
            return firstHalf + COLON + secondHalf;
        } else if (path.contains(SLASH)) {
            String[] p = path.split(SLASH);
            if (p.length > 4) {
                String actual = p[3] + COLON + p[4];
                if (p.length > 5) {
                    secondHalf = path.substring(
                           p[1].length() + p[2].length() + actual.length() + 3);
                    path = actual + secondHalf;
                } else {
                    path = actual;
                }
            }
        }
        return path;
    }

    /**
     * Returns the schema context of the YANG files present in a directory.
     *
     * @param di directory path
     * @return YANG schema context
     * @throws SvcLogicException when YANG file reading fails
     */
    static SchemaContext getSchemaCtxFromDir(String di)
            throws SvcLogicException {
        Path d = Paths.get(di);
        File dir = d.toFile();
        List<File> yangFiles = new LinkedList<>();
        getYangFiles(dir, yangFiles);
        final Collection<YangStatementStreamSource> sources =
                new ArrayList<>(yangFiles.size());
        for (File file : yangFiles) {
            try {
                sources.add(create(forFile(file)));
            } catch (IOException | YangSyntaxErrorException e) {
                throw new SvcLogicException(YANG_FILE_ERR + e.getMessage(), e);
            }
        }

        final CrossSourceStatementReactor.BuildAction reactor = defaultReactor()
                .newBuild(DEFAULT_MODE).addSources(sources);
        try {
            return reactor.buildEffective();
        } catch (ReactorException e) {
            throw new SvcLogicException(YANG_FILE_ERR + e.getMessage(), e);
        }
    }

    /**
     * Returns all the YANG files present in a directory recursively.
     *
     * @param dir       path of the directory
     * @param yangFiles list of YANG files
     */
    private static void getYangFiles(File dir, List<File> yangFiles) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                processFiles(files, yangFiles);
            }
        }
    }

    /**
     * Processes all the obtained files by isolating all the YANG files from
     * all the directory of the given path recursively.
     *
     * @param files     files in the given path
     * @param yangFiles YANG files list
     */
    private static void processFiles(File[] files, List<File> yangFiles) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(YANG)) {
                yangFiles.add(file);
            } else if (file.isDirectory()) {
                getYangFiles(file, yangFiles);
            }
        }
    }

    /**
     * Returns the updated XML request message by adding root node to it.
     *
     * @param req      XML request
     * @param nodeName root node name
     * @param modNs    module namespace of the root node
     * @return updated XML request message
     */
    static String getUpdatedXmlReq(String req, String nodeName, String modNs) {
        String rootNode = "\n<" + nodeName + " xmlns=\"" + modNs +
                "\">\n";
        req = req.replaceFirst("\n", rootNode);
        req = req + "</" + nodeName + ">";
        return req.replaceAll(">\\s+<", "><");
    }
}
