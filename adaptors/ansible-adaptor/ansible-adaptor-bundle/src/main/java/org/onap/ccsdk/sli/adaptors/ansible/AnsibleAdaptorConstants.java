package org.onap.ccsdk.sli.adaptors.ansible;

public class AnsibleAdaptorConstants {

    public static final String ID = "Id";
    public static final String USER = "User";
    public static final String PSWD = "Password";
    public static final String SERVERIP = "ServerIP";
    public static final String AGENT_URL = "AgentUrl";
    public static final String NODE_LIST = "NodeList";
    public static final String ANSIBLE_SERVER = "AnsibleServer";

    public static final String APPC_PROPS = "/appc.properties";
    public static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    public static final String PROPDIR = System.getenv(SDNC_CONFIG_DIR);

    public static final String ACTION = "Action";
    public static final String OUTPUT = "Output";
    public static final String TIMEOUT = "Timeout";
    public static final String VERSION = "Version";

    public static final String FAILURE = "failure";
    public static final String SUCCESS = "success";
    public static final String STATUS_CODE = "StatusCode";
    public static final String STATUS_MESSAGE = "StatusMessage";

    public static final String EXTRA_VARS = "ExtraVars";
    public static final String PLAYBOOK_NAME = "PlaybookName";
    public static final String AUTO_NODE_LIST = "AutoNodeList";
    public static final String ENV_PARAMETERS = "EnvParameters";
    public static final String FILE_PARAMETERS = "FileParameters";
    public static final String INVENTORY_NAMES = "InventoryNames";
    public static final String LOCAL_PARAMETERS = "LocalParameters";

    public static final String ID_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.Id";
    public static final String LOG_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.log";
    public static final String OUTPUT_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.output";
    public static final String TIMEOUT_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.timeout";
    public static final String MESSAGE_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.message";
    public static final String RESULTS_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.results";
    public static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adaptor.ansible.result.code";

    public static final String TRUSTSTORE_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.trustStore";
    public static final String CLIENT_TYPE_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.clientType";
    public static final String POLL_INTERVAL_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.pollInterval";
    public static final String SOCKET_TIMEOUT_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.socketTimeout";
    public static final String TRUSTSTORE_PASS_PROPERTY_NAME = "org.onap.appc.adaptor.ansible.trustStore.trustPasswd";


}
