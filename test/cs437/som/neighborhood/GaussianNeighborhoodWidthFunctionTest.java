package cs437.som.neighborhood;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GaussianNeighborhoodWidthFunctionTest {
    private static final int ITERATIONS = 1000;
    private static final double STANDARD_DEVIATION = 100.0;
    private static final int[] SAMPLES = {0, 1, 10, 100, 200, 500, 1000};
    private static final double[] SAMPLE_RESULTS =
            {0.00398942, 0.00398922, 0.00396953, 0.00241971, 0.00053991,
                    1.48672e-8, 7.6946e-25};
    private static final double[] SAMPLE_ACCURACY =
            {1.0e-8, 1.0e-8, 1.0e-8, 1.0e-8, 1.0e-8, 1.0e-13, 1.0e-29};

    private GaussianNeighborhoodWidthFunction gnwf;

    @BeforeTest
    public void setUp() {
        gnwf = new GaussianNeighborhoodWidthFunction(STANDARD_DEVIATION);
        gnwf.setExpectedIterations(ITERATIONS);
    }

    @Test
    public void testConsistentlyDecreasing() throws Exception {
        double last = gnwf.neighborhoodWidth(0);
        for (int i = 1; i < ITERATIONS; i++) {
            double current = gnwf.neighborhoodWidth(i);
            assertTrue(current <= last, "Must consistently decrease.");
            last = current;
        }
    }

    @Test
    public void testBySampling() {
        for (int j = 0; j < SAMPLES.length; j++) {
            assertEquals(gnwf.neighborhoodWidth(SAMPLES[j]), SAMPLE_RESULTS[j],
                    SAMPLE_ACCURACY[j]);
        }
    }
}
