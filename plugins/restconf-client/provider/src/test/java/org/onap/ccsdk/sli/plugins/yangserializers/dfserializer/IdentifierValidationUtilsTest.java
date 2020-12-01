/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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

import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.addSpace;

/**
 * Unit test case utilities for identifier validation and restconf api
 * call node.
 */
public final class IdentifierValidationUtilsTest {

    static final String ENCODE_TO_JSON_YANG_COMMON_ID = "\n    " +
            "\"test_augment_1_for_module:name_of_the_ll6\": [\n" +
            "        \"unbounded\",\n" +
            "        \"8\"\n" +
            "    ],\n" +
            "    \"test_augment_1_for_module:name_of_the_cont13\": {\n" +
            "        \"name_of_the_cont12\": {\n" +
            "            \"name_of_the_leaf26\": \"abc\"\n" +
            "        },\n" +
            "        \"name_of_the_ll9\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_leaf28\": \"abc\",\n" +
            "        \"name_of_the_list9\": [\n" +
            "            {\n" +
            "                \"name_of_the_leaf27\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_leaf27\": \"abc\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"test_augment_1_for_module:name_of_the_list7\": [\n" +
            "        {\n" +
            "            \"name_of_the_leaf14\": \"test\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name_of_the_leaf14\": \"create\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"test_augment_1_for_module:name_of_the_leaf15\": \"abc\",\n" +
            "    \"test_augment_1_for_module:name_of_the_cont5\": {\n" +
            "        \"name_of_the_leaf13\": \"true\"\n" +
            "    }";

    static final String ENCODE_TO_JSON_YANG_AUG_POST_ID = "{\n" +
            "    \"test_name_of_the_module:name_of_the_leaf10\": \"abc\"," +
            ENCODE_TO_JSON_YANG_COMMON_ID + "\n}";

    static final String ENCODE_TO_JSON_YANG_ID = "{\n" +
            "    \"test_name_of_the_module:name_of_the_cont2\": {\n" +
            "        \"name_of_the_ll4\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_leaf5\": \"abc\",\n" +
            "        \"name_of_the_list6\": [\n" +
            "            {\n" +
            "                \"name_of_the_leaf11\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_leaf11\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"name_of_the_ll5\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_ll3\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_leaf6\": \"abc\",\n" +
            "        \"name_of_the_cont3\": {\n" +
            "            \"name_of_the_leaf10\": \"abc\"\n" +
            "        },\n" +
            "        \"name_of_the_list2\": [\n" +
            "            {\n" +
            "                \"name_of_the_leaf4\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_leaf4\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"name_of_the_list1\": [\n" +
            "            {\n" +
            "                \"name_of_the_ll2\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"name_of_the_list5\": [\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf9\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf9\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"name_of_the_list4\": [\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf8\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf8\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"name_of_the_leaf1\": \"true\",\n" +
            "                \"name_of_the_leaf3\": \"abc\",\n" +
            "                \"name_of_the_leaf2\": \"abc\",\n" +
            "                \"name_of_the_cont4\": {\n" +
            "                    \"name_of_the_leaf11\": \"abc\"\n" +
            "                },\n" +
            "                \"name_of_the_ll1\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_ll2\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ],\n" +
            "                \"name_of_the_list5\": [\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf9\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf9\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"name_of_the_list4\": [\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf8\": \"abc\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name_of_the_leaf8\": \"abc\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"name_of_the_leaf1\": \"true\",\n" +
            "                \"name_of_the_leaf3\": \"abc\",\n" +
            "                \"name_of_the_leaf2\": \"abc\",\n" +
            "                \"name_of_the_cont4\": {\n" +
            "                    \"name_of_the_leaf11\": \"abc\"\n" +
            "                },\n" +
            "                \"name_of_the_ll1\": [\n" +
            "                    \"abc\",\n" +
            "                    \"abc\"\n" +
            "                ]\n" +
            "            }\n" +
            "        ],\n" +
            "        \"name_of_the_cont4\": {\n" +
            "            \"name_of_the_leaf10\": \"abc\"," +
            addSpace(ENCODE_TO_JSON_YANG_COMMON_ID,8) + "\n" +
            "        },\n" +
            "        \"name_of_the_leaf12\": \"abc\"\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_JSON_WITH_AUG_PATH = "{\n" +
            "    \"test_augment_1_for_module:name_of_the_leaf13\": \"true\"\n" +
            "}";

    static final String ENCODE_TO_JSON_YANG_PUT_ID = "{\n" +
            "    \"test_name_of_the_module:name_of_the_cont4\": {" + addSpace(
            ENCODE_TO_JSON_YANG_COMMON_ID, 4) + ",\n" +
            "        \"name_of_the_leaf10\": \"abc\"\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_XML_YANG_COMMON_ID = "\n" +
            "<name_of_the_ll6 xmlns=\"urn:opendaylight:params:xml:ns:yang:" +
            "test:augment:name\">unbounded</name_of_the_ll6>\n" +
            "<name_of_the_ll6 xmlns=\"urn:opendaylight:params:xml:ns:yang:" +
            "test:augment:name\">8</name_of_the_ll6>\n" +
            "<name_of_the_cont13 xmlns=\"urn:opendaylight:params:xml:ns:ya" +
            "ng:test:augment:name\">\n" +
            "    <name_of_the_cont12>\n" +
            "        <name_of_the_leaf26>abc</name_of_the_leaf26>\n" +
            "    </name_of_the_cont12>\n" +
            "    <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "    <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "    <name_of_the_leaf28>abc</name_of_the_leaf28>\n" +
            "    <name_of_the_list9>\n" +
            "        <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "    </name_of_the_list9>\n" +
            "    <name_of_the_list9>\n" +
            "        <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "    </name_of_the_list9>\n" +
            "</name_of_the_cont13>\n" +
            "<name_of_the_list7 xmlns=\"urn:opendaylight:params:xml:ns:yan" +
            "g:test:augment:name\">\n" +
            "    <name_of_the_leaf14>test</name_of_the_leaf14>\n" +
            "</name_of_the_list7>\n" +
            "<name_of_the_list7 xmlns=\"urn:opendaylight:params:xml:ns:yan" +
            "g:test:augment:name\">\n" +
            "    <name_of_the_leaf14>create</name_of_the_leaf14>\n" +
            "</name_of_the_list7>\n" +
            "<name_of_the_leaf15 xmlns=\"urn:opendaylight:params:xml:ns:ya" +
            "ng:test:augment:name\">abc</name_of_the_leaf15>\n" +
            "<name_of_the_cont5 xmlns=\"urn:opendaylight:params:xml:ns:yan" +
            "g:test:augment:name\">\n" +
            "    <name_of_the_leaf13>true</name_of_the_leaf13>\n" +
            "</name_of_the_cont5>";

    static final String ENCODE_TO_XML_YANG_AUG_POST_ID = "<?xml version=\"1" +
            ".0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<name_of_the_leaf10 xmlns=\"urn:opendaylight:params:xml:ns:yan" +
            "g:test:name\">abc</name_of_the_leaf10>" +
            ENCODE_TO_XML_YANG_COMMON_ID + "\n";

    static final String ENCODE_TO_XML_YANG_PUT_ID = "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<name_of_the_cont4 xmlns=\"urn:opendaylight:params:xml:ns:yang" +
            ":test:name\">\n" +
            "    <name_of_the_leaf10>abc</name_of_the_leaf10>" +
            addSpace(ENCODE_TO_XML_YANG_COMMON_ID, 4) + "\n</name_of_the_co" +
            "nt4>\n";

    static final String ENCODE_TO_XML_YANG_ID= "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<name_of_the_cont2 xmlns=\"urn:opendaylight:params:xml:ns:yan" +
            "g:test:name\">\n" +
            "    <name_of_the_ll4>abc</name_of_the_ll4>\n" +
            "    <name_of_the_ll4>abc</name_of_the_ll4>\n" +
            "    <name_of_the_leaf5>abc</name_of_the_leaf5>\n" +
            "    <name_of_the_list6>\n" +
            "        <name_of_the_leaf11>abc</name_of_the_leaf11>\n" +
            "    </name_of_the_list6>\n" +
            "    <name_of_the_list6>\n" +
            "        <name_of_the_leaf11>abc</name_of_the_leaf11>\n" +
            "    </name_of_the_list6>\n" +
            "    <name_of_the_ll5>abc</name_of_the_ll5>\n" +
            "    <name_of_the_ll5>abc</name_of_the_ll5>\n" +
            "    <name_of_the_ll3>abc</name_of_the_ll3>\n" +
            "    <name_of_the_ll3>abc</name_of_the_ll3>\n" +
            "    <name_of_the_leaf6>abc</name_of_the_leaf6>\n" +
            "    <name_of_the_cont3>\n" +
            "        <name_of_the_leaf10>abc</name_of_the_leaf10>\n" +
            "    </name_of_the_cont3>\n" +
            "    <name_of_the_list2>\n" +
            "        <name_of_the_leaf4>abc</name_of_the_leaf4>\n" +
            "    </name_of_the_list2>\n" +
            "    <name_of_the_list2>\n" +
            "        <name_of_the_leaf4>abc</name_of_the_leaf4>\n" +
            "    </name_of_the_list2>\n" +
            "    <name_of_the_list1>\n" +
            "        <name_of_the_ll2>abc</name_of_the_ll2>\n" +
            "        <name_of_the_ll2>abc</name_of_the_ll2>\n" +
            "        <name_of_the_list5>\n" +
            "            <name_of_the_leaf9>abc</name_of_the_leaf9>\n" +
            "        </name_of_the_list5>\n" +
            "        <name_of_the_list5>\n" +
            "            <name_of_the_leaf9>abc</name_of_the_leaf9>\n" +
            "        </name_of_the_list5>\n" +
            "        <name_of_the_list4>\n" +
            "            <name_of_the_leaf8>abc</name_of_the_leaf8>\n" +
            "        </name_of_the_list4>\n" +
            "        <name_of_the_list4>\n" +
            "            <name_of_the_leaf8>abc</name_of_the_leaf8>\n" +
            "        </name_of_the_list4>\n" +
            "        <name_of_the_leaf1>true</name_of_the_leaf1>\n" +
            "        <name_of_the_leaf3>abc</name_of_the_leaf3>\n" +
            "        <name_of_the_leaf2>abc</name_of_the_leaf2>\n" +
            "        <name_of_the_cont4>\n" +
            "            <name_of_the_leaf11>abc</name_of_the_leaf11>\n" +
            "        </name_of_the_cont4>\n" +
            "        <name_of_the_ll1>abc</name_of_the_ll1>\n" +
            "        <name_of_the_ll1>abc</name_of_the_ll1>\n" +
            "    </name_of_the_list1>\n" +
            "    <name_of_the_list1>\n" +
            "        <name_of_the_ll2>abc</name_of_the_ll2>\n" +
            "        <name_of_the_ll2>abc</name_of_the_ll2>\n" +
            "        <name_of_the_list5>\n" +
            "            <name_of_the_leaf9>abc</name_of_the_leaf9>\n" +
            "        </name_of_the_list5>\n" +
            "        <name_of_the_list5>\n" +
            "            <name_of_the_leaf9>abc</name_of_the_leaf9>\n" +
            "        </name_of_the_list5>\n" +
            "        <name_of_the_list4>\n" +
            "            <name_of_the_leaf8>abc</name_of_the_leaf8>\n" +
            "        </name_of_the_list4>\n" +
            "        <name_of_the_list4>\n" +
            "            <name_of_the_leaf8>abc</name_of_the_leaf8>\n" +
            "        </name_of_the_list4>\n" +
            "        <name_of_the_leaf1>true</name_of_the_leaf1>\n" +
            "        <name_of_the_leaf3>abc</name_of_the_leaf3>\n" +
            "        <name_of_the_leaf2>abc</name_of_the_leaf2>\n" +
            "        <name_of_the_cont4>\n" +
            "            <name_of_the_leaf11>abc</name_of_the_leaf11>\n" +
            "        </name_of_the_cont4>\n" +
            "        <name_of_the_ll1>abc</name_of_the_ll1>\n" +
            "        <name_of_the_ll1>abc</name_of_the_ll1>\n" +
            "    </name_of_the_list1>\n" +
            "    <name_of_the_cont4>\n" +
            "        <name_of_the_leaf10>abc</name_of_the_leaf10>" +
            addSpace(ENCODE_TO_XML_YANG_COMMON_ID, 8) + "\n" +
            "    </name_of_the_cont4>\n" +
            "    <name_of_the_leaf12>abc</name_of_the_leaf12>\n" +
            "</name_of_the_cont2>\n";

    static final String ENCODE_TO_JSON_RPC_ID = "{\n" +
            "    \"test_name_of_the_module:input\": {\n" +
            "        \"name_of_the_cont14\": {\n" +
            "            \"name_of_the_leaf28\": \"abc\"\n" +
            "        },\n" +
            "        \"name_of_the_cont13\": {\n" +
            "            \"name_of_the_ll9\": [\n" +
            "                \"abc\",\n" +
            "                \"abc\"\n" +
            "            ],\n" +
            "            \"name_of_the_leaf28\": \"abc\",\n" +
            "            \"name_of_the_list9\": [\n" +
            "                {\n" +
            "                    \"name_of_the_leaf27\": \"abc\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name_of_the_leaf27\": \"abc\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"name_of_the_leaf30\": \"abc\",\n" +
            "        \"name_of_the_ll10\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_list10\": [\n" +
            "            {\n" +
            "                \"name_of_the_leaf29\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_leaf29\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"name_of_the_cont15\": {\n" +
            "            \"name_of_the_leaf31\": \"abc\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    static final String DECODE_FROM_JSON_RPC_ID = "{\n" +
            "    \"test_name_of_the_module:output\": {\n" +
            "        \"name_of_the_cont16\": {\n" +
            "            \"name_of_the_leaf32\": \"abc\"\n" +
            "        },\n" +
            "        \"name_of_the_list11\": [\n" +
            "            {\n" +
            "                \"name_of_the_leaf33\": \"abc\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name_of_the_leaf33\": \"abc\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"name_of_the_leaf34\": \"abc\",\n" +
            "        \"name_of_the_ll11\": [\n" +
            "            \"abc\",\n" +
            "            \"abc\"\n" +
            "        ],\n" +
            "        \"name_of_the_cont17\": {\n" +
            "            \"name_of_the_leaf35\": \"abc\"\n" +
            "        },\n" +
            "        \"name_of_the_cont13\": {\n" +
            "            \"name_of_the_cont12\": {\n" +
            "                \"name_of_the_leaf26\": \"abc\"\n" +
            "            },\n" +
            "            \"name_of_the_list9\": [\n" +
            "                {\n" +
            "                    \"name_of_the_leaf27\": \"abc\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name_of_the_leaf27\": \"abc\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"name_of_the_ll9\": [\n" +
            "                \"abc\",\n" +
            "                \"abc\"\n" +
            "            ],\n" +
            "            \"name_of_the_leaf28\": \"abc\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    static final String ENCODE_TO_XML_RPC_ID = "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<input xmlns=\"urn:opendaylight:params:xml:ns:yang:test:name\"" +
            ">\n" +
            "    <name_of_the_cont14>\n" +
            "        <name_of_the_leaf28>abc</name_of_the_leaf28>\n" +
            "    </name_of_the_cont14>\n" +
            "    <name_of_the_cont13>\n" +
            "        <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "        <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "        <name_of_the_leaf28>abc</name_of_the_leaf28>\n" +
            "        <name_of_the_list9>\n" +
            "            <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "        </name_of_the_list9>\n" +
            "        <name_of_the_list9>\n" +
            "            <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "        </name_of_the_list9>\n" +
            "    </name_of_the_cont13>\n" +
            "    <name_of_the_leaf30>abc</name_of_the_leaf30>\n" +
            "    <name_of_the_ll10>abc</name_of_the_ll10>\n" +
            "    <name_of_the_ll10>abc</name_of_the_ll10>\n" +
            "    <name_of_the_list10>\n" +
            "        <name_of_the_leaf29>abc</name_of_the_leaf29>\n" +
            "    </name_of_the_list10>\n" +
            "    <name_of_the_list10>\n" +
            "        <name_of_the_leaf29>abc</name_of_the_leaf29>\n" +
            "    </name_of_the_list10>\n" +
            "    <name_of_the_cont15>\n" +
            "        <name_of_the_leaf31>abc</name_of_the_leaf31>\n" +
            "    </name_of_the_cont15>\n" +
            "</input>\n";

    static final String DECODE_FROM_XML_RPC_ID = "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<output xmlns=\"urn:opendaylight:params:xml:ns:yang:test:name" +
            "\">\n" +
            "    <name_of_the_cont16>\n" +
            "        <name_of_the_leaf32>abc</name_of_the_leaf32>\n" +
            "    </name_of_the_cont16>\n" +
            "    <name_of_the_list11>\n" +
            "        <name_of_the_leaf33>abc</name_of_the_leaf33>\n" +
            "    </name_of_the_list11>\n" +
            "    <name_of_the_list11>\n" +
            "        <name_of_the_leaf33>abc</name_of_the_leaf33>\n" +
            "    </name_of_the_list11>\n" +
            "    <name_of_the_leaf34>abc</name_of_the_leaf34>\n" +
            "    <name_of_the_ll11>abc</name_of_the_ll11>\n" +
            "    <name_of_the_ll11>abc</name_of_the_ll11>\n" +
            "    <name_of_the_cont17>\n" +
            "        <name_of_the_leaf35>abc</name_of_the_leaf35>\n" +
            "    </name_of_the_cont17>\n" +
            "    <name_of_the_cont13>\n" +
            "        <name_of_the_cont12>\n" +
            "            <name_of_the_leaf26>abc</name_of_the_leaf26>\n" +
            "        </name_of_the_cont12>\n" +
            "        <name_of_the_list9>\n" +
            "            <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "        </name_of_the_list9>\n" +
            "        <name_of_the_list9>\n" +
            "            <name_of_the_leaf27>abc</name_of_the_leaf27>\n" +
            "        </name_of_the_list9>\n" +
            "        <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "        <name_of_the_ll9>abc</name_of_the_ll9>\n" +
            "        <name_of_the_leaf28>abc</name_of_the_leaf28>\n" +
            "    </name_of_the_cont13>\n" +
            "</output>";
}
