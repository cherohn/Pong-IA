package com.pongai.game.pongai;

import java.util.*;

/**
 * Controla o processo evolutivo com estratégias para reduzir estagnação:
 * - adaptive mutation based on stagnation and fitness variance
 * - occasional random genomes injection
 * - mutated copies of bestEver for guided exploration
 * - returns a full next-generation list
 */
public class GeneticAlgorithm {

    private final Random random = new Random();
    private Genome bestEver; // Armazena o melhor genoma de todas as gerações
    private int stagnantGenerations = 0; // Gerações sem melhoria
    private double lastBestFitness = Double.NEGATIVE_INFINITY;

    public GeneticAlgorithm() {}

    public Genome createRandomGenome() {
        return new Genome();
    }

    public Genome getBestEver() {
        return bestEver;
    }

    /**
     * Produce a full next generation from the current population.
     * - pop must contain current genomes with fitness values set.
     * - returned genomes have fitness reset to 0.
     */
    public List<Genome> produceNextGeneration(List<Genome> pop, int popSize) {
        // sort by fitness desc
        pop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());

        // update global bestEver / stagnation tracking
        double currentBest = pop.isEmpty() ? Double.NEGATIVE_INFINITY : pop.get(0).getFitness();
        if (bestEver == null || currentBest > bestEver.getFitness()) {
            bestEver = pop.get(0).cloneGenome();
            stagnantGenerations = 0;
        } else {
            stagnantGenerations++;
        }

        // compute fitness variance (measure of diversity)
        double mean = pop.stream().mapToDouble(Genome::getFitness).average().orElse(0.0);
        double variance = pop.stream().mapToDouble(g -> Math.pow(g.getFitness() - mean, 2)).average().orElse(0.0);

        List<Genome> next = new ArrayList<>(popSize);

        // strong but limited elitism: keep top-2 (cloned)
        int elites = Math.min(2, pop.size());
        for (int i = 0; i < elites && next.size() < popSize; i++) {
            Genome e = pop.get(i).cloneGenome();
            e.setFitness(0);
            next.add(e);
        }

        // Parameters adaptive by stagnation / variance
        double baseRandomInjectProb = 0.06; // base chance to inject random genome
        double randomInjectProb = baseRandomInjectProb + (stagnantGenerations * 0.015) + (variance < 1.0 ? 0.03 : 0.0);
        double bestMutateProb = 0.08 + (stagnantGenerations * 0.02); // chance to inject mutated bestEver
        int tournamentSize = Math.min(Math.max(4, 3 + stagnantGenerations/3), Math.max(3, pop.size())); // larger tour if stagnating

        // fill remaining slots
        while (next.size() < popSize) {
            // occasional exploration: random genome
            if (random.nextDouble() < randomInjectProb) {
                Genome rndG = createRandomGenome();
                rndG.setFitness(0);
                next.add(rndG);
                continue;
            }

            // occasional mutated bestEver to nudge population
            if (bestEver != null && random.nextDouble() < bestMutateProb) {
                Genome b = bestEver.cloneGenome();
                // apply a stronger one-off mutation to create a variant
                double prevMR = b.getMutationRate();
                b.setMutationRate(Utils.clamp(prevMR * (1.0 + random.nextDouble()*0.8), 0.02, 0.8));
                b.mutate();
                b.setMutationRate(prevMR);
                b.setFitness(0);
                next.add(b);
                continue;
            }

            // normal reproduction: tournament selection, crossover, mutate
            Genome p1 = tournamentPick(pop, tournamentSize);
            Genome p2 = tournamentPick(pop, tournamentSize);

            Genome[] children = crossoverAndMutate(p1, p2, stagnantGenerations, variance);
            // prefer second child like before, but if only one, accept it
            Genome child = children.length > 1 ? children[1] : children[0];
            child.setFitness(0);
            next.add(child);
        }

        // keep global bestEver strongly (replace worst if necessary)
        if (bestEver != null) {
            // ensure it's present at least once: replace last slot if not similar fitness
            boolean containsBestLike = next.stream().anyMatch(g -> Math.abs(g.getFitness() - bestEver.getFitness()) < 1e-6);
            if (!containsBestLike) {
                Genome be = bestEver.cloneGenome();
                be.setFitness(0);
                next.set(next.size() - 1, be);
            }
        }

        // adjust mutation/adaptive counters: if pop improved reset, else continue
        if (currentBest > lastBestFitness + 1e-6) {
            lastBestFitness = currentBest;
            stagnantGenerations = 0;
        }

        return next;
    }

    // helper: tournament selection with specified tournament size
    private Genome tournamentPick(List<Genome> pop, int tourSize) {
        Genome best = null;
        for (int i = 0; i < tourSize; i++) {
            Genome g = pop.get(random.nextInt(pop.size()));
            if (best == null || g.getFitness() > best.getFitness()) best = g;
        }
        return best;
    }

    // helper: crossover + adaptive mutation
    private Genome[] crossoverAndMutate(Genome a, Genome b, int stagnant, double variance) {
        // choose best/second as in previous logic
        Genome best = a.getFitness() >= b.getFitness() ? a.cloneGenome() : b.cloneGenome();
        Genome second = a.getFitness() >= b.getFitness() ? b.cloneGenome() : a.cloneGenome();

        // optionally update global bestEver here too
        if (bestEver == null || best.getFitness() > bestEver.getFitness()) {
            bestEver = best.cloneGenome();
        }

        // crossover (existing static method)
        Genome child1 = Genome.crossover(best, second);
        Genome child2 = Genome.crossover(second, best);

        // adapt mutation intensity: more if stagnating or low variance (need exploration)
        double factor = 1.0;
        if (stagnant > 8) factor *= 1.6;
        if (variance < 0.5) factor *= 1.3;

        // apply mutation with scaled randomness
        scaleAndMutate(child1, factor);
        scaleAndMutate(child2, factor);

        // small chance to tweak mutation rates to adapt
        if (random.nextDouble() < 0.12) {
            child1.setMutationRate(Utils.clamp(child1.getMutationRate() + (random.nextDouble()*0.08 - 0.04), 0.02, 0.8));
            child2.setMutationRate(Utils.clamp(child2.getMutationRate() + (random.nextDouble()*0.08 - 0.04), 0.02, 0.8));
        }

        // return [elite, child] style for backward compatibility not required here
        return new Genome[]{child1, child2};
    }

    private void scaleAndMutate(Genome g, double factor) {
        // temporarily scale mutationRate
        double mr = g.getMutationRate();
        g.setMutationRate(Utils.clamp(mr * factor, 0.02, 0.9));
        g.mutate();
        // restore some of the rate (small random drift)
        g.setMutationRate(Utils.clamp(g.getMutationRate() * (0.92 + 0.16 * random.nextDouble()), 0.02, 0.9));
    }
}
