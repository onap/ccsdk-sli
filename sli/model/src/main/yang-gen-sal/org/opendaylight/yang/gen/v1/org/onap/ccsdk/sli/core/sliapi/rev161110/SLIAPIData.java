package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * Defines API to service logic interpreter
 *
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * module SLI-API {
 *     yang-version 1;
 *     namespace "org:onap:ccsdk:sli:core:sliapi";
 *     prefix "sample";
 *
 *     import ietf-inet-types { prefix "inet"; }
 *     revision 2016-11-10 {
 *         description "Defines API to service logic interpreter
 *         ";
 *     }
 *
 *     container test-results {
 *         list test-result {
 *             key "test-identifier"
 *             leaf test-identifier {
 *                 type string;
 *             }
 *             leaf-list results {
 *                 type string;
 *             }
 *         }
 *     }
 *
 *     grouping response-fields {
 *         leaf response-code {
 *             type string;
 *         }
 *         leaf ack-final-indicator {
 *             type string;
 *         }
 *         leaf response-message {
 *             type string;
 *         }
 *         leaf context-memory-json {
 *             type string;
 *         }
 *     }
 *     grouping parameter-setting {
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
 *     }
 *
 *     rpc healthcheck {
 *         input {
 *         }
 *         
 *         output {
 *             leaf response-code {
 *                 type string;
 *             }
 *             leaf ack-final-indicator {
 *                 type string;
 *             }
 *             leaf response-message {
 *                 type string;
 *             }
 *             leaf context-memory-json {
 *                 type string;
 *             }
 *         }
 *     }
 *     rpc execute-graph {
 *         " Method to add a new parameter.";
 *         input {
 *             leaf module-name {
 *                 type string;
 *             }
 *             leaf rpc-name {
 *                 type string;
 *             }
 *             leaf mode {
 *                 type enumeration;
 *             }
 *             list sli-parameter {
 *                 key "parameter-name"
 *                 leaf parameter-name {
 *                     type string;
 *                 }
 *                 leaf int-value {
 *                     type int32;
 *                 }
 *                 leaf string-value {
 *                     type string;
 *                 }
 *                 leaf boolean-value {
 *                     type boolean;
 *                 }
 *                 uses parameter-setting;
 *             }
 *         }
 *         
 *         output {
 *             leaf response-code {
 *                 type string;
 *             }
 *             leaf ack-final-indicator {
 *                 type string;
 *             }
 *             leaf response-message {
 *                 type string;
 *             }
 *             leaf context-memory-json {
 *                 type string;
 *             }
 *         }
 *     }
 * }
 * </pre>
 *
 */
public interface SLIAPIData
    extends
    DataRoot
{




    /**
     * Test results
     *
     *
     *
     * @return <code>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults</code> <code>testResults</code>, or <code>null</code> if not present
     */
    TestResults getTestResults();

}

