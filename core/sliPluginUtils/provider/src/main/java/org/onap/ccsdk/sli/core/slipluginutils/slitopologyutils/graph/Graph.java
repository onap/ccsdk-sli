package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Immutable directionless graph implementation using adjacency lists.
 * @param <V>
 * @param <E>
 */
public class Graph<V extends Vertex, E extends Edge<V>>
{

    private final Set<V> vertexes;
    private final Set<E> edges;

    private final ImmutableSetMultimap<V, E> sources;
    private final ImmutableSetMultimap<V, E> destinations;

    /**
     * Creates a graph comprising of the specified vertexes and edges.
     *
     * @param vertexes set of graph vertexes
     * @param edges    set of graph edges
     */
    public Graph(Set<V> vertexes, Set<E> edges) {
        checkNotNull(vertexes, "Vertex set cannot be null");
        checkNotNull(edges, "Edge set cannot be null");

        // Record ingress/egress edges for each vertex.
        ImmutableSetMultimap.Builder<V, E> srcMap = ImmutableSetMultimap.builder();
        ImmutableSetMultimap.Builder<V, E> dstMap = ImmutableSetMultimap.builder();

        // Also make sure that all edge end-points are added as vertexes
        ImmutableSet.Builder<V> actualVertexes = ImmutableSet.builder();
        actualVertexes.addAll(vertexes);

        for (E edge : edges) {
            srcMap.put(edge.src(), edge);
            actualVertexes.add(edge.src());
            dstMap.put(edge.dst(), edge);
            actualVertexes.add(edge.dst());
        }

        // Make an immutable copy of the edge and vertex sets
        this.edges = ImmutableSet.copyOf(edges);
        this.vertexes = actualVertexes.build();

        // Build immutable copies of sources and destinations edge maps
        sources = srcMap.build();
        destinations = dstMap.build();
    }


    public Set<V> getVertexes() {
        return vertexes;
    }


    public Set<E> getEdges() {
        return edges;
    }


    public Set<E> getEdgesFrom(V src) {
        return sources.get(src);
    }


    public Set<E> getEdgesTo(V dst) {
        return destinations.get(dst);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Graph) {
            Graph that = (Graph) obj;
            return this.getClass() == that.getClass() &&
                    Objects.equals(this.vertexes, that.vertexes) &&
                    Objects.equals(this.edges, that.edges);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexes, edges);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("vertexes", vertexes)
                .add("edges", edges)
                .toString();
    }
}
