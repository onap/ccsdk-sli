package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

import java.util.Objects;

public class OtnLink implements Link{

    private final Type type = Type.OTN;
    private final PInterface src;
    private final PInterface dst;


    private final String linkName;

    public OtnLink(String linkName, PInterface src, PInterface dst){
        this.linkName = linkName;
        this.src = src;
        this.dst = dst;
    }

    public PInterface src() {
        return src;
    }

    public PInterface dst() {
        return dst;
    }

    public String linkName() {
        return linkName;
    }

    @Override
    public boolean isInnerDomain(){
        if (src != null && dst != null){
            if (src.pInterfaceName() != null && dst.pInterfaceName() != null){
                if (src.pInterfaceName().getNetworkId() != null
                    && dst.pInterfaceName().getNetworkId() != null) {
                    return src.pInterfaceName().getNetworkId().equals(dst.pInterfaceName().getNetworkId());
                }
            }
        }
        return false;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }
        if (o instanceof OtnLink){
            final OtnLink other = (OtnLink) o;
            return  Objects.equals(this.linkName, other.linkName);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(linkName, type, src, dst);
    }
}
