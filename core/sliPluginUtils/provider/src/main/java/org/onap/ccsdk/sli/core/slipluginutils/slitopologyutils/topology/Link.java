package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

public interface Link {

    enum Type {
        OTN,
        ETH,
        DUMMY
    }

    PInterface src();

    PInterface dst();

    boolean isInnerDomain();

    Type type();
}
