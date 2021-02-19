package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

/**
 * Abstraction of a graph edge weight function.
 */
public interface EdgeWeigher<V extends Vertex, E extends Edge<V>> {

    /**
     * Returns the weight of the given edge.
     *
     * @param edge edge to be weighed
     * @return edge weight
     */
    Weight weight(E edge);

    /**
     * Returns initial weight value (i.e. weight of a "path" starting and
     * terminating in the same vertex; typically 0 value is used).
     *
     * @return null path weight
     */
    Weight getInitialWeight();

    /**
     * Returns weight of a link/path that should be skipped
     * (can be considered as an infinite weight).
     *
     * @return non viable weight
     */
    Weight getNonViableWeight();
}
