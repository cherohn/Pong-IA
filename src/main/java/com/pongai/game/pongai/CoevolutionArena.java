package com.pongai.game.pongai;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class CoevolutionArena {

    public static final int POP = 10;
    private static final int HOF_SIZE = 3;
    private static final double GENERATION_TIME = 30.0; // increased from 3.0 to 30s
    private static final int TURBO_UPDATES = 8;
    private static final int NORMAL_UPDATES = 2;

    private final int screenW, screenH;
    private double cellW, cellH;

    private final List<Genome> leftPop = new ArrayList<>();
    private final List<Genome> rightPop = new ArrayList<>();
    private final Deque<Genome> leftHof = new ArrayDeque<>();
    private final Deque<Genome> rightHof = new ArrayDeque<>();

    private final GeneticAlgorithm leftGA = new GeneticAlgorithm();
    private final GeneticAlgorithm rightGA = new GeneticAlgorithm();

    private Match[][] grid;
    private Match[] championMatches; // modo x1

    private int generation = 1;
    private double genTimer = 0.0;
    private boolean championMode = false;

    private final Random rng = new Random();

    public CoevolutionArena(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.cellW = screenW / (double) POP;
        this.cellH = screenH / (double) POP;

        for (int i = 0; i < POP; i++) leftPop.add(leftGA.createRandomGenome());
        for (int i = 0; i < POP; i++) rightPop.add(rightGA.createRandomGenome());

        rebuildGrid();
    }

    private void rebuildGrid() {
        grid = new Match[POP][POP];
        for (int r = 0; r < POP; r++) {
            for (int c = 0; c < POP; c++) {
                double ox = c * cellW;
                double oy = r * cellH;
                grid[r][c] = new Match(leftPop.get(r), rightPop.get(c), ox, oy, cellW, cellH);
            }
        }
    }

    /** entra no modo de campeões: apenas 2x2 melhores jogam 1x1 */
    private void enterChampionMode() {
        System.out.println("🏆 Entrando no modo X1 de campeões!");
        championMode = true;

        // ordena e pega top 2 de cada lado
        leftPop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());
        rightPop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());

        Genome left1 = leftPop.get(0);
        Genome left2 = leftPop.get(1);
        Genome right1 = rightPop.get(0);
        Genome right2 = rightPop.get(1);

        double halfH = screenH / 2.0;
        double matchH = halfH - 20;
        double matchW = screenW / 2.0 - 20;

        championMatches = new Match[]{
                new Match(left1, right1, 10, 10, matchW, matchH),
                new Match(left2, right2, 10, halfH + 10, matchW, matchH)
        };

        // disable resets so matches run continuously without centering the ball
        for (Match m : championMatches) m.setAllowReset(false);

        System.out.println("Champions chosen — Left: " + left1 + ", " + left2 + " | Right: " + right1 + ", " + right2);
    }

    // java
    public void update(double delta, boolean turbo, boolean doBackprop) {

        delta = Math.min(delta, 0.05);

        // If we're in champion mode, only update the champion matches (no evolution, no timer)
        if (championMode && championMatches != null) {
            int steps = turbo ? TURBO_UPDATES : NORMAL_UPDATES;
            for (Match m : championMatches) {
                m.update(delta, steps);
            }
            return; // stop here so evolution / genTimer don't run — champion mode is infinite
        }

        int steps = turbo ? TURBO_UPDATES : NORMAL_UPDATES;
        for (int r = 0; r < POP; r++)
            for (int c = 0; c < POP; c++)
                grid[r][c].update(delta, steps);

        genTimer += delta * steps;

        if (genTimer >= GENERATION_TIME) {
            if (doBackprop) refineTopWithBackprop();

            evolveSide(leftPop, leftGA);
            evolveSide(rightPop, rightGA);

            updateHallOfFame(leftPop, leftHof);
            updateHallOfFame(rightPop, rightHof);

            generation++;
            // entra no modo campeões na geração 200
            if (generation == 30 && !championMode)
                enterChampionMode();

            leftPop.forEach(g -> g.setFitness(0));
            rightPop.forEach(g -> g.setFitness(0));

            rebuildGrid();

            genTimer = 0.0;
        }
    }


    public void render(GraphicsContext gc) {
        if (championMode) {
            renderChampionMode(gc);
            return;
        }

        for (int r = 0; r < POP; r++) {
            for (int c = 0; c < POP; c++) {
                grid[r][c].render(gc);
                gc.setStroke(Color.color(1, 1, 1, 0.08));
                gc.strokeRect(grid[r][c].getOffsetX(), grid[r][c].getOffsetY(), cellW, cellH);
            }
        }
    }

    private void renderChampionMode(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, screenW, screenH);

        gc.setFill(Color.WHITE);
        gc.fillText("🏆 MODO CAMPEÕES — Geração " + generation, 20, 30);
        gc.fillText("Apenas os 2 melhores de cada lado se enfrentam!", 20, 50);

        for (int i = 0; i < championMatches.length; i++) {
            championMatches[i].render(gc);
            double offsetY = championMatches[i].getOffsetY();
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("Partida " + (i + 1), screenW / 2.0 - 80, offsetY + 20);
        }
    }

    /**
     * Evolve a side using provided GeneticAlgorithm for child generation.
     * Keeps top-2 elites and injects the GA's bestEver into position 0 to preserve global improvement.
     */
    // java
    private void evolveSide(List<Genome> pop, GeneticAlgorithm ga) {
        List<Genome> next = ga.produceNextGeneration(pop, POP);
        pop.clear();
        pop.addAll(next);
    }


    // stronger tournament (larger sample) to increase selection pressure
    private Genome tournamentPick(List<Genome> pop) {
        Genome best = null;
        for (int i = 0; i < 5; i++) { // increased from 3 to 5
            Genome g = pop.get(rng.nextInt(pop.size()));
            if (best == null || g.getFitness() > best.getFitness()) best = g;
        }
        return best;
    }

    private void updateHallOfFame(List<Genome> pop, Deque<Genome> hof) {
        pop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());
        for (int i = 0; i < Math.min(HOF_SIZE, pop.size()); i++) {
            if (hof.size() >= HOF_SIZE) hof.removeLast();
            hof.addFirst(pop.get(i).cloneGenome());
        }
    }

    private void refineTopWithBackprop() {
        int k = Math.min(2, POP);
        leftPop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());
        rightPop.sort(Comparator.comparingDouble(Genome::getFitness).reversed());

        for (int i = 0; i < k; i++) {
            NeuralNetwork.refineGenomeWithBackprop(leftPop.get(i), 80, 0.05);
            NeuralNetwork.refineGenomeWithBackprop(rightPop.get(i), 80, 0.05);
        }
    }

    public int getGeneration() { return generation; }
    public double getBestLeftFitness() { return leftPop.stream().mapToDouble(Genome::getFitness).max().orElse(0); }
    public double getBestRightFitness() { return rightPop.stream().mapToDouble(Genome::getFitness).max().orElse(0); }
}
