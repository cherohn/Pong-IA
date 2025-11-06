package com.pongai.game.pongai;

import java.util.Random;

/** Feedforward NN 9-14-1 with simple backprop. */
public class NeuralNetwork {
    private final int inputSize = 9;
    private final int hiddenSize = 14;
    private final int outputSize = 1;

    private double[][] wIH;   // [in][hid]
    private double[]   bH;    // [hid]
    private double[][] wHO;   // [hid][out]
    private double[]   bO;    // [out]

    // buffers
    private double[] hidden;
    private final Random rng = new Random();

    public NeuralNetwork(double[] flat) {
        loadWeights(flat);
        hidden = new double[hiddenSize];
    }

    public double[] feedForward(double[] in) {
        if (in.length != inputSize) throw new IllegalArgumentException("inputs != " + inputSize);
        for (int i = 0; i < hiddenSize; i++) {
            double s = bH[i];
            for (int j = 0; j < inputSize; j++) s += in[j] * wIH[j][i];
            hidden[i] = sigmoid(s);
        }
        double[] out = new double[outputSize];
        for (int k = 0; k < outputSize; k++) {
            double s = bO[k];
            for (int i = 0; i < hiddenSize; i++) s += hidden[i] * wHO[i][k];
            out[k] = sigmoid(s);
        }
        return out;
    }

    /** Backprop single sample (target in [0,1]) */
    public void trainOne(double[] in, double target, double lr) {
        // forward
        double[] out = feedForward(in);

        // output error
        double[] errO = new double[outputSize];
        for (int k = 0; k < outputSize; k++) errO[k] = (target - out[k]);

        // output gradient
        double[] gradO = new double[outputSize];
        for (int k = 0; k < outputSize; k++) gradO[k] = errO[k] * out[k] * (1 - out[k]);

        // hidden error
        double[] errH = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            double s = 0;
            for (int k = 0; k < outputSize; k++) s += gradO[k] * wHO[i][k];
            errH[i] = s;
        }

        // hidden gradient
        double[] gradH = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) gradH[i] = errH[i] * hidden[i] * (1 - hidden[i]);

        // update wHO and bO
        for (int i = 0; i < hiddenSize; i++) {
            for (int k = 0; k < outputSize; k++) {
                wHO[i][k] += lr * gradO[k] * hidden[i];
            }
        }
        for (int k = 0; k < outputSize; k++) bO[k] += lr * gradO[k];

        // update wIH and bH
        for (int j = 0; j < inputSize; j++) {
            for (int i = 0; i < hiddenSize; i++) {
                wIH[j][i] += lr * gradH[i] * in[j];
            }
        }
        for (int i = 0; i < hiddenSize; i++) bH[i] += lr * gradH[i];
    }

    /**
     * Refine genome via lightweight synthetic backprop.
     * Generates physics-like samples (predict impact Y) and trains NN to move paddle center toward impact Y.
     */
    public static void refineGenomeWithBackprop(Genome g, int steps, double lr) {
        NeuralNetwork nn = new NeuralNetwork(g.getWeights());
        Random rnd = new Random();

        for (int s = 0; s < steps; s++) {
            // normalized arena
            double arenaW = 1.0;
            double arenaH = 1.0;

            // sample ball state
            double ballX = rnd.nextDouble();                      // [0,1]
            double ballY = rnd.nextDouble();                      // [0,1]
            double velX  = (rnd.nextDouble() * 2.0 - 1.0) * 0.9;  // [-0.9,0.9]
            double velY  = (rnd.nextDouble() * 2.0 - 1.0) * 0.8;  // [-0.8,0.8]

            // paddle assumed at right side
            double paddleX = 1.0;
            double paddleY = rnd.nextDouble();                    // [0,1]

            // time to impact (approx)
            double relX = paddleX - ballX;
            double tImpact = Math.abs(velX) > 1e-6 ? Math.abs(relX / velX) : 0.0;

            // predict future Y with vertical reflection
            double futureY = ballY + velY * tImpact;
            double arenaHMinus = arenaH; // normalized, ignoring ball size
            if (futureY < 0 || futureY > arenaHMinus) {
                double period = arenaHMinus * 2.0;
                double mod = ((futureY % period) + period) % period;
                futureY = mod > arenaHMinus ? period - mod : mod;
            }

            double distX = Math.abs(ballX - paddleX);
            double distY = ballY - paddleY;
            double velMag = Math.sqrt(velX * velX + velY * velY);
            double dirX = velX > 0 ? 1.0 : 0.0;
            double dirY = velY > 0 ? 1.0 : 0.0;

            double[] in = new double[] {
                    Utils.normalize(ballX, 0, arenaW),
                    Utils.normalize(ballY, 0, arenaH),
                    Utils.normalize(velX, -arenaW, arenaW),
                    Utils.normalize(velY, -arenaH, arenaH),
                    Utils.normalize(paddleY, 0, arenaH),
                    Utils.normalize(distX, 0, arenaW),
                    Utils.normalize(distY, -arenaH, arenaH),
                    Utils.normalize(futureY, 0, arenaH),
                    Utils.normalize(tImpact, 0, 2.0)
            };

            // target: move paddle center toward futureY
            double diff = futureY - paddleY;
            double target;
            if (Math.abs(diff) < 0.02) {
                target = 0.5; // stay
            } else {
                // FIXED: map ball above -> move up (0.0), ball below -> move down (1.0)
                target = diff < 0 ? 0.0 : 1.0;
            }

            double lrNoise = lr * (0.8 + 0.4 * rnd.nextDouble());
            nn.trainOne(in, target, lrNoise);
        }

        g.setWeights(nn.flattenWeights());
    }

    private void loadWeights(double[] flat) {
        int expected = getTotalWeightsCount();
        if (flat.length != expected) throw new IllegalArgumentException("weights length invalid: " + flat.length);

        int idx = 0;
        wIH = new double[inputSize][hiddenSize];
        for (int j = 0; j < inputSize; j++)
            for (int i = 0; i < hiddenSize; i++)
                wIH[j][i] = flat[idx++];

        bH = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) bH[i] = flat[idx++];

        wHO = new double[hiddenSize][outputSize];
        for (int i = 0; i < hiddenSize; i++)
            for (int k = 0; k < outputSize; k++)
                wHO[i][k] = flat[idx++];

        bO = new double[outputSize];
        for (int k = 0; k < outputSize; k++)
            bO[k] = flat[idx++];
    }

    public double[] flattenWeights() {
        int n = getTotalWeightsCount();
        double[] flat = new double[n];
        int idx = 0;
        for (int j = 0; j < inputSize; j++)
            for (int i = 0; i < hiddenSize; i++)
                flat[idx++] = wIH[j][i];
        for (int i = 0; i < hiddenSize; i++)
            flat[idx++] = bH[i];
        for (int i = 0; i < hiddenSize; i++)
            for (int k = 0; k < outputSize; k++)
                flat[idx++] = wHO[i][k];
        for (int k = 0; k < outputSize; k++)
            flat[idx++] = bO[k];
        return flat;
    }

    private double sigmoid(double x) { return 1.0 / (1.0 + Math.exp(-x)); }

    public static int getTotalWeightsCount() {
        int in=9, hid=14, out=1;
        return (in*hid) + hid + (hid*out) + out;
    }
}
