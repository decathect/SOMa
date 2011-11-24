package cs437.som.network;

import cs437.som.Dimension;
import cs437.som.SOMError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic self-organizing map where the neurons are arranged by their weights.
 *
 * An simple example usage:
 *
 * <code>
 * <pre>
 * private static final int iterations = 500;
 *
 * public static void main(String[] args) {
 *     TrainableSelfOrganizingMap som = new BasicPlanarSOM(7, 2, iterations);
 *     Random r = new SecureRandom();
 *
 *     for (int i = 0; i < iterations; i++) {
 *         double[] in = {r.nextDouble() * 10, r.nextDouble() * 10};
 *         som.trainWith(in);
 *     }
 *
 *     // At this point, the SOM would be queried for it's BMU.
 * }
 * </pre>
 * </code>
 */
public class BasicPlanarSOM extends NetworkBase {

    /**
     * Create a new BasicPlanarSOM.
     *
     * @param neuronCount The number of neurons to employ.
     * @param inputVectorSize The input vector size.
     * @param expectedIterations The expected number of training iterations.
     */
    public BasicPlanarSOM(int neuronCount, int inputVectorSize,
                          int expectedIterations) {
        super(new Dimension(neuronCount, 1), inputVectorSize, expectedIterations);
    }

    @Override
    protected double neuronDistance(int neuron0, int neuron1) {
        double sum = 0.0;
        for (int i = 0; i < inputVectorSize; i++) {
            double difference = weightMatrix[neuron0][i] - weightMatrix[neuron1][i];
            sum += difference * difference;
        }
        return Math.sqrt(sum);
    }

    @Override
    protected double neighborhoodWidth() {
        if (time < (expectedIterations / 5))
            return 1.0 * (1.0 - (time / (double) expectedIterations));
        return 0.0;
    }

    @Override
    public String toString() {
        return "BasicPlanarSOM{weightMatrix=" +
                (weightMatrix == null ? null : Arrays.toString(weightMatrix))
                + '}';
    }

    @Override
    public void write(OutputStreamWriter destination) throws IOException {
        destination.write(String.format("Map type: BasicPlanarSOM%n"));
        super.write(destination);
        destination.flush();
    }

    /**
     * Read a BasicPlanarSOM from an input stream.
     *
     * @param input The stream to read from.  This stream should be passed in
     * as soon as it is known to represent a BasicPlanarSOM.
     * @return A BasicPlanarSOM as represented by the contents of
     * {@code input}.
     * @throws IOException if something fails while reading the stream.
     */
    public static BasicPlanarSOM read(BufferedReader input) throws IOException {
        SOMFileReader sfr = new SOMFileReader();
        sfr.parse(input);

        BasicPlanarSOM bhgsom = new BasicPlanarSOM(
                sfr.dimension.x, sfr.inputVectorSize, sfr.iterations);
        bhgsom.weightMatrix = readWeightMatrix(
                input, sfr.dimension.area, sfr.inputVectorSize);
        return bhgsom;
    }

    private static class SOMFileReader {
        private static final Pattern dimensionRegEx = Pattern.compile(
                "(?:grid)?\\s*dimensions\\s*:\\s*(\\d+)\\s*,\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE);
        private static final Pattern inputVectorSizeRegEx = Pattern.compile(
                "(?:input)?\\s*length\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        private static final Pattern iterationsRegEx = Pattern.compile(
                "iterations\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        private static final Pattern weightRegEx = Pattern.compile(
                "weights\\s*(?::)", Pattern.CASE_INSENSITIVE);

        public Dimension dimension = null;
        public int inputVectorSize = 0;
        public int iterations = 0;

        public void parse(BufferedReader input) throws IOException {
            String line = input.readLine();
            Matcher match = weightRegEx.matcher(line);
            while (! match.matches() && input.ready()) {
                matchDimension(line);
                matchInputVectorSize(line);
                matchIterations(line);

                line = input.readLine();
                match = weightRegEx.matcher(line);
            }

            if (dimension == null || inputVectorSize < 1) {
                throw new SOMError("A valid dimension and input vector size must" +
                        " appear in a map's configuration%nand they must appear " +
                        "before the weight matrix.");
            }
        }

        private void matchIterations(String line) {
            Matcher iterationsMatch = iterationsRegEx.matcher(line);
            if (iterationsMatch.matches()) {
                iterations = Integer.parseInt(iterationsMatch.group(1));
            }
        }

        private void matchInputVectorSize(String line) {
            Matcher inputMatch = inputVectorSizeRegEx.matcher(line);
            if (inputMatch.matches()) {
                inputVectorSize = Integer.parseInt(inputMatch.group(1));
            }
        }

        private void matchDimension(String line) {
            Matcher dimMatch = dimensionRegEx.matcher(line);
            if (dimMatch.matches()) {
                dimension = new Dimension(Integer.parseInt(dimMatch.group(1)),
                        Integer.parseInt(dimMatch.group(2)));
            }
        }

    }
}
