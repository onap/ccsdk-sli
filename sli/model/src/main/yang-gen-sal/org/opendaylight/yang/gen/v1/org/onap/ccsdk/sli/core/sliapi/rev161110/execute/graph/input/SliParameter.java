package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput;
import org.opendaylight.yangtools.yang.binding.Identifiable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * list sli-parameter {
 *     key "parameter-name"
 *     leaf parameter-name {
 *         type string;
 *     }
 *     leaf int-value {
 *         type int32;
 *     }
 *     leaf string-value {
 *         type string;
 *     }
 *     leaf boolean-value {
 *         type boolean;
 *     }
 *     uses parameter-setting;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/execute-graph/input/sli-parameter</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder}.
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterKey
 *
 */
public interface SliParameter
    extends
    ChildOf<ExecuteGraphInput>,
    Augmentable<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>,
    ParameterSetting,
    Identifiable<SliParameterKey>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "sli-parameter").intern();

    /**
     * Returns Primary Key of Yang List Type
     *
     *
     *
     * @return <code>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterKey</code> <code>key</code>, or <code>null</code> if not present
     */
    SliParameterKey getKey();

}

