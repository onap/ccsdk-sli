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

import java.net.URI;

/**
 * Representation of YANG namespace.
 */
public class Namespace {
    private String moduleName;
    private URI moduleNs;
    private String revision;

    /**
     * Creates an instance of namespace with specified module name,
     * namespace and revision.
     *
     * @param modulename module name
     * @param moduleNs module namespace
     * @param revision revision
     */
    public Namespace(String modulename, URI moduleNs, String revision) {
        this.moduleName = modulename;
        this.moduleNs = moduleNs;
        this.revision = revision;
    }

    /**
     * Returns module name.
     *
     * @return module name
     */
    public String moduleName() {
        return moduleName;
    }

    /**
     * Sets module name.
     *
     * @param moduleName module name
     */
    public void moduleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Sets module namespace.
     *
     * @return module namespace
     */
    public URI moduleNs() {
        return moduleNs;
    }

    /**
     * Sets module namespace.
     *
     * @param moduleNs module namespace
     */
    public void moduleNs(URI moduleNs) {
        this.moduleNs = moduleNs;
    }

    /**
     * Returns revision.
     *
     * @return revision
     */
    public String revision() {
        return revision;
    }

    /**
     * Sets revision.
     *
     * @param revision revision
     */
    public void revision(String revision) {
        this.revision = revision;
    }
}
