package org.onap.ccsdk.sli.core.sli.provider;

import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link SvcLogicClassResolver} WITH REMOVED OSGi DEPENDENCIES
 */
public class SvcLogicClassResolverLighty implements SvcLogicResolver {

	private static final Logger LOG = LoggerFactory.getLogger(SvcLogicClassResolverLighty.class);

	private final SvcLogicResource svcLogicResource;
	private final SvcLogicRecorder svcLogicRecorder;
	private final SvcLogicJavaPlugin svcLogicJavaPlugin;
	private final SvcLogicAdaptor svcLogicAdaptor;

	public SvcLogicClassResolverLighty(SvcLogicResource svcLogicResource, SvcLogicRecorder svcLogicRecorder,
			SvcLogicJavaPlugin svcLogicJavaPlugin, SvcLogicAdaptor svcLogicAdaptor) {
		this.svcLogicResource = svcLogicResource;
		this.svcLogicRecorder = svcLogicRecorder;
		this.svcLogicJavaPlugin = svcLogicJavaPlugin;
		this.svcLogicAdaptor = svcLogicAdaptor;
	}

	@Override
	public SvcLogicResource getSvcLogicResource(String resourceName) {
		return svcLogicResource;
	}

	@Override
	public SvcLogicRecorder getSvcLogicRecorder(String recorderName) {
		return svcLogicRecorder;
	}

	@Override
	public SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
		return svcLogicJavaPlugin;
	}

	@Override
	public SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName) {
		return svcLogicAdaptor;
	}

}
