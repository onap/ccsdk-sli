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

package org.onap.ccsdk.sli.core.sli.provider;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Driver;

public class SvcLogicActivator implements BundleActivator {

	private static final String SVCLOGIC_PROP_VAR = "SDNC_SLI_PROPERTIES";
	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

	private static final Map<String, SvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, SvcLogicNodeExecutor>() {
		{
			put("block", new BlockNodeExecutor());
			put("call", new CallNodeExecutor());
			put("configure", new ConfigureNodeExecutor());
			put("delete", new DeleteNodeExecutor());
			put("execute", new ExecuteNodeExecutor());
			put("exists", new ExistsNodeExecutor());
			put("for", new ForNodeExecutor());
			put("get-resource", new GetResourceNodeExecutor());
			put("is-available", new IsAvailableNodeExecutor());
			put("notify", new NotifyNodeExecutor());
			put("record", new RecordNodeExecutor());
			put("release", new ReleaseNodeExecutor());
			put("reserve", new ReserveNodeExecutor());
			put("return", new ReturnNodeExecutor());
			put("save", new SaveNodeExecutor());
			put("set", new SetNodeExecutor());
			put("switch", new SwitchNodeExecutor());
			put("update", new UpdateNodeExecutor());
            put("break", new BreakNodeExecutor());

		}
	};

	private static LinkedList<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

	private static HashMap<String, SvcLogicAdaptor> adaptorMap = null;
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicActivator.class);
	
	private static Properties props = null;

	private static BundleContext bundleCtx = null;
	
	private static SvcLogicService svcLogicServiceImpl = null;
	
	@Override
	public void start(BundleContext ctx) throws Exception {

		LOG.info("Activating SLI");
		
		bundleCtx = ctx;

		// Read properties
		props = new Properties();
		String propPath = System.getenv(SVCLOGIC_PROP_VAR);
		
		if (propPath == null) {
			String propDir = System.getenv(SDNC_CONFIG_DIR);
			if (propDir == null) {
				
				propDir = "/opt/sdnc/data/properties";
			}
			propPath = propDir + "/svclogic.properties";
			LOG.warn("Environment variable "+SVCLOGIC_PROP_VAR+" unset - defaulting to "+propPath);
		}
		
		File propFile = new File(propPath);
		
		if (!propFile.exists()) {
			
			throw new ConfigurationException(
					"Missing configuration properties file : "
							+ propFile);
		}
		try {
			
			props.load(new FileInputStream(propFile));
		} catch (Exception e) {
			throw new ConfigurationException(
					"Could not load properties file " + propPath, e);

		}


		if (registrations == null) {

			registrations = new LinkedList<ServiceRegistration>();
		}

		// Advertise SvcLogicService
		svcLogicServiceImpl = new SvcLogicServiceImpl();

		LOG.info("SLI: Registering service " + SvcLogicService.NAME
				+ " in bundle " + ctx.getBundle().getSymbolicName());
		ServiceRegistration reg = ctx.registerService(SvcLogicService.NAME,
				svcLogicServiceImpl, null);
		registrations.add(reg);

		// Initialize SvcLogicStore
		try {
			SvcLogicStore store = getStore();
			registerNodeTypes(store);
		} catch (ConfigurationException e) {
			LOG.warn("Could not initialize SvcLogicScore", e);
		}

		LOG.info("SLI - done registering services");
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {

		if (registrations != null) {
			for (ServiceRegistration reg : registrations) {
				ServiceReference regRef = reg.getReference();
				/* Don't bother to remove node types from table
				String nodeType = (String) regRef.getProperty("nodeType");
				if (nodeType != null) {
					LOG.info("SLI - unregistering node type " + nodeType);
					store.unregisterNodeType(nodeType);
				}
				*/
				reg.unregister();
			}
			registrations = null;
		}
	}
	
	public static SvcLogicStore getStore() throws SvcLogicException {
		// Create and initialize SvcLogicStore object - used to access
		// saved service logic.
		
		SvcLogicStore store = null;
		
		try {
			Driver dvr = new Driver();
			store = SvcLogicStoreFactory.getSvcLogicStore(props);
		} catch (Exception e) {
			throw new ConfigurationException(
					"Could not get service logic store", e);

		}

		try {
			store.init(props);
		} catch (Exception e) {
			throw new ConfigurationException(
					"Could not get service logic store", e);
		}
		
		return(store);
	}
	
	private static void registerNodeTypes(SvcLogicStore store) throws SvcLogicException {
		
		if (store == null) {
			return;
		}
		// Advertise built-in node executors
		LOG.info("SLI : Registering built-in node executors");
		Hashtable propTable = new Hashtable();

		for (String nodeType : BUILTIN_NODES.keySet()) {
			LOG.info("SLI - registering node type " + nodeType);
			propTable.clear();
			propTable.put("nodeType", nodeType);

			ServiceRegistration reg = bundleCtx.registerService(SvcLogicNodeExecutor.class.getName(),
					BUILTIN_NODES.get(nodeType), propTable);
			registrations.add(reg);

			store.registerNodeType(nodeType);
			
			LOG.info("SLI - registering node executor");
			
			((SvcLogicServiceImpl)svcLogicServiceImpl).registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));

		}
		
	}

}
