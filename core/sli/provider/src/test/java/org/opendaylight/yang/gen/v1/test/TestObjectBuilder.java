package org.opendaylight.yang.gen.v1.test;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yangtools.concepts.Builder;

public class TestObjectBuilder implements Builder<TestObject> {
	private List<IpAddress> _floatingIp;
	private List<Ipv4Address> _floatingIpV4;
	private List<Ipv6Address> _floatingIpV6;
	private IpAddress _singleIp;
	private Ipv4Address _singleIpV4;
	private Ipv6Address _singleIpV6;

	public List<IpAddress> getFloatingIp() {
		return _floatingIp;
	}
	
	public List<Ipv4Address> getFloatingIpV4() {
		return _floatingIpV4;
	}

	public List<Ipv6Address> getFloatingIpV6() {
		return _floatingIpV6;
	}

	public Ipv4Address getSingleIpV4() {
		return _singleIpV4;
	}

	public Ipv6Address getSingleIpV6() {
		return _singleIpV6;
	}

	public IpAddress getSingleIp() {
		return _singleIp;
	}

	public TestObjectBuilder setFloatingIp(final List<IpAddress> value) {
		this._floatingIp = value;
		return this;
	}
	
	public TestObjectBuilder setFloatingIpV4(final List<Ipv4Address> value) {
		this._floatingIpV4 = value;
		return this;
	}

	public TestObjectBuilder setFloatingIpV6(final List<Ipv6Address> value) {
		this._floatingIpV6 = value;
		return this;
	}
	
	public TestObjectBuilder setSingleIp(final IpAddress value) {
		this._singleIp = value;
		return this;
	}
	
	public TestObjectBuilder setSingleIpV4(final Ipv4Address value) {
		this._singleIpV4 = value;
		return this;
	}

	public TestObjectBuilder setSingleIpV6(final Ipv6Address value) {
		this._singleIpV6 = value;
		return this;
	}

	public TestObjectBuilder() {

	}

	public TestObject build() {
		return new TestObjectImpl(this);
	}
	
	@Override
	public String toString() {
		return "TestObjectBuilder [_floatingIp=" + _floatingIp + ", _floatingIpV4=" + _floatingIpV4 + ", _floatingIpV6="
				+ _floatingIpV6 + ", _singleIp=" + _singleIp + ", _singleIpV4=" + _singleIpV4 + ", _singleIpV6="
				+ _singleIpV6 + "]";
	}

	private static final class TestObjectImpl implements TestObject {
		private List<IpAddress> _floatingIp;
		private List<Ipv4Address> _floatingIpV4;
		private List<Ipv6Address> _floatingIpV6;
		private IpAddress _singleIp;
		private Ipv4Address _singleIpV4;
		private Ipv6Address _singleIpV6;

		@Override
		public List<IpAddress> getFloatingIp() {
			return _floatingIp;
		}
		
		@Override
		public List<Ipv4Address> getFloatingIpV4() {
			return _floatingIpV4;
		}

		@Override
		public List<Ipv6Address> getFloatingIpV6() {
			return _floatingIpV6;
		}

		@Override
		public Ipv4Address getSingleIpV4() {
			return _singleIpV4;
		}

		@Override
		public Ipv6Address getSingleIpV6() {
			return _singleIpV6;
		}

		@Override
		public IpAddress getSingleIp() {
			return _singleIp;
		}

		private TestObjectImpl(TestObjectBuilder base) {
			this._floatingIp = base.getFloatingIp();
			this._floatingIpV4 = base.getFloatingIpV4();
			this._floatingIpV6 = base.getFloatingIpV6();
			this._singleIp = base.getSingleIp();
			this._singleIpV4 = base.getSingleIpV4();
			this._singleIpV6 = base.getSingleIpV6();
		}
	}
}
