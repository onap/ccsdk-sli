package org.onap.ccsdk.sli.core.sli.provider;

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
	private static SvcLogicClassResolver instance = new SvcLogicClassResolver();

	private SvcLogicClassResolver() {
	}

	public static SvcLogicClassResolver getInstance() {
		return instance;
	}

	public Object resolve(String className) {

		Bundle bundle = FrameworkUtil.getBundle(SvcLogicClassResolver.class);

		if (bundle == null) {
			// Running outside OSGi container (e.g. jUnit). Use Reflection
			// to resolve class
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
		return SvcLogicAdaptorFactory.getInstance(adaptorName);
	}

}
