package org.onap.ccsdk.sli.adaptors.aai.data;

import java.util.HashMap;
import java.util.Map;

import org.onap.aai.inventory.v25.CloudRegion;
import org.onap.aai.inventory.v25.Configuration;
import org.onap.aai.inventory.v25.InstanceGroup;
import org.onap.aai.inventory.v25.L3InterfaceIpv4AddressList;
import org.onap.aai.inventory.v25.L3InterfaceIpv6AddressList;
import org.onap.aai.inventory.v25.LInterface;
import org.onap.aai.inventory.v25.LagInterface;
import org.onap.aai.inventory.v25.LogicalLink;
import org.onap.aai.inventory.v25.PInterface;
import org.onap.aai.inventory.v25.Pnf;
import org.onap.aai.inventory.v25.ServiceInstance;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"cloud-region",
	"configuration",
	"instance-group",
	"l-interface",
	"lag-interface",
	"logical-link",
	"p-interface",
	"pnf",
	"l3-interface-ipv6-address-list",
	"l3-interface-ipv6-address-list",
	"service-instance"
})
public class RelatedNode implements AAIDatum {
	@JsonProperty("cloud-region")
	private CloudRegion cloudRegion;
	@JsonProperty("configuration")
	private Configuration configuration;
	@JsonProperty("instance-group")
	private InstanceGroup instanceGroup;
	@JsonProperty("l-interface")
	protected LInterface lInterface;
	@JsonProperty("logical-link")
	protected LogicalLink logicalLink;
	@JsonProperty("lag-interface")
	protected LagInterface lagInterface;
	@JsonProperty("l3-interface-ipv4-address-list")
    protected L3InterfaceIpv4AddressList l3InterfaceIpv4AddressList;
	@JsonProperty("l3-interface-ipv6-address-list")
    protected L3InterfaceIpv6AddressList l3InterfaceIpv6AddressList;
	@JsonProperty("p-interface")
	protected PInterface pInterface;
	@JsonProperty("pnf")
	protected Pnf pnf;
	@JsonProperty("service-instance")
	private ServiceInstance serviceInstance;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("service-instance")
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	@JsonProperty("service-instance")
	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	@JsonProperty("cloud-region")
	public CloudRegion getCloudRegion() {
		return cloudRegion;
	}

	@JsonProperty("cloud-region")
	public void setCloudRegion(CloudRegion cloudRegion) {
		this.cloudRegion = cloudRegion;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}
	@JsonProperty("configuration")
	public Configuration getConfiguration() {
		return configuration;
	}
	@JsonProperty("configuration")
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	@JsonProperty("l-interface")
	public LInterface getlInterface() {
		return lInterface;
	}
	@JsonProperty("l-interface")
	public void setlInterface(LInterface lInterface) {
		this.lInterface = lInterface;
	}
	@JsonProperty("pnf")
	public Pnf getPnf() {
		return pnf;
	}
	@JsonProperty("pnf")
	public void setPnf(Pnf pnf) {
		this.pnf = pnf;
	}
	@JsonProperty("l3-interface-ipv4-address-list")
	public L3InterfaceIpv4AddressList getL3InterfaceIpv4AddressList() {
		return l3InterfaceIpv4AddressList;
	}
	@JsonProperty("l3-interface-ipv4-address-list")
	public void setL3InterfaceIpv4AddressList(L3InterfaceIpv4AddressList l3InterfaceIpv4AddressList) {
		this.l3InterfaceIpv4AddressList = l3InterfaceIpv4AddressList;
	}
	@JsonProperty("l3-interface-ipv6-address-list")
	public L3InterfaceIpv6AddressList getL3InterfaceIpv6AddressList() {
		return l3InterfaceIpv6AddressList;
	}
	@JsonProperty("l3-interface-ipv6-address-list")
	public void setL3InterfaceIpv6AddressList(L3InterfaceIpv6AddressList l3InterfaceIpv6AddressList) {
		this.l3InterfaceIpv6AddressList = l3InterfaceIpv6AddressList;
	}
	@JsonProperty("instance-group")
	public InstanceGroup getInstanceGroup() {
		return instanceGroup;
	}
	@JsonProperty("instance-group")
	public void setInstanceGroup(InstanceGroup instanceGroup) {
		this.instanceGroup = instanceGroup;
	}
}
