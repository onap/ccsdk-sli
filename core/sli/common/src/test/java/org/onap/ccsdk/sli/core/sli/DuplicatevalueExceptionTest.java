package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;

import org.junit.Test;

public class DuplicatevalueExceptionTest {

	
	@Test
	public void DuplicateValueExceptionTest() {
		assertNotNull(new DuplicateValueException());
	
	}
	
	@Test
	public void DuplicateValueExceptionTestString() {
		assertNotNull(new DuplicateValueException("JUnit Test"));
	
	}
	
	@Test
	public void DuplicateValueExceptionTestStringThrowable() {
		assertNotNull(new DuplicateValueException("JUnit Test", new Exception("JUnit Test")));
	
	}

}
