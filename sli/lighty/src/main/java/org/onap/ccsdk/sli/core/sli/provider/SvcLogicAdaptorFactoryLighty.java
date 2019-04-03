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

package org.onap.ccsdk.sli.core.sli.provider;

import java.util.HashMap;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link SvcLogicAdaptorFactory} WITH REMOVED OSGi DEPENDENCIES
 */
public class SvcLogicAdaptorFactoryLighty {

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicAdaptorFactoryLighty.class);

	private static HashMap<String, SvcLogicAdaptor> adaptorMap = new HashMap<>();

	public static void registerAdaptor(SvcLogicAdaptor adaptor) {
		String name = adaptor.getClass().getName();
		LOG.info("Registering adaptor " + name);
		adaptorMap.put(name, adaptor);

	}

	public static void unregisterAdaptor(String name) {
		if (adaptorMap.containsKey(name)) {
			LOG.info("Unregistering " + name);
			adaptorMap.remove(name);
		}
	}

}
