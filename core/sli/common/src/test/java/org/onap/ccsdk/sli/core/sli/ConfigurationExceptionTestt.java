package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationExceptionTestt {

	
	@Test
	public void ConfigurationExceptionTest() {
		assertNotNull(new ConfigurationException());
	
	}
	
	@Test
	public void ConfigurationExceptionTestString() {
		assertNotNull(new ConfigurationException("JUnit Test"));
	
	}
	
	@Test
	public void ConfigurationExceptionTestStringThrowable() {
		assertNotNull(new ConfigurationException("JUnit Test", new Exception("JUnit Test")));
	
	}
	
	 

}
