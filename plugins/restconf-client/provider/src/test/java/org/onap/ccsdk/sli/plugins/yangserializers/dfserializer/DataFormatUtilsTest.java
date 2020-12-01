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

package org.onap.ccsdk.sli.plugins.yangserializers.dfserializer;

/**
 * Unit test case utilities for data format serializer and restconf api call
 * node.
 */
public final class DataFormatUtilsTest {

    static final String ENCODE_TO_JSON_ID_COMMON = "\n        \"interfaces\"" +
            ": " +
            "{\n" +
            "            \"int-list\": [\n" +
            "                {\n" +
            "                    \"iden\": \"optical\",\n" +
            "                    \"available\": {\n" +
            "                        \"ll\": [\n" +
            "                            \"Giga\",\n" +
            "                            \"identity-types:Loopback\",\n" +
            "                            \"identity-types-second:Ethernet" +
            "\"\n" +
            "                        ],\n" +
            "                        \"leaf1\": \"58\",\n" +
            "                        \"leaf2\": \"identity-types-second:iden" +
            "2\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"iden\": \"214748364\",\n" +
            "                    \"available\": {\n" +
            "                        \"ll\": [\n" +
            "                            \"Giga\",\n" +
            "                            \"identity-types:Loopback\",\n" +
            "                            \"identity-types-second:Ethernet" +
            "\"\n" +
            "                        ],\n" +
            "                        \"leaf1\": \"8888\",\n" +
            "                        \"leaf2\": \"identity-types-second:ide" +
            "n2\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"interface\": \"identity-types:physical\"\n" +
            "    }";

    static final String ENCODE_TO_JSON_ID = "{\n" +
            "    \"identity-test:con1\": {" + ENCODE_TO_JSON_ID_COMMON +
            ",\n" +
            "    \"identity-test:l\": \"abc\"\n" +
            "}";

    static final String ENCODE_TO_ANYXML = "{\n" +
            "    \"actionIdentifiers\": {\n" +
            "        \"mode\": \"sync\",\n" +
            "        \"blueprintName\": \"baseconfiguration\",\n" +
            "        \"blueprintVersion\": \"1.0.0\",\n" +
            "        \"actionName\": \"assign-activate\"\n" +
            "    },\n" +
            "    \"payload\": {\n" +
            "        \"template-prefix\": \"vDNS-test\",\n" +
            "        \"resource-assignment-request\": {\n" +
            "            \"resource-assignment-properties\": {\n" +
            "                \"service-instance-id\": \"1234\",\n" +
            "                \"vnf-id\": \"3526\",\n" +
            "                \"customer-name\": \"htipl\",\n" +
            "                \"subscriber-name\": \"huawei\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"commonHeader\": {\n" +
            "        \"subRequestId\": \"sub-123456-1000\",\n" +
            "        \"requestId\": \"123456-1000\",\n" +
            "        \"originatorId\": \"SDNC_DG\"\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_JSON_ID_PUT = "{\n" +
            "    \"identity-test:test\": {\n" +
            "        \"con1\": {" + addSpace(ENCODE_TO_JSON_ID_COMMON, 4) +
            ",\n" +
            "        \"l\": \"abc\"\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_XML_ID_COMMON = "\n    <interfaces>\n" +
            "        <int-list>\n" +
            "            <iden>optical</iden>\n" +
            "            <available>\n" +
            "                <ll>Giga</ll>\n" +
            "                <ll xmlns:yangid=\"identity:list:ns:test:json:se" +
            "r\">yangid:Loopback</ll>\n" +
            "                <ll xmlns:yangid=\"identity:list:second:ns:test" +
            ":json:ser\">yangid:Ethernet</ll>\n" +
            "                <leaf1>58</leaf1>\n" +
            "                <leaf2 xmlns:yangid=\"identity:list:second:ns:t" +
            "est:json:ser\">yangid:iden2</leaf2>\n" +
            "            </available>\n" +
            "        </int-list>\n" +
            "        <int-list>\n" +
            "            <iden>214748364</iden>\n" +
            "            <available>\n" +
            "                <ll>Giga</ll>\n" +
            "                <ll xmlns:yangid=\"identity:list:ns:test:json:s" +
            "er\">yangid:Loopback</ll>\n" +
            "                <ll xmlns:yangid=\"identity:list:second:ns:test" +
            ":json:ser\">yangid:Ethernet</ll>\n" +
            "                <leaf1>8888</leaf1>\n" +
            "                <leaf2 xmlns:yangid=\"identity:list:second:ns:t" +
            "est:json:ser\">yangid:iden2</leaf2>\n" +
            "            </available>\n" +
            "        </int-list>\n" +
            "    </interfaces>\n" +
            "    <interface xmlns:yangid=\"identity:list:ns:test:json:ser\">" +
            "yangid:physical</interface>";

    static final String ENCODE_TO_XML_ID = "<?xml version=\"1.0\" encoding=" +
            "\"UTF-8\" standalone=\"no\"?>\n" +
            "<con1 xmlns=\"identity:ns:test:json:ser\">" +
            ENCODE_TO_XML_ID_COMMON + "\n</con1>\n";

    static final String ENCODE_TO_XML_ID_PUT = "<?xml version=\"1.0\" enco" +
            "ding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<test xmlns=\"identity:ns:test:json:ser\">\n" +
            "    <con1>" + addSpace(ENCODE_TO_XML_ID_COMMON, 4)
            + "\n    </con1>\n" +
            "</test>\n";

    static final String ENCODE_TO_JSON_YANG_COMMON = "\n    " +
            "\"test-augment:ll6\": [\n" +
            "        \"unbounded\",\n" +
            "        \"8\"\n" +
            "    ],\n" +
            "    \"test-augment:cont13\": {\n" +
            "        \"ll9\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"list9\": [\n" +
            "            {\n" +
            "                \"leaf27\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"leaf27\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"leaf28\": \"abc\",\n" +
            "        \"cont12\": {\n" +
            "            \"leaf26\": \"abc\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"test-augment:list7\": [\n" +
            "        {\n" +
            "            \"leaf14\": \"test\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"leaf14\": \"create\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"test-augment:leaf15\": \"abc\",\n" +
            "    \"test-augment:cont5\": {\n" +
            "        \"leaf13\": \"true\"\n" +
            "    }";

    static final String ENCODE_TO_JSON_YANG_AUG_POST = "{\n" +
            "    \"test-yang:leaf10\": \"abc\"," +
            ENCODE_TO_JSON_YANG_COMMON + "\n}";

    static final String ENCODE_TO_JSON_YANG = "{\n" +
            "    \"test-yang:cont2\": {\n" +
            "        \"list1\": [\n" +
            "            {\n" +
            "                \"ll1\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"leaf1\": \"true\",\n" +
            "                \"ll2\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"list5\": [\n" +
            "                    {\n" +
            "                        \"leaf9\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"leaf9\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"leaf3\": \"abc\",\n" +
            "                \"leaf2\": \"abc\",\n" +
            "                \"list4\": [\n" +
            "                    {\n" +
            "                        \"leaf8\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"leaf8\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"cont4\": {\n" +
            "                    \"leaf11\": \"abc\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"ll1\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"leaf1\": \"true\",\n" +
            "                \"ll2\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"leaf3\": \"abc\",\n" +
            "                \"list5\": [\n" +
            "                    {\n" +
            "                        \"leaf9\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"leaf9\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"list4\": [\n" +
            "                    {\n" +
            "                        \"leaf8\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"leaf8\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"leaf2\": \"abc\",\n" +
            "                \"cont4\": {\n" +
            "                    \"leaf11\": \"abc\"\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"ll3\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"ll5\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"cont4\": {\n" +
            "            \"leaf10\": \"abc\"," +
            addSpace(ENCODE_TO_JSON_YANG_COMMON, 8) + "\n" +
            "        },\n" +
            "        \"ll4\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"cont3\": {\n" +
            "            \"leaf10\": \"abc\"\n" +
            "        },\n" +
            "        \"leaf5\": \"abc\",\n" +
            "        \"list2\": [\n" +
            "            {\n" +
            "                \"leaf4\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"leaf4\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"leaf12\": \"abc\",\n" +
            "        \"leaf6\": \"abc\",\n" +
            "        \"list6\": [\n" +
            "            {\n" +
            "                \"leaf11\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"leaf11\": \"abc\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_JSON_YANG_PUT = "{\n" +
            "    \"test-yang:cont4\": {" + addSpace(
            ENCODE_TO_JSON_YANG_COMMON, 4) + ",\n" +
            "        \"leaf10\": \"abc\"\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_XML_YANG_COMMON = "\n" +
            "<ll6 xmlns=\"urn:opendaylight:params:xml:ns:yang:aug" +
            "ment\">unbounded</ll6>\n" +
            "<ll6 xmlns=\"urn:opendaylight:params:xml:ns:yang:aug" +
            "ment\">8</ll6>\n" +
            "<cont13 xmlns=\"urn:opendaylight:params:xml:ns:yang:" +
            "augment\">\n" +
            "    <ll9>abc</ll9>\n" +
            "    <ll9>abc</ll9>\n" +
            "    <list9>\n" +
            "        <leaf27>abc</leaf27>\n" +
            "    </list9>\n" +
            "    <list9>\n" +
            "        <leaf27>abc</leaf27>\n" +
            "    </list9>\n" +
            "    <leaf28>abc</leaf28>\n" +
            "    <cont12>\n" +
            "        <leaf26>abc</leaf26>\n" +
            "    </cont12>\n" +
            "</cont13>\n" +
            "<list7 xmlns=\"urn:opendaylight:params:xml:ns:yang:a" +
            "ugment\">\n" +
            "    <leaf14>test</leaf14>\n" +
            "</list7>\n" +
            "<list7 xmlns=\"urn:opendaylight:params:xml:ns:yang:a" +
            "ugment\">\n" +
            "    <leaf14>create</leaf14>\n" +
            "</list7>\n" +
            "<leaf15 xmlns=\"urn:opendaylight:params:xml:ns:yang:" +
            "augment\">abc</leaf15>\n" +
            "<cont5 xmlns=\"urn:opendaylight:params:xml:ns:yang:a" +
            "ugment\">\n" +
            "    <leaf13>true</leaf13>\n" +
            "</cont5>";

    static final String ENCODE_TO_XML_YANG_AUG_POST = "<?xml version=\"1.0\"" +
            " encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<leaf10 xmlns=\"urn:opendaylight:params:xml:ns:yang:test\">abc" +
            "</leaf10>" +
            ENCODE_TO_XML_YANG_COMMON + "\n";

    static final String ENCODE_TO_XML_YANG_PUT = "<?xml version=\"1.0\" enc" +
            "oding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<cont4 xmlns=\"urn:opendaylight:params:xml:ns:yang:test\">\n" +
            "    <leaf10>abc</leaf10>" +
            addSpace(ENCODE_TO_XML_YANG_COMMON, 4) + "\n</cont4>\n";

    static final String ENCODE_TO_XML_YANG = "<?xml version=\"1.0\" encoding" +
            "=\"UTF-8\" standalone=\"no\"?>\n" +
            "<cont2 xmlns=\"urn:opendaylight:params:xml:ns:yang:test\">\n" +
            "    <list1>\n" +
            "        <ll1>abc</ll1>\n" +
            "        <ll1>abc</ll1>\n" +
            "        <leaf1>true</leaf1>\n" +
            "        <ll2>abc</ll2>\n" +
            "        <ll2>abc</ll2>\n" +
            "        <list5>\n" +
            "            <leaf9>abc</leaf9>\n" +
            "        </list5>\n" +
            "        <list5>\n" +
            "            <leaf9>abc</leaf9>\n" +
            "        </list5>\n" +
            "        <leaf3>abc</leaf3>\n" +
            "        <leaf2>abc</leaf2>\n" +
            "        <list4>\n" +
            "            <leaf8>abc</leaf8>\n" +
            "        </list4>\n" +
            "        <list4>\n" +
            "            <leaf8>abc</leaf8>\n" +
            "        </list4>\n" +
            "        <cont4>\n" +
            "            <leaf11>abc</leaf11>\n" +
            "        </cont4>\n" +
            "    </list1>\n" +
            "    <list1>\n" +
            "        <ll1>abc</ll1>\n" +
            "        <ll1>abc</ll1>\n" +
            "        <leaf1>true</leaf1>\n" +
            "        <ll2>abc</ll2>\n" +
            "        <ll2>abc</ll2>\n" +
            "        <leaf3>abc</leaf3>\n" +
            "        <list5>\n" +
            "            <leaf9>abc</leaf9>\n" +
            "        </list5>\n" +
            "        <list5>\n" +
            "            <leaf9>abc</leaf9>\n" +
            "        </list5>\n" +
            "        <list4>\n" +
            "            <leaf8>abc</leaf8>\n" +
            "        </list4>\n" +
            "        <list4>\n" +
            "            <leaf8>abc</leaf8>\n" +
            "        </list4>\n" +
            "        <leaf2>abc</leaf2>\n" +
            "        <cont4>\n" +
            "            <leaf11>abc</leaf11>\n" +
            "        </cont4>\n" +
            "    </list1>\n" +
            "    <ll3>abc</ll3>\n" +
            "    <ll3>abc</ll3>\n" +
            "    <ll5>abc</ll5>\n" +
            "    <ll5>abc</ll5>\n" +
            "    <cont4>\n" +
            "        <leaf10>abc</leaf10>"+
            addSpace(ENCODE_TO_XML_YANG_COMMON, 8) + "\n" +
            "    </cont4>\n" +
            "    <ll4>abc</ll4>\n" +
            "    <ll4>abc</ll4>\n" +
            "    <cont3>\n" +
            "        <leaf10>abc</leaf10>\n" +
            "    </cont3>\n" +
            "    <leaf5>abc</leaf5>\n" +
            "    <list2>\n" +
            "        <leaf4>abc</leaf4>\n" +
            "    </list2>\n" +
            "    <list2>\n" +
            "        <leaf4>abc</leaf4>\n" +
            "    </list2>\n" +
            "    <leaf12>abc</leaf12>\n" +
            "    <leaf6>abc</leaf6>\n" +
            "    <list6>\n" +
            "        <leaf11>abc</leaf11>\n" +
            "    </list6>\n" +
            "    <list6>\n" +
            "        <leaf11>abc</leaf11>\n" +
            "    </list6>\n" +
            "</cont2>\n";

    static final String ENCODE_TO_JSON_RPC = "{\n" +
            "    \"test-yang:input\": {\n" +
            "        \"leaf30\": \"abc\",\n" +
            "        \"list10\": [\n" +
            "            {\n" +
            "                \"leaf29\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"leaf29\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"cont15\": {\n" +
            "            \"leaf31\": \"abc\"\n" +
            "        },\n" +
            "        \"cont14\": {\n" +
            "            \"leaf28\": \"abc\"\n" +
            "        },\n" +
            "        \"cont13\": {\n" +
            "            \"list9\": [\n" +
            "                {\n" +
            "                    \"leaf27\": \"abc\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"leaf27\": \"abc\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ll9\": [\n" +
            "                \"abc\",\n" +
            "                \"abc\"\n" +
            "            ],\n" +
            "            \"leaf28\": \"abc\"\n" +
            "        },\n" +
            "        \"ll10\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    static final String DECODE_FROM_JSON_RPC = "{\n" +
            "    \"test-yang:output\": {\n" +
            "        \"cont16\": {\n" +
            "            \"leaf32\": \"abc\"\n" +
            "        },\n" +
            "        \"list11\": [\n" +
            "            {\n" +
            "                \"leaf33\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"leaf33\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"leaf34\": \"abc\",\n" +
            "        \"ll11\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"cont17\": {\n" +
            "            \"leaf35\": \"abc\"\n" +
            "        },\n" +
            "        \"cont13\": {\n" +
            "            \"cont12\": {\n" +
            "                \"leaf26\": \"abc\"\n" +
            "            },\n" +
            "            \"list9\": [\n" +
            "                {\n" +
            "                    \"leaf27\": \"abc\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"leaf27\": \"abc\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ll9\": [\n" +
            "                \"abc\",\n" +
            "                \"abc\"\n" +
            "            ],\n" +
            "            \"leaf28\": \"abc\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    static final String DECODE_ANYXML_RESPONSE = "{\n" +
            "    \"commonHeader\": {\n" +
            "        \"timestamp\": \"2019-05-18T23:42:41.658Z\",\n" +
            "        \"originatorId\": \"System\",\n" +
            "        \"requestId\": \"1234\",\n" +
            "        \"subRequestId\": \"1234-12234\",\n" +
            "        \"flags\": null\n" +
            "    },\n" +
            "    \"actionIdentifiers\": {\n" +
            "        \"blueprintName\": \"golden\",\n" +
            "        \"blueprintVersion\": \"1.0.0\",\n" +
            "        \"actionName\": \"resource-assignment\",\n" +
            "        \"mode\": \"sync\"\n" +
            "    },\n" +
            "    \"status\": {\n" +
            "        \"code\": 200,\n" +
            "        \"eventType\": \"EVENT_COMPONENT_EXECUTED\",\n" +
            "        \"timestamp\": \"2019-05-18T23:42:41.950Z\",\n" +
            "        \"errorMessage\": null,\n" +
            "        \"message\": \"success\"\n" +
            "    },\n" +
            "    \"payload\": {\n" +
            "        \"resource-assignment-response\": {\n" +
            "            \"meshed-template\": {\n" +
            "                \"vf-module-1\": \"<interface>\\n   " +
            " <description>This is the Virtual Firewall entity</" +
            "description>\\n    <vfw>10.0.101.20/24</vfw>\\n</interface>\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"stepData\": {\n" +
            "        \"name\": \"resource-assignment\",\n" +
            "        \"properties\": {\n" +
            "            \"resource-assignment-params\": null,\n" +
            "            \"status\": null\n" +
            "        }\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_XML_RPC = "<?xml version=\"1.0\" encoding" +
            "=\"UTF-8\" standalone=\"no\"?>\n" +
            "<input xmlns=\"urn:opendaylight:params:xml:ns:yang:test\">\n" +
            "    <leaf30>abc</leaf30>\n" +
            "    <list10>\n" +
            "        <leaf29>abc</leaf29>\n" +
            "    </list10>\n" +
            "    <list10>\n" +
            "        <leaf29>abc</leaf29>\n" +
            "    </list10>\n" +
            "    <cont15>\n" +
            "        <leaf31>abc</leaf31>\n" +
            "    </cont15>\n" +
            "    <cont14>\n" +
            "        <leaf28>abc</leaf28>\n" +
            "    </cont14>\n" +
            "    <cont13>\n" +
            "        <list9>\n" +
            "            <leaf27>abc</leaf27>\n" +
            "        </list9>\n" +
            "        <list9>\n" +
            "            <leaf27>abc</leaf27>\n" +
            "        </list9>\n" +
            "        <ll9>abc</ll9>\n" +
            "        <ll9>abc</ll9>\n" +
            "        <leaf28>abc</leaf28>\n" +
            "    </cont13>\n" +
            "    <ll10>abc</ll10>\n" +
            "    <ll10>abc</ll10>\n" +
            "</input>\n";

    static final String DECODE_FROM_XML_RPC = "<?xml version=\"1.0\" encodi" +
            "ng=\"UTF-8\" standalone=\"no\"?>\n" +
            "<output xmlns=\"urn:opendaylight:params:xml:ns:yang:test\">\n" +
            "    <cont16>\n" +
            "        <leaf32>abc</leaf32>\n" +
            "    </cont16>\n" +
            "    <list11>\n" +
            "        <leaf33>abc</leaf33>\n" +
            "    </list11>\n" +
            "    <list11>\n" +
            "        <leaf33>abc</leaf33>\n" +
            "    </list11>\n" +
            "    <leaf34>abc</leaf34>\n" +
            "    <ll11>abc</ll11>\n" +
            "    <ll11>abc</ll11>\n" +
            "    <cont17>\n" +
            "        <leaf35>abc</leaf35>\n" +
            "    </cont17>\n" +
            "    <cont13>\n" +
            "        <cont12>\n" +
            "            <leaf26>abc</leaf26>\n" +
            "        </cont12>\n" +
            "        <list9>\n" +
            "            <leaf27>abc</leaf27>\n" +
            "        </list9>\n" +
            "        <list9>\n" +
            "            <leaf27>abc</leaf27>\n" +
            "        </list9>\n" +
            "        <ll9>abc</ll9>\n" +
            "        <ll9>abc</ll9>\n" +
            "        <leaf28>abc</leaf28>\n" +
            "    </cont13>\n" +
            "</output>";

    /**
     * Adds the specified number of space required for a req in each line.
     *
     * @param req request message
     * @param i   number of space
     * @return space appended string
     */
    public static String addSpace(String req, int i) {
        StringBuilder space = new StringBuilder();
        for (int sp = 0; sp < i; sp++) {
            space = space.append(" ");
        }
        return req.replaceAll("\n", "\n" + space.toString());
    }
}
