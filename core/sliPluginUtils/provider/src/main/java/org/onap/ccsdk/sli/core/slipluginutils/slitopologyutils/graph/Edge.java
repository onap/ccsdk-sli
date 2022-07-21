package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

public interface Edge<V extends Vertex> {

    /**
     * Returns the edge source vertex.
     *
     * @return source vertex
     */
    V src();

    /**
     * Returns the edge destination vertex.
     *
     * @return destination vertex
     */
    V dst();

    /**
     * Returns true if edge is permissable
     */
    default boolean isPermitted(V src, V dst){
        return true;
    }
}
