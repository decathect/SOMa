package cs437.som.network;

import java.util.Arrays;

/**
 * A simple self-organizing map, using a hexagonal grid for the neurons.
 *
 * An simple example usage:
 *
 * <code>
 * <pre>
 * private static final int iterations = 500;
 *
 * public static void main(String[] args) {
 *     SelfOrganizingMap som = new BasicHexGridSOM(7, 2, iterations);
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
public class BasicHexGridSOM extends NetworkBase {
    private int gridSize;

    public BasicHexGridSOM(int gridSize, int inputVectorSize, int expectedIterations) {
        super(inputVectorSize, gridSize * gridSize, expectedIterations, gridSize);
        this.gridSize = gridSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double neuronDistance(int neuron0, int neuron1) {
        int row0 = neuron0 / gridSize;
        int col0 = neuron0 % gridSize;
        int row1 = neuron1 / gridSize;
        int col1 = neuron1 % gridSize;

        int dx = row1 - row0;
        int dy = col1 - col0;

        int distance;
        if (sign(dx) == sign(dy)) {
            distance = Math.abs(dx + dy);
        } else {
            distance = Math.max(Math.abs(dx), Math.abs(dy));
        }

        return distance;
    }

    private int sign(int n) {
        return (n >= 0) ? 1 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BasicHexGridSOM{time=" + time +
                ", weightMatrix=" + (weightMatrix == null ? null : Arrays.asList(weightMatrix)) +
                ", neuronCount=" + neuronCount + ", inputSize=" + inputVectorSize + '}';
    }

}
