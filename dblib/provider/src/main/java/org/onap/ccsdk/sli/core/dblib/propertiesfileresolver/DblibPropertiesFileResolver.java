/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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

package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import java.io.File;
import java.util.Optional;

/**
 * Strategy for resolving dblib properties.
 */
public interface DblibPropertiesFileResolver {

    /**
     * Resolve dblib properties file.
     *
     * @param dblibFileName the name of the file to look for at the specific location.
     * @return An optional File or empty.
     */
    Optional<File> resolveFile(final String dblibFileName);

    /**
     * A success message, used only for logging now.
     *
     * @return a success message, used only for logging now.
     */
    String getSuccessfulResolutionMessage();
}
