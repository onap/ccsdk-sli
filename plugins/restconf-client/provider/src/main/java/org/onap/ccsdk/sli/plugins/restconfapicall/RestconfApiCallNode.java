/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import javax.ws.rs.core.UriBuilder;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.plugins.restapicall.Format;
import org.onap.ccsdk.sli.plugins.restapicall.HttpResponse;
import org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode;
import org.onap.ccsdk.sli.plugins.restapicall.XmlParser;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatSerializer;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatSerializerContext;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerFactory;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.Listener;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.MdsalSerializerHelper;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.SerializerHelper;
import org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.YangParameters;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeSerializer;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.Namespace;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeSerializer;
import org.opendaylight.restconf.common.context.InstanceIdentifierContext;
import org.opendaylight.restconf.nb.rfc8040.utils.parser.ParserIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Strings.repeat;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.join;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.DELETE;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.GET;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.PATCH;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.PUT;
import static org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode.parseParam;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.ATTEMPTS_MSG;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.COLON;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.COMMA;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.COMM_FAIL;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.HEADER;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.HTTP_REQ;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.HTTP_RES;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.MAX_RETRY_ERR;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.NO_MORE_RETRY;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.REQ_ERR;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.REST_API_URL;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.RES_CODE;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.RES_MSG;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.RES_PRE;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.RETRY_COUNT;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.RETRY_FAIL;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.UPDATED_URL;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.getSchemaCtxFromDir;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.getUpdatedXmlReq;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.getYangParameters;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.parseUrl;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfListenerFactory.instance;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.FORMAT_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.UTF_HEADER;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.XML_TREE_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.getXmlWriter;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getModuleNamespace;
import static org.osgi.framework.FrameworkUtil.getBundle;

/**
 * Representation of a plugin to enable RESTCONF based CRUD operations from DG.
 */
public class RestconfApiCallNode implements SvcLogicJavaPlugin {

    /**
     * Logger for the restconf api call node class.
     */
    private static final Logger log = LoggerFactory.getLogger(
            RestconfApiCallNode.class);

    /**
     * Rest api call node service instance
     */
    private RestapiCallNode restapiCallNode;

    /**
     * Creates an instance of restconf api call node with restapi call node.
     *
     * @param r restapi call node
     */
    public RestconfApiCallNode(RestapiCallNode r) {
        this.restapiCallNode = r;
    }

    /**
     * Returns the restapi call node instance.
     * @return
     */
    public RestapiCallNode getRestapiCallNode() {
        return restapiCallNode;
    }

    /**
     * Sends the restconf request using the parameters map and the memory
     * context. And this method allows the directed graphs to interact with
     * the restconf api call node
     *
     * @param paramMap parameters map
     * @param ctx      service logic context
     * @throws SvcLogicException when svc logic exception occurs
     */
    public void sendRequest(Map<String, String> paramMap, SvcLogicContext ctx)
            throws SvcLogicException {
        sendRequest(paramMap, ctx, 0);
    }

    /**
     * Sends the restconf request using the parameters map and the memory
     * context along with the retry count.
     *
     * @param paramMap   parameters map
     * @param ctx        service logic context
     * @param retryCount number of retry counts
     * @throws SvcLogicException when svc logic exception occurs
     */
    public void sendRequest(Map<String, String> paramMap, SvcLogicContext ctx,
                            Integer retryCount) throws SvcLogicException {
        RestapiCallNode rest = getRestapiCallNode();
        HttpResponse r = new HttpResponse();
        try {
            YangParameters p = getYangParameters(paramMap);

            String pp = p.responsePrefix != null ? p.responsePrefix + '.' : "";
            Map<String, String> props = new HashMap<>((Map)ctx.toProperties());
            String uri = parseUrl(p.restapiUrl, p.httpMethod);
            InstanceIdentifierContext<?> insIdCtx = getInsIdCtx(p, uri);

            String req = null;
            if (p.httpMethod != GET && p.httpMethod != DELETE) {
                req = serializeRequest(props, p, uri, insIdCtx);
                if (p.httpMethod == PUT || p.httpMethod == PATCH) {
                    updateReq(req, p, insIdCtx);
                }
            }
            if (req == null && p.requestBody != null) {
                req = p.requestBody;
            }

            r = rest.sendHttpRequest(req, p);
            if (p.returnRequestPayload && req != null) {
                ctx.setAttribute(pp + HTTP_REQ, req);
            }

            String response = getResponse(ctx, p, pp, r);
            if (response != null) {
                try {
                    Map<String, String> resProp = serializeResponse(
                            p, uri, response, insIdCtx);
                    for (Map.Entry<String, String> pro : resProp.entrySet()) {
                        ctx.setAttribute(pro.getKey(), pro.getValue());
                    }
                } catch (SvcLogicException e) {
                    convertToNormalRes(ctx, p, pp, response);
                }
            }
        } catch (SvcLogicException e) {
            boolean shouldRetry = false;
            if (e.getCause().getCause() instanceof SocketException) {
                shouldRetry = true;
            }

            log.error(REQ_ERR + e.getMessage(), e);
            String prefix = parseParam(paramMap, RES_PRE, false, null);
            setFailureResponseStatus(ctx, prefix, e.getMessage());
        }

        if (r != null && r.code >= 300) {
            throw new SvcLogicException(valueOf(r.code) +
                                                COLON + " " + r.message);
        }
    }

    private void convertToNormalRes(SvcLogicContext ctx ,
                                    YangParameters p, String pp, String body)
            throws SvcLogicException {
        if (p.convertResponse) {
            Map<String, String> mm = null;
            if (p.format == Format.XML) {
                mm = XmlParser.convertToProperties(body, p.listNameList);
            } else if (p.format == Format.JSON) {
                mm = org.onap.ccsdk.sli.plugins.restapicall.JsonParser
                        .convertToProperties(body);
            }

            if (mm != null) {
                for (Map.Entry<String, String> entry : mm.entrySet()) {
                    ctx.setAttribute(pp + entry.getKey(),
                                     entry.getValue());
                }
            }
        }
    }

    /**
     * Serializes the request message to JSON or XML from the properties.
     *
     * @param properties properties
     * @param params     YANG parameters
     * @param uri        URI
     * @param insIdCtx   instance identifier context
     * @return JSON or XML message to be sent
     * @throws SvcLogicException when serializing the request fails
     */
     public String serializeRequest(Map<String, String> properties,
                                    YangParameters params, String uri,
                                    InstanceIdentifierContext insIdCtx)
             throws SvcLogicException {
        PropertiesNodeSerializer propSer = new MdsalPropertiesNodeSerializer(
                insIdCtx.getSchemaNode(), insIdCtx.getSchemaContext(), uri);
        DataFormatSerializerContext serCtx = new DataFormatSerializerContext(
                null, uri, null, propSer);
        DataFormatSerializer ser = DfSerializerFactory.instance()
                .getSerializer(serCtx, params);
         //TODO: Handling of XML annotations
        return ser.encode(properties, null);
    }

    /**
     * Serializes the response message from JSON or XML to the properties.
     *
     * @param params   YANG parameters
     * @param uri      URI
     * @param response response message
     * @param insIdCtx instance identifier context
     * @return response message as properties
     * @throws SvcLogicException when serializing the response fails
     */
    public Map<String, String> serializeResponse(YangParameters params,
                                                 String uri, String response,
                                                 InstanceIdentifierContext insIdCtx)
            throws SvcLogicException {
        PropertiesNodeSerializer propSer = new MdsalPropertiesNodeSerializer(
                insIdCtx.getSchemaNode(), insIdCtx.getSchemaContext(), uri);
        SerializerHelper helper = new MdsalSerializerHelper(
                insIdCtx.getSchemaNode(), insIdCtx.getSchemaContext(), uri);
        Listener listener = instance().getListener(helper, params);
        DataFormatSerializerContext serCtx = new DataFormatSerializerContext(
                listener, uri, null, propSer);
        DataFormatSerializer ser = DfSerializerFactory.instance()
                .getSerializer(serCtx, params);
        return ser.decode(response);
    }

    /**
     * Returns instance identifier context for a uri using the schema context.
     *
     * @param params YANG parameters
     * @param uri    URI
     * @return instance identifier context
     * @throws SvcLogicException when getting schema context fails
     */
    private InstanceIdentifierContext<?> getInsIdCtx(YangParameters params,
                                                     String uri)
            throws SvcLogicException {
        SchemaContext context = getSchemaContext(params);
        return ParserIdentifier.toInstanceIdentifier(uri, context, null);
    }

    /**
     * Returns the global schema context or schema context of particular YANG
     * files present in a directory path.
     *
     * @param params YANG parameters
     * @return schema context
     * @throws SvcLogicException when schema context fetching fails
     */
    private SchemaContext getSchemaContext(YangParameters params)
            throws SvcLogicException {
        if (params.dirPath != null) {
            return getSchemaCtxFromDir(params.dirPath);
        }
        BundleContext bc = getBundle(SchemaContext.class).getBundleContext();
        SchemaContext schemaContext = null;
        if (bc != null) {
            ServiceReference reference = bc.getServiceReference(
                    SchemaContext.class);
            if (reference != null) {
                schemaContext = (SchemaContext) bc.getService(reference);
            }
        }
        return schemaContext;
    }

    /**
     * Returns the response message body of a http response message.
     *
     * @param ctx    svc logic context
     * @param params parameters
     * @param pre    prefix to be appended
     * @param res    http response
     * @return response message body
     */
    public String getResponse(SvcLogicContext ctx, YangParameters params,
                               String pre, HttpResponse res) {
        ctx.setAttribute(pre + RES_CODE, valueOf(res.code));
        ctx.setAttribute(pre + RES_MSG, res.message);

        if (params.dumpHeaders && res.headers != null) {
            for (Map.Entry<String, List<String>> a : res.headers.entrySet()) {
                ctx.setAttribute(pre + HEADER + a.getKey(),
                                 join(a.getValue(), COMMA));
            }
        }

        if (res.body != null && res.body.trim().length() > 0) {
            ctx.setAttribute(pre + HTTP_RES, res.body);
            return res.body;
        }
        return null;
    }

    /**
     * Sets the failure response status in the context memory.
     *
     * @param ctx    service logic context
     * @param prefix prefix to be added
     * @param errMsg error message
     */
    private void setFailureResponseStatus(SvcLogicContext ctx, String prefix,
                                          String errMsg) {
        HttpResponse res = new HttpResponse();
        res.code = 500;
        res.message = errMsg;
        ctx.setAttribute(prefix + RES_CODE, valueOf(res.code));
        ctx.setAttribute(prefix + RES_MSG, res.message);
    }

    /**
     * Updates request message for JSON and XML data format, when the HTTP
     * method points it as PUT or PATCH.
     *
     * @param req      current request message
     * @param p        YANG parameters
     * @param insIdCtx instance identifier context
     * @return update request message
     * @throws SvcLogicException when the data format type is wrong
     */
    public String updateReq(String req, YangParameters p,
                             InstanceIdentifierContext<?> insIdCtx)
            throws SvcLogicException {

        SchemaNode schemaNode = insIdCtx.getSchemaNode();
        Namespace modNs = getModuleNamespace(schemaNode.getQName(),
                                             insIdCtx.getSchemaContext());
        String nodeName = schemaNode.getQName().getLocalName();

        switch (p.format) {
            case JSON:
                return getUpdatedJsonReq(req, nodeName, modNs.moduleName());

            case XML:
                return getXmlReqForPutOp(req, nodeName, modNs.moduleNs());

            default:
                throw new SvcLogicException(format(FORMAT_ERR, p.format));
        }
    }

    /**
     * Returns the updated JSON request message, when the HTTP method
     * points to PUT or PATCH.
     *
     * @param req      current JSON request message
     * @param nodeName root node name
     * @param modName  module name of the root node
     * @return update JSON request message
     */
    private String getUpdatedJsonReq(String req, String nodeName,
                                     String modName) {
        Writer writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent(repeat(" ", 4));

        JsonParser jsonParser = new JsonParser();
        JsonObject oldJson = (JsonObject)jsonParser.parse(req);
        oldJson = remChildModName(oldJson, modName);
        JsonObject newJson = new JsonObject();
        newJson.add(modName + COLON + nodeName, oldJson.deepCopy());

        Gson gson= new Gson();
        gson.toJson(newJson, jsonWriter);
        return writer.toString();
    }

    /**
     * Removes module name from all the updated first level child node, if it
     * is same as the root node added.
     *
     * @param oldJson JSON object for old request
     * @param modName module name of root node
     * @return JSON object for old request with updated child module name
     */
    private JsonObject remChildModName(JsonObject oldJson, String modName) {
        Iterator<Map.Entry<String, JsonElement>> it = oldJson.entrySet().iterator();
        Map<String, JsonElement> m = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, JsonElement> jNode = it.next();
            if (jNode.getKey().contains(COLON)) {
                String[] modArr = jNode.getKey().split(COLON);
                if (modArr[0].equals(modName)) {
                    it.remove();
                    m.put(modArr[1], jNode.getValue());
                }
            }
        }
        if (!m.isEmpty()) {
            for (Map.Entry<String, JsonElement> element : m.entrySet()) {
                oldJson.add(element.getKey(), element.getValue());
            }
        }
        return oldJson;
    }

    /**
     * Returns the updated XML request message, when the HTTP method points
     * to PUT or PATCH.
     *
     * @param req      current JSON request message
     * @param nodeName root node name
     * @param modNs    module namespace of the root node
     * @return update JSON request message
     * @throws SvcLogicException when XML parsing fails
     */
    private String getXmlReqForPutOp(String req, String nodeName,
                                     URI modNs) throws SvcLogicException {
        req = getUpdatedXmlReq(req, nodeName, modNs.toString());
        Document oldDoc;
        try {
            oldDoc = DocumentHelper.parseText(req);
        } catch (DocumentException e) {
            throw new SvcLogicException(XML_TREE_ERR, e);
        }
        Writer writer = getXmlWriter(
                UTF_HEADER + oldDoc.getRootElement().asXML(), "4");
        return writer.toString();
    }
}
