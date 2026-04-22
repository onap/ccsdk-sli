package org.onap.ccsdk.sli.northbound.uebclient;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

public class TestSdncUebCallback {


	private static final String CRTBL_SERVICE_MODEL = "CREATE TABLE SERVICE_MODEL (" +
			"  service_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  name varchar(255) DEFAULT NULL," +
			"  description varchar(1024) DEFAULT NULL," +
			"  type varchar(255) DEFAULT NULL," +
			"  category varchar(255) DEFAULT NULL," +
			"  ecomp_naming CHAR(1) DEFAULT NULL," +
			"  service_instance_name_prefix varchar(255) DEFAULT NULL," +
			"  filename varchar(100) DEFAULT NULL," +
			"  naming_policy varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (service_uuid)" +
			")";
			
	private static final String CRTBL_SERVICE_MODEL_TO_VF_MODEL_MAPPING = "CREATE TABLE SERVICE_MODEL_TO_VF_MODEL_MAPPING (" +
			"  service_uuid varchar(255) NOT NULL," +
			"  vf_uuid varchar(255) DEFAULT NULL," +
			"  vf_customization_uuid varchar(255) DEFAULT NULL," +
			"  service_invariant_uuid varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (service_uuid)" +
			")";

	private static final String CRTBL_ATTRIBUTE_VALUE_PAIR = "CREATE TABLE ATTRIBUTE_VALUE_PAIR (" +
			"  resource_uuid varchar(255) NOT NULL," +
			"  attribute_name varchar(255) NOT NULL," +
			"  resource_type varchar(255) NOT NULL," +
			"  attribute_value varchar(255) DEFAULT NULL," +
			"  resource_customization_uuid varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (resource_uuid,attribute_name,resource_type)" +
			")";

	private static final String CRTBL_NETWORK_MODEL = "CREATE TABLE NETWORK_MODEL (" +
			"  customization_uuid varchar(255) NOT NULL," +
			"  service_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  uuid varchar(255) DEFAULT NULL," +
			"  network_type varchar(255) DEFAULT NULL," +
			"  network_role varchar(255) DEFAULT NULL," +
			"  network_technology varchar(255) DEFAULT NULL," +
			"  network_scope varchar(255) DEFAULT NULL," +
			"  naming_policy varchar(255) DEFAULT NULL," +
			"  ecomp_generated_naming CHAR(1) DEFAULT NULL," +
			"  is_shared_network CHAR(1) DEFAULT NULL," +
			"  is_external_network CHAR(1) DEFAULT NULL," +
			"  is_provider_network CHAR(1) DEFAULT NULL," +
			"  physical_network_name varchar(255) DEFAULT NULL," +
			"  is_bound_to_vpn CHAR(1) DEFAULT NULL," +
			"  vpn_binding varchar(255) DEFAULT NULL," +
			"  use_ipv4 CHAR(1) DEFAULT NULL," +
			"  ipv4_dhcp_enabled CHAR(1) DEFAULT NULL," +
			"  ipv4_ip_version CHAR(1) DEFAULT NULL," +
			"  ipv4_cidr_mask varchar(255) DEFAULT NULL," +
			"  eipam_v4_address_plan varchar(255) DEFAULT NULL," +
			"  use_ipv6 CHAR(1) DEFAULT NULL," +
			"  ipv6_dhcp_enabled CHAR(1) DEFAULT NULL," +
			"  ipv6_ip_version CHAR(1) DEFAULT NULL," +
			"  ipv6_cidr_mask varchar(255) DEFAULT NULL," +
			"  eipam_v6_address_plan varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (customization_uuid)" +
			")";

	private static final String CRTBL_ALLOTTED_RESOURCE_MODEL = "CREATE TABLE ALLOTTED_RESOURCE_MODEL (" +
			"  customization_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  uuid varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  naming_policy varchar(255) DEFAULT NULL," +
			"  ecomp_generated_naming CHAR(1) DEFAULT NULL," +
			"  depending_service varchar(255) DEFAULT NULL," +
			"  role varchar(255) DEFAULT NULL," +
			"  type varchar(255) DEFAULT NULL," +
			"  service_dependency varchar(255) DEFAULT NULL," +
			"  allotted_resource_type varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (customization_uuid)" +
			")";

	private static final String CRTBL_VFC_MODEL = "CREATE TABLE VFC_MODEL (" +
			"  customization_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  uuid varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  naming_policy varchar(255) DEFAULT NULL," +
			"  ecomp_generated_naming CHAR(1) DEFAULT NULL," +
			"  nfc_function varchar(255) DEFAULT NULL," +
			"  nfc_naming_code varchar(255) DEFAULT NULL," +
			"  vm_type varchar(255) DEFAULT NULL," +
			"  vm_type_tag varchar(255) DEFAULT NULL," +
			"  vm_image_name varchar(255) DEFAULT NULL," +
			"  vm_flavor_name varchar(255) DEFAULT NULL," +
			"  high_availability varchar(255) DEFAULT NULL," +
			"  nfc_naming varchar(255) DEFAULT NULL," +
			"  min_instances INTEGER DEFAULT NULL," +
			"  max_instances INTEGER DEFAULT NULL," +
			"  PRIMARY KEY (customization_uuid)" +
			")";

	private static final String CRTBL_VFC_RELATED_NETWORK_ROLE = "CREATE TABLE VFC_RELATED_NETWORK_ROLE (" +
			"  vfc_customization_uuid varchar(255) NOT NULL," +
			"  vm_type varchar(255) NOT NULL," +
			"  network_role varchar(255) NOT NULL," +
			"  related_network_role varchar(255) NOT NULL," +
			"  PRIMARY KEY (vfc_customization_uuid,vm_type,network_role,related_network_role)" +
			")";

	private static final String CRTBL_VFC_TO_NETWORK_ROLE_MAPPING = "CREATE TABLE VFC_TO_NETWORK_ROLE_MAPPING (" +
			"  seq INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY," +
			"  vfc_customization_uuid varchar(255) NOT NULL," +
			"  network_role varchar(255) NOT NULL," +
			"  vm_type varchar(255) DEFAULT NULL," +
			"  network_role_tag varchar(255) DEFAULT NULL," +
			"  ipv4_count INTEGER NOT NULL," +
			"  ipv6_count INTEGER NOT NULL," +
			"  ipv4_use_dhcp CHAR(1) DEFAULT NULL," +
			"  ipv6_use_dhcp CHAR(1) DEFAULT NULL," +
			"  ipv4_ip_version CHAR(1) DEFAULT NULL," +
			"  ipv6_ip_version CHAR(1) DEFAULT NULL," +
			"  extcp_subnetpool_id varchar(512) DEFAULT NULL," +
			"  ipv4_floating_count INTEGER DEFAULT NULL," +
			"  ipv6_floating_count INTEGER DEFAULT NULL," +
			"  ipv4_address_plan_name varchar(512) DEFAULT NULL," +
			"  ipv6_address_plan_name varchar(512) DEFAULT NULL," +
			"  ipv4_vrf_name varchar(512) DEFAULT NULL," +
			"  ipv6_vrf_name varchar(512) DEFAULT NULL," +
			"  subnet_role varchar(255) DEFAULT NULL," +
			"  subinterface_indicator CHAR(1) DEFAULT NULL," +
			"  PRIMARY KEY (seq)" +
			")";

	private static final String CRTBL_VF_MODEL = "CREATE TABLE VF_MODEL (" +
			"  customization_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  uuid varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  name varchar(255) DEFAULT NULL," +
			"  naming_policy varchar(255) DEFAULT NULL," +
			"  ecomp_generated_naming CHAR(1) DEFAULT NULL," +
			"  avail_zone_max_count INTEGER DEFAULT NULL," +
			"  nf_function varchar(255) DEFAULT NULL," +
			"  nf_code varchar(255) DEFAULT NULL," +
			"  nf_type varchar(255) DEFAULT NULL," +
			"  nf_role varchar(255) DEFAULT NULL," +
			"  vendor varchar(255) DEFAULT NULL," +
			"  vendor_version varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (customization_uuid)" +
			")";

	private static final String CRTBL_VNF_RELATED_NETWORK_ROLE = "CREATE TABLE VNF_RELATED_NETWORK_ROLE (" +
			"  vnf_customization_uuid varchar(255) NOT NULL," +
			"  network_role varchar(255) NOT NULL," +
			"  related_network_role varchar(255) NOT NULL," +
			"  PRIMARY KEY (vnf_customization_uuid,network_role,related_network_role)" +
			")";

	private static final String CRTBL_VF_TO_NETWORK_ROLE_MAPPING = "CREATE TABLE VF_TO_NETWORK_ROLE_MAPPING (" +
			"  seq INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY," +
			"  vf_customization_uuid varchar(255) NOT NULL," +
			"  network_role varchar(255) NOT NULL," +
			"  PRIMARY KEY (seq)" +
			")";

	private static final String CRTBL_VF_MODULE_MODEL = "CREATE TABLE VF_MODULE_MODEL (" +
			"  customization_uuid varchar(255) NOT NULL," +
			"  model_yaml BLOB," +
			"  invariant_uuid varchar(255) DEFAULT NULL," +
			"  uuid varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  vf_module_type varchar(255) DEFAULT NULL," +
			"  availability_zone_count INTEGER DEFAULT NULL," +
			"  ecomp_generated_vm_assignments CHAR(1) DEFAULT NULL," +
			"  vf_customization_uuid varchar(255) DEFAULT NULL," +
			"  vf_module_label varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (customization_uuid)" +
			")";
	
	private static final String CRTBL_VF_MODULE_TO_VFC_MAPPING = "CREATE TABLE VF_MODULE_TO_VFC_MAPPING (" +
			"  seq INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY," +
			"  vf_module_customization_uuid varchar(255) NOT NULL," +
			"  vfc_customization_uuid varchar(255) NOT NULL," +
			"  vm_type varchar(255) NOT NULL," +
			"  vm_count INTEGER NOT NULL," +
			"  PRIMARY KEY (seq)" +
			")";
		
	private static final String CRTBL_RESOURCE_GROUP = "CREATE TABLE RESOURCE_GROUP (" +
			"  resource_uuid varchar(255) NOT NULL," +
			"  group_uuid varchar(255) NOT NULL," +
			"  group_customization_uuid varchar(255) DEFAULT NULL," +
			"  group_invariant_uuid varchar(255) DEFAULT NULL," +
			"  group_name varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  group_type varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (resource_uuid,group_uuid)" +
			")";
		
	private static final String CRTBL_RESOURCE_GROUP_TO_TARGET_NODE_MAPPING = "CREATE TABLE RESOURCE_GROUP_TO_TARGET_NODE_MAPPING (" +
			"  group_uuid varchar(255) NOT NULL," +
			"  parent_uuid varchar(255) NOT NULL," +
			"  target_node_uuid varchar(255) NOT NULL," +
			"  target_type varchar(255) DEFAULT NULL," +
			"  table_name varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (group_uuid,parent_uuid,target_node_uuid)" +
			")";

	private static final String CRTBL_RESOURCE_POLICY = "CREATE TABLE RESOURCE_POLICY (" +
			"  resource_uuid varchar(255) NOT NULL," +
			"  policy_uuid varchar(255) NOT NULL," +
			"  policy_invariant_uuid varchar(255) NOT NULL," +
			"  policy_name varchar(255) DEFAULT NULL," +
			"  version varchar(255) DEFAULT NULL," +
			"  policy_type varchar(255) DEFAULT NULL," +
			"  property_type varchar(255) DEFAULT NULL," +
			"  property_source varchar(255) DEFAULT NULL," +
			"  property_name varchar(255) DEFAULT NULL," +
			"  policy_customization_uuid varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (resource_uuid,policy_uuid)" +
			")";

	private static final String CRTBL_RESOURCE_POLICY_TO_TARGET_NODE_MAPPING = "CREATE TABLE RESOURCE_POLICY_TO_TARGET_NODE_MAPPING (" +
			"  policy_uuid varchar(255) NOT NULL," +
			"  parent_uuid varchar(255) NOT NULL," +
			"  target_node_uuid varchar(255) NOT NULL," +
			"  target_type varchar(255) DEFAULT NULL," +
			"  target_node_customization_uuid varchar(255) DEFAULT NULL," +
			"  policy_customization_uuid varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (policy_uuid,parent_uuid,target_node_uuid)" +
			")";
		
	private static final String CRTBL_NODE_CAPABILITY = "CREATE TABLE NODE_CAPABILITY (" +
			"  capability_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY," +
			"  capability_provider_uuid varchar(255) NOT NULL," +
			"  capability_provider_customization_uuid varchar(255) NOT NULL," +
			"  capability_name varchar(255) DEFAULT NULL," +
			"  capability_type varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (capability_id)" +
			")";
		
	private static final String CRTBL_NODE_CAPABILITY_PROPERTY = "CREATE TABLE NODE_CAPABILITY_PROPERTY (" +
			"  capability_id INTEGER NOT NULL," +
			"  capability_property_name varchar(255) NOT NULL," +
			"  capability_property_type varchar(255) DEFAULT NULL," +
			"  PRIMARY KEY (capability_id,capability_property_name)," +
			"  CONSTRAINT NODE_CAPABILITY_PROPERTY_TO_NODE_CAPABILITY FOREIGN KEY (capability_id) REFERENCES NODE_CAPABILITY (capability_id) ON DELETE CASCADE" +
			")";
					

	 private static final Logger LOG = LoggerFactory
	            .getLogger(TestSdncUebCallback.class);
	private static int dbCounter = 0;
	private String testDbName;
	SdncUebConfiguration config;
	DBResourceManager dblibSvc;
	List<IArtifactInfo > processLevelArtifactList;
	List<IArtifactInfo > serviceLevelArtifactList;
	ArrayList<IResourceInstance> resourceList;
	IArtifactInfo mockProcessArtifact1;
	IArtifactInfo mockProcessArtifact2;
	IArtifactInfo mockProcessArtifact3;
	IArtifactInfo mockServiceArtifact1;
	IResourceInstance resource;
	

	@Before
	public void setUp() throws Exception {
		config = new SdncUebConfiguration("src/test/resources");


		URL propUrl = getClass().getResource("/dblib.properties");

		InputStream propStr = getClass().getResourceAsStream("/dblib.properties");

		Properties props = new Properties();

		props.load(propStr);


		// Use Derby embedded database
		testDbName = "uebtest" + (++dbCounter);
		props.setProperty("org.onap.ccsdk.sli.jdbc.database", testDbName);
		props.setProperty("org.onap.ccsdk.sli.jdbc.url", "jdbc:derby:memory:" + testDbName + ";create=true");
		props.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.apache.derby.iapi.jdbc.AutoloadedDriver");
		props.setProperty("org.onap.ccsdk.sli.jdbc.user", "sa");
		props.setProperty("org.onap.ccsdk.sli.jdbc.password", "");



		// Create dblib connection
		dblibSvc = new DBResourceManager(props);

		// Create TOSCA tables
		dblibSvc.writeData(CRTBL_SERVICE_MODEL, null, null);
		dblibSvc.writeData(CRTBL_SERVICE_MODEL_TO_VF_MODEL_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_ATTRIBUTE_VALUE_PAIR, null, null);
		dblibSvc.writeData(CRTBL_NETWORK_MODEL, null, null);
		dblibSvc.writeData(CRTBL_VFC_MODEL, null, null);
		dblibSvc.writeData(CRTBL_VFC_RELATED_NETWORK_ROLE, null, null);
		dblibSvc.writeData(CRTBL_VFC_TO_NETWORK_ROLE_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_VF_MODEL, null, null);
		dblibSvc.writeData(CRTBL_VNF_RELATED_NETWORK_ROLE, null, null);
		dblibSvc.writeData(CRTBL_VF_TO_NETWORK_ROLE_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_VF_MODULE_MODEL, null, null);
		dblibSvc.writeData(CRTBL_VF_MODULE_TO_VFC_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_ALLOTTED_RESOURCE_MODEL, null, null);
		dblibSvc.writeData(CRTBL_RESOURCE_GROUP, null, null);
		dblibSvc.writeData(CRTBL_RESOURCE_GROUP_TO_TARGET_NODE_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_RESOURCE_POLICY, null, null);
		dblibSvc.writeData(CRTBL_RESOURCE_POLICY_TO_TARGET_NODE_MAPPING, null, null);
		dblibSvc.writeData(CRTBL_NODE_CAPABILITY, null, null);
		dblibSvc.writeData(CRTBL_NODE_CAPABILITY_PROPERTY, null, null);
		
		processLevelArtifactList = new ArrayList<>();
		serviceLevelArtifactList = new ArrayList<>();
		resourceList = new ArrayList<>();

		
		mockProcessArtifact1 = mock(IArtifactInfo.class);
		when(mockProcessArtifact1.getArtifactName()).thenReturn("mockProcessArtifact1");
		when(mockProcessArtifact1.getArtifactType()).thenReturn("HEAT");
		when(mockProcessArtifact1.getArtifactURL()).thenReturn("https://asdc.sdc.com/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
		when(mockProcessArtifact1.getArtifactChecksum()).thenReturn("123tfg123 1234ftg");
		when(mockProcessArtifact1.getArtifactTimeout()).thenReturn(110);
		
		mockProcessArtifact2 = mock(IArtifactInfo.class);
		when(mockProcessArtifact2.getArtifactName()).thenReturn("mockProcessArtifact2");
		when(mockProcessArtifact2.getArtifactType()).thenReturn("DG_XML");
		when(mockProcessArtifact2.getArtifactURL()).thenReturn("https://asdc.sdc.com/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
		when(mockProcessArtifact2.getArtifactChecksum()).thenReturn("456jhgt 1234ftg");
		when(mockProcessArtifact2.getArtifactTimeout()).thenReturn(110);
		
		mockProcessArtifact3 = mock(IArtifactInfo.class);
		when(mockProcessArtifact3.getArtifactName()).thenReturn("mockProcessArtifact3");
		when(mockProcessArtifact3.getArtifactType()).thenReturn("HEAT");
		when(mockProcessArtifact3.getArtifactURL()).thenReturn("https://asdc.sdc.com/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
		when(mockProcessArtifact3.getArtifactChecksum()).thenReturn("123tfg123 543gtd");
		when(mockProcessArtifact3.getArtifactTimeout()).thenReturn(110);
		
		
		mockServiceArtifact1 = mock(IArtifactInfo.class);
		when(mockServiceArtifact1.getArtifactName()).thenReturn("mockProcessArtifact4");
		when(mockServiceArtifact1.getArtifactType()).thenReturn("HEAT");
		when(mockServiceArtifact1.getArtifactURL()).thenReturn("https://asdc.sdc.com/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
		when(mockServiceArtifact1.getArtifactChecksum()).thenReturn("123t3455 543gtd");
		when(mockServiceArtifact1.getArtifactTimeout()).thenReturn(110);
		
		resource = mock(IResourceInstance.class);
	}

	@After
	public void tearDown() throws Exception {
		// Move anything in archive back to incoming
        String curFileName = "";

        Path incomingPath = new File(config.getIncomingDir()).toPath();
        File archiveDir = new File(config.getArchiveDir());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(archiveDir.toPath())) {
            for (Path file: stream) {
            		Files.move(file, incomingPath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            LOG.warn("Cannot replace spool file {}", curFileName, x);
        }

        try (java.sql.Connection c = DriverManager.getConnection("jdbc:derby:memory:" + testDbName + ";drop=true")) {
        } catch (SQLException e) {
            // Derby throws SQLState 08006 on successful drop, XJ004 if DB not found - both acceptable in teardown
        }

	}

	@Test
	public void test() {

		IDistributionClient iDistClient = mock(IDistributionClient.class);
		SdncUebCallback cb = new SdncUebCallback(iDistClient, config);
		SdncUebCallback.setJdbcDataSource(dblibSvc);

		INotificationData iData = mock(INotificationData.class);
		/*IArtifactInfo iArtifactInfo = mock(IArtifactInfo.class);
		when(iArtifactInfo.getArtifactName()).thenReturn("testArtifact1");
		when(iArtifactInfo.getArtifactType()).thenReturn("TOSCA_CSAR");
		List artifactInfoList = new ArrayList();
		artifactInfoList.add(iArtifactInfo);*/
		
		when(iData.getServiceName()).thenReturn("testServiceName");
		//when(iData.getServiceArtifacts()).thenReturn(artifactInfoList);
		cb.deployDownloadedFiles(null, null, null);
		cb.activateCallback(iData);

	}
	
	
	
	@Test
	public void testServiceAndProcessArtifactsactivateCallback() {

		try {
		processLevelArtifactList.add(mockProcessArtifact1);
		processLevelArtifactList.add(mockProcessArtifact2);
		processLevelArtifactList.add(mockProcessArtifact3);
		
		resourceList.add(resource);
		serviceLevelArtifactList.add(mockServiceArtifact1);
		when(resource.getArtifacts()).thenReturn(serviceLevelArtifactList);
		when(resource.getResourceName()).thenReturn("Resource_service_name");


		IDistributionClient iDistClient1 = mock(IDistributionClient.class);
		INotificationData mockData = mock(INotificationData.class);
		when(mockData.getResources()).thenReturn(resourceList);
		when(mockData.getServiceName()).thenReturn("Test_service_name");
		when(mockData.getServiceArtifacts()).thenReturn(processLevelArtifactList);
		
		/*IArtifactInfo iArtifactInfo = mock(IArtifactInfo.class);
		when(iArtifactInfo.getArtifactName()).thenReturn("testArtifact1");
		when(iArtifactInfo.getArtifactType()).thenReturn("TOSCA_CSAR");
		List artifactInfoList = new ArrayList();
		artifactInfoList.add(iArtifactInfo);
		
		//when(mockData.getServiceName()).thenReturn("testServiceName");
		when(mockData.getServiceArtifacts()).thenReturn(artifactInfoList);*/

		
		SdncUebCallback cb1 = new SdncUebCallback(iDistClient1, config);
		cb1.activateCallback(mockData);
		assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

}
