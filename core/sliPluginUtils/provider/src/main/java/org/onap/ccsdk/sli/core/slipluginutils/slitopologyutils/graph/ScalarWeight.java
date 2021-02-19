package org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Weight implementation based on a double value.
 */
public class ScalarWeight implements Weight {

    /**
     * Instance of scalar weight to mark links/paths which
     * can not be traversed.
     */
    public static final ScalarWeight NON_VIABLE_WEIGHT =
            new ScalarWeight(Double.POSITIVE_INFINITY);

    private static double samenessThreshold = Double.MIN_VALUE;

    private final double value;

    /**
     * Creates a new scalar weight with the given double value.
     * @param value double value
     * @return scalar weight instance
     */
    public static ScalarWeight toWeight(double value) {
        return new ScalarWeight(value);
    }

    /**
     * Creates a new scalar weight with the given double value.
     * @param value double value
     */
    public ScalarWeight(double value) {
        this.value = value;
    }

    @Override
    public Weight merge(Weight otherWeight) {
        return new ScalarWeight(value + ((ScalarWeight) otherWeight).value);
    }

    @Override
    public Weight subtract(Weight otherWeight) {
        return new ScalarWeight(value - ((ScalarWeight) otherWeight).value);
    }

    @Override
    public boolean isViable() {
        return !this.equals(NON_VIABLE_WEIGHT);
    }

    @Override
    public int compareTo(Weight otherWeight) {
        //check equality with samenessThreshold
        if (equals(otherWeight)) {
            return 0;
        }
        return Double.compare(value, ((ScalarWeight) otherWeight).value);
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof ScalarWeight) &&
                (Math.abs(value - ((ScalarWeight) obj).value) < samenessThreshold)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean isNegative() {
        return value < 0;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("value", value).toString();
    }


    /**
     * Returns inner double value.
     *
     * @return double value
     */
    public double value() {
        return value;
    }

}
