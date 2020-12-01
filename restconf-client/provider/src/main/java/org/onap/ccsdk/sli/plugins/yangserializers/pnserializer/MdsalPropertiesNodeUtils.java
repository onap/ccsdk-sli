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

package org.onap.ccsdk.sli.plugins.yangserializers.pnserializer;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.restconf.common.context.InstanceIdentifierContext;
import org.opendaylight.restconf.common.errors.RestconfDocumentedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.opendaylight.restconf.nb.rfc8040.utils.parser.ParserIdentifier.toInstanceIdentifier;

/**
 * Represents utilities for properties node tree.
 */
public final class MdsalPropertiesNodeUtils {

    static final String COLON = ":";

    static final String UNDERSCORE = "_";

    static final String SLASH = "/";

    static final String DOT_REGEX = "\\.";

    private static final String INFO_MSG = "The %s formed is currently not" +
            " valid";

    private static final String EXC_MSG = "Unable to form a formatted path";

    /**
     * Logger for the Mdsal properties util class.
     */
    private static final Logger log = LoggerFactory.getLogger(
            MdsalPropertiesNodeUtils.class);

    private MdsalPropertiesNodeUtils() {
    }

    /**
     * Returns the index from multi instance property name.
     *
     * @param name name of the property
     * @return index from multi instance property name
     */
    public static String getIndex(String name) {
        return name.substring(name.indexOf("[") + 1,
                              name.indexOf("]"));
    }

    /**
     * Returns the multi instance property name.
     *
     * @param name name of the property
     * @return the multi instance property name
     */
    public static String getListName(String name) {
        String[] s = name.split("\\[");
        return s[0];
    }

    /**
     * Returns true if property is multi instance.
     *
     * @param name name of the property
     * @return true if property is multi instance
     */
    public static boolean isListEntry(String name) {
        String s[] = name.split("\\[");
        return s.length > 1;
    }

    /**
     * Returns name of the property after pruning namespace and
     * index if the property is multi instance.
     *
     * @param name name of the property
     * @return name of the property
     */
    static String resolveName(String name) {
        String localName = getListName(name);
        final int lastIndexOfColon = localName.lastIndexOf(":");
        if (lastIndexOfColon != -1) {
            localName = localName.substring(lastIndexOfColon + 1);
        }
        return localName;
    }

    /**
     * Returns name of the property after pruning namespace and index if the
     * property is multi instance by knowing the module name from namespace.
     *
     * @param ns   namespace
     * @param name name of the node
     * @return resolved name
     */
    static String resolveName(Namespace ns, String name) {
        String localName = getListName(name);
        String modName = ns.moduleName();
        if ((localName.contains(COLON) || localName.contains(UNDERSCORE))
                && localName.startsWith(modName)) {
            localName = localName.substring(modName.length()+1);
        }
        return localName;
    }

    /**
     * Adds current node to parent's augmentation map.
     *
     * @param augSchema augment schema
     * @param parent parent property node
     * @param curNode current property node
     */
    public static void addToAugmentations(AugmentationSchemaNode augSchema,
                                          PropertiesNode parent,
                                          PropertiesNode curNode) {
        Collection<PropertiesNode> childsFromAugmentation = parent
                .augmentations().get(augSchema);
        if (!childsFromAugmentation.isEmpty()) {
            for (PropertiesNode pNode : childsFromAugmentation) {
                if (pNode.name().equals(curNode.name())) {
                    return;
                }
            }
        }
        parent.augmentations().put(augSchema, curNode);
    }


    /**
     * Returns augmented properties node if it is already
     * added in properties tree.
     *
     * @param augSchema augmented schema node
     * @param parent parent properties node
     * @param name name of the properties
     * @return augmented properties node if it is already added
     */
    public static PropertiesNode getAugmentationNode(
            AugmentationSchemaNode augSchema,
            PropertiesNode parent, String name) {
        if (augSchema == null) {
            return null;
        }

        Collection<PropertiesNode> childsFromAugmentation = parent
            .augmentations().get(augSchema);
        if (!childsFromAugmentation.isEmpty()) {
            for (PropertiesNode pNode : childsFromAugmentation) {
                if (pNode.name().equals(name)) {
                    return pNode;
                }
            }
        }

        return null;
    }

    /**
     * Creates uri with specified name and namespace.
     *
     * @param parent parent properties node
     * @param name name of the node
     * @param ns namespace of the node
     * @return uri with specified name and namespace
     */
    public static String getUri(PropertiesNode parent, String name,
                                Namespace ns) {
        String uri = name;
        if (!(parent.namespace().moduleNs().equals(ns.moduleNs()))) {
            uri = ns.moduleName() + ":" + name;
        }
        return parent.uri() + "." + uri;
    }

    /**
     * Creates new properties with specified parameters.
     *
     * @param name name of the properties node
     * @param namespace namespace of the properties node
     * @param uri uri of the properties node
     * @param parent parent node
     * @param appInfo application info
     * @param type node type
     * @return new properties node
     * @throws SvcLogicException exception while creating properties node
     */
    public static PropertiesNode createNode(String name, Namespace namespace,
                                            String uri, PropertiesNode parent,
                                            Object appInfo, NodeType type)
            throws SvcLogicException {
        switch (type) {
            case SINGLE_INSTANCE_NODE:
                return new SingleInstanceNode(name, namespace, uri, parent, appInfo, type);
            case MULTI_INSTANCE_HOLDER_NODE:
                return new ListHolderNode(name, namespace, uri, parent, appInfo, type);
            case MULTI_INSTANCE_LEAF_HOLDER_NODE:
                return new LeafListHolderNode(name, namespace, uri, parent, appInfo, type);
            default:
                throw new SvcLogicException("Invalid node type " + type);
        }
    }

    /**
     * Returns true if namespace is same as parent's namespace.
     *
     * @param parent parent property node
     * @param curNode current property node
     * @return true if namespace is same as parent namespace
     */
    public static boolean isNamespaceAsParent(PropertiesNode parent,
                                              PropertiesNode curNode) {
        return parent.namespace().moduleNs().equals(curNode.namespace().moduleNs());
    }

    /**
     * Returns the schema path holder with a formatted url and the instance
     * identifier context from a given uri or the parameters from svc logic
     * context.
     *
     * @param uri     unformatted uri or parameter
     * @param context schema context
     * @return schema path holder
     */
    public static SchemaPathHolder getProcessedPath(String uri,
                                                    SchemaContext context) {

        String uri1 = uri.replaceAll(UNDERSCORE, COLON);
        try {
            InstanceIdentifierContext<?> id = toInstanceIdentifier(
                    uri1, context, null);
            return new SchemaPathHolder(id, uri1);
        } catch (IllegalArgumentException | RestconfDocumentedException
                | NullPointerException e) {
            log.info("Exception while converting uri to instance identifier" +
                " context. Process each node in uri to get instance identifier" +
                " context " + e);
            return processNodesAndAppendPath(uri, context);
        }
    }

    /**
     * Processes the nodes in the given uri and finds instance identifier
     * context till it reaches the last node in uri. If its not able to find
     * schema for the path, it appends the suffix part and puts it back in
     * the param list.
     *
     * @param uri     uri with underscore
     * @param context schema context
     * @return schema and path holder
     */
    private static SchemaPathHolder processNodesAndAppendPath(String uri,
                                                              SchemaContext context) {

        String actPath = "";
        SchemaPathHolder id = new SchemaPathHolder(null, "");
        String[] uriParts = uri.split(SLASH);
        String sec = "";
        if (uri.contains(UNDERSCORE)) {
            sec = uri.substring(uriParts[0].length()+1);
        }
        for (int i = 0; i<uriParts.length; i++) {

            try {
                id = processIdentifier(uriParts[i], context, actPath);
            } catch (IllegalArgumentException e) {
                log.info(format(EXC_MSG, e));
                id.setUri(actPath+ uriParts[i] + sec);
                return id;
            }

            actPath = actPath + id.getUri() + SLASH;
            if (sec.startsWith(SLASH)) {
                sec = sec.replaceFirst(SLASH, "");
            }
            if (i+1 < uriParts.length) {
                sec = sec.replaceFirst(quote(uriParts[i + 1]), "");
            }
        }
        id.setUri(actPath.substring(0,actPath.length() - 1));
        return id;
    }

    /**
     * Processes the schema and path holder for a given node in the path. It
     * figures if the path is valid by replacing underscore in the node
     * consecutively, till it finds the proper schema for the node.
     *
     * @param node    node in the path
     * @param context schema context
     * @param prefix  prefix for the node in the path
     * @return schema and path holder
     */
    private static SchemaPathHolder processIdentifier(String node,
                                                      SchemaContext context,
                                                      String prefix) {

        String[] values = node.split(UNDERSCORE);
        String val = values[0];
        StringBuilder firstHalf = new StringBuilder();
        String secondHalf = "";
        if (node.contains(UNDERSCORE)) {
            secondHalf = node.substring(values[0].length()+1);
        }
        InstanceIdentifierContext<?> id;
        for (int i = 0; i< values.length-1; i++) {
            val = values[i];
            val = firstHalf + val + COLON + secondHalf;
            try {
                id = toInstanceIdentifier(prefix + val, context, null);
                return new SchemaPathHolder(id, val);
            } catch (IllegalArgumentException | RestconfDocumentedException |
                    NullPointerException e) {
                log.info(format(INFO_MSG, val, e));
            }
            firstHalf.append(values[i]).append(UNDERSCORE);
            secondHalf = secondHalf.replaceFirst(
                    values[i + 1] + UNDERSCORE,"");
        }
        val = val.replace(COLON,UNDERSCORE);
        try {
            id = toInstanceIdentifier(prefix + val, context, null);
            return new SchemaPathHolder(id, val);
        } catch (IllegalArgumentException | RestconfDocumentedException |
                NullPointerException e1) {
            throw new IllegalArgumentException(EXC_MSG, e1);
        }
    }

    /**
     * Returns the namespace of the given node name. If the node name is
     * separated by colon, the it splits with colon and forms the namespace.
     * If the node name is formed with underscore, then it splits the node
     * name consecutively to figure out the proper module name.
     *
     * @param childName node name
     * @param ctx       schema context
     * @param parent    parent properties node
     * @param curSchema current schema
     * @return namespace of the given node
     */
    static Namespace getNamespace(String childName, SchemaContext ctx,
                                  PropertiesNode parent, SchemaNode curSchema) {

        Namespace parentNs = parent.namespace();
        Namespace ns = new Namespace(parentNs.moduleName(),
                                     parentNs.moduleNs(), parentNs.revision());
        int lastIndexOfColon = childName.lastIndexOf(COLON);
        if (lastIndexOfColon != -1) {
            String moduleName = childName.substring(0, lastIndexOfColon);
            childName = childName.substring(lastIndexOfColon+1);
            Namespace ns1 = getNs(moduleName, ctx);
            if (ns1 != null) {
                ns = ns1;
            }
        }

        SchemaNode child = getChildSchemaNode(curSchema, childName, ns);

        if (child == null && childName.contains(UNDERSCORE)) {
            String[] children = childName.split(UNDERSCORE);
            String second = childName.substring(children[0].length() + 1);
            StringBuilder first = new StringBuilder();

            for (int i =0; i< children.length; i++) {
                String moduleName = first + children[i];
                Namespace newNs = getNs(moduleName, ctx);
                if (newNs != null) {
                    return newNs;
                }
                first.append(children[i]).append(UNDERSCORE);
                if (i + 1 < children.length) {
                    second = second.replaceFirst(
                            children[i + 1] + UNDERSCORE, "");
                }
            }
            return ns;
        }
        return ns;
    }

    /**
     * Returns the namespace by finding the given module in the schema context.
     *
     * @param modName module name
     * @param ctx     schema context
     * @return namespace of the given node name
     */
    private static Namespace getNs(String modName, SchemaContext ctx) {
        Iterator<Module> it = ctx.findModules(modName).iterator();
        if (it.hasNext()) {
            Module m = it.next();
            return new Namespace(modName, m.getQNameModule().getNamespace(),
                                 getRevision(m.getRevision()));
        }
        return null;
    }

    /**
     * Returns child schema node.
     *
     * @param curSchema current schema node
     * @param name name of the property
     * @param namespace namespace of the property
     * @return child schema node
     */
    public static SchemaNode getChildSchemaNode(SchemaNode curSchema,
                                                String name,
                                                Namespace namespace) {
        if (namespace == null) {
            return null;
        }

        QName qname =  QName.create(namespace.moduleNs(),
                                    Revision.of(namespace.revision()), name);

        // YANG RPC will not be instance of DataSchemaNode
        if (curSchema instanceof DataSchemaNode) {
            Deque<DataSchemaNode> schemaNodeDeque = ParserStreamUtils.
                    findSchemaNodeByNameAndNamespace(((DataSchemaNode)
                            curSchema), name, namespace.moduleNs());
            if (schemaNodeDeque.isEmpty()) {
                // could not find schema node
                return null;
            }

            DataSchemaNode schemaNode = schemaNodeDeque.pop();
            if (schemaNodeDeque.isEmpty()){
                // Simple node
                return schemaNode;
            }

            // node is child of Choice/case
            return SchemaUtils.findSchemaForChild(((ChoiceSchemaNode) schemaNode),
                                                  qname);
        } else {
            return SchemaUtils.findDataChildSchemaByQName(curSchema, qname);
        }
    }

    /**
     * Returns the property node type.
     *
     * @param index current index
     * @param length length of the properties
     * @param name name of the property
     * @return the property node type
     */
    public static NodeType getNodeType(int index, int length, String name,
                                       SchemaNode schema) {
        if (index == length-1) {
            if (schema instanceof AnyXmlSchemaNode){
                return NodeType.ANY_XML_NODE;
            }
            return (isListEntry(name) ? NodeType.MULTI_INSTANCE_LEAF_NODE :
                    NodeType.SINGLE_INSTANCE_LEAF_NODE);
        } else {
            return (isListEntry(name) ? NodeType.MULTI_INSTANCE_NODE :
                    NodeType.SINGLE_INSTANCE_NODE);
        }
    }

    /**
     * Returns revision in string.
     *
     * @param r YANG revision
     * @return revision in string
     */
    public static String getRevision(Optional<Revision> r) {
        return (r.isPresent()) ? r.get().toString() : null;
    }

    /**
     * Returns value namespace for leaf value.
     *
     * @param value value of the leaf
     * @param ctx schema context
     * @return value namespace
     * @throws SvcLogicException if identity/module could not be found
     */
    static Namespace getValueNamespace(String value,
                                              SchemaContext ctx)
            throws SvcLogicException {
        String prefix = getPrefixFromValue(value);
        if (prefix == null) {
            return null;
        }

        IdentitySchemaNode id = IdentityCodecUtil.parseIdentity(value,
                                                                ctx,
                                                                prefixToModule -> {
            final Iterator<Module> modules = ctx.findModules(prefix).iterator();
            checkArgument(modules.hasNext(), "Could not find " +
                                  "module %s", prefix);
            return modules.next().getQNameModule();
        });

        if (id == null) {
            throw new SvcLogicException("Could not find identity");
        }

        return getModuleNamespace(id.getQName(), ctx);
    }

    private static String getPrefixFromValue(String value) {
        int lastIndexOfColon = value.lastIndexOf(":");
        if (lastIndexOfColon != -1) {
            return value.substring(0, lastIndexOfColon);
        }
        return null;
    }

    /**
     * Returns module namespace from a given qName.
     *
     * @param qName qName of a node
     * @param ctx   schema context
     * @return module namespace of the node
     * @throws SvcLogicException when the module is not available
     */
    public static Namespace getModuleNamespace(QName qName, SchemaContext ctx)
            throws SvcLogicException {
        Optional<Module> module = ctx.findModule(qName.getModule());
        if (!module.isPresent()) {
            throw new SvcLogicException("Could not find module node");
        }
        Module m = module.get();
        return new Namespace(m.getName(), m.getQNameModule().getNamespace(),
                             getRevision(m.getRevision()));
    }

    static String getParsedValue(Namespace valNs, String value) {
        if (valNs != null && value.contains(":")) {
            String[] valArr = value.split(":");
            return valArr[1];
        }
        return value;
    }
}
