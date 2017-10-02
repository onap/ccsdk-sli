package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * container output {
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
 *     uses response-fields;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/execute-graph/output</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutputBuilder}.
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutputBuilder
 *
 */
public interface ExecuteGraphOutput
    extends
    ResponseFields,
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "output").intern();


}

