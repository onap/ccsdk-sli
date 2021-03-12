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

package org.onap.ccsdk.sli.adaptors.netconf.internal;

import java.io.ByteArrayOutputStream;

class NetconfMessage {

    private static final String EOM = "]]>]]>";

    private String text;
    private MessageBuffer buffer = new MessageBuffer();
    private int eomNotch;

    NetconfMessage() {
    }

    NetconfMessage(String text) {
        if(text == null) {
            throw new NullPointerException("Netconf message payload is null");
        }
        append(text.getBytes(), 0, text.length());
        if(this.text == null) {
            this.text = text;
        }
    }

    void append(byte[] bytes, int start, int finish) {
        boolean eomFound = false;
        int end = finish;
        for(int i = start; i < end; i++) {
            if(bytes[i] == EOM.charAt(eomNotch)) {
                // advance notch
                eomNotch++;
            } else {
                // reset notch
                eomNotch = 0;
            }
            if(eomNotch == EOM.length()) {
                // end of message found
                eomFound = true;
                end = i + 1;
                break;
            }
        }
        buffer.write(bytes, start, end);
        if(eomFound) {
            text = new String(buffer.getBytes(), 0, buffer.size() - EOM.length());
            buffer.reset();
        }
    }

    String getText() {
        return text;
    }

    boolean isCompleted() {
        return text != null;
    }

    byte[] getFrame() {
        StringBuilder sb = new StringBuilder();
        if(text != null) {
            sb.append(text).append("\n");
        }
        sb.append(EOM);
        return sb.toString().getBytes();
    }

    private class MessageBuffer extends ByteArrayOutputStream {

        byte[] getBytes() {
            return buf;
        }
    }
}
