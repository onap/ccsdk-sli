/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.openecomp.sdnc.sli.provider;

import java.util.HashMap;

import org.openecomp.sdnc.sli.SvcLogicAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicAdaptorFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicAdaptorFactory.class);

	private static HashMap<String, SvcLogicAdaptor> adaptorMap = new HashMap<String, SvcLogicAdaptor>();

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

	public static SvcLogicAdaptor getInstance(String name) {
		if (adaptorMap.containsKey(name)) {
			return (adaptorMap.get(name));
		} else {
			BundleContext bctx = null;
			try
			{
			 bctx = FrameworkUtil.getBundle(SvcLogicAdaptorFactory.class)
					.getBundleContext();
			}
			catch (Exception e)
			{
				LOG.debug("Caught exception trying to locate device adaptor "+name, e);
				return(null);
			}

			ServiceReference sref = bctx.getServiceReference(name);

			if (sref != null) {
				SvcLogicAdaptor adaptor = (SvcLogicAdaptor) bctx
						.getService(sref);

				if (adaptor != null) {
					registerAdaptor(adaptor);

					return (adaptor);
				}
				return (null);
			}
		}
		return(null);
	}
}
