package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Immutable directionless graph implementation using adjacency lists.
 * @param <V>
 * @param <E>
 */
public class Graph<V extends Vertex, E extends Edge<V>> {

    private Set<V> vertexes = new HashSet<>();
    private Set<E> edges = new HashSet<>();

    private SetMultimap<V, E> sources = HashMultimap.create();
    private SetMultimap<V, E> destinations = HashMultimap.create();

    /**
     * Creates a graph comprising of the specified vertexes and edges.
     *
     * @param vertex set of graph vertexes
     * @param edge   set of graph edges
     */
    public Graph(Set<V> vertex, Set<E> edge) {
        vertexes.addAll(vertex);
        edges.addAll(edge);
        for (E e : edge) {
            sources.put(e.src(), e);
            vertexes.add(e.src());
            destinations.put(e.dst(), e);
            vertexes.add(e.dst());
        }
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

    public void addVertex(V vertex) {
        vertexes.add(vertex);
    }

    public void removeVertex(V vertex) {
        if (vertexes.remove(vertex)) {
            Set<E> srcEdgesList = sources.get(vertex);
            Set<E> dstEdgesList = destinations.get(vertex);
            edges.removeAll(srcEdgesList);
            edges.removeAll(dstEdgesList);
            sources.remove(vertex, srcEdgesList);
            sources.remove(vertex, dstEdgesList);
        }
    }

    public void addEdge(E edge) {
        if (edges.add(edge)) {
            sources.put(edge.src(), edge);
            destinations.put(edge.dst(), edge);
        }
    }

    public void removeEdge(E edge) {
        if (edges.remove(edge)) {
            sources.remove(edge.src(), edge);
            destinations.remove(edge.dst(), edge);
        }
    }

    /**
     * Clear the graph.
     */
    public void clear() {
        edges.clear();
        vertexes.clear();
        sources.clear();
        destinations.clear();
    }
}
