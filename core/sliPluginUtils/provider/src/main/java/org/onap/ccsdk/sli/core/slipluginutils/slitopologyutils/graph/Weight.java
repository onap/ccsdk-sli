package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

/**
 * Abstraction of a graph edge weight.
 */
public interface Weight extends Comparable<Weight> {

    /**
     * Merges the given weight with this one returning a new aggregated
     * weight.
     *
     * @param otherWeight weight to add
     * @return aggregated weight
     */
    Weight merge(Weight otherWeight);

    /**
     * Subtracts the given weight from this one and produces a new weight.
     *
     * @param otherWeight weight to subtract
     * @return residual weight
     */
    Weight subtract(Weight otherWeight);

    /**
     * Returns true if the weighted subject (link/path) can be traversed; false otherwise.
     *
     * @return true if weight is adequate, false if weight is infinite
     */
    boolean isViable();

    /**
     * Returns true if the weight is negative (means that aggregated
     * path cost will decrease if we add weighted subject to it).
     *
     * @return true if the weight is negative, false otherwise
     */
    boolean isNegative();
}
