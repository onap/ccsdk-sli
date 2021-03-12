/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.encryption;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to encapsulate the encryption and decryption support in one place and to
 * provide a utility to encrypt and decrypt data.
 */
public class EncryptionTool {

    /**
     * The prefix we insert onto any data we encrypt so that we can tell if it is encrypted later and
     * therefore decrypt it
     */
    public static final String ENCRYPTED_VALUE_PREFIX = "enc:";

    /**
     * The instance of the encryption utility object
     */
    private static EncryptionTool instance = null;

    /**
     * The logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EncryptionTool.class);

    /**
     * The secret passphrase (PBE) that we use to perform encryption and decryption. The algorithm we
     * are using is a symmetrical cipher.
     */
    private static final char[] secret = {'C', '_', 'z', 'l', '!', 'K', '!', '4', '?', 'O', 'z', 'E', 'K', 'E', '>', 'U', 'R',
                                          '/', '%', 'Y', '\\', 'f', 'b', '"', 'e', 'n', '{', '"', 'l', 'U', 'F', '+', 'E', '\'', 'R', 'T', 'p', '1',
                                          'V', '4', 'l', 'a', '9', 'w', 'v', '5', 'Z', '#', 'i', 'V', '"', 'd', 'l', '!', 'L', 'M', 'g', 'L', 'Q',
                                          '{', 'v', 'v', 'K', 'V'};



    /**
     * Get an instance of the EncryptionTool
     *
     * @return The encryption tool to be used
     */
    public static synchronized EncryptionTool getInstance() {
        if (instance == null) {
            instance = new EncryptionTool();
        }
        return instance;
    }

    /**
     * Create the EncryptionTool instance
     */
    private EncryptionTool() {

        StringBuilder sb = new StringBuilder("Found the following security algorithms:");
        for (Provider p : Security.getProviders()) {
            for (Service s : p.getServices()) {
                String algo = s.getAlgorithm();
                sb.append(String.format("%n -Algorithm [ %s ] in provider [ %s ] and service [ %s ]", algo, p.getName(),
                        s.getClassName()));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(sb.toString());
        }
    }

    /**
     * Decrypt the provided encrypted text
     *
     * @param cipherText THe cipher text to be decrypted. If the ciphertext is not encrypted, then it is
     *        returned as is.
     * @return the clear test of the (possibly) encrypted value. The original value if the string is not
     *         encrypted.
     */
    public synchronized String decrypt(String cipherText) {
        if (isEncrypted(cipherText)) {
            String encValue = cipherText.substring(ENCRYPTED_VALUE_PREFIX.length());
            byte[] plainByte = Base64.getDecoder().decode(encValue.getBytes());
            byte[] decryptByte = xorWithSecret(plainByte);
            return new String(decryptByte);
        } else {
            return cipherText;
        }

    }

    /**
     * Encrypt the provided clear text
     *
     * @param clearText The clear text to be encrypted
     * @return the encrypted text. If the clear text is empty (null or zero length), then an empty
     *         string is returned. If the clear text is already encrypted, it is not encrypted again and
     *         is returned as is. Otherwise, the clear text is encrypted and returned.
     */
    public synchronized String encrypt(String clearText) {
        if (clearText != null) {
            byte[] encByte = xorWithSecret(clearText.getBytes());
            String encryptedValue = new String(Base64.getEncoder().encode(encByte));
            return ENCRYPTED_VALUE_PREFIX + encryptedValue;
        } else {
            return null;
        }
    }

    /**
     * Is a value encrypted? A value is considered to be encrypted if it begins with the
     * {@linkplain #ENCRYPTED_VALUE_PREFIX encrypted value prefix}.
     *
     * @param value the value to check.
     * @return true/false;
     */
    private static boolean isEncrypted(final String value) {
        return value != null && value.startsWith(ENCRYPTED_VALUE_PREFIX);
    }

    /**
     * XORs the input byte array with the secret key, padding 0x0 to the end of the secret key if the
     * input is longer and returns a byte array the same size as input
     *
     * @param inp The byte array to be XORed with secret
     * @return A byte array the same size as inp or null if input is null.
     */
    private byte[] xorWithSecret(byte[] inp) {
        if (inp == null) {
            return new byte[0];
        }

        byte[] secretBytes = new String(secret).getBytes();
        int size = inp.length;

        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = (byte) ((inp[i]) ^ (secretBytes[i % secretBytes.length]));
        }
        return out;
    }

}

