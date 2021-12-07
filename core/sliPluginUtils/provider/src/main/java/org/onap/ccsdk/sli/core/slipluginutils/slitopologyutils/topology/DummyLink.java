package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

public class DummyLink implements Link {

    private final Type type = Type.DUMMY;

    @Override
    public PInterface src() {
        return null;
    }

    @Override
    public PInterface dst() {
        return null;
    }

    @Override
    public boolean isInnerDomain() {
        return false;
    }

    @Override
    public Type type() {
        return type;
    }
}
