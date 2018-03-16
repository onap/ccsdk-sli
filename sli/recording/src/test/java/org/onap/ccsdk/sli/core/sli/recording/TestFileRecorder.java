/**
 *
 */
package org.onap.ccsdk.sli.core.sli.recording;

import static org.junit.Assert.*;
import java.util.HashMap;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * @author dt5972
 *
 */
public class TestFileRecorder {

    /**
     * Test method for {@link org.onap.ccsdk.sli.core.sli.recording.FileRecorder#record(java.util.Map)}.
     */
    @Test
    public void testRecord() {

        FileRecorder recorder = new FileRecorder();

        HashMap<String,String> parms = new HashMap<>();
        parms.put("file", "/dev/null");
        parms.put("field1","hi");
        try {
            recorder.record(parms);
        } catch (SvcLogicException e) {
            fail("Caught SvcLogicException : "+e.getMessage());
        }
    }

}
