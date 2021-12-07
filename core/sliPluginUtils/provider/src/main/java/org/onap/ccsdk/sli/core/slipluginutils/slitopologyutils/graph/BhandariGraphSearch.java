package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

import java.util.*;
import java.util.stream.Collectors;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class BhandariGraphSearch<V extends Vertex, E extends Edge<V>> extends DijkstraGraphSearch<V, E>{

	/*public class Result {

		private final V src;
		private final V dst;
		protected final Set<Path<V, E>> paths = new HashSet<>();
		protected final Map<V, Weight> costs = new HashMap<>();
		protected final Map<V, Set<E>> parents = new HashMap<>();
		protected final int maxPaths;

		*//**
		 * Creates the result of a single-path search.
		 *
		 * @param src path source
		 * @param dst optional path destination
		 *//*
		public Result(V src, V dst) {
			this(src, dst, 1);
		}

		*//**
		 * Creates the result of path search.
		 *
		 * @param src      path source
		 * @param dst      optional path destination
		 * @param maxPaths optional limit of number of paths;
		 *//*
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

		*//**
		 * Indicates whether or not the given vertex has a cost yet.
		 *
		 * @param v vertex to test
		 * @return true if the vertex has cost already
		 *//*
		public boolean hasCost(V v) {
			return costs.containsKey(v);
		}

		*//**
		 * Returns the current cost to reach the specified vertex.
		 * If the vertex has not been accessed yet, it has no cost
		 * associated and null will be returned.
		 *
		 * @param v vertex to reach
		 * @return weight cost to reach the vertex if already accessed;
		 *         null otherwise
		 *//*
		public Weight cost(V v) {
			return costs.get(v);
		}

		*//**
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
		 *//*
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

		*//**
		 * Removes the set of parent edges for the specified vertex.
		 *
		 * @param v vertex
		 *//*
		void removeVertex(V v) {
			parents.remove(v);
		}

		*//**
		 * If possible, relax the specified edge using the supplied base cost
		 * and edge-weight function.
		 *
		 * @param edge            edge to be relaxed
		 * @param cost            base cost to reach the edge destination vertex
		 * @param ew              optional edge weight function
		 * @param forbidNegatives if true negative values will forbid the link
		 * @return true if the edge was relaxed; false otherwise
		 *//*
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

		*//**
		 * Builds a set of paths for the specified src/dst vertex pair.
		 *//*
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

	}*/
	private static int K_DISJOINT_PATH = 2;
	private static final boolean LINK_DISJOINT_ONLY = false;
	private static final boolean COMMON_NODE_ALLOWED = true;
	private static final boolean COMMON_LINK_ALLOWED = true;
	// Assuming no more than 100_000 vertices and hop count as link distance (One unit per link)
	// If applying different distance matrics, double check with MAX_VALUE of double type
	// Rule of Thumb: COMMON_LINK_PENALTY * num_possible_vertex < double.MAX_VALUE
	// make greater than sum of link distances;
	private static final ScalarWeight COMMON_NODE_PENALTY = ScalarWeight.toWeight(1_000_000_000.0) ;
	// maek much greater than COMMON_NODE_PENALTY
	private static final ScalarWeight COMMON_LINK_PENALTY = ScalarWeight.toWeight(120_000_000_000.0);
	// sameness threshold
	private static final ScalarWeight SAMENESS_THRESHOLD= ScalarWeight.toWeight(0.0001);

	protected DisjointPathResult internalSearchFunc(Graph<V, E> graph, V src, V dst,
				EdgeWeigher<V, E> weigher, int maxPaths) {

		if (src.equals(dst)){
			return null;
		}
		DijkstraGraphSearch<V, E>.Result firstBFS = super.internalSearch(
				graph, src, dst, weigher, 1);
		if (firstBFS.paths().isEmpty()) {
			return null;
		}

		DisjointPathResult result = new DisjointPathResult(src, dst, maxPaths);
		result.disjointPathsList.add(firstBFS.paths().iterator().next());
		for(int i = 1; i < K_DISJOINT_PATH; i++){
			result.disjointPathsList.add(new DefaultMutablePath<>());
		}

		final List<Path<V, E>> tempPathsList = new ArrayList<>(K_DISJOINT_PATH);
		tempPathsList.add(firstBFS.paths().iterator().next());

		for(int i = 1; i < K_DISJOINT_PATH; i++){
			Graph<V, E> gt = mutableCopy(graph);
			InnerEdgeWeigher weigher1 = new InnerEdgeWeigher(weigher);
			for (int j = 0; j < i; j++){
				Path<V, E> prevPath = result.disjointPathsList.get(j);
				kDualPathGraphTransformation(gt, prevPath, src, dst, weigher1);
			}
			//Run shotest on the transformed graph
			DijkstraGraphSearch<V, E>.Result pathSearchResult = super.internalSearch(
					gt, src, dst, weigher1, 1);
			if (pathSearchResult.paths.isEmpty()){
				break;
			}

			Path<V, E> cleanNewPath = cleanPath(gt, weigher1, pathSearchResult.paths().iterator().next());
			tempPathsList.add(cleanNewPath);
			for(int r = i; r > 0; r--){
				for(int l = r-1; l >= 0; l--) {
					generateTwoRealPaths(graph, weigher, tempPathsList.get(l), tempPathsList.get(r),
							result.disjointPathsList, l, r);
					tempPathsList.set(r, result.disjointPathsList.get(r));
					tempPathsList.set(l, result.disjointPathsList.get(l));
				}
			}
		}
		//TODO: result.buildPaths();
		return result;
	}


	/**
	 * Creates a mutable copy of an immutable graph.
	 *
	 * @param graph   immutable graph
	 * @return mutable copy
	 */
	private Graph<V, E> mutableCopy(Graph<V, E> graph) {
		return new Graph<>(graph.getVertexes(), graph.getEdges());
	}

	private void kDualPathGraphTransformation(Graph<V, E> graph, Path<V, E> prevPath, V src, V dst,
											  InnerEdgeWeigher weigher) {
		for (int i = prevPath.edges().size() - 1; i >= 0; i--) {
			E edge = prevPath.edges().get(i);
			if (i == 0) {
				if (weigher.weight(edge).compareTo(COMMON_LINK_PENALTY) > 0) {
					// Has been a common link in previous paths; increase penalty if use again
					weigher.setWeight(edge, weigher.weight(edge).merge(COMMON_LINK_PENALTY));
					continue;
				}
				Edge<V> reverse = getReverseEdge(graph, edge);
				double revEdgeDist = ((ScalarWeight) weigher.weight(edge)).value() * -1.0;
				weigher.setWeight((E) reverse, ScalarWeight.toWeight(revEdgeDist));

				weigher.setWeight(edge, weigher.weight(edge).merge(COMMON_LINK_PENALTY));
				weigher.banEdge(edge);

				if (COMMON_LINK_ALLOWED) {
					weigher.allowEdge(edge);
				}
				continue;
			}

			Vertex prevVertex = edge.src();
			if (prevVertex instanceof DummyNode) {
				// must be a commmon node in the previous paths that have been found
				if ((!LINK_DISJOINT_ONLY) && COMMON_NODE_ALLOWED) {
					for (E edgeIncoming : graph.getEdgesTo((V) prevVertex)) {
						if (weigher.weight(edgeIncoming).compareTo(COMMON_NODE_PENALTY) > 0) {
							weigher.setWeight(edgeIncoming, weigher.weight(edgeIncoming).merge(COMMON_NODE_PENALTY));
							break;
						}
					}
				}


				if (weigher.weight(edge).compareTo(COMMON_LINK_PENALTY) > 0) {
					weigher.setWeight(edge, weigher.weight(edge).merge(COMMON_LINK_PENALTY));
					continue;
				}
				Edge<V> reverse = getReverseEdge(graph, edge);
				graph.addEdge((E) reverse);
				double revEdgeDist = ((ScalarWeight) weigher.weight(edge)).value() * -1.0;
				weigher.setWeight((E) reverse, ScalarWeight.toWeight(revEdgeDist));

				weigher.setWeight(edge, weigher.weight(edge).merge(COMMON_LINK_PENALTY));
				weigher.banEdge(edge);
				if (COMMON_LINK_ALLOWED) {
					weigher.allowEdge(edge);
				}
				continue;
			}

			//Split prevNode
			DummyNode dummyNode = new DummyNode(prevVertex);
			graph.addVertex((V) dummyNode);

			Edge<V> reverseproxy = getReverseEdge(graph, (E) edge);
			double revEdgeDist = ((ScalarWeight) weigher.weight(edge)).value() * -1.0;
			weigher.setWeight((E) reverseproxy, ScalarWeight.toWeight(revEdgeDist));
			//graph.addEdge((E) reverseproxy);
			changeLinkDest(graph, weigher, (E) reverseproxy, (V) prevVertex,  (V) dummyNode);

			weigher.setWeight((E) edge, weigher.weight(edge).merge(COMMON_LINK_PENALTY));
			weigher.banEdge(edge);

			if (COMMON_LINK_ALLOWED) {
				weigher.allowEdge(edge);
				changeLinkSource(graph, weigher, edge, (V) prevVertex ,(V) dummyNode);
			}
			Edge<V> edgeLastHop = prevPath.edges().get(i-1);
			Edge<V> reverseLastHop = getReverseEdge(graph, edgeLastHop);
			List<E> outgoingLinksFromPrevNode = graph.getEdgesFrom((V) prevVertex).stream().collect(Collectors.toList());
			for (E edgeOutgoing : outgoingLinksFromPrevNode) {
				if (edgeOutgoing.src().equals(reverseLastHop.src())
						&& edgeOutgoing.dst().equals(reverseLastHop.dst())){
					continue;
				} else {
					changeLinkSource(graph, weigher, edgeOutgoing, (V) prevVertex ,(V) dummyNode);
				}
			}

			DummyEdge<V> dummyEdge = new DummyEdge<V>((V)dummyNode, (V) prevVertex);
			weigher.setWeight((E) dummyEdge, ScalarWeight.toWeight(0.0));
			graph.addEdge((E)dummyEdge);

			if (LINK_DISJOINT_ONLY) {
				Edge<V> reverseDummyEdge = new DummyEdge<V>(dummyEdge.dst(), dummyEdge.src());
				weigher.setWeight((E) reverseDummyEdge, SAMENESS_THRESHOLD);
				graph.addEdge((E)reverseDummyEdge);
			} else if (COMMON_NODE_ALLOWED){
				Edge<V> reverseDummyEdge = new DummyEdge<V>(dummyEdge.dst(), dummyEdge.src());
				weigher.setWeight((E) reverseDummyEdge, COMMON_NODE_PENALTY);
				graph.addEdge((E)reverseDummyEdge);
			}
		}
	}

	private void generateTwoRealPaths(Graph<V, E> graph, EdgeWeigher<V, E> weigher, Path<V, E> tempPath0,  Path<V, E> tempPath1,
									  List<Path<V, E>> disjointPathList, int realpath0idx, int realpath1idx) {

		List<E> realPEdge0 = new ArrayList<>();
		List<E> realPEdge1 = new ArrayList<>();
		int i, j, e, k, m, lastj, lastpos = 0;
		boolean exchanged = false;
		for( i = 0, e = tempPath0.edges().size(); i < e; i++) {
			for( j = 0, k = tempPath1.edges().size(); j < k; j++){
				if (getReverseEdge(graph, tempPath1.edges().get(j)).equals(tempPath0.edges().get(i)) ){
					//Found an overlapping
					lastj = j;
					for(i++, j--; i < tempPath0.edges().size() && j >= 0; i++, j--) {
						if (getReverseEdge(graph, tempPath1.edges().get(j)).equals(tempPath0.edges().get(i)) == false ) {
							//No longer overlapping
							break;
						}
					}
					j++;
					i--; //Go back to last overlapping
					for (m= lastpos; m < j; m++){
						//prepend edges before exchanging;
						if (!exchanged){
							realPEdge1.add(tempPath1.edges().get(m));
						} else {
							realPEdge0.add(tempPath1.edges().get(m));
						}
					}
					exchanged = !exchanged;
					lastpos = lastj + 1;
					break;
				}
			}
			// Depends on overlapping happened or not, append this edge to path accordingly.
			if (j == tempPath1.edges().size()){
				if (!exchanged){
					realPEdge0.add(tempPath0.edges().get(i));
				} else {
					realPEdge1.add(tempPath0.edges().get(i));
				}
			}
		}
		//Dealing with trailing edges for the other link in both cases.
		for (m = lastpos; m < tempPath1.edges().size(); m++) {
			if (!exchanged){
				realPEdge1.add(tempPath1.edges().get(m));
			} else {
				realPEdge0.add(tempPath1.edges().get(m));
			}
		}

		disjointPathList.set(realpath0idx, new DefaultPath<>(realPEdge0, computePathCost(realPEdge0, weigher)));
		disjointPathList.set(realpath1idx, new DefaultPath<>(realPEdge1, computePathCost(realPEdge1, weigher)));
	}

	Weight computePathCost(List<E> path, EdgeWeigher<V, E> weigher) {
		Weight total = new ScalarWeight(0.0);
		for (E e: path){
			total = total.merge(weigher.weight(e));
		}
		return total;
	}

	private Path<V, E> cleanPath(Graph<V, E> graph, InnerEdgeWeigher weigher, Path<V, E> path) {
		MutablePath<V, E> newPath = new DefaultMutablePath<>();
		for(E edge: path.edges()) {
			if (edge instanceof DummyEdge) {
				if(((DummyEdge)edge).isProxy()){
					E origEdge = (E) ((DummyEdge)edge).origin;
					newPath.appendEdge(origEdge);
					weigher.allowEdge(origEdge);
				}
			} else {
				newPath.appendEdge(edge);
			}
		}
		return newPath;
	}
	private void changeLinkSource(Graph<V, E> graph, InnerEdgeWeigher weigher, E edge, V prevSrc, V newSrc) {
		weigher.allowEdge(edge);
		Edge<V> proxy = new DummyEdge<>((V) newSrc, edge.dst(), edge);
		graph.removeEdge((E) edge);
		graph.addEdge((E) proxy);
		weigher.setWeight((E) proxy, weigher.weight(edge));
		weigher.clearWeight(edge);
	}

	private void changeLinkDest(Graph<V, E> graph, InnerEdgeWeigher weigher, E edge, V prevDst, V newDst) {
		weigher.allowEdge(edge);
		Edge<V> proxy = new DummyEdge<V>(edge.src(), (V) newDst, edge);
		graph.removeEdge((E) edge);
		graph.addEdge((E) proxy);
		weigher.setWeight((E) proxy, weigher.weight(edge));
		weigher.clearWeight(edge);
	}

	//Be noted, in OTN network, always assume links are added in both directions at once.
	private Edge<V> getReverseEdge(Graph<V, E> graph, Edge<V> forwardEdge){
		V src = forwardEdge.src();
		V dst = forwardEdge.dst();
		for (E edge: graph.getEdgesFrom(dst)){
			if (edge.dst().equals(src)){
				return edge;
			}
		}
		return null;
	}
    /*private static final class ReverseEdge<V extends Vertex> extends AbstractEdge<V> {

        private ReverseEdge(V src, V dst) {
            super(src, dst);
        }
        private ReverseEdge(Edge<V> edge) {
            this(edge.dst(), edge.src());
        }

        @Override
        public String toString() {
            return "ReversedEdge " + "src=" + src() + " dst=" + dst();
        }
    }*/

	protected static final class DummyEdge<V extends Vertex> implements Edge<V> {

		private final V src;
		private final V dst;
		private final Edge<V> origin;
		private DummyEdge(V src, V dst) {
			this(src, dst, null);
		}
		private DummyEdge(V src, V dst, Edge<V> edge) {
			this.src = checkNotNull(src, "Source vertex cannot be null");
			this.dst = checkNotNull(dst, "Destination vertex cannot be null");
			origin = edge;
		}

		@Override
		public V src() {
			return src;
		}

		@Override
		public V dst() {
			return dst;
		}
		public boolean isProxy() {
			return origin != null;
		}
		public String toString() {
			return "DummyEdge " + "src=" + src() + " dst=" + dst();
		}
	}

	protected static final class DummyNode implements Vertex {
		private final Vertex origin;
		private DummyNode(Vertex vertex) {
			origin = vertex;
		}

		@Override
		public String toString() {
			return "DummyNode " + origin.toString();
		}
	}

	private final class InnerEdgeWeigher implements EdgeWeigher<V, E> {

		private Set<E> disabledEdges = new HashSet<>();
		private Map<E, Weight> cost = new HashMap<>();
		private final EdgeWeigher weigherReal;

		private InnerEdgeWeigher(EdgeWeigher weigherReal) {
			this.weigherReal = weigherReal;
		}

		@Override
		public Weight weight(E edge) {
			if (disabledEdges.contains(edge)) {
				return ScalarWeight.NON_VIABLE_WEIGHT;
			} else if (cost.containsKey(edge)) {
				return cost.get(edge);
			} else {
				return weigherReal.weight(edge);
			}
		}

		public void banEdge(E edge){
			disabledEdges.add(edge);
		}

		public void allowEdge(E edge){
			disabledEdges.remove(edge);
		}

		public void setWeight(E edge, Weight val){
			cost.put(edge, val);
		}

		public void clearWeight(E edge){
			cost.remove(edge);
		}

		@Override
		public Weight getInitialWeight() {
			return new ScalarWeight(0.0);
		}

		@Override
		public Weight getNonViableWeight() {
			return ScalarWeight.NON_VIABLE_WEIGHT;
		}
	}
	public final class DisjointPathResult {
		//private final DijkstraGraphSearch.Result searchResult;
		private final V src, dst;
		protected final List<Path<V, E>> disjointPathsList = new ArrayList<>();

		public DisjointPathResult( V src, V dst) {
			//this.searchResult = searchResult;
			this.src = src;
			this.dst = dst;
		}

		public DisjointPathResult( V src, V dst, int maxPaths) {
			//this.searchResult = searchResult;
			this.src = src;
			this.dst = dst;
		}


		public Set<Path<V, E>> paths() {
			return disjointPathsList.stream().collect(Collectors.toSet());
		}


		public Map<V, Set<E>> parents() {
			return null;
			//return searchResult.parents();
		}


		public Map<V, Weight> costs () {
			//throw new Exception("No cost for diverse paths");
			return null;
		}
	}

	public DisjointPathResult searchForDisjointPath(Graph<V, E> graph, V src, V dst,
						 EdgeWeigher<V, E> weigher, int maxPaths) {
		checkArguments(graph, src, dst);

		return internalSearchFunc(graph, src, dst,
				weigher != null ? weigher : new DefaultEdgeWeigher<>(),
				maxPaths);
	}
}
