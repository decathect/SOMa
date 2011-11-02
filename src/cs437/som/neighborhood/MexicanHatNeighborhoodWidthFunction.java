package cs437.som.neighborhood;

import cs437.som.NeightborhoodWidthFunction;

/**
 * Neighborhood width strategy for self-organizing maps that decays the width
 * by the so called "Mexican Hat" function (also, the Ricker wavelet or
 * negative normalized second derivative of a Gaussian), centered at 0 and
 * parametrized by the standard deviation.
 *
 * The exact behavior follows the formula:
 *      \frac{2}{\sqrt{3 \sigma} \pi^{\frac{1}{4}}}
 *      e^{\frac{-t^2}{2 \sigma^2}} (1 - \frac{t^2}{\sigma^2})
 *  where
 *      \sigma  is the standard deviation of the corresponding Gaussian
 *      e       is the base of the natural logarithm
 *      t       is the current iteration
 *
 */
public class MexicanHatNeighborhoodWidthFunction implements NeightborhoodWidthFunction {
    private static final double oneFourth = 0.25;
    private final double coefficient;
    private final double variance;

    /**
     * Create a neighborhood width function based on the "Maexican Hat" function.
     *
     * @param standardDeviation The standard deviation of the corresponding
     * Gaussian function.
     */
    public MexicanHatNeighborhoodWidthFunction(double standardDeviation) {
        coefficient = 2 / (Math.sqrt(3 * standardDeviation) * Math.pow(Math.PI, oneFourth));
        variance = standardDeviation * standardDeviation;
    }

    /**
     * {@inheritDoc}
     */
    public void setExpectedIterations(int expectedIterations) {
    }

    /**
     * {@inheritDoc}
     */
    public double neighborhoodWidth(int iteration) {
        double i2 = (double) iteration * iteration;
        return coefficient *
                Math.exp(-i2 / (2 * variance)) *
                (1 - (i2 / variance));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MexicanHatNeighborhoodWidthFunction";
    }
}
