/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
 * Modifications Copyright (C) 2018 IBM.
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

import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicClassResolver implements SvcLogicResolver {

	private static final Logger LOG = LoggerFactory.getLogger(SvcLogicClassResolver.class);
	private static final String CLASS_RESOLVER_PROPERTIES="classresolver.properties";
	private static final String ALLOWED_PACKAGES = "org.onap.ccsdk.sli.allowed.packages";
	private static HashMap<String, SvcLogicAdaptor> adaptorMap = new HashMap<>();
	List<String> allowedPackages = new ArrayList<String>();

	public SvcLogicClassResolver() {
		// Initialize list of allowed package names
		Properties props = new Properties();
		try {
			props.load(SvcLogicClassResolver.class.getResourceAsStream(CLASS_RESOLVER_PROPERTIES));
			String allowedPackagesProp = props.getProperty(ALLOWED_PACKAGES, "org.onap.ccsdk.sli");
			String[] allowedPackageArray = allowedPackagesProp.split(",");
			for (int i = 0 ; i < allowedPackageArray.length ; i++) {
				allowedPackages.add(allowedPackageArray[i]);
			}
		} catch (Exception e)
		{
			LOG.warn("Caught exception trying to load properties file {}", CLASS_RESOLVER_PROPERTIES, e);
			allowedPackages.add("org.onap.ccsdk.sli");
		}
	}

	public void registerAdaptor(SvcLogicAdaptor adaptor) {
		String name = adaptor.getClass().getName();
		LOG.info("Registering adaptor " + name);
		adaptorMap.put(name, adaptor);

	}

	public void unregisterAdaptor(String name) {
		if (adaptorMap.containsKey(name)) {
			LOG.info("Unregistering " + name);
			adaptorMap.remove(name);
		}
	}

	private SvcLogicAdaptor getAdaptorInstance(String name) {
		if (adaptorMap.containsKey(name)) {
			return adaptorMap.get(name);
		} else {

			SvcLogicAdaptor adaptor = (SvcLogicAdaptor) resolve(name);

			if (adaptor != null) {
				registerAdaptor(adaptor);
			}

			return adaptor;
		}
	}

	private Object resolve(String className) {

		Bundle bundle = FrameworkUtil.getBundle(SvcLogicClassResolver.class);

		if (bundle == null) {
			// Running outside OSGi container (e.g. jUnit). Use Reflection
			// to resolve class
			if (!isAllowedClassName(className)) {
				LOG.error("Could not resolve class {} - invalid class name", className);
				return null;
			}
			
			try {
				return (Class.forName(className).newInstance());
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

				LOG.error("Could not resolve class " + className, e);
				return null;
			}

		} else {
			BundleContext bctx = bundle.getBundleContext();
			ServiceReference sref = bctx.getServiceReference(className);
			if (sref != null) {
				return bctx.getService(sref);
			} else {

				LOG.warn("Could not find service reference object for class " + className);
				return null;
			}
		}
	}

	private boolean isAllowedClassName(String className) {
		if (className == null) {
			return false;
		}
	
		Iterator<String> packageIter = allowedPackages.iterator();
		while (packageIter.hasNext()) {
			if (className.startsWith(packageIter.next()+".")) {
				return true;
			}
		}

		return false;
	}


	@Override
	public SvcLogicResource getSvcLogicResource(String resourceName) {
		return (SvcLogicResource) resolve(resourceName);
	}

	@Override
	public SvcLogicRecorder getSvcLogicRecorder(String recorderName) {
		return (SvcLogicRecorder) resolve(recorderName);
	}

	@Override
	public SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
		return (SvcLogicJavaPlugin) resolve(pluginName);
	}

	@Override
	public SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName) {
		return getAdaptorInstance(adaptorName);
	}

}
