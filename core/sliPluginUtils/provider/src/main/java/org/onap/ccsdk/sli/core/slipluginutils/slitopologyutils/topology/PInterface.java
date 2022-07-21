package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

import java.util.Objects;

public class PInterface {

    private final String pnfName;
    private final PInterfaceName pInterfaceName;

    public PInterface(String pnfName, PInterfaceName pInterfaceName){
        this.pnfName = pnfName;
        this.pInterfaceName = pInterfaceName;
    }

    public PInterfaceName pInterfaceName() {
        return pInterfaceName;
    }

    public String pnfName() {
        return pnfName;
    }

    @Override
    public int hashCode(){
        return Objects.hash(pnfName, pInterfaceName);
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o instanceof PInterface){
            final PInterface other = (PInterface) o;
            return Objects.equals(this.pnfName, other.pnfName) &&
                    Objects.equals(this.pInterfaceName, other.pInterfaceName);
        }
        return false;
    }
}
