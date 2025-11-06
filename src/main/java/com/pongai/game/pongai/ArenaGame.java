package com.pongai.game.pongai;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ArenaGame {

    private final GraphicsContext gc;
    private final int width, height;

    private final CoevolutionArena arena;

    private boolean turbo = true;     // acelera a simulação
    private boolean paused = false;   // pausa evolução (continua render)
    private boolean doBackprop = true; // refino por backprop no fim da geração

    public ArenaGame(GraphicsContext gc, int width, int height) {
        this.gc = gc;
        this.width = width;
        this.height = height;
        this.arena = new CoevolutionArena(width, height);
    }

    public void update(double delta) {
        if (!paused) arena.update(delta, turbo, doBackprop);
    }

    public void render() {
        // fundo
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // desenha toda a grade de partidas
        arena.render(gc);

        // HUD
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));
        gc.fillText("Geração: " + arena.getGeneration(), 10, 20);
        gc.fillText("Turbo: " + (turbo ? "ON (T)" : "OFF (T)"), 10, 40);
        gc.fillText("Pausado: " + (paused ? "SIM (P)" : "NÃO (P)"), 10, 60);
        gc.fillText("Backprop: " + (doBackprop ? "ON (B)" : "OFF (B)"), 10, 80);
        gc.fillText("Melhor Esquerda (gen atual): " + String.format("%.2f", arena.getBestLeftFitness()), 10, 100);
        gc.fillText("Melhor Direita  (gen atual): " + String.format("%.2f", arena.getBestRightFitness()), 10, 120);
    }

    public void toggleTurbo() { turbo = !turbo; }
    public void togglePause() { paused = !paused; }
    public void toggleBackpropRefine() { doBackprop = !doBackprop; }
}
