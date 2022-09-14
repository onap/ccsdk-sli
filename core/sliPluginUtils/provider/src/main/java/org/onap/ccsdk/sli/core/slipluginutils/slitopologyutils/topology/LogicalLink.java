package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology;

import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.Edge;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class LogicalLink implements Edge<Pnf> {

    private final Pnf src;
    private final Pnf dst;
    private final Link link;

    public LogicalLink (Pnf src, Pnf dst, Link underlayLink) {
        this.src = src;
        this.dst = dst;
        this.link = underlayLink;
    }

    public Link underlayLink(){
        return this.link;
    }

    @Override
    public Pnf src() {
        return src;
    }

    @Override
    public Pnf dst() {
        return dst;
    }

    @Override
    public boolean isPermitted(Pnf src, Pnf dst) {
        String curSrcName = src().toString();
        String curDstName = dst().toString();
        return link.isInnerDomain()
                || !curSrcName.equals(src.toString()) && !curDstName.equals(dst.toString())
                && !curSrcName.equals(dst.toString()) && !curDstName.equals(src.toString());
    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LogicalLink) {
            final LogicalLink other = (LogicalLink) obj;
            return Objects.equals(this.link, other.link);
        }
        return false;
    }
}
