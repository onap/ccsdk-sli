package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.List;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yangtools.yang.binding.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * container input {
 *     leaf module-name {
 *         type string;
 *     }
 *     leaf rpc-name {
 *         type string;
 *     }
 *     leaf mode {
 *         type enumeration;
 *     }
 *     list sli-parameter {
 *         key "parameter-name"
 *         leaf parameter-name {
 *             type string;
 *         }
 *         leaf int-value {
 *             type int32;
 *         }
 *         leaf string-value {
 *             type string;
 *         }
 *         leaf boolean-value {
 *             type boolean;
 *         }
 *         uses parameter-setting;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/execute-graph/input</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder}.
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder
 *
 */
public interface ExecuteGraphInput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput>
{


    public enum Mode {
        Sync(0, "sync"),
        
        Async(1, "async")
        ;
    
    
        java.lang.String name;
        int value;
        private static final java.util.Map<java.lang.Integer, Mode> VALUE_MAP;
    
        static {
            final com.google.common.collect.ImmutableMap.Builder<java.lang.Integer, Mode> b = com.google.common.collect.ImmutableMap.builder();
            for (Mode enumItem : Mode.values())
            {
                b.put(enumItem.value, enumItem);
            }
    
            VALUE_MAP = b.build();
        }
    
        private Mode(int value, java.lang.String name) {
            this.value = value;
            this.name = name;
        }
    
        /**
         * Returns the name of the enumeration item as it is specified in the input yang.
         *
         * @return the name of the enumeration item as it is specified in the input yang
         */
        public java.lang.String getName() {
            return name;
        }
    
        /**
         * @return integer value
         */
        public int getIntValue() {
            return value;
        }
    
        /**
         * @param valueArg
         * @return corresponding Mode item
         */
        public static Mode forValue(int valueArg) {
            return VALUE_MAP.get(valueArg);
        }
    }

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "input").intern();

    /**
     * @return <code>java.lang.String</code> <code>moduleName</code>, or <code>null</code> if not present
     */
    java.lang.String getModuleName();
    
    /**
     * @return <code>java.lang.String</code> <code>rpcName</code>, or <code>null</code> if not present
     */
    java.lang.String getRpcName();
    
    /**
     * @return <code>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode</code> <code>mode</code>, or <code>null</code> if not present
     */
    Mode getMode();
    
    /**
     * @return <code>java.util.List</code> <code>sliParameter</code>, or <code>null</code> if not present
     */
    List<SliParameter> getSliParameter();

}

