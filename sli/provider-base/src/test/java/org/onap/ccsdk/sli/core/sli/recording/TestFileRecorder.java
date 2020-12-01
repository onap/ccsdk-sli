/**
 *
 */
package org.onap.ccsdk.sli.core.sli.recording;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * @author dt5972
 *
 */
public class TestFileRecorder {
	private FileRecorder recorder;

	@Before
	public void setUp() {
		recorder = new FileRecorder();
	}

	/**
	 * Test method for
	 * {@link org.onap.ccsdk.sli.core.sli.recording.FileRecorder#record(java.util.Map)}.
	 */
	@Test
	public void testRecord() {
		HashMap<String, String> parms = new HashMap<>();
		parms.put("file", "/dev/null");
		parms.put("field1", "hi");
		try {
			recorder.record(parms);
		} catch (SvcLogicException e) {
			fail("Caught SvcLogicException : " + e.getMessage());
		}
	}

	@Test(expected = Exception.class)
	public void testRecordForEmptyFileName() throws Exception {
		HashMap<String, String> parms = new HashMap<>();
		parms.put("field1", "hi");
		recorder.record(parms);
	}

}
