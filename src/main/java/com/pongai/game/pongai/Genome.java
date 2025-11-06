package com.pongai.game.pongai;

import java.util.Arrays;
import java.util.Random;

public class Genome {
    private double[] weights;
    private double fitness = 0.0;
    private double mutationRate = 0.12;
    private static final Random rng = new Random();

    public Genome() {
        weights = new double[NeuralNetwork.getTotalWeightsCount()];
        randomize();
    }

    public Genome(double[] w) {
        int expected = NeuralNetwork.getTotalWeightsCount();
        if (w.length != expected) {
            // recria com novo tamanho
            weights = new double[expected];
            randomize();
        } else {
            this.weights = Arrays.copyOf(w, w.length);
        }
    }


    private void randomize() {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rng.nextDouble()*2 - 1;
        }
    }

    public double[] getWeights() { return weights; }
    public void setWeights(double[] w) { this.weights = Arrays.copyOf(w, w.length); }

    public double getFitness() { return fitness; }
    public void setFitness(double f) { this.fitness = f; }
    public void addFitness(double f) { this.fitness += f; }

    public double getMutationRate() { return mutationRate; }
    public void setMutationRate(double r) { this.mutationRate = r; }

    public Genome cloneGenome() {
        Genome g = new Genome(weights);
        g.fitness = this.fitness;
        g.mutationRate = this.mutationRate;
        return g;
    }

    public static Genome crossover(Genome a, Genome b) {
        double[] wa = a.getWeights(), wb = b.getWeights();
        double[] wc = new double[wa.length];
        for (int i = 0; i < wc.length; i++) {
            // corte único
            wc[i] = (rng.nextBoolean()) ? wa[i] : wb[i];
        }
        Genome child = new Genome(wc);
        child.mutationRate = (a.mutationRate + b.mutationRate) / 2.0;
        return child;
    }

    public void mutate() {
        double rate = mutationRate;
        if (fitness < 0) rate *= 1.4;
        if (fitness > 30) rate *= 0.7;

        for (int i = 0; i < weights.length; i++) {
            if (rng.nextDouble() < rate) {
                weights[i] += (rng.nextDouble()*2 - 1) * 0.45;
                weights[i] = Utils.clamp(weights[i], -1, 1);
            }
        }
        // chance pequena de mutação brusca
        if (rng.nextDouble() < 0.01) {
            int j = rng.nextInt(weights.length);
            weights[j] = rng.nextDouble()*2 - 1;
        }
        // evolui taxa
        if (rng.nextDouble() < 0.08) {
            mutationRate = Utils.clamp(mutationRate + (rng.nextDouble()*0.1 - 0.05), 0.02, 0.6);
        }
    }

    @Override
    public String toString() {
        return "Genome{fitness=" + String.format("%.2f", fitness) + ", mr=" + String.format("%.2f", mutationRate) + "}";
    }
}
