package org.onap.ccsdk.sli.core.utils.encryption;/*-
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EncryptionToolTest {

    private static final String PLAIN_TEXT = "text to encrypt";
    private static final String EMPTY_STR = "";

    private final EncryptionTool encryptionTool = EncryptionTool.getInstance();

    @Test
    public void should_return_prefix_given_empty_string() {
        assertEquals("enc:", encryptionTool.encrypt(EMPTY_STR));
    }

    @Test
    public void should_return_null_given_null() {
        assertNull(encryptionTool.encrypt(null));
    }

    @Test
    public void should_encrypt_given_string() {
        String encrypted = encryptionTool.encrypt(PLAIN_TEXT);
        assertNotEquals(encrypted, PLAIN_TEXT);
        assertTrue(encrypted.startsWith(EncryptionTool.ENCRYPTED_VALUE_PREFIX));
    }

    @Test
    public void should_not_decrypt_string_when_not_starting_with_prefix() {
        assertNull(encryptionTool.decrypt(null));
        assertEquals("mdi/12!dsao91", encryptionTool.decrypt("mdi/12!dsao91"));
    }

    @Test
    public void should_decrypt_given_encrypted_string() {
        String encrypted = encryptionTool.encrypt(PLAIN_TEXT);
        assertEquals(PLAIN_TEXT, encryptionTool.decrypt(encrypted));
    }
}
