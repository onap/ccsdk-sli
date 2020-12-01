/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;



public class LunchSelectorPlugin implements SvcLogicJavaPlugin {
    public class UnknownLunchDayException extends Exception{

        public UnknownLunchDayException(String string) {
            super(string);
        }

    }
    class Sandwhich {
        String meat;
        String cheese;

        public Sandwhich(String meat, String cheese) {
            this.meat = meat;
            this.cheese = cheese;
        }
    }

    public String selectLunch(Map<String, String> parameters, SvcLogicContext ctx) throws Exception {
        String day = parameters.get("day");
        if (day == null || day.length() < 1) {
            throw new UnknownLunchDayException("What day is it?");
        }
        switch (day) {
        case ("monday"): {
            return "pizza";
        }
        case ("tuesday"): {
            return "soup";
        }
        case ("wednesday"): {
            return "salad";
        }
        case ("thursday"): {
            return "sushi";
        }
        case ("friday"): {
            return "bbq";
        }
        }
        throw new SvcLogicException("Lunch cannot be served");
    }

    public Sandwhich makeLunch(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        return new Sandwhich("ham", "american");
    }
}
