package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.Vertex;

import java.util.Objects;

public class Pnf implements Vertex {

    private final String pnfName;
    public Pnf(String pnfName){
        this.pnfName = pnfName;
    }

    @Override
    public int hashCode(){
        return pnfName.hashCode();
    }

    @Override
    public String toString() {
        return pnfName;
    }

    @Override
    public boolean equals (Object o){
        if (this == o) {
            return true;
        }
        if (o instanceof Pnf) {
            final Pnf other = (Pnf) o;
            return Objects.equals(this.pnfName, other.pnfName);
        }
        return false;
    }
}

