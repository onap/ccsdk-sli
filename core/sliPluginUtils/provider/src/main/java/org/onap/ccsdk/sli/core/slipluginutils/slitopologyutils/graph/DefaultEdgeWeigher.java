package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

/**
 * Default weigher returns identical weight for every graph edge. Basically it
 * is a hop count weigher.
 * Produces weights of {@link ScalarWeight} type.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultEdgeWeigher<V extends Vertex, E extends Edge<V>>
        implements EdgeWeigher<V, E> {

    /**
     * Common weight value for any link.
     */
    protected static final double HOP_WEIGHT_VALUE = 1;
    /**
     * Weight value for null path (without links).
     */
    protected static final double NULL_WEIGHT_VALUE = 0;

    /**
     * Default weight based on hop count.
     * {@value #HOP_WEIGHT_VALUE}
     */
    public static final ScalarWeight DEFAULT_HOP_WEIGHT =
            new ScalarWeight(HOP_WEIGHT_VALUE);

    /**
     * Default initial weight.
     * {@value #NULL_WEIGHT_VALUE}
     */
    public static final ScalarWeight DEFAULT_INITIAL_WEIGHT =
            new ScalarWeight(NULL_WEIGHT_VALUE);

    @Override
    public Weight weight(E edge) {
        return DEFAULT_HOP_WEIGHT;
    }

    @Override
    public Weight getInitialWeight() {
        return DEFAULT_INITIAL_WEIGHT;
    }

    @Override
    public Weight getNonViableWeight() {
        return ScalarWeight.NON_VIABLE_WEIGHT;
    }
}

