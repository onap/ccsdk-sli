package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * grouping response-fields {
 *     leaf response-code {
 *         type string;
 *     }
 *     leaf ack-final-indicator {
 *         type string;
 *     }
 *     leaf response-message {
 *         type string;
 *     }
 *     leaf context-memory-json {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/response-fields</i>
 *
 */
public interface ResponseFields
    extends
    DataObject
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "response-fields").intern();

    /**
     * @return <code>java.lang.String</code> <code>responseCode</code>, or <code>null</code> if not present
     */
    java.lang.String getResponseCode();
    
    /**
     * @return <code>java.lang.String</code> <code>ackFinalIndicator</code>, or <code>null</code> if not present
     */
    java.lang.String getAckFinalIndicator();
    
    /**
     * @return <code>java.lang.String</code> <code>responseMessage</code>, or <code>null</code> if not present
     */
    java.lang.String getResponseMessage();
    
    /**
     * @return <code>java.lang.String</code> <code>contextMemoryJson</code>, or <code>null</code> if not present
     */
    java.lang.String getContextMemoryJson();

}

