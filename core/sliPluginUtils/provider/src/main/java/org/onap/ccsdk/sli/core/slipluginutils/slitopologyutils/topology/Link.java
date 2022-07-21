package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

public interface Link {

    enum Type {
        OTN,
        ETH
    }

    PInterface src();

    PInterface dst();

    boolean isInnerDomain();
}
