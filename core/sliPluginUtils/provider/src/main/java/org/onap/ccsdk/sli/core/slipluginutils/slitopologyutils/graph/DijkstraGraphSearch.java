package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dijkstra shortest-path graph search algorithm capable of finding not just
 * one, but all shortest paths between the source and destinations.
 */
public class DijkstraGraphSearch<V extends Vertex, E extends Edge<V>> {

    static int ALL_PATHS = -1;
    /**
     * Default path search result that uses the DefaultPath to convey paths
     * in a graph.
     */
    public class Result {

        private final V src;
        private final V dst;
        protected final Set<Path<V, E>> paths = new HashSet<>();
        protected final Map<V, Weight> costs = new HashMap<>();
        protected final Map<V, Set<E>> parents = new HashMap<>();
        protected final int maxPaths;

        /**
         * Creates the result of a single-path search.
         *
         * @param src path source
         * @param dst optional path destination
         */
        public Result(V src, V dst) {
            this(src, dst, 1);
        }

        /**
         * Creates the result of path search.
         *
         * @param src      path source
         * @param dst      optional path destination
         * @param maxPaths optional limit of number of paths;
         *                 {@link #ALL_PATHS} if no limit
         */
        public Result(V src, V dst, int maxPaths) {
            checkNotNull(src, "Source cannot be null");
            this.src = src;
            this.dst = dst;
            this.maxPaths = maxPaths;
        }


        public V src() {
            return src;
        }


        public V dst() {
            return dst;
        }


        public Set<Path<V, E>> paths() {
            return paths;
        }

        public Map<V, Weight> costs() {
            return costs;
        }

        public Map<V, Set<E>> parents() {
            return parents;
        }

        /**
         * Indicates whether or not the given vertex has a cost yet.
         *
         * @param v vertex to test
         * @return true if the vertex has cost already
         */
        public boolean hasCost(V v) {
            return costs.containsKey(v);
        }

        /**
         * Returns the current cost to reach the specified vertex.
         * If the vertex has not been accessed yet, it has no cost
         * associated and null will be returned.
         *
         * @param v vertex to reach
         * @return weight cost to reach the vertex if already accessed;
         *         null otherwise
         */
        public Weight cost(V v) {
            return costs.get(v);
        }

        /**
         * Updates the cost of the vertex using its existing cost plus the
         * cost to traverse the specified edge. If the search is in single
         * path mode, only one path will be accrued.
         *
         * @param vertex  vertex to update
         * @param edge    edge through which vertex is reached
         * @param cost    current cost to reach the vertex from the source
         * @param replace true to indicate that any accrued edges are to be
         *                cleared; false to indicate that the edge should be
         *                added to the previously accrued edges as they yield
         *                the same cost
         */
        public void updateVertex(V vertex, E edge, Weight cost, boolean replace) {
            costs.put(vertex, cost);
            if (edge != null) {
                Set<E> edges = parents.get(vertex);
                if (edges == null) {
                    edges = new HashSet<>();
                    parents.put(vertex, edges);
                }
                if (replace) {
                    edges.clear();
                }
                if (maxPaths == ALL_PATHS || edges.size() < maxPaths) {
                    edges.add(edge);
                }
            }
        }

        /**
         * Removes the set of parent edges for the specified vertex.
         *
         * @param v vertex
         */
        void removeVertex(V v) {
            parents.remove(v);
        }

        /**
         * If possible, relax the specified edge using the supplied base cost
         * and edge-weight function.
         *
         * @param edge            edge to be relaxed
         * @param cost            base cost to reach the edge destination vertex
         * @param ew              optional edge weight function
         * @param forbidNegatives if true negative values will forbid the link
         * @return true if the edge was relaxed; false otherwise
         */
        public boolean relaxEdge(E edge, Weight cost, EdgeWeigher<V, E> ew,
                                 boolean... forbidNegatives) {
            V v = edge.dst();

            Weight hopCost = ew.weight(edge);
            if ((!hopCost.isViable()) ||
                    (hopCost.isNegative() && forbidNegatives.length == 1 && forbidNegatives[0])) {
                return false;
            }
            Weight newCost = cost.merge(hopCost);

            int compareResult = -1;
            if (hasCost(v)) {
                Weight oldCost = cost(v);
                compareResult = newCost.compareTo(oldCost);
            }

            if (compareResult <= 0) {
                updateVertex(v, edge, newCost, compareResult < 0);
            }
            return compareResult < 0;
        }

        /**
         * Builds a set of paths for the specified src/dst vertex pair.
         */
        public void buildPaths() {
            Set<V> destinations = new HashSet<>();
            if (dst == null) {
                destinations.addAll(costs.keySet());
            } else {
                destinations.add(dst);
            }

            // Build all paths between the source and all requested destinations.
            for (V v : destinations) {
                // Ignore the source, if it is among the destinations.
                if (!v.equals(src)) {
                    buildAllPaths(this, src, v, maxPaths);
                }
            }
        }

    }
    /**
     * Builds a set of all paths between the source and destination using the
     * graph search result by applying breadth-first search through the parent
     * edges and vertex costs.
     *
     * @param result   graph search result
     * @param src      source vertex
     * @param dst      destination vertex
     * @param maxPaths limit on the number of paths built;
     *                 {@link #ALL_PATHS} if no limit
     */
    private void buildAllPaths(Result result, V src, V dst, int maxPaths) {
        DefaultMutablePath<V, E> basePath = new DefaultMutablePath<>();
        basePath.setCost(result.cost(dst));

        Set<DefaultMutablePath<V, E>> pendingPaths = new HashSet<>();
        pendingPaths.add(basePath);

        while (!pendingPaths.isEmpty() &&
                (maxPaths == ALL_PATHS || result.paths.size() < maxPaths)) {
            Set<DefaultMutablePath<V, E>> frontier = new HashSet<>();

            for (DefaultMutablePath<V, E> path : pendingPaths) {
                // For each pending path, locate its first vertex since we
                // will be moving backwards from it.
                V firstVertex = firstVertex(path, dst);

                // If the first vertex is our expected source, we have reached
                // the beginning, so add the this path to the result paths.
                if (firstVertex.equals(src)) {
                    path.setCost(result.cost(dst));
                    result.paths.add(new DefaultPath<>(path.edges(), path.cost()));

                } else {
                    // If we have not reached the beginning, i.e. the source,
                    // fetch the set of edges leading to the first vertex of
                    // this pending path; if there are none, abandon processing
                    // this path for good.
                    Set<E> firstVertexParents = result.parents.get(firstVertex);
                    if (firstVertexParents == null || firstVertexParents.isEmpty()) {
                        break;
                    }

                    // Now iterate over all the edges and for each of them
                    // cloning the current path and then insert that edge to
                    // the path and then add that path to the pending ones.
                    // When processing the last edge, modify the current
                    // pending path rather than cloning a new one.
                    Iterator<E> edges = firstVertexParents.iterator();
                    while (edges.hasNext()) {
                        E edge = edges.next();
                        boolean isLast = !edges.hasNext();
                        // Exclude any looping paths
                        if (!isInPath(edge, path)) {
                            DefaultMutablePath<V, E> pendingPath = isLast ? path : new DefaultMutablePath<>(path);
                            pendingPath.insertEdge(edge);
                            frontier.add(pendingPath);
                        }
                    }
                }
            }

            // All pending paths have been scanned so promote the next frontier
            pendingPaths = frontier;
        }
    }

    /**
     * Indicates whether or not the specified edge source is already visited
     * in the specified path.
     *
     * @param edge edge to test
     * @param path path to test
     * @return true if the edge.src() is a vertex in the path already
     */
    private boolean isInPath(E edge, DefaultMutablePath<V, E> path) {
        return path.edges().stream().anyMatch(e -> edge.src().equals(e.dst()));
    }

    // Returns the first vertex of the specified path. This is either the source
    // of the first edge or, if there are no edges yet, the given destination.
    private V firstVertex(Path<V, E> path, V dst) {
        return path.edges().isEmpty() ? dst : path.edges().get(0).src();
    }

    /**
     * Checks the specified path search arguments for validity.
     *
     * @param graph graph; must not be null
     * @param src   source vertex; must not be null and belong to graph
     * @param dst   optional target vertex; must belong to graph
     */
    protected void checkArguments(Graph<V, E> graph, V src, V dst) {
        checkNotNull(graph, "Graph cannot be null");
        checkNotNull(src, "Source cannot be null");
        Set<V> vertices = graph.getVertexes();
        checkArgument(vertices.contains(src), "Source not in the graph");
        checkArgument(dst == null || vertices.contains(dst),
                "Destination not in graph");
    }

    public Result search(Graph<V, E> graph, V src, V dst,
                         EdgeWeigher<V, E> weigher, int maxPaths) {
        checkArguments(graph, src, dst);

        return internalSearch(graph, src, dst,
                weigher != null ? weigher : new DefaultEdgeWeigher<>(),
                maxPaths);
    }

    protected Result internalSearch(Graph<V, E> graph, V src, V dst,
                                    EdgeWeigher<V, E> weigher, int maxPaths) {

        // Use the default result to remember cumulative costs and parent
        // edges to each each respective vertex.
        Result result = new Result(src, dst, maxPaths);

        // Cost to reach the source vertex is 0 of course.
        result.updateVertex(src, null, weigher.getInitialWeight(), false);

        if (graph.getEdges().isEmpty()) {
            result.buildPaths();
            return result;
        }

        // Use the min priority queue to progressively find each nearest
        // vertex until we reach the desired destination, if one was given,
        // or until we reach all possible destinations.
        Heap<V> minQueue = createMinQueue(graph.getVertexes(),
                new PathCostComparator(result));
        while (!minQueue.isEmpty()) {
            // Get the nearest vertex
            V nearest = minQueue.extractExtreme();
            if (nearest.equals(dst)) {
                break;
            }

            // Find its cost and use it to determine if the vertex is reachable.
            if (result.hasCost(nearest)) {
                Weight cost = result.cost(nearest);

                // If the vertex is reachable, relax all its egress edges.
                for (E e : graph.getEdgesFrom(nearest)) {
                    if (!e.isPermitted(src, dst)) continue;
                    result.relaxEdge(e, cost, weigher, true);
                }
            }

            // Re-prioritize the min queue.
            minQueue.heapify();
        }

        // Now construct a set of paths from the results.
        result.buildPaths();
        return result;
    }

    // Compares path weights using their accrued costs; used for sorting the
    // min priority queue.
    private final class PathCostComparator implements Comparator<V> {
        private final Result result;

        private PathCostComparator(Result result) {
            this.result = result;
        }

        @Override
        public int compare(V v1, V v2) {
            //not accessed vertices should be pushed to the back of the queue
            if (!result.hasCost(v1) && !result.hasCost(v2)) {
                return 0;
            } else if (!result.hasCost(v1)) {
                return -1;
            } else if (!result.hasCost(v2)) {
                return 1;
            }

            return result.cost(v2).compareTo(result.cost(v1));
        }
    }

    // Creates a min priority queue from the specified vertexes and comparator.
    private Heap<V> createMinQueue(Set<V> vertexes, Comparator<V> comparator) {
        return new Heap<>(new ArrayList<>(vertexes), comparator);
    }

}
