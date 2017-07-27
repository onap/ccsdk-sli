/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 ONAP
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

/**
 *
 */
package org.openecomp.sdnc.sli.SliPluginUtils.commondatastructures;

/**
 * An enum found in many Yang models. It is commonly used as a
 * substitute for boolean.
 */
public enum YesNo {
    N, Y;

    /**
     * Method overload for {@link #valueOf(String)} for the char primative
     */
    public static YesNo valueOf( final char name ) {
        return YesNo.valueOf( Character.toString(name) );
    }

    /**
     * Method overload for {@link #valueOf(String)} for the Character object
     */
    public static YesNo valueOf( final Character name ) {
        if( name == null ) {
            return null;
        }

        return YesNo.valueOf( name.toString() );
    }
}
