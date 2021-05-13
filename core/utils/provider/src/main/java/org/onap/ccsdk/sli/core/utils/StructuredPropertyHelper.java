/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to assemble properties that are defined using a structured name into groups, and allow them to be
 * processed as sets of definitions.
 * <p>
 * For example, a structured name uses a dotted-notation, like "provider.name". Further, the nodes of the structured
 * name may be serialized using a suffix ordinal number (e.g., "provider1.name"). These structured properties form a
 * hierarchical name space where the names are grouped together and can be retrieved as a set.
 * </p>
 *
 */

public class StructuredPropertyHelper {

    /**
     * This method scans the properties object for all properties that match the root name and constructs a list of
     * structured property node graphs that represents the namespaces of the properties.
     * <p>
     * For example, assume that there are structured properties of the form "provider1.name", "provider2.name",
     * "provider3.name", and so forth. There may also be other subordinate properties as well (e.g., "provider1.type").
     * This method would construct a list of graphs of nodes, where each node represents one value of the structured
     * name. The roots would be the values "provider1", "provider2", "provider3", and so forth. The values of the
     * subordinate nodes would be the second, third, and so forth name nodes of the compound name. The value of the
     * property is associated with nodes that are representative of the leaf of the name space.
     * </p>
     *
     * @param properties
     *            The properties to be processed
     * @param prefix
     *            The prefix of the root structured property name
     * @return The node graph of the properties
     */
    public static List<Node> getStructuredProperties(Properties properties, String prefix) {
        List<Node> roots = new ArrayList<>();

        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith(prefix)) {
                String value = properties.getProperty(name);
                processNamespace(roots, name, value);
            }
        }

        return roots;
    }

    /**
     * This method recursively walks the name space of the structured property and constructs the node graph to
     * represent the property
     *
     * @param nodes
     *            The collection of nodes for the current level of the name space
     * @param propertyName
     *            The name of the node
     * @param value
     *            The value, if any
     * @return The node for this level in the namespace
     */
    @SuppressWarnings("nls")
    private static Node processNamespace(List<Node> nodes, String propertyName, String value) {
        String[] tokens = propertyName.split("\\.", 2);
        String nodeName = normalizeNodeName(tokens[0]);

        Node namespaceNode = null;
        for (Node node : nodes) {
            if (node.getName().equals(nodeName)) {
                namespaceNode = node;
                break;
            }
        }
        if (namespaceNode == null) {
            namespaceNode = new Node();
            namespaceNode.setName(nodeName);
            nodes.add(namespaceNode);
        }

        if (tokens.length == 1 || tokens[1] == null || tokens[1].length() == 0) {
            namespaceNode.setValue(value);
        } else {
            processNamespace(namespaceNode.getChildren(), tokens[1], value);
        }

        return namespaceNode;
    }

    /**
     * This method normalizes a node name of the structured property name by removing leading and trailing whitespace,
     * and by converting any ordinal position to a simple expression without leading zeroes.
     *
     * @param token
     *            The token to be normalized
     * @return The normalized name, or null if the token was null;
     */
    @SuppressWarnings("nls")
    private static String normalizeNodeName(String token) {
        if (token == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(token.trim());
        Pattern pattern = Pattern.compile("([^0-9]+)([0-9]*)");
        Matcher matcher = pattern.matcher(builder);
        if (matcher.matches()) {
            String nameRoot = matcher.group(1);
            String ordinal = matcher.group(2);
            if (ordinal != null && ordinal.length() > 0) {
                int i = Integer.parseInt(ordinal);
                builder.setLength(0);
                builder.append(nameRoot);
                builder.append(Integer.toString(i));
            }
        }
        return builder.toString();
    }

    /**
     * This class represents a node in the structured property name space
     *
     */
    public static class Node implements Comparable<Node> {

        /**
         * The name of the structured property node
         */
        private String name;

        /**
         * If the node is a leaf, then the value of the property
         */
        private String value;

        /**
         * If the node is not a leaf, then the sub-nodes of the property
         */
        private List<Node> children;

        /**
         * @return the value of name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name
         *            the value for name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the value of value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value
         *            the value for value
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * @return the value of children
         */
        public List<Node> getChildren() {
            if (children == null) {
                children = new ArrayList<>();
            }
            return children;
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return name.hashCode() + (value != null ? value.hashCode() : children.hashCode());
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;

            Node other = (Node) obj;
            boolean result = name.equals(other.name);

            if (value == null) {
                result &= other.value == null;
            } else {
                result &= value.equals(other.value);
            }
            if (children == null) {
                result &= other.children == null;
            } else {
                result &= children.equals(other.children);
            }
            return result;
        }

        /**
         * @see Object#toString()
         */
        @SuppressWarnings("nls")
        @Override
        public String toString() {
            if (value != null) {
                return String.format("%s = %s", name, value);
            }
            return String.format("%s.%s", name, children.toString());
        }

        @Override
        public int compareTo(Node o) {
            return name.compareTo(o.name);
        }
    }
}
