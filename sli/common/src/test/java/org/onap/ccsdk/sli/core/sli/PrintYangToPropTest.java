/**
 *
 */
package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dt5972
 *
 */
public class PrintYangToPropTest {

	private static final Logger LOG = LoggerFactory.getLogger(PrintYangToPropTest.class);
	@Test
	public void test() {

		Properties props = new Properties();

		// Set up a builder with data
		ExecuteGraphInputBuilder egBuilder = new ExecuteGraphInputBuilder();
		egBuilder.setMode(Mode.Sync);
		egBuilder.setModuleName("my-module");
		egBuilder.setRpcName("my-rpc");

		List<SliParameter> pList = new LinkedList<>();

		SliParameterBuilder pBuilder = new SliParameterBuilder();
		pBuilder.setParameterName("string-param");
		pBuilder.setStringValue("hi");
		pList.add(pBuilder.build());
		pBuilder.setParameterName("int-param");
		pBuilder.setIntValue(1);
		pBuilder.setStringValue(null);
		pList.add(pBuilder.build());
		pBuilder.setParameterName("bool-param");
		pBuilder.setIntValue(null);
		pBuilder.setBooleanValue(true);
		pList.add(pBuilder.build());

		egBuilder.setSliParameter(pList);


		// Generate properties
		props = PrintYangToProp.toProperties(props, egBuilder);

		Enumeration propNames = props.propertyNames();

		while (propNames.hasMoreElements()) {
			String propName = (String) propNames.nextElement();
			LOG.info("Property {} = {}", propName, props.getProperty(propName));
		}

	}

}
