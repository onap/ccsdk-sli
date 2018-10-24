package org.opendaylight.yang.gen.v1.test;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;

public interface TestObject {
    List<IpAddress> getFloatingIp();
    List<Ipv4Address> getFloatingIpV4();
    List<Ipv6Address> getFloatingIpV6();
    Ipv4Address getSingleIpV4();
    Ipv6Address getSingleIpV6();
    IpAddress getSingleIp();

}
