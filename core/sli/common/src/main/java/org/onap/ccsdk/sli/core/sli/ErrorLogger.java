package org.onap.ccsdk.sli.core.sli;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ErrorLogger {
	public static final int ERROR_CODE_100 = 100;
	public static final int ERROR_CODE_200 = 200;
	public static final int ERROR_CODE_300 = 300;
	public static final int ERROR_CODE_400 = 400;
	public static final int ERROR_CODE_500 = 500;
	public static final int ERROR_CODE_900 = 900;

	private static final String DEFAULT_100_DESCRIPTION = "Permission Error";
	private static final String DEFAULT_200_DESCRIPTION = "Availability Error or Timeout";
	private static final String DEFAULT_300_DESCRIPTION = "Data Error";
	private static final String DEFAULT_400_DESCRIPTION = "Schema Error";
	private static final String DEFAULT_500_DESCRIPTION = "Business Process Error";
	private static final String DEFAULT_900_DESCRIPTION = "Unknown Error";
	private Logger log;
	
	public ErrorLogger() {
		this.log = LoggerFactory.getLogger(ErrorLogger.class);
	}
	
	public ErrorLogger(Logger log) {
		this.log = log;	
	}

	public void logError(String message, int errorCode) {
		createLogEntry(message, errorCode, null, null);
	}

	public void logError(String message, int errorCode, Exception e) {
		createLogEntry(message, errorCode, null, e);
	}

	public void logError(String message, int errorCode, String description) {
		createLogEntry(message, errorCode, description, null);
	}

	public void logError(String message, int errorCode, String description, Exception e) {
		createLogEntry(message, errorCode, description, e);
	}

	public void createLogEntry(String message, int errorCode, String description, Exception e) {
		//If the error code isn't valid default it to unknown error code
		if(!isValidCode(errorCode)) {
			errorCode = 900;
		}
	
		MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, String.valueOf(errorCode));

		if (description == null || description.isEmpty()) {
			description = getDefaultDescription(errorCode);
		}

		MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, description);
		if (e != null) {
			log.error(message, e);
		} else {
			log.error(message);
		}
		clearKeys();
	}

	public boolean isValidCode(int errorCode) {
		if (errorCode == ERROR_CODE_100 || errorCode == ERROR_CODE_200 || errorCode == ERROR_CODE_300 || errorCode == ERROR_CODE_400 || errorCode == ERROR_CODE_500
				|| errorCode == ERROR_CODE_900) {
			return true;
		}
		return false;
	}

	public String getDefaultDescription(int errorCode) {
		if (errorCode == ERROR_CODE_100) {
			return DEFAULT_100_DESCRIPTION;
		}
		if (errorCode == ERROR_CODE_200) {
			return DEFAULT_200_DESCRIPTION;
		}
		if (errorCode == ERROR_CODE_300) {
			return DEFAULT_300_DESCRIPTION;
		}
		if (errorCode == ERROR_CODE_400) {
			return DEFAULT_400_DESCRIPTION;
		}
		if (errorCode == ERROR_CODE_500) {
			return DEFAULT_500_DESCRIPTION;
		}
		return DEFAULT_900_DESCRIPTION;
	}

	public void clearKeys() {
		MDC.remove(ONAPLogConstants.MDCs.ERROR_CODE);
		MDC.remove(ONAPLogConstants.MDCs.ERROR_DESC);
	}
}
