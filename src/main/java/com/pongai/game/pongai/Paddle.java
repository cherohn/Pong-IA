package com.pongai.game.pongai;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Paddle AI: predictive-chase logic — only chase when ball is coming to this side.
 */
public class Paddle {
    private final double width;
    private final double height;
    private double x, y;
    private final double speed; // px/sec

    private Genome genome;
    private NeuralNetwork nn;

    public Paddle(double x, double y, Color colorIgnored, Genome genome, double width, double height, double speed) {
        this.x = x; this.y = y;
        this.genome = genome;
        this.nn = new NeuralNetwork(genome.getWeights());
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    /**
     * Predictive chase:
     * - compute time until ball reaches paddle front (signed)
     * - if ball will arrive soon and is moving toward paddle -> chase predicted Y
     * - otherwise return to center (hold position)
     */
    public void updateAI(Ball ball, double delta, boolean isLeft, double arenaW, double arenaH) {
        // paddle front X (where collision happens)
        double paddleFrontX = isLeft ? (x + width) : x;
        double velX = ball.getVelX();
        double relX = paddleFrontX - ball.getX(); // positive if paddle is to the right of ball

        double tImpact = Double.POSITIVE_INFINITY;
        if (Math.abs(velX) > 1e-6) {
            tImpact = relX / velX; // signed time: >0 means ball will reach paddleFrontX in future
        }

        // Predict future Y with vertical reflections if impact is in future and within reasonable horizon
        double futureY = ball.getY();
        boolean willArrive = tImpact > 0 && tImpact < 3.0; // only consider arrivals within 3s
        if (willArrive) {
            futureY = ball.getY() + ball.getVelY() * tImpact;
            double arenaHMinus = arenaH - ball.getSize();
            if (futureY < 0 || futureY > arenaHMinus) {
                double period = arenaHMinus * 2.0;
                double mod = ((futureY % period) + period) % period;
                futureY = mod > arenaHMinus ? period - mod : mod;
            }
        }

        // Determine target: if ball is coming -> aim at futureY center; else go to middle
        double targetCenterY = (arenaH - height) / 2.0;
        double targetY = willArrive ? Utils.clamp(futureY - height / 2.0, 0, arenaH - height) : targetCenterY;

        // Compute desired movement toward target with aggression when chasing
        double dy = targetY - y;
        double norm = Math.abs(dy) / Math.max(1.0, arenaH); // 0..1
        double speedMultiplier = willArrive ? 2.0 : 0.9; // fast when chasing
        double desiredVel = Math.signum(dy) * speed * speedMultiplier * norm;

        // ensure a minimum responsive velocity when chasing
        if (willArrive && Math.abs(desiredVel) < speed * 0.35) {
            desiredVel = Math.signum(dy) * speed * 0.35;
        }

        // apply velocity and clamp position
        velY = desiredVel;
        y += velY * delta;
        y = Utils.clamp(y, 0, arenaH - height);
    }

    private double velY = 0;
    public double getVelY() { return velY; }

    public void render(GraphicsContext gc, double ox, double oy, Color color) {
        gc.setFill(color);
        gc.fillRect(ox + x, oy + y, width, height);
    }

    public void resetY(double newY) { this.y = newY; }

    // getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public Genome getGenome() { return genome; }
    public void setGenome(Genome g) { this.genome = g; this.nn = new NeuralNetwork(g.getWeights()); }
}
