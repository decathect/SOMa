package cs437.som.distancemetrics;

import cs437.som.DistanceMetric;
import cs437.som.SOMError;

/**
 * Euclidean distance strategy.
 */
public class EuclideanDistanceMetric implements DistanceMetric {

    /**
     * Calculate the Euclidean distance between 2 vectors.
     *
     * The Euclidean distance of 2 vectors is the square root of the sum of the
     * squares of the differences of the individual components of the 2 vectors.
     *
     * In LaTeX:
     *      for v_1, v_2 \in \mathbb{R}^n
     *      \sqrt{\sum_{i=1}^{n}(v_{1_i}-v_{2_i})^2}
     *
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The distance between v0 and v1.
     * @throws cs437.som.SOMError If the vector sizes do not match.
     */
    public double distance(double[] v0, double[] v1) throws SOMError {
        if (v0.length != v1.length) {
            throw new SOMError("EuclideanDistanceMetric: input vector lengths do not match.");
        }

        double sum = 0.0;
        for (int i = 0; i < v0.length; i++) {
            double difference = v0[i] - v1[i];
            sum += difference * difference;
        }

        return Math.sqrt(sum);
    }

    @Override
    public String toString() {
        return "EuclideanDistanceMetric";
    }
}
