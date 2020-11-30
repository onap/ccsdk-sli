package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExitNodeExceptionTest {
	
	@Test
	public void ExitNodeExceptionTest() {
		assertNotNull(new ExitNodeException());
	
	}
	
	@Test
	public void ExitNodeExceptionTestString() {
		assertNotNull(new ExitNodeException("JUnit Test"));
	
	}
	
	@Test
	public void ExitNodeExceptionTestStringThrowable() {
		assertNotNull(new ExitNodeException("JUnit Test", new Exception("JUnit Test")));
	
	}

}
