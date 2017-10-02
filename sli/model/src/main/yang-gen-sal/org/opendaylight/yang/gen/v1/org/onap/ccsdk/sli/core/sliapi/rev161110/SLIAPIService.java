package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.concurrent.Future;

/**
 * Interface for implementing the following YANG RPCs defined in module <b>SLI-API</b>
 * <pre>
 * rpc healthcheck {
 *     input {
 *     }
 *     
 *     output {
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
 * }
 * rpc execute-graph {
 *     " Method to add a new parameter.";
 *     input {
 *         leaf module-name {
 *             type string;
 *         }
 *         leaf rpc-name {
 *             type string;
 *         }
 *         leaf mode {
 *             type enumeration;
 *         }
 *         list sli-parameter {
 *             key "parameter-name"
 *             leaf parameter-name {
 *                 type string;
 *             }
 *             leaf int-value {
 *                 type int32;
 *             }
 *             leaf string-value {
 *                 type string;
 *             }
 *             leaf boolean-value {
 *                 type boolean;
 *             }
 *             uses parameter-setting;
 *         }
 *     }
 *     
 *     output {
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
 * }
 * </pre>
 *
 */
public interface SLIAPIService
    extends
    RpcService
{




    Future<RpcResult<HealthcheckOutput>> healthcheck();
    
    /**
     * Method to add a new parameter.
     *
     */
    Future<RpcResult<ExecuteGraphOutput>> executeGraph(ExecuteGraphInput input);

}

