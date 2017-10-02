package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Parameter setting
 *
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * grouping parameter-setting {
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
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/parameter-setting</i>
 *
 */
public interface ParameterSetting
    extends
    DataObject
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "parameter-setting").intern();

    /**
     * Parameter name
     *
     *
     *
     * @return <code>java.lang.String</code> <code>parameterName</code>, or <code>null</code> if not present
     */
    java.lang.String getParameterName();
    
    /**
     * @return <code>java.lang.Integer</code> <code>intValue</code>, or <code>null</code> if not present
     */
    java.lang.Integer getIntValue();
    
    /**
     * @return <code>java.lang.String</code> <code>stringValue</code>, or <code>null</code> if not present
     */
    java.lang.String getStringValue();
    
    /**
     * @return <code>java.lang.Boolean</code> <code>booleanValue</code>, or <code>null</code> if not present
     */
    java.lang.Boolean isBooleanValue();

}

