package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class PInterfaceName {

    private final String name;
    private final String networkId;
    private final String pnfId;
    private final String ltpId;
    private final boolean parsable;
    private final static String pattern = "(.*)-nodeId-([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})-ltpId-(\\d+)";
    private final static Pattern regex = Pattern.compile(pattern);

    public PInterfaceName (String pInterfaceName){
        this.name = pInterfaceName;
        this.networkId = null;
        this.pnfId = null;
        this.ltpId = null;
        this.parsable = false;
    }

    public PInterfaceName(String pInterfaceName, String networkId, String pnfId, String ltpId){
        this.name = pInterfaceName;
        this.networkId = networkId;
        this.pnfId = pnfId;
        this.ltpId = ltpId;
        this.parsable = true;
    }

    public static PInterfaceName of(String pInterfaceName){
        String name = checkNotNull(pInterfaceName);
        Matcher m = regex.matcher(name);
        if (m.find()) {
            checkNotNull(m.group(1));
            checkNotNull(m.group(2));
            checkNotNull(m.group(3));
            return new PInterfaceName(name, m.group(1), m.group(2), m.group(3));
        } else {
            return new PInterfaceName(name);
        }
    }

    public boolean isParsable() {
        return parsable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PInterfaceName that = (PInterfaceName) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getPnfId() {
        return pnfId;
    }

    public String getLtpId() {
        return ltpId;
    }

    public String getName() {
        return name;
    }


}
