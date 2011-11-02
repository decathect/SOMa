package cs437.som.demo;

import cs437.som.Dimension;
import cs437.som.SelfOrganizingMap;
import cs437.som.network.BasicSquareGridSOM;
import cs437.som.visualization.SOM2dPlotter;

import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;

/**
 * Demonstrates maps with 2-dimensional inputs with nearness grids and a
 * visualization during training.
 */
public class BasicMapDemo {
    private static final int iterDelay = 20; /* ms */
    private static final double tenByTenStep = 10.1;
    private static final double nearnessOffest = 0.5;
    private static final int MAPPING_LINE_WIDTH = 330;

    private SelfOrganizingMap som = null;
    private Logger logger = Logger.getLogger("BasicMapDemo");

    /**
     * Create a new SOM demo.
     * @param map An untrained SOM to watch.
     */
    public BasicMapDemo(SelfOrganizingMap map) {
        som = map;
    }

    /**
     * Logs a 10x10 of neurons matching grid points, train the SOM while using
     * a visualizer, log another 10x10 grid and then a 10x10 nearby grid.
     */
    public void run() {
        int iterations = som.getExpectedIterations();
        Random r = new SecureRandom();

        logger.info("Before Training");
        log10x10Map();

        SOM2dPlotter plot = new SOM2dPlotter(som);
        for (int i = 0; i < iterations; i++) {
            double[] in = {r.nextDouble() * 10, r.nextDouble() * 10};
            som.trainWith(in);
            plot.draw();
            try { Thread.sleep(iterDelay); } catch (InterruptedException ignored) { }
        }

        logger.info("After training");
        log10x10Map();

        logger.info("Nearby points");
        log10x10NearbyMap();
    }

    /**
     * Create a ten by ten grid of index neurons that are the best matching
     * neuron given an input matching the (x, y) of the position in the grid.
     * This is then printed to the object's logger with info severity.
     */
    private void log10x10Map() {
        String lineSep = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder(MAPPING_LINE_WIDTH);
        sb.append(lineSep);
        sb.append("  \t 1  2  3  4  5  6  7  8  9 10");

        for (double i = 1.0; i < tenByTenStep; i += 1.0) {
            sb.append(String.format(lineSep + "%2d\t", (int) Math.round(i)));
            for (double j = 1.0; j < tenByTenStep; j += 1.0) {
                sb.append(String.format("%2d ", som.getBestMatchingNeuron(new double[]{i, j})));
            }
        }
        logger.info(sb.toString());
    }

    /**
     * Create a ten by ten grid of neuron indexes that match an input near the
     * (x, y) of the position in the grid. This is then printed to the object's
     * logger with info severity.
     */
    private void log10x10NearbyMap() {
        String lineSep = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder(MAPPING_LINE_WIDTH);
        sb.append(lineSep);
        sb.append("  \t 1  2  3  4  5  6  7  8  9 10");

        Random r = new SecureRandom();
        for (double i = 1.0; i < tenByTenStep; i += 1.0) {
            sb.append(String.format(lineSep + "%2d\t", (int) Math.round(i)));
            for (double j = 1.0; j < tenByTenStep; j += 1.0) {
                sb.append(String.format("%2d ",
                        som.getBestMatchingNeuron(new double[]{i + r.nextDouble() - nearnessOffest,
                                j + r.nextDouble() - nearnessOffest})));
            }
        }
        logger.info(sb.toString());
    }

    public static void main(String[] args) {
        new BasicMapDemo(new BasicSquareGridSOM(new Dimension(7, 8), 2, 1000)).run();
    }

    @Override
    public String toString() {
        return "BasicMapDemo{som=" + som + '}';
    }
}