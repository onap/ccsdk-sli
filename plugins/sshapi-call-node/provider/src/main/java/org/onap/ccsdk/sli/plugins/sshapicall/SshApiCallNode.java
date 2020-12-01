/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Copyright (C) 2018 Samsung Electronics. All rights
  			reserved.
 * ================================================================================
 * Modifications Copyright © 2018 IBM
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.plugins.sshapicall;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.onap.appc.adapter.ssh.SshAdapter;
import org.onap.appc.adapter.ssh.SshConnection;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.plugins.sshapicall.model.AuthType;
import org.onap.ccsdk.sli.plugins.sshapicall.model.Parameters;
import org.onap.ccsdk.sli.plugins.sshapicall.model.ParseParam;


public class SshApiCallNode implements SvcLogicJavaPlugin {

    private static final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

    /**
     * Output parameter - SSH command execution status.
     */
    private String PARAM_OUT_status = "sshApi.call.node.status";

    /**
     * Output parameter - content of SSH command stdout.
     */
    private String PARAM_OUT_stdout = "sshApi.call.node.stdout";

    /**
     * Output parameter - content of SSH command stderr.
     */
    private String PARAM_OUT_stderr = "sshApi.call.node.stderr";

    /**
     * Testing parameter - content of mocked SSH.
     */
    private boolean PARAM_TEST_MODE = false;
    private String PARAM_OUT_MESSAGE = "TestOut";


    /**
     * Default success status.
     */
    private int DEF_SUCCESS_STATUS = 0;



    private SshAdapter sshAdapter;

    /**
     * Used for jUnit test and testing interface
     */
    public SshApiCallNode(boolean mode) {
        PARAM_TEST_MODE = mode;
    }

    public SshApiCallNode() {
        PARAM_TEST_MODE = false;
    }

    public void setSshAdapter(SshAdapter sshAdapter) {
        this.sshAdapter = sshAdapter;
    }

    /**
     * Allows Directed Graphs  the ability to interact with SSH APIs.
     * @param params HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>Url</td><td>Mandatory</td><td>url to make the SSH connection request to.</td></tr>
     *      <tr><td>Port</td><td>Mandatory</td><td>port to make the SSH connection request to.</td></tr>
     *      <tr><td>User</td><td>Optional</td><td>user name to use for ssh basic authentication</td><td>sdnc_ws</td></tr>
     *      <tr><td>Password</td><td>Optional</td><td>unencrypted password to use for ssh basic authentication</td><td>plain_password</td></tr>
     *      <tr><td>SshKey</td><td>Optional</td><td>Consumer SSH key to use for ssh authentication</td><td>plain_key</td></tr>
     *      <tr><td>ExecTimeout</td><td>Optional</td><td>SSH command execution timeout</td><td>plain_key</td></tr>
     *      <tr><td>Retry</td><td>Optional</td><td>Make ssh connection with default retry policy</td><td>plain_key</td></tr>
     *      <tr><td>Cmd</td><td>Mandatory</td><td>ssh command to be executed on the server.</td><td>get post put delete patch</td></tr>
     *      <tr><td>ResponsePrefix</td><td>Optional</td><td>location the response will be written to in context memory</td></tr>
     *      <tr><td>ResponseType</td><td>Optional</td><td>If we know the response is to be in a specific format (supported are JSON, XML and NONE) </td></tr>
     *      <tr><td>listName[i]</td><td>Optional</td><td>Used for processing XML responses with repeating elements.</td>vpn-information.vrf-details<td></td></tr>
     *      <tr><td>ConvertResponse </td><td>Optional</td><td>whether the response should be converted</td><td>true or false</td></tr>
     *      <tr><td>AuthType</td><td>Optional</td><td>Type of authentiation to be used BASIC or sshKey based</td><td>true or false</td></tr>
     *      <tr><td>EnvParameters</td><td>Optional</td><td>A JSON dictionary which should list key value pairs to be passed to the command execution.
     *               These values would correspond to instance specific parameters that a command may need to execute an action.</td></tr>
     *      <tr><td>FileParameters</td><td>Optional</td><td>A JSON dictionary where keys are filenames and values are contents of files.
     *               The SSH Server will utilize this feature to generate files with keys as filenames and values as content.
     *               This attribute can be used to generate files that a command may require as part of execution.</td></tr>
     *  </tbody>
     * </table>
     * Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param ctx Reference to context memory
     */

    public void execCommand(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        execSshCommand(params, ctx, false);
    }

    private void execSshCommand(Map<String, String> params, SvcLogicContext ctx, boolean withPty) throws SvcLogicException {
        ParseParam parser = new ParseParam();
        Parameters p = parser.getParameters(params);
        logger.debug("=> Connecting to SSH server...");
        SshConnection sshConnection = null;
        int status = -1;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        String stdoutRes = "";
        String stderrRes = "";
        try {
            if (!PARAM_TEST_MODE) {
                sshConnection = getSshConnection(p);
                sshConnection.connect();
                logger.debug("=> Connected to SSH server...");
                logger.debug("=> Running SSH command...");
                sshConnection.setExecTimeout(p.sshExecTimeout);
                if (withPty) {
                    status = sshConnection.execCommandWithPty(parser.makeCommand(params), stdout);
                    stderr = stdout;
                }
                else
                    status = sshConnection.execCommand(parser.makeCommand(params), stdout, stderr);
                stdoutRes = stdout.toString();
                stderrRes = stderr.toString();

            } else {
                if (("true").equalsIgnoreCase(params.get("TestFail")))
                    status = 202;
                else
                    status = DEF_SUCCESS_STATUS;
                stdoutRes = params.get(PARAM_OUT_MESSAGE);
            }
            logger.debug("=> executed SSH command");

            if(status == DEF_SUCCESS_STATUS) {
                parser.parseOutput(ctx, stdoutRes);
            }
            ctx.setAttribute(PARAM_OUT_status, String.format("%01d", status));
            ctx.setAttribute(PARAM_OUT_stdout, stdoutRes);
            ctx.setAttribute(PARAM_OUT_stderr, stderrRes);
        } catch (Exception e){
            throw new SvcLogicException("Exception in SSH adaptor : " + e.getMessage());
        } finally {
            if (sshConnection != null)
               sshConnection.disconnect();
        }
    }

    private SshConnection getSshConnection(Parameters p) throws SvcLogicException {
        if (p.authtype == AuthType.BASIC)
            return sshAdapter.getConnection(p.sshapiUrl, p.sshapiPort, p.sshapiUser, p.sshapiPassword);
        // This is not supported yet in the API, patch has already been added to APPC
        else if (p.authtype == AuthType.KEY){
            throw new SvcLogicException("SSH Key based Auth method not supported");
        }
        else if (p.authtype == AuthType.NONE){
            throw new SvcLogicException("SSH Auth type required, BASIC auth in support");
        }
        else if (p.authtype == AuthType.UNSPECIFIED){
            if (p.sshapiUser != null && p.sshapiPassword != null)
                return sshAdapter.getConnection(p.sshapiUrl, p.sshapiPort, p.sshapiUser, p.sshapiPassword);
            else if (p.sshKey != null)
                throw new SvcLogicException("SSH Key based Auth method not supported");
        }
        throw new SvcLogicException("SSH Auth type required, BASIC auth in support");
    }


    /**
     * Allows Directed Graphs  the ability to interact with SSH APIs.
     * @param params HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>Url</td><td>Mandatory</td><td>url to make the SSH connection request to.</td></tr>
     *      <tr><td>Port</td><td>Mandatory</td><td>port to make the SSH connection request to.</td></tr>
     *      <tr><td>User</td><td>Optional</td><td>user name to use for ssh basic authentication</td><td>sdnc_ws</td></tr>
     *      <tr><td>Password</td><td>Optional</td><td>unencrypted password to use for ssh basic authentication</td><td>plain_password</td></tr>
     *      <tr><td>SshKey</td><td>Optional</td><td>Consumer SSH key to use for ssh authentication</td><td>plain_key</td></tr>
     *      <tr><td>ExecTimeout</td><td>Optional</td><td>SSH command execution timeout</td><td>plain_key</td></tr>
     *      <tr><td>Retry</td><td>Optional</td><td>Make ssh connection with default retry policy</td><td>plain_key</td></tr>
     *      <tr><td>Cmd</td><td>Mandatory</td><td>ssh command to be executed on the server.</td><td>get post put delete patch</td></tr>
     *      <tr><td>ResponsePrefix</td><td>Optional</td><td>location the response will be written to in context memory</td></tr>
     *      <tr><td>ResponseType</td><td>Optional</td><td>If we know the response is to be in a specific format (supported are JSON, XML and NONE) </td></tr>
     *      <tr><td>listName[i]</td><td>Optional</td><td>Used for processing XML responses with repeating elements.</td>vpn-information.vrf-details<td></td></tr>
     *      <tr><td>ConvertResponse </td><td>Optional</td><td>whether the response should be converted</td><td>true or false</td></tr>
     *      <tr><td>AuthType</td><td>Optional</td><td>Type of authentiation to be used BASIC or sshKey based</td><td>true or false</td></tr>
     *      <tr><td>EnvParameters</td><td>Optional</td><td>A JSON dictionary which should list key value pairs to be passed to the command execution.
     *               These values would correspond to instance specific parameters that a command may need to execute an action.</td></tr>
     *      <tr><td>FileParameters</td><td>Optional</td><td>A JSON dictionary where keys are filenames and values are contents of files.
     *               The SSH Server will utilize this feature to generate files with keys as filenames and values as content.
     *               This attribute can be used to generate files that a command may require as part of execution.</td></tr>
     *  </tbody>
     * </table>
     * Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param ctx Reference to context memory
     */

    public void execWithStatusCheck(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        execCommand(params, ctx);
        parseResponse(ctx);
    }

    private void parseResponse (SvcLogicContext ctx) throws SvcLogicException {
        int status = Integer.parseInt(ctx.getAttribute(PARAM_OUT_status));
        if(status != DEF_SUCCESS_STATUS) {
            StringBuilder errmsg = new StringBuilder();
            errmsg.append("SSH command returned error status [").append(status).append(']');
            String stderr = ctx.getAttribute(PARAM_OUT_stderr);
            if((stderr != null) && !stderr.isEmpty()) {
                errmsg.append(". Error: [").append(stderr).append(']');
            }
            throw new SvcLogicException(errmsg.toString());
        }
    }

    /**
     * Allows Directed Graphs  the ability to interact with SSH APIs.
     * @param params HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>Url</td><td>Mandatory</td><td>url to make the SSH connection request to.</td></tr>
     *      <tr><td>Port</td><td>Mandatory</td><td>port to make the SSH connection request to.</td></tr>
     *      <tr><td>User</td><td>Optional</td><td>user name to use for ssh basic authentication</td><td>sdnc_ws</td></tr>
     *      <tr><td>Password</td><td>Optional</td><td>unencrypted password to use for ssh basic authentication</td><td>plain_password</td></tr>
     *      <tr><td>SshKey</td><td>Optional</td><td>Consumer SSH key to use for ssh authentication</td><td>plain_key</td></tr>
     *      <tr><td>ExecTimeout</td><td>Optional</td><td>SSH command execution timeout</td><td>plain_key</td></tr>
     *      <tr><td>Retry</td><td>Optional</td><td>Make ssh connection with default retry policy</td><td>plain_key</td></tr>
     *      <tr><td>Cmd</td><td>Mandatory</td><td>ssh command to be executed on the server.</td><td>get post put delete patch</td></tr>
     *      <tr><td>ResponsePrefix</td><td>Optional</td><td>location the response will be written to in context memory</td></tr>
     *      <tr><td>ResponseType</td><td>Optional</td><td>If we know the response is to be in a specific format (supported are JSON, XML and NONE) </td></tr>
     *      <tr><td>listName[i]</td><td>Optional</td><td>Used for processing XML responses with repeating elements.</td>vpn-information.vrf-details<td></td></tr>
     *      <tr><td>ConvertResponse </td><td>Optional</td><td>whether the response should be converted</td><td>true or false</td></tr>
     *      <tr><td>AuthType</td><td>Optional</td><td>Type of authentiation to be used BASIC or sshKey based</td><td>true or false</td></tr>
     *      <tr><td>EnvParameters</td><td>Optional</td><td>A JSON dictionary which should list key value pairs to be passed to the command execution.
     *               These values would correspond to instance specific parameters that a command may need to execute an action.</td></tr>
     *      <tr><td>FileParameters</td><td>Optional</td><td>A JSON dictionary where keys are filenames and values are contents of files.
     *               The SSH Server will utilize this feature to generate files with keys as filenames and values as content.
     *               This attribute can be used to generate files that a command may require as part of execution.</td></tr>
     *  </tbody>
     * </table>
     * Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param ctx Reference to context memory
     */
    public void execCommandWithPty(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        execSshCommand(params, ctx, true);
    }
}
