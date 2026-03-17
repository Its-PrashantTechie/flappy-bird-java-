package game;

import java.util.Random;

/**
 * Simple feed-forward neural network used as the "brain" for the AI bird.
 * Architecture: 6 inputs -> 8 hidden units -> 1 output (jump probability).
 */
public class NeuralNetwork {

    private static final int INPUTS = 6;
    private static final int HIDDEN = 8;

    double[][] w1 = new double[HIDDEN][INPUTS];
    double[] b1 = new double[HIDDEN];
    double[] w2 = new double[HIDDEN];
    double b2;

    private static final Random rand = new Random();

    public NeuralNetwork() {
        // Initialize weights and biases separately to avoid constructor call issues
        w1 = new double[HIDDEN][INPUTS];
        b1 = new double[HIDDEN];
        w2 = new double[HIDDEN];
        b2 = 0;
        randomize();
    }

    public void randomize() {
        for (int i = 0; i < HIDDEN; i++) {
            for (int j = 0; j < INPUTS; j++) {
                w1[i][j] = rand.nextGaussian() * 0.5;
            }
            b1[i] = rand.nextGaussian() * 0.5;
            w2[i] = rand.nextGaussian() * 0.5;
        }
        b2 = rand.nextGaussian() * 0.5;
    }

    public NeuralNetwork copy() {
        NeuralNetwork nn = new NeuralNetwork();
        for (int i = 0; i < HIDDEN; i++) {
            System.arraycopy(this.w1[i], 0, nn.w1[i], 0, INPUTS);
        }
        System.arraycopy(this.b1, 0, nn.b1, 0, HIDDEN);
        System.arraycopy(this.w2, 0, nn.w2, 0, HIDDEN);
        nn.b2 = this.b2;
        return nn;
    }

    public void mutate(double rate, double magnitude) {
        for (int i = 0; i < HIDDEN; i++) {
            for (int j = 0; j < INPUTS; j++) {
                if (rand.nextDouble() < rate) {
                    w1[i][j] += rand.nextGaussian() * magnitude;
                }
            }
            if (rand.nextDouble() < rate) {
                b1[i] += rand.nextGaussian() * magnitude;
            }
            if (rand.nextDouble() < rate) {
                w2[i] += rand.nextGaussian() * magnitude;
            }
        }
        if (rand.nextDouble() < rate) {
            b2 += rand.nextGaussian() * magnitude;
        }
    }

    /**
     * Forward pass. Input must have length 6.
     *
     * @param input normalized game state
     * @return probability (0..1) that the bird should jump
     */
    public double forward(double[] input) {
        double[] hidden = new double[HIDDEN];
        for (int i = 0; i < HIDDEN; i++) {
            double sum = b1[i];
            for (int j = 0; j < INPUTS; j++) {
                sum += w1[i][j] * input[j];
            }
            hidden[i] = Math.tanh(sum);
        }

        double out = b2;
        for (int i = 0; i < HIDDEN; i++) {
            out += w2[i] * hidden[i];
        }
        return 1.0 / (1.0 + Math.exp(-out));
    }
}

