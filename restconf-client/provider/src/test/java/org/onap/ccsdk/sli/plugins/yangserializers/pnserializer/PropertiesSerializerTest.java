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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.restconf.common.context.InstanceIdentifierContext;
import org.opendaylight.restconf.nb.rfc8040.utils.parser.ParserIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public final class PropertiesSerializerTest {
    private SchemaContext context;

    @Before
    public void initialization() throws FileNotFoundException {
        context = compileYangFile();
    }

    @Test
    public void testBasicConstructs() throws SvcLogicException {
        String uri = "test-yang:cont1/cont2";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont1.cont2.cont3.leaf10", "abc");
        params.put("test-yang_cont1.cont2.list1[0].leaf1", "abc");
        params.put("test-yang_cont1.cont2.list1[0].leaf2", "abc");
        params.put("test-yang_cont1.cont2.list1[0].leaf3", "abc");
        params.put("test-yang_cont1.cont2.list1[0].ll1[0]", "abc");
        params.put("test-yang_cont1.cont2.list1[0].ll1[1]", "abc");
        params.put("test-yang_cont1.cont2.list1[0].ll2[0]", "abc");
        params.put("test-yang_cont1.cont2.list1[0].ll2[1]", "abc");
        params.put("test-yang_cont1.cont2.list1[0].cont4.leaf11", "abc");
        params.put("test-yang_cont1.cont2.list1[0].list4[0].leaf8", "abc");
        params.put("test-yang_cont1.cont2.list1[0].list4[1].leaf8", "abc");
        params.put("test-yang_cont1.cont2.list1[0].list5[0].leaf9", "abc");
        params.put("test-yang_cont1.cont2.list1[0].list5[1].leaf9", "abc");
        params.put("test-yang_cont1.cont2.list1[1].leaf1", "abc");
        params.put("test-yang_cont1.cont2.list1[1].leaf2", "abc");
        params.put("test-yang_cont1.cont2.list1[1].leaf3", "abc");
        params.put("test-yang_cont1.cont2.list1[1].ll1[0]", "abc");
        params.put("test-yang_cont1.cont2.list1[1].ll1[1]", "abc");
        params.put("test-yang_cont1.cont2.list1[1].ll2[0]", "abc");
        params.put("test-yang_cont1.cont2.list1[1].ll2[1]", "abc");
        params.put("test-yang_cont1.cont2.list1[1].cont4.leaf11", "abc");
        params.put("test-yang_cont1.cont2.list1[1].list4[0].leaf8", "abc");
        params.put("test-yang_cont1.cont2.list1[1].list4[1].leaf8", "abc");
        params.put("test-yang_cont1.cont2.list1[1].list5[0].leaf9", "abc");
        params.put("test-yang_cont1.cont2.list1[1].list5[1].leaf9", "abc");
        params.put("test-yang_cont1.cont2.list2[0].leaf4", "abc");
        params.put("test-yang_cont1.cont2.list2[1].leaf4", "abc");
        params.put("test-yang_cont1.cont2.leaf5", "abc");
        params.put("test-yang_cont1.cont2.leaf6", "abc");
        params.put("test-yang_cont1.cont2.ll3[0]", "abc");
        params.put("test-yang_cont1.cont2.ll3[1]", "abc");
        params.put("test-yang_cont1.cont2.ll4[0]", "abc");
        params.put("test-yang_cont1.cont2.ll4[1]", "abc");
        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);

        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont3"), is(true));
        SingleInstanceNode cont3 = ((SingleInstanceNode) childNodes.get("cont3"));
        assertThat(cont3.uri(), is("test-yang:cont1.cont2.cont3"));
        assertThat(cont3.children().containsKey("leaf10"), is(true));

        assertThat(childNodes.containsKey("list1"), is(true));
        HolderNode list1Holder = ((ListHolderNode) childNodes.get("list1"));
        assertThat(list1Holder.uri(), is("test-yang:cont1.cont2.list1"));
        MultiInstanceNode list10 = ((MultiInstanceNode) list1Holder.child("0"));
        assertThat(list10.uri(), is("test-yang:cont1.cont2.list1[0]"));
        Map<String, DataNodeChild> list10Child = list10.children();
        assertThat(list10Child.containsKey("leaf1"), is(true));
        LeafNode l = ((LeafNode) list10Child.get("leaf1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].leaf1"));
        assertThat(list10Child.containsKey("leaf2"), is(true));
        l = ((LeafNode) list10Child.get("leaf2"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].leaf2"));
        assertThat(list10Child.containsKey("leaf2"), is(true));
        l = ((LeafNode) list10Child.get("leaf3"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].leaf3"));

        LeafListHolderNode ll1Holder = ((LeafListHolderNode) list10Child.get("ll1"));
        assertThat(ll1Holder.uri(), is("test-yang:cont1.cont2.list1[0].ll1"));
        assertThat(ll1Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll1Holder.child("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].ll1[0]"));
        assertThat(ll1Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll1Holder.child("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].ll1[1]"));

        LeafListHolderNode ll2Holder = ((LeafListHolderNode) list10Child.get("ll2"));
        assertThat(ll2Holder.uri(), is("test-yang:cont1.cont2.list1[0].ll2"));
        assertThat(ll2Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll2Holder.child("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].ll2[0]"));
        assertThat(ll2Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll2Holder.child("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].ll2[1]"));

        SingleInstanceNode cont4 = ((SingleInstanceNode) list10Child.get("cont4"));
        assertThat(cont4.uri(), is("test-yang:cont1.cont2.list1[0].cont4"));
        assertThat(cont4.children().containsKey("leaf11"), is(true));
        l = ((LeafNode) cont4.children().get("leaf11"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].cont4.leaf11"));

        HolderNode list4Holder = ((HolderNode) list10Child.get("list4"));
        assertThat(list4Holder.uri(), is("test-yang:cont1.cont2.list1[0].list4"));
        Map<String, PropertiesNode> c = list4Holder.children();
        MultiInstanceNode list40 = ((MultiInstanceNode) c.get("0"));
        assertThat(list40.uri(), is("test-yang:cont1.cont2.list1[0].list4[0]"));
        assertThat(list40.children().containsKey("leaf8"), is(true));
        l = ((LeafNode) list40.children().get("leaf8"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].list4[0].leaf8"));
        MultiInstanceNode list41 = ((MultiInstanceNode) c.get("1"));
        assertThat(list41.uri(), is("test-yang:cont1.cont2.list1[0].list4[1]"));
        assertThat(list41.children().containsKey("leaf8"), is(true));
        l = ((LeafNode) list41.children().get("leaf8"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].list4[1].leaf8"));

        HolderNode list5Holder = ((HolderNode) list10Child.get("list5"));
        assertThat(list5Holder.uri(), is("test-yang:cont1.cont2.list1[0].list5"));
        c = list5Holder.children();
        MultiInstanceNode list50 = ((MultiInstanceNode) c.get("0"));
        assertThat(list50.uri(), is("test-yang:cont1.cont2.list1[0].list5[0]"));
        assertThat(list50.children().containsKey("leaf9"), is(true));
        l = ((LeafNode) list50.children().get("leaf9"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].list5[0].leaf9"));
        MultiInstanceNode list51 = ((MultiInstanceNode) c.get("1"));
        assertThat(list51.uri(), is("test-yang:cont1.cont2.list1[0].list5[1]"));
        assertThat(list51.children().containsKey("leaf9"), is(true));
        l = ((LeafNode) list51.children().get("leaf9"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[0].list5[1].leaf9"));

        MultiInstanceNode list11 = ((MultiInstanceNode) list1Holder.child("1"));
        assertThat(list11.uri(), is("test-yang:cont1.cont2.list1[1]"));
        Map<String, DataNodeChild> list11Child = list11.children();
        assertThat(list11Child.containsKey("leaf1"), is(true));
        l = ((LeafNode) list11Child.get("leaf1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].leaf1"));
        assertThat(list11Child.containsKey("leaf2"), is(true));
        l = ((LeafNode) list11Child.get("leaf2"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].leaf2"));
        assertThat(list11Child.containsKey("leaf3"), is(true));
        l = ((LeafNode) list11Child.get("leaf3"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].leaf3"));

        ll1Holder = ((LeafListHolderNode) list11Child.get("ll1"));
        assertThat(ll1Holder.uri(), is("test-yang:cont1.cont2.list1[1].ll1"));
        assertThat(ll1Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll1Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].ll1[0]"));
        assertThat(ll1Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll1Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].ll1[1]"));

        ll2Holder = ((LeafListHolderNode) list11Child.get("ll2"));
        assertThat(ll2Holder.uri(), is("test-yang:cont1.cont2.list1[1].ll2"));
        assertThat(ll2Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll2Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].ll2[0]"));
        assertThat(ll2Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll2Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].ll2[1]"));

        cont4 = ((SingleInstanceNode) list11Child.get("cont4"));
        assertThat(cont4.uri(), is("test-yang:cont1.cont2.list1[1].cont4"));
        assertThat(cont4.children().containsKey("leaf11"), is(true));
        l = ((LeafNode) cont4.children().get("leaf11"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].cont4.leaf11"));

        list4Holder = ((HolderNode) list11Child.get("list4"));
        assertThat(list4Holder.uri(), is("test-yang:cont1.cont2.list1[1].list4"));
        c = list4Holder.children();
        list40 = ((MultiInstanceNode) c.get("0"));
        assertThat(list40.uri(), is("test-yang:cont1.cont2.list1[1].list4[0]"));
        assertThat(list40.children().containsKey("leaf8"), is(true));
        l = ((LeafNode) list40.children().get("leaf8"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].list4[0].leaf8"));
        list41 = ((MultiInstanceNode) c.get("1"));
        assertThat(list41.uri(), is("test-yang:cont1.cont2.list1[1].list4[1]"));
        assertThat(list41.children().containsKey("leaf8"), is(true));
        l = ((LeafNode) list41.children().get("leaf8"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].list4[1].leaf8"));

        list5Holder = ((HolderNode) list11Child.get("list5"));
        assertThat(list5Holder.uri(), is("test-yang:cont1.cont2.list1[1].list5"));
        c = list5Holder.children();
        list50 = ((MultiInstanceNode) c.get("0"));
        assertThat(list50.uri(), is("test-yang:cont1.cont2.list1[1].list5[0]"));
        assertThat(list50.children().containsKey("leaf9"), is(true));
        l = ((LeafNode) list50.children().get("leaf9"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].list5[0].leaf9"));
        list51 = ((MultiInstanceNode) c.get("1"));
        assertThat(list51.uri(), is("test-yang:cont1.cont2.list1[1].list5[1]"));
        assertThat(list51.children().containsKey("leaf9"), is(true));
        l = ((LeafNode) list51.children().get("leaf9"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list1[1].list5[1].leaf9"));

        assertThat(childNodes.containsKey("list2"), is(true));
        HolderNode list2Holder = ((HolderNode) childNodes.get("list2"));
        assertThat(list2Holder.uri(), is("test-yang:cont1.cont2.list2"));
        InnerNode list20 = ((InnerNode) list2Holder.children().get("0"));
        assertThat(list20.uri(), is("test-yang:cont1.cont2.list2[0]"));
        assertThat(list20.children().containsKey("leaf4"), is(true));
        l = ((LeafNode) list20.children().get("leaf4"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list2[0].leaf4"));
        InnerNode list21 = ((InnerNode) list2Holder.children().get("1"));
        assertThat(list21.uri(), is("test-yang:cont1.cont2.list2[1]"));
        assertThat(list21.children().containsKey("leaf4"), is(true));
        l = ((LeafNode) list21.children().get("leaf4"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list2[1].leaf4"));

        assertThat(childNodes.containsKey("leaf5"), is(true));
        l = ((LeafNode) childNodes.get("leaf5"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.leaf5"));
        assertThat(childNodes.containsKey("leaf6"), is(true));
        l = ((LeafNode) childNodes.get("leaf6"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.leaf6"));

        HolderNode ll3Holder = ((HolderNode) childNodes.get("ll3"));
        assertThat(ll3Holder.uri(), is("test-yang:cont1.cont2.ll3"));
        assertThat(((LeafNode) ll3Holder.children().get("0")).name(), is("ll3"));
        l = ((LeafNode) ll3Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll3[0]"));
        assertThat(((LeafNode) ll3Holder.children().get("1")).name(), is("ll3"));
        l = ((LeafNode) ll3Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll3[1]"));

        HolderNode ll4Holder = ((HolderNode) childNodes.get("ll4"));
        assertThat(ll4Holder.uri(), is("test-yang:cont1.cont2.ll4"));
        assertThat(((LeafNode) ll4Holder.children().get("0")).name(), is("ll4"));
        l = ((LeafNode) ll4Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll4[0]"));
        assertThat(((LeafNode) ll4Holder.children().get("1")).name(), is("ll4"));
        l = ((LeafNode) ll4Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll4[1]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testAugment() throws SvcLogicException {
        String uri = "test-yang:cont1/cont2";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont1.cont2.cont4.leaf10", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_cont5.leaf13", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_list7[0].leaf14", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_list7[1].leaf14", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_leaf15", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_ll6[0]", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_ll6[1]", "abc");
        params.put("test-yang_cont1.cont2.list6[0].leaf11", "abc");
        params.put("test-yang_cont1.cont2.list6[1].leaf11", "abc");
        params.put("test-yang_cont1.cont2.leaf12", "abc");
        params.put("test-yang_cont1.cont2.ll5[0]", "abc");
        params.put("test-yang_cont1.cont2.ll5[1]", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont4"), is(true));
        SingleInstanceNode cont4 = ((SingleInstanceNode) childNodes.get("cont4"));
        for (Map.Entry<Object, Collection<PropertiesNode>> augToChild
                : cont4.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> child = augToChild.getValue();
            if (!child.isEmpty()) {
                List<String> expectedNodes = new LinkedList<>();
                expectedNodes.add("test-yang:cont1.cont2.cont4.test-augment:cont5");
                expectedNodes.add("test-yang:cont1.cont2.cont4.test-augment:list7");
                expectedNodes.add("test-yang:cont1.cont2.cont4.test-augment:leaf15");
                expectedNodes.add("test-yang:cont1.cont2.cont4.test-augment:ll6");
                assertThat(expectedNodes.size(), is(child.size()));
                for (PropertiesNode pNode : child) {
                    assertThat(expectedNodes.contains(pNode.uri()), is(true));
                    if (pNode.uri().equals("test-yang:cont1.cont2.cont4.test-augment:cont5")) {
                        assertThat(((SingleInstanceNode) pNode).children().containsKey("leaf13"),  is(true));
                        LeafNode l = ((LeafNode) ((SingleInstanceNode) pNode).children().get("leaf13"));
                        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont5.leaf13"));
                    } else if (pNode.uri().equals("test-yang:cont1.cont2.cont4.test-augment:list7")) {
                        ListHolderNode list7Holder = ((ListHolderNode) pNode);
                        MultiInstanceNode list7 = ((MultiInstanceNode) list7Holder.child("0"));
                        assertThat(list7.uri(), is("test-yang:cont1.cont2.cont4.test-augment:list7[0]"));
                        Map<String, DataNodeChild> list7Child = list7.children();
                        assertThat(list7Child.containsKey("leaf14"), is(true));
                        LeafNode l = ((LeafNode) list7Child.get("leaf14"));
                        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:list7[0].leaf14"));
                        list7 = ((MultiInstanceNode) list7Holder.child("1"));
                        assertThat(list7.uri(), is("test-yang:cont1.cont2.cont4.test-augment:list7[1]"));
                        list7Child = list7.children();
                        assertThat(list7Child.containsKey("leaf14"), is(true));
                        l = ((LeafNode) list7Child.get("leaf14"));
                        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:list7[1].leaf14"));
                    } else if (pNode.uri().equals("test-yang:cont1.cont2.cont4.test-augment:leaf15")) {
                        LeafNode leaf15 = ((LeafNode) pNode);
                        assertThat(leaf15.name(), is("leaf15"));
                        assertThat(leaf15.uri(), is("test-yang:cont1.cont2.cont4.test-augment:leaf15"));
                    } else if (pNode.uri().equals("test-yang:cont1.cont2.cont4.test-augment:ll6")) {
                        LeafListHolderNode ll6Holder = ((LeafListHolderNode) pNode);
                        assertThat(ll6Holder.children().containsKey("0"), is(true));
                        LeafNode l = ((LeafNode) ll6Holder.children().get("0"));
                        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:ll6[0]"));
                        assertThat(ll6Holder.children().containsKey("1"), is(true));
                        l = ((LeafNode) ll6Holder.children().get("1"));
                        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:ll6[1]"));
                    }
                }
            }
        }
        assertThat(cont4.uri(), is("test-yang:cont1.cont2.cont4"));
        assertThat(cont4.children().containsKey("leaf10"), is(true));
        LeafNode l = ((LeafNode) cont4.children().get("leaf10"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.leaf10"));

        assertThat(childNodes.containsKey("list6"), is(true));
        HolderNode list6Holder = ((ListHolderNode) childNodes.get("list6"));
        assertThat(list6Holder.uri(), is("test-yang:cont1.cont2.list6"));
        MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
        assertThat(list6.uri(), is("test-yang:cont1.cont2.list6[0]"));
        Map<String, DataNodeChild> list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf11"), is(true));
        l = ((LeafNode) list6Child.get("leaf11"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list6[0].leaf11"));
        list6 = ((MultiInstanceNode) list6Holder.child("1"));
        assertThat(list6.uri(), is("test-yang:cont1.cont2.list6[1]"));
        list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf11"), is(true));
        l = ((LeafNode) list6Child.get("leaf11"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.list6[1].leaf11"));

        assertThat(childNodes.containsKey("leaf12"), is(true));
        LeafNode leaf12 = ((LeafNode) childNodes.get("leaf12"));
        assertThat(leaf12.name(), is("leaf12"));
        assertThat(leaf12.uri(), is("test-yang:cont1.cont2.leaf12"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) childNodes.get("ll5"));
        assertThat(ll5Holder.uri(), is("test-yang:cont1.cont2.ll5"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll5[0]"));
        assertThat(ll5Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll5Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont1.cont2.ll5[1]"));

        Map<String, String> output = ser.decode(node);
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testChoiceCase1() throws SvcLogicException {
        String uri = "test-yang:cont8";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont8.cont6.leaf16", "abc");
        params.put("test-yang_cont8.list8[0].leaf18", "abc");
        params.put("test-yang_cont8.list8[1].leaf18", "abc");
        params.put("test-yang_cont8.leaf19", "abc");
        params.put("test-yang_cont8.ll7[0]", "abc");
        params.put("test-yang_cont8.ll7[1]", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont6"), is(true));
        SingleInstanceNode cont6 = ((SingleInstanceNode) childNodes.get("cont6"));
        assertThat(cont6.uri(), is("test-yang:cont8.cont6"));
        assertThat(cont6.children().containsKey("leaf16"), is(true));
        LeafNode l = ((LeafNode) cont6.children().get("leaf16"));
        assertThat(l.uri(), is("test-yang:cont8.cont6.leaf16"));

        assertThat(childNodes.containsKey("list8"), is(true));
        HolderNode list6Holder = ((ListHolderNode) childNodes.get("list8"));
        assertThat(list6Holder.uri(), is("test-yang:cont8.list8"));
        MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
        assertThat(list6.uri(), is("test-yang:cont8.list8[0]"));
        Map<String, DataNodeChild> list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf18"), is(true));
        l = ((LeafNode) list6Child.get("leaf18"));
        assertThat(l.uri(), is("test-yang:cont8.list8[0].leaf18"));
        list6 = ((MultiInstanceNode) list6Holder.child("1"));
        list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf18"), is(true));
        l = ((LeafNode) list6Child.get("leaf18"));
        assertThat(l.uri(), is("test-yang:cont8.list8[1].leaf18"));

        assertThat(childNodes.containsKey("leaf19"), is(true));
        LeafNode leaf12 = ((LeafNode) childNodes.get("leaf19"));
        assertThat(leaf12.name(), is("leaf19"));
        assertThat(leaf12.uri(), is("test-yang:cont8.leaf19"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) childNodes.get("ll7"));
        assertThat(ll5Holder.uri(), is("test-yang:cont8.ll7"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont8.ll7[0]"));
        assertThat(ll5Holder.children().containsKey("1"), is(true));
        l = ((LeafNode) ll5Holder.children().get("1"));
        assertThat(l.uri(), is("test-yang:cont8.ll7[1]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testChoiceCase2() throws SvcLogicException {
        String uri = "test-yang:cont9";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont9.leaf20", "abc");
        params.put("test-yang_cont9.ll8[0]", "abc");
        params.put("test-yang_cont9.cont11.leaf25", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont11"), is(true));
        SingleInstanceNode cont4 = ((SingleInstanceNode) childNodes.get("cont11"));
        assertThat(cont4.uri(), is("test-yang:cont9.cont11"));
        assertThat(cont4.children().containsKey("leaf25"), is(true));
        LeafNode l = ((LeafNode) cont4.children().get("leaf25"));
        assertThat(l.uri(), is("test-yang:cont9.cont11.leaf25"));

        assertThat(childNodes.containsKey("leaf20"), is(true));
        l = ((LeafNode) childNodes.get("leaf20"));
        assertThat(l.uri(), is("test-yang:cont9.leaf20"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) childNodes.get("ll8"));
        assertThat(ll5Holder.uri(), is("test-yang:cont9.ll8"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont9.ll8[0]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testChoiceCase3() throws SvcLogicException {
        String uri = "test-yang:cont8/cont6";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont8.cont6.test-augment_leaf21", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        for (Map.Entry<Object, Collection<PropertiesNode>> augToChild
                : node.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> child = augToChild.getValue();
            if (!child.isEmpty()) {
                List<String> expectedNodes = new LinkedList<>();
                expectedNodes.add("test-yang:cont8.cont6.test-augment:leaf21");
                assertThat(expectedNodes.size(), is(child.size()));
                for (PropertiesNode pNode : child) {
                    assertThat(expectedNodes.contains(pNode.uri()), is(true));
                }
            }
        }

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("leaf21"), is(true));
        LeafNode leaf12 = ((LeafNode) childNodes.get("leaf21"));
        assertThat(leaf12.name(), is("leaf21"));
        assertThat(leaf12.uri(), is("test-yang:cont8.cont6.test-augment:leaf21"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testGrouping() throws SvcLogicException {
        String uri = "test-yang:cont13";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont13.cont12.leaf26", "abc");
        params.put("test-yang_cont13.list9[0].leaf27", "abc");
        params.put("test-yang_cont13.leaf28", "abc");
        params.put("test-yang_cont13.ll9[0]", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont12"), is(true));
        SingleInstanceNode cont4 = ((SingleInstanceNode) childNodes.get("cont12"));
        assertThat(cont4.uri(), is("test-yang:cont13.cont12"));
        assertThat(cont4.children().containsKey("leaf26"), is(true));
        LeafNode l = ((LeafNode) cont4.children().get("leaf26"));
        assertThat(l.uri(), is("test-yang:cont13.cont12.leaf26"));

        assertThat(childNodes.containsKey("list9"), is(true));
        HolderNode list6Holder = ((ListHolderNode) childNodes.get("list9"));
        assertThat(list6Holder.uri(), is("test-yang:cont13.list9"));
        MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
        assertThat(list6.uri(), is("test-yang:cont13.list9[0]"));
        Map<String, DataNodeChild> list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf27"), is(true));
        l = ((LeafNode) list6Child.get("leaf27"));
        assertThat(l.uri(), is("test-yang:cont13.list9[0].leaf27"));

        assertThat(childNodes.containsKey("leaf28"), is(true));
        LeafNode leaf12 = ((LeafNode) childNodes.get("leaf28"));
        assertThat(leaf12.name(), is("leaf28"));
        assertThat(leaf12.uri(), is("test-yang:cont13.leaf28"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) childNodes.get("ll9"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testGrouping2() throws SvcLogicException {
        String uri = "test-yang:cont9/cont11";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont9.cont11.leaf25", "abc");
        params.put("test-yang_cont9.cont11.cont13.cont12.leaf26", "abc");
        params.put("test-yang_cont9.cont11.cont13.list9[0].leaf27", "abc");
        params.put("test-yang_cont9.cont11.cont13.leaf28", "abc");
        params.put("test-yang_cont9.cont11.cont13.ll9[0]", "abc");
        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont13"), is(true));
        SingleInstanceNode cont13 = ((SingleInstanceNode) childNodes.get("cont13"));
        assertThat(cont13.uri(), is("test-yang:cont9.cont11.cont13"));
        SingleInstanceNode cont12 = ((SingleInstanceNode) cont13.children().get("cont12"));
        assertThat(cont12.children().containsKey("leaf26"), is(true));
        assertThat(cont12.uri(), is("test-yang:cont9.cont11.cont13.cont12"));
        assertThat(cont12.children().containsKey("leaf26"), is(true));
        LeafNode l = ((LeafNode) cont12.children().get("leaf26"));
        assertThat(l.uri(), is("test-yang:cont9.cont11.cont13.cont12.leaf26"));

        assertThat(cont13.children().containsKey("list9"), is(true));
        HolderNode list6Holder = ((ListHolderNode) cont13.children().get("list9"));
        assertThat(list6Holder.uri(), is("test-yang:cont9.cont11.cont13.list9"));
        MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
        assertThat(list6.uri(), is("test-yang:cont9.cont11.cont13.list9[0]"));
        Map<String, DataNodeChild> list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf27"), is(true));
        l = ((LeafNode) list6Child.get("leaf27"));
        assertThat(l.uri(), is("test-yang:cont9.cont11.cont13.list9[0].leaf27"));

        assertThat(cont13.children().containsKey("leaf28"), is(true));
        LeafNode leaf12 = ((LeafNode) cont13.children().get("leaf28"));
        assertThat(leaf12.name(), is("leaf28"));
        assertThat(leaf12.uri(), is("test-yang:cont9.cont11.cont13.leaf28"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) cont13.children().get("ll9"));
        assertThat(ll5Holder.uri(), is("test-yang:cont9.cont11.cont13.ll9"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:cont9.cont11.cont13.ll9[0]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testGrouping3() throws SvcLogicException {
        String uri = "test-augment:cont13";
        Map<String, String> params = new HashMap<>();
        params.put("test-augment_cont13.cont12.leaf26", "abc");
        params.put("test-augment_cont13.list9[0].leaf27", "abc");
        params.put("test-augment_cont13.leaf28", "abc");
        params.put("test-augment_cont13.ll9[0]", "abc");
        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont12"), is(true));
        SingleInstanceNode cont12 = ((SingleInstanceNode) childNodes.get("cont12"));
        assertThat(cont12.uri(), is("test-augment:cont13.cont12"));
        assertThat(cont12.children().containsKey("leaf26"), is(true));
        LeafNode l = ((LeafNode) cont12.children().get("leaf26"));
        assertThat(l.uri(), is("test-augment:cont13.cont12.leaf26"));

        assertThat(childNodes.containsKey("list9"), is(true));
        HolderNode list6Holder = ((ListHolderNode) childNodes.get("list9"));
        assertThat(list6Holder.uri(), is("test-augment:cont13.list9"));
        MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
        assertThat(list6.uri(), is("test-augment:cont13.list9[0]"));
        Map<String, DataNodeChild> list6Child = list6.children();
        assertThat(list6Child.containsKey("leaf27"), is(true));
        l = ((LeafNode) list6Child.get("leaf27"));
        assertThat(l.uri(), is("test-augment:cont13.list9[0].leaf27"));

        assertThat(childNodes.containsKey("leaf28"), is(true));
        LeafNode leaf12 = ((LeafNode) childNodes.get("leaf28"));
        assertThat(leaf12.name(), is("leaf28"));
        assertThat(leaf12.uri(), is("test-augment:cont13.leaf28"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) childNodes.get("ll9"));
        assertThat(ll5Holder.uri(), is("test-augment:cont13.ll9"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-augment:cont13.ll9[0]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testGrouping4() throws SvcLogicException {
        String uri = "test-yang:cont1/cont2/cont4";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont1.cont2.cont4.test-augment_cont13.cont12.leaf26", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_cont13.list9[0].leaf27", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_cont13.leaf28", "abc");
        params.put("test-yang_cont1.cont2.cont4.test-augment_cont13.ll9[0]", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        for (Map.Entry<Object, Collection<PropertiesNode>> augToChild
                : node.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> child = augToChild.getValue();
            if (!child.isEmpty()) {
                List<String> expectedNodes = new LinkedList<>();
                expectedNodes.add("test-yang:cont1.cont2.cont4.test-augment:cont13");
                assertThat(expectedNodes.size(), is(child.size()));
                for (PropertiesNode pNode : child) {
                    assertThat(expectedNodes.contains(pNode.uri()), is(true));
                    SingleInstanceNode cont13 = ((SingleInstanceNode) pNode);
                    assertThat(cont13.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13"));
                    SingleInstanceNode cont12 = ((SingleInstanceNode) cont13.children().get("cont12"));
                    assertThat(cont12.children().containsKey("leaf26"), is(true));
                    LeafNode l = ((LeafNode) cont12.children().get("leaf26"));
                    assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.cont12.leaf26"));

                    assertThat(cont13.children().containsKey("list9"), is(true));
                    HolderNode list6Holder = ((ListHolderNode) cont13.children().get("list9"));
                    assertThat(list6Holder.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.list9"));
                    MultiInstanceNode list6 = ((MultiInstanceNode) list6Holder.child("0"));
                    assertThat(list6.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.list9[0]"));
                    Map<String, DataNodeChild> list6Child = list6.children();
                    assertThat(list6Child.containsKey("leaf27"), is(true));
                    l = ((LeafNode) list6Child.get("leaf27"));
                    assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.list9[0].leaf27"));

                    assertThat(cont13.children().containsKey("leaf28"), is(true));
                    LeafNode leaf12 = ((LeafNode) cont13.children().get("leaf28"));
                    assertThat(leaf12.name(), is("leaf28"));
                    assertThat(leaf12.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.leaf28"));

                    LeafListHolderNode ll5Holder = ((LeafListHolderNode) cont13.children().get("ll9"));
                    assertThat(ll5Holder.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.ll9"));
                    assertThat(ll5Holder.children().containsKey("0"), is(true));
                    l = ((LeafNode) ll5Holder.children().get("0"));
                    assertThat(l.uri(), is("test-yang:cont1.cont2.cont4.test-augment:cont13.ll9[0]"));
                }
            }
        }

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testRpcInput() throws SvcLogicException {
        String uri = "test-yang:create-sfc";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_create-sfc.input.cont14.leaf28", "abc");
        params.put("test-yang_create-sfc.input.list10[0].leaf29", "abc");
        params.put("test-yang_create-sfc.input.leaf30", "abc");
        params.put("test-yang_create-sfc.input.ll10[0]", "abc");
        params.put("test-yang_create-sfc.input.cont15.leaf31", "abc");
        params.put("test-yang_create-sfc.input.cont13.cont12.leaf26", "abc");
        params.put("test-yang_create-sfc.input.cont13.list9[0].leaf27", "abc");
        params.put("test-yang_create-sfc.input.cont13.leaf28", "abc");
        params.put("test-yang_create-sfc.input.cont13.ll9[0]", "abc");
        params.put("test-yang_create-sfc.input.test-augment_leaf36", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        PropertiesNode input = childNodes.get("input");
        assertThat(input.uri(), is("test-yang:create-sfc.input"));
        for (Map.Entry<Object, Collection<PropertiesNode>> augToChild
                : node.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> child = augToChild.getValue();
            if (!child.isEmpty()) {
                List<String> expectedNodes = new LinkedList<>();
                expectedNodes.add("test-yang:create-sfc.input.test-augment:leaf36");
                assertThat(expectedNodes.size(), is(child.size()));
                for (PropertiesNode pNode : child) {
                    assertThat(expectedNodes.contains(pNode.uri()), is(true));
                    LeafNode leaf37 = ((LeafNode) pNode);
                    assertThat(leaf37.name(), is("leaf36"));
                    assertThat(leaf37.uri(), is("test-yang:create-sfc.input.test-augment:leaf36"));
                }
            }
        }
        childNodes = ((InnerNode) input).children();

        assertThat(childNodes.containsKey("cont14"), is(true));
        SingleInstanceNode cont14 = ((SingleInstanceNode) childNodes.get("cont14"));
        assertThat(cont14.uri(), is("test-yang:create-sfc.input.cont14"));
        assertThat(cont14.children().containsKey("leaf28"), is(true));
        LeafNode l = ((LeafNode) cont14.children().get("leaf28"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.cont14.leaf28"));

        assertThat(childNodes.containsKey("list10"), is(true));
        HolderNode list10Holder = ((ListHolderNode) childNodes.get("list10"));
        assertThat(list10Holder.uri(), is("test-yang:create-sfc.input.list10"));
        MultiInstanceNode list10 = ((MultiInstanceNode) list10Holder.child("0"));
        assertThat(list10.uri(), is("test-yang:create-sfc.input.list10[0]"));
        Map<String, DataNodeChild> list10Child = list10.children();
        assertThat(list10Child.containsKey("leaf29"), is(true));
        l = ((LeafNode) list10Child.get("leaf29"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.list10[0].leaf29"));

        assertThat(childNodes.containsKey("leaf30"), is(true));
        LeafNode leaf30 = ((LeafNode) childNodes.get("leaf30"));
        assertThat(leaf30.name(), is("leaf30"));
        assertThat(leaf30.uri(), is("test-yang:create-sfc.input.leaf30"));

        LeafListHolderNode ll10Holder = ((LeafListHolderNode) childNodes.get("ll10"));
        assertThat(ll10Holder.uri(), is("test-yang:create-sfc.input.ll10"));
        assertThat(ll10Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll10Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.ll10[0]"));

        assertThat(childNodes.containsKey("cont15"), is(true));
        SingleInstanceNode cont15 = ((SingleInstanceNode) childNodes.get("cont15"));
        assertThat(cont15.uri(), is("test-yang:create-sfc.input.cont15"));
        assertThat(cont15.children().containsKey("leaf31"), is(true));
        l = ((LeafNode) cont15.children().get("leaf31"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.cont15.leaf31"));

        assertThat(childNodes.containsKey("cont13"), is(true));
        SingleInstanceNode cont13 = ((SingleInstanceNode) childNodes.get("cont13"));
        assertThat(cont13.uri(), is("test-yang:create-sfc.input.cont13"));
        SingleInstanceNode cont12 = ((SingleInstanceNode) cont13.children().get("cont12"));
        assertThat(cont12.uri(), is("test-yang:create-sfc.input.cont13.cont12"));
        assertThat(cont12.children().containsKey("leaf26"), is(true));
        l = ((LeafNode) cont12.children().get("leaf26"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.cont13.cont12.leaf26"));

        assertThat(cont13.children().containsKey("list9"), is(true));
        HolderNode list9Holder = ((ListHolderNode) cont13.children().get("list9"));
        assertThat(list9Holder.uri(), is("test-yang:create-sfc.input.cont13.list9"));
        MultiInstanceNode list9 = ((MultiInstanceNode) list9Holder.child("0"));
        assertThat(list9.uri(), is("test-yang:create-sfc.input.cont13.list9[0]"));
        Map<String, DataNodeChild> list6Child = list9.children();
        assertThat(list6Child.containsKey("leaf27"), is(true));
        l = ((LeafNode) list6Child.get("leaf27"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.cont13.list9[0].leaf27"));

        assertThat(cont13.children().containsKey("leaf28"), is(true));
        LeafNode leaf12 = ((LeafNode) cont13.children().get("leaf28"));
        assertThat(leaf12.name(), is("leaf28"));
        assertThat(leaf12.uri(), is("test-yang:create-sfc.input.cont13.leaf28"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) cont13.children().get("ll9"));
        assertThat(ll5Holder.uri(), is("test-yang:create-sfc.input.cont13.ll9"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:create-sfc.input.cont13.ll9[0]"));

        Map<String, String> output = ser.decode(node);
        assertThat(output.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testRpcOutput() throws SvcLogicException {
        String uri = "test-yang:create-sfc";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_create-sfc.output.cont16.leaf32", "abc");
        params.put("test-yang_create-sfc.output.list11[0].leaf33", "abc");
        params.put("test-yang_create-sfc.output.leaf34", "abc");
        params.put("test-yang_create-sfc.output.ll11[0]", "abc");
        params.put("test-yang_create-sfc.output.cont17.leaf35", "abc");
        params.put("test-yang_create-sfc.output.cont13.cont12.leaf26", "abc");
        params.put("test-yang_create-sfc.output.cont13.list9[0].leaf27", "abc");
        params.put("test-yang_create-sfc.output.cont13.leaf28", "abc");
        params.put("test-yang_create-sfc.output.cont13.ll9[0]", "abc");
        params.put("test-yang_create-sfc.output.test-augment_leaf37", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        PropertiesNode output = childNodes.get("output");
        assertThat(output.uri(), is("test-yang:create-sfc.output"));
        for (Map.Entry<Object, Collection<PropertiesNode>> augmentationToChild :
                node.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> c = augmentationToChild.getValue();
            if(!c.isEmpty()) {
                List<String> expectedNodes = new LinkedList<>();
                expectedNodes.add("test-yang:create-sfc.output.test-augment:leaf37");
                assertThat(expectedNodes.size(), is(expectedNodes));
                for (PropertiesNode pNode : c) {
                    assertThat(expectedNodes.contains(pNode.uri()), is(true));
                    LeafNode leaf37 = ((LeafNode) pNode);
                    assertThat(leaf37.name(), is("leaf37"));
                    assertThat(leaf37.uri(), is("test-yang:create-sfc.output.test-augment:leaf37"));
                }
            }
        }
        childNodes = ((InnerNode) output).children();

        assertThat(childNodes.containsKey("cont16"), is(true));
        SingleInstanceNode cont16 = ((SingleInstanceNode) childNodes.get("cont16"));
        assertThat(cont16.uri(), is("test-yang:create-sfc.output.cont16"));
        assertThat(cont16.children().containsKey("leaf32"), is(true));
        LeafNode l = ((LeafNode) cont16.children().get("leaf32"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.cont16.leaf32"));

        assertThat(childNodes.containsKey("list11"), is(true));
        HolderNode list11Holder = ((ListHolderNode) childNodes.get("list11"));
        assertThat(list11Holder.uri(), is("test-yang:create-sfc.output.list11"));
        MultiInstanceNode list11 = ((MultiInstanceNode) list11Holder.child("0"));
        assertThat(list11.uri(), is("test-yang:create-sfc.output.list11[0]"));
        Map<String, DataNodeChild> list11Child = list11.children();
        assertThat(list11Child.containsKey("leaf33"), is(true));
        l = ((LeafNode) list11Child.get("leaf33"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.list11[0].leaf33"));

        assertThat(childNodes.containsKey("leaf34"), is(true));
        LeafNode leaf34 = ((LeafNode) childNodes.get("leaf34"));
        assertThat(leaf34.name(), is("leaf34"));
        assertThat(leaf34.uri(), is("test-yang:create-sfc.output.leaf34"));

        LeafListHolderNode ll10Holder = ((LeafListHolderNode) childNodes.get("ll11"));
        assertThat(ll10Holder.uri(), is("test-yang:create-sfc.output.ll11"));
        assertThat(ll10Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll10Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.ll11[0]"));

        assertThat(childNodes.containsKey("cont17"), is(true));
        SingleInstanceNode cont17 = ((SingleInstanceNode) childNodes.get("cont17"));
        assertThat(cont17.uri(), is("test-yang:create-sfc.output.cont17"));
        assertThat(cont17.children().containsKey("leaf35"), is(true));
        l = ((LeafNode) cont17.children().get("leaf35"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.cont17.leaf35"));

        assertThat(childNodes.containsKey("cont13"), is(true));
        SingleInstanceNode cont13 = ((SingleInstanceNode) childNodes.get("cont13"));
        assertThat(cont13.uri(), is("test-yang:create-sfc.output.cont13"));
        SingleInstanceNode cont12 = ((SingleInstanceNode) cont13.children().get("cont12"));
        assertThat(cont12.uri(), is("test-yang:create-sfc.output.cont13.cont12"));
        assertThat(cont12.children().containsKey("leaf26"), is(true));
        l = ((LeafNode) cont12.children().get("leaf26"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.cont13.cont12.leaf26"));

        assertThat(cont13.children().containsKey("list9"), is(true));
        HolderNode list9Holder = ((ListHolderNode) cont13.children().get("list9"));
        assertThat(list9Holder.uri(), is("test-yang:create-sfc.output.cont13.list9"));
        MultiInstanceNode list9 = ((MultiInstanceNode) list9Holder.child("0"));
        assertThat(list9.uri(), is("test-yang:create-sfc.output.cont13.list9[0]"));
        Map<String, DataNodeChild> list6Child = list9.children();
        assertThat(list6Child.containsKey("leaf27"), is(true));
        l = ((LeafNode) list6Child.get("leaf27"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.cont13.list9[0].leaf27"));

        assertThat(cont13.children().containsKey("leaf28"), is(true));
        LeafNode leaf12 = ((LeafNode) cont13.children().get("leaf28"));
        assertThat(leaf12.name(), is("leaf28"));
        assertThat(leaf12.uri(), is("test-yang:create-sfc.output.cont13.leaf28"));

        LeafListHolderNode ll5Holder = ((LeafListHolderNode) cont13.children().get("ll9"));
        assertThat(ll5Holder.uri(), is("test-yang:create-sfc.output.cont13.ll9"));
        assertThat(ll5Holder.children().containsKey("0"), is(true));
        l = ((LeafNode) ll5Holder.children().get("0"));
        assertThat(l.uri(), is("test-yang:create-sfc.output.cont13.ll9[0]"));

        Map<String, String> output1 = ser.decode(node);
        assertThat(output1.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output1.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testContainerSameName() throws SvcLogicException {
        String uri = "test-yang:cont18";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont18.cont18.list12[0].list12[0].leaf36", "abc");
        params.put("test-yang_cont18.cont18.list12[0].leaf36", "hi");
        params.put("test-yang_cont18.cont18.list12[1].list12[0].leaf36", "xyz");
        params.put("test-yang_cont18.cont18.list12[1].list12[1].leaf36", "hey!");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();

        assertThat(childNodes.containsKey("cont18"), is(true));
        node = childNodes.get("cont18");
        assertThat(node.uri(), is("test-yang:cont18.cont18"));
        childNodes = ((InnerNode) node).children();

        assertThat(childNodes.containsKey("list12"), is(true));
        HolderNode holder = ((ListHolderNode) childNodes.get("list12"));
        assertThat(holder.uri(), is("test-yang:cont18.cont18.list12"));
        MultiInstanceNode node1 = ((MultiInstanceNode) holder.child("0"));
        assertThat(node1.uri(), is("test-yang:cont18.cont18.list12[0]"));
        Map<String, DataNodeChild> list12Child = node1.children();

        assertThat(list12Child.containsKey("leaf36"), is(true));
        LeafNode leaf = ((LeafNode) list12Child.get("leaf36"));
        assertThat(leaf.value(), is("hi"));
        assertThat(leaf.uri(), is("test-yang:cont18.cont18.list12[0].leaf36"));

        assertThat(list12Child.containsKey("list12"), is(true));
        HolderNode holder1 = ((ListHolderNode) list12Child.get("list12"));
        assertThat(holder1.uri(), is("test-yang:cont18.cont18.list12[0].list12"));
        node1 = ((MultiInstanceNode) holder1.child("0"));
        assertThat(node1.uri(), is("test-yang:cont18.cont18.list12[0].list12[0]"));
        list12Child = node1.children();
        assertThat(list12Child.containsKey("leaf36"), is(true));
        leaf = ((LeafNode) list12Child.get("leaf36"));
        assertThat(leaf.value(), is("abc"));
        assertThat(leaf.uri(), is("test-yang:cont18.cont18.list12[0].list12[0].leaf36"));

        node1 = ((MultiInstanceNode) holder.child("1"));
        assertThat(node1.uri(), is("test-yang:cont18.cont18.list12[1]"));
        list12Child = node1.children();
        assertThat(list12Child.containsKey("list12"), is(true));
        holder = ((ListHolderNode) list12Child.get("list12"));
        assertThat(holder.uri(), is("test-yang:cont18.cont18.list12[1].list12"));
        node1 = ((MultiInstanceNode) holder.child("0"));
        assertThat(node1.uri(), is("test-yang:cont18.cont18.list12[1].list12[0]"));
        assertThat(node1.children().containsKey("leaf36"), is(true));
        leaf = ((LeafNode) node1.children().get("leaf36"));
        assertThat(leaf.value(), is("xyz"));
        assertThat(leaf.uri(), is("test-yang:cont18.cont18.list12[1].list12[0].leaf36"));

        node1 = ((MultiInstanceNode) holder.child("1"));
        assertThat(node1.uri(), is("test-yang:cont18.cont18.list12[1].list12[1]"));
        assertThat(node1.children().containsKey("leaf36"), is(true));
        leaf = ((LeafNode) node1.children().get("leaf36"));
        assertThat(leaf.value(), is("hey!"));
        assertThat(leaf.uri(), is("test-yang:cont18.cont18.list12[1].list12[1].leaf36"));

        Map<String, String> output1 = ser.decode(node);
        assertThat(output1.size(), is(params.size()));
        for (Map.Entry<String, String> entry : output1.entrySet()) {
            assertTrue(params.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testPropertiesWithoutSchema() throws SvcLogicException {
        String uri = "test-yang:cont18";
        Map<String, String> params = new HashMap<>();
        params.put("test-yang_cont18.leaf40", "abc");
        params.put("leaf41", "hi");
        params.put("test-yang_cont18.leaf41", "abc");

        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);
        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);

        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();
        assertThat(childNodes.containsKey("leaf40"), is(true));
        node = childNodes.get("leaf40");
        assertThat(node.uri(), is("test-yang:cont18.leaf40"));
    }

    @Test
    public void testIdentityRef() throws SvcLogicException {
        String uri = "identity-test:test";
        Map<String, String> params = new HashMap<>();
        params.put("identity-test_test.con1.interface", "identity-types" +
                ":physical");
        params.put("identity-test_test.con1.interfaces.int-list[0].iden", "identity-test:Giga");
        params.put("identity-test_test.con1.interfaces.int-list[0].available.ll[0]", "identity-types:Loopback");
        params.put("identity-test_test.con1.interfaces.int-list[0].available.leaf1", "identity-types-second:Ethernet");
        params.put("identity-test_test.con1.interfaces.int-list[0].available.leaf2", "identity-types-second:iden2");
        InstanceIdentifierContext<?> iCtx = ParserIdentifier
                .toInstanceIdentifier(uri, context, null);

        PropertiesNodeSerializer ser = new MdsalPropertiesNodeSerializer(
                iCtx.getSchemaNode(), context, uri);
        PropertiesNode node = ser.encode(params);
        Map<String, PropertiesNode> childNodes = ((RootNode) node).children();
        assertThat(childNodes.containsKey("con1"), is(true));
        node = childNodes.get("con1");
        assertThat(node.uri(), is("identity-test:test.con1"));
        LeafNode l = ((LeafNode) ((SingleInstanceNode) node).children().get("interface"));
        assertThat(l.uri(), is("identity-test:test.con1.interface"));
        assertThat(l.valueNs().moduleName(), is("identity-types"));
        assertThat(l.valueNs().moduleNs().toString(), is("identity:list:ns:test:json:ser"));

        // identity type inside union
        node = ((SingleInstanceNode) ((SingleInstanceNode) node).children().get("interfaces"));
        node = ((ListHolderNode) ((SingleInstanceNode) node).children().get("int-list"));
        node = ((MultiInstanceNode) ((ListHolderNode) node).children().get("0"));
        l = ((LeafNode) ((MultiInstanceNode) node).children().get("iden"));
        assertThat(l.uri(), is("identity-test:test.con1.interfaces.int-list[0].iden"));
        assertThat(l.valueNs().moduleName(), is("identity-test"));
        assertThat(l.valueNs().moduleNs().toString(), is("identity:ns:test:json:ser"));

        // leaf-list test
        node = (SingleInstanceNode) ((MultiInstanceNode) node).children().get("available");
        LeafListHolderNode holder = (LeafListHolderNode) ((SingleInstanceNode) node).children().get("ll");
        l = ((LeafNode) holder.children().get("0"));
        assertThat(l.uri(), is("identity-test:test.con1.interfaces.int-list[0].available.ll[0]"));
        assertThat(l.valueNs().moduleName(), is("identity-types"));
        assertThat(l.valueNs().moduleNs().toString(), is("identity:list:ns:test:json:ser"));

        // leaf-ref test
        l = ((LeafNode) ((SingleInstanceNode) node).children().get("leaf1"));
        assertThat(l.uri(), is("identity-test:test.con1.interfaces.int-list[0].available.leaf1"));
        assertThat(l.valueNs().moduleName(), is("identity-types-second"));
        assertThat(l.valueNs().moduleNs().toString(), is("identity:list:second:ns:test:json:ser"));

        // list of base identity test
        l = ((LeafNode) ((SingleInstanceNode) node).children().get("leaf2"));
        assertThat(l.uri(), is("identity-test:test.con1.interfaces.int-list[0].available.leaf2"));
        assertThat(l.valueNs().moduleName(), is("identity-types-second"));
        assertThat(l.valueNs().moduleNs().toString(), is("identity:list:second:ns:test:json:ser"));
    }

    public static SchemaContext compileYangFile() throws FileNotFoundException {
        String path = PropertiesSerializerTest.class.getResource("/yang").getPath();
        File dir = new File(path);
        String[] fileList = dir.list();
        List<File> yangFiles = new ArrayList<File>();
        if (fileList == null) {
            throw new FileNotFoundException("/yang");
        }
        for (int i = 0; i < fileList.length; i++) {
            final String fileName = fileList[i];
            if (new File(dir, fileName).isDirectory() == false) {
                yangFiles.add(new File(dir, fileName));
            }
        }
        return YangParserTestUtils.parseYangFiles(yangFiles);
    }
}