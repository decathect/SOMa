package cs437.som.network;

import cs437.som.Dimension;
import cs437.som.SOMError;
import cs437.som.TrainableSelfOrganizingMap;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Common functionality for basic self-organizing maps.
 */
public abstract class NetworkBase implements TrainableSelfOrganizingMap {
    /**
     * The default learning rate.
     */
    public static final double DEFAULT_LEARNING_RATE = 0.1;

    /**
     * The initial, default neighborhood width.
     */
    public final double initialNeighborhoodWidth;

    /**
     * The current training iteration.
     */
    protected int time = 0;

    /**
     * The count of neurons.
     */
    protected final int neuronCount;

    /**
     * The length of the input vector.
     */
    protected final int inputVectorSize;

    /**
     * The expected number of training iterations.
     */
    protected final int expectedIterations;

    /**
     * A matrix of the neurons input weights.  The "left" index is the neuron
     * and the "right" index is the input weight.
     */
    protected double[][] weightMatrix;

    /**
     * The dimensions of the map's neuron grid.
     */
    protected final Dimension gridSize;
    private Random random;

    /**
     * Constructs the common functionality for SOMs.
     *
     * @param gridSize The neuron grid dimensions.
     * @param inputVectorSize The length of expected input vectors
     * @param expectedIterations The expected count of iterations for training.
     */
    protected NetworkBase(Dimension gridSize, int inputVectorSize,
                          int expectedIterations) {
        this.inputVectorSize = inputVectorSize;
        this.expectedIterations = expectedIterations;
        this.gridSize = gridSize;
        this.neuronCount = gridSize.area;

        initialNeighborhoodWidth = Math.min(gridSize.x, gridSize.y) / 3;

        weightMatrix = new double[neuronCount][inputVectorSize];
        initialize();
    }

    public int getBestMatchingNeuron(double[] input) {
        checkInput(input);

        int bestMatch = 0;
        double lowestDistance2 = distanceToInput(0, input);
        for (int i = 1; i < neuronCount; i++) {
            double distance2temp = distanceToInput(i, input);
            if (distance2temp < lowestDistance2) {
                lowestDistance2 = distance2temp;
                bestMatch = i;
            }
        }
        return bestMatch;
    }

    protected int getBMUDuringTraining(double[] input) {
        List<Integer> bmuList = new ArrayList<Integer>(10);
        double lowestDistance2 = distanceToInput(0, input);
        bmuList.add(0);
        for (int i = 1; i < neuronCount; i++) {
            double distance2temp = distanceToInput(i, input);
            if (Math.abs(distance2temp - lowestDistance2) < /*0.1/time*/1.0e-6) {
                bmuList.add(i);
            } else if (distance2temp < lowestDistance2) {
                lowestDistance2 = distance2temp;
                bmuList.clear();
                bmuList.add(i);
            }
        }
        return bmuList.get(random.nextInt(bmuList.size()));
    }

    public int getBestMatchingNeuron(int[] input) {
        double[] dbls = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            dbls[i] = input[i];
        }
        return getBestMatchingNeuron(dbls);
    }

    public int getExpectedIterations() {
        return expectedIterations;
    }

    public Dimension getGridSize() {
        return gridSize;
    }

    public int getNeuronCount() {
        return gridSize.area;
    }

    public int getInputLength() {
        return inputVectorSize;
    }

    public double getWeight(int neuron, int weightIndex) {
        return weightMatrix[neuron][weightIndex];
    }

    public void trainWith(double[] data) {
        checkInput(data);

        int best = getBMUDuringTraining(data);
        adjustNeuronWeights(best, data);
        adjustNeighborsOf(best, data);
        time++;
    }

    public void trainWith(int[] data) {
        double[] dbls = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            dbls[i] = data[i];
        }
        trainWith(dbls);
    }

    /**
     * Create a string to display the neurons' weights.
     *
     * @return A string grid of the neurons weights (neurons left to right,
     * weights top to bottom).
     */
    public String weightString() {
        StringBuilder sb = new StringBuilder(weightMatrix.length * 3);

        for (double[] aWeightMatrix : weightMatrix) {
            sb.append(Arrays.toString(aWeightMatrix));
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * Calculate the learning rate (alpha in most equations) for the current
     * iteration. The current iteration will be taken from the object's current
     * count
     *
     * @return The learning rate for the current iteration.
     */
    protected double learningRate() {
        return DEFAULT_LEARNING_RATE;
    }

    /**
     * Verify that a given input vector's length matches the length expected by
     * the map.
     *
     * @param input The input vector to inspect.
     * @throws cs437.som.SOMError when the length of input does not match the
     * expected length.
     */
    protected void checkInput(double[] input) throws SOMError {
        if (input.length != inputVectorSize) {
            throw new SOMError(
                    "Input vector length does not match network input size.");
        }
    }

    /**
     * Initializes the neuron's weight matrix to all random doubles.
     */
    private void initialize() {
        random = new SecureRandom();
        for (int i = 0; i < neuronCount; i++) {
            for (int j = 0; j < inputVectorSize; j++) {
                weightMatrix[i][j] = random.nextDouble();
            }
        }
    }

    /**
     * Adjust the weights of a neuron to more closely match a given input vector.
     *
     * @param neuron The index of the neuron to adjust.
     * @param input The input vector to adjust towards.
     */
    protected void adjustNeuronWeights(int neuron, double[] input) {
        for (int i = 0; i < weightMatrix[neuron].length; i++) {
            double delta = input[i] - weightMatrix[neuron][i];
            weightMatrix[neuron][i] += learningRate() * delta;
        }
    }

    /**
     * Adjust the weights of all neurons in the neighborhood if a given neuron to
     * more closely match a given input vector.
     *
     * @param neuron The index of the neuron who's neighborhood will be examined.
     * @param input The input vector to adjust towards.
     */
    protected void adjustNeighborsOf(int neuron, double[] input) {
        for (int i = 0; i < neuronCount; i++) {
            if (i != neuron && inNeighborhoodOf(neuron, i)) {
                adjustNeuronWeights(i, input);
            }
        }
    }

    /**
     * Decide whether a neuron is in another neuron's neighborhood.  The
     * parameters imply a specific ordering, but in most cases this is an
     * unnecessary constraint.  This would only be the case if d(i, j) != d(j, i).
     * Asymmetric metrics are likely very uncommon, and it should be safe to
     * ignore this constraint.
     *
     * @param bestMatchingNeuron The winning neuron who's neighborhood we are
     * testing.
     * @param testNeuron The neuron we're testing for neighborhood inclusion.
     * @return true if the testNeuron is in the neighborhood of
     * bestMatchingNeuron, false otherwise
     */
    protected boolean inNeighborhoodOf(int bestMatchingNeuron, int testNeuron) {
        return neuronDistance(bestMatchingNeuron, testNeuron) < neighborhoodWidth();
    }

    /**
     * Get the width of the neighborhood of adjustment at a given iteration.
     * The iteration is taken from the object's current count.
     *
     * @return The width of the neighborhood of adjustment for the current
     * iteration.
     */
    protected double neighborhoodWidth() {
        return initialNeighborhoodWidth *
                (1.0 - (time / (double) expectedIterations));
    }

    /**
     * Calculate the distance between 2 neurons, given the map's layout.
     *
     * @param neuron0 The index of the first neuron.
     * @param neuron1 The index of the second neuron.
     * @return The distance between the two neurons.
     */
    protected abstract double neuronDistance(int neuron0, int neuron1);

    /**
     * Measure the distance from a neuron (specifically, its weight vector) to
     * an input vector.
     *
     * @param neuron The index of the neuron in question.
     * @param input The input vector.
     * @return The distance from the neuron to the vector.
     */
    public double distanceToInput(int neuron, double[] input) {
        double sum = 0.0;
        for (int i = 0; i < inputVectorSize; i++) {
            double difference = input[i] - weightMatrix[neuron][i];
            sum += difference * difference;
        }
        return sum;
    }

    @Override
    public String toString() {
        return "NetworkBase{" +
                "time=" + time +
                ", dimensions=" + gridSize +
                ", inputVectorSize=" + inputVectorSize +
                ", expectedIterations=" + expectedIterations +
                ", weightMatrix=" + weightString() +
                '}';
    }

    public void write(OutputStreamWriter destination) throws IOException {
        destination.write(String.format("Grid dimensions: %d, %d%n",
                gridSize.x, gridSize.y));
        destination.write(String.format("Input length: %d%n", inputVectorSize));
        destination.write(String.format("Weights:%n"));
        for (double[] doubles : weightMatrix) {
            destination.write(String.format("\t%s%n", Arrays.toString(doubles)));
        }
        destination.write(String.format("end weights%n"));
    }

}
