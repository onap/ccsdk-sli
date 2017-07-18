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

package org.openecomp.sdnc.sli.recording;

import java.util.LinkedList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RecordingActivator implements BundleActivator {

	private LinkedList<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

	private static final Logger LOG = LoggerFactory
			.getLogger(RecordingActivator.class);
	
	@Override
	public void start(BundleContext ctx) throws Exception {
		
		if (registrations == null)
		{
			registrations = new LinkedList<ServiceRegistration>();
		}
		

		FileRecorder fileRecorder = new FileRecorder();
		String regName = fileRecorder.getClass().getName();
		LOG.debug("Registering FileRecorder service "+regName);
		ServiceRegistration reg =ctx.registerService(regName, fileRecorder, null);
		registrations.add(reg);
		
		Slf4jRecorder slf4jRecorder = new Slf4jRecorder();
		regName = slf4jRecorder.getClass().getName();
		LOG.debug("Registering Slf4jRecorder service "+regName);
		reg =ctx.registerService(regName, slf4jRecorder, null);
		registrations.add(reg);
		
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		if (registrations != null) {
			for (ServiceRegistration reg : registrations) {
				ServiceReference regRef = reg.getReference();
				reg.unregister();
			}
			registrations = null;
		}
	}

}
