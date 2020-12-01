package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLoggerTest {
	private Logger log = LoggerFactory.getLogger(ErrorLoggerTest.class);

	@Test
	public void testOverloads() throws Exception {
		ErrorLogger e = new ErrorLogger();
		Exception exc = new Exception();
		e.logError("failure", 200);
		e.logError("failure", 200, exc);
		e.logError("failure", 200, "Timeout during HTTP operation");
		e.logError("failure", 200, "Timeout during HTTP operation", exc);
	}

	@Test
	public void testInvalidErrorCode() throws Exception {
		ErrorLogger e = new ErrorLogger();
		e.logError("failure", 0);
	}

	@Test
	public void testDescriptionMapping() throws Exception {
		ErrorLogger e = new ErrorLogger();
		e.logError("failure", 100);
		e.logError("failure", 200);
		e.logError("failure", 300);
		e.logError("failure", 400);
		e.logError("failure", 500);
		e.logError("failure", 900);
	}

	@Test
	public void testIsValidCode() throws Exception {
		ErrorLogger e = new ErrorLogger(log);
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_100));
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_200));
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_300));
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_400));
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_500));
		assertTrue(e.isValidCode(ErrorLogger.ERROR_CODE_900));

		assertFalse(e.isValidCode(0));
		assertFalse(e.isValidCode(204));
		assertFalse(e.isValidCode(404));
		assertFalse(e.isValidCode(501));
	}

}
