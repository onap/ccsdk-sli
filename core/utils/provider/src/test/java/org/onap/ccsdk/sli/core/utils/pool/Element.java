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



package org.onap.ccsdk.sli.core.utils.pool;

import java.io.IOException;

public class Element implements Testable {
    private boolean closed;
    private Integer id;

    public Element(int id) {
        this.id = Integer.valueOf(id);
        closed = false;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Element) {
            Element other = (Element) obj;
            result = this.id.equals(other.id);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public Boolean isClosed() {
        return Boolean.valueOf(closed);
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }

    @Override
    public Integer getId() {
        return id;
    }
}
