package com.pongai.game.pongai;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class Ball {
    private double x, y;
    private double velX, velY;
    private double pendingVelX, pendingVelY;
    private double startDelayRemaining = 0.0;
    private final double size;
    private final double w, h; // limites locais
    private final double baseSpeed;
    private final Random random = new Random();

    public Ball(double startX, double startY, double arenaW, double arenaH, double size) {
        this.w = arenaW;
        this.h = arenaH;
        this.size = size;
        this.baseSpeed = Math.max(120, Math.min(w, h) * 0.6);
        reset(startX, startY); // default: no extra delay
    }

    public void update(double delta) {
        // if there's a pending start delay, countdown and only start movement when it hits zero
        if (startDelayRemaining > 0.0) {
            startDelayRemaining -= delta;
            if (startDelayRemaining <= 0.0) {
                // activate pending velocity
                velX = pendingVelX;
                velY = pendingVelY;
            } else {
                return; // still waiting; don't move
            }
        }

        x += velX * delta;
        y += velY * delta;
    }

    public void render(GraphicsContext gc, double ox, double oy) {
        gc.setFill(Color.WHITE);
        gc.fillOval(ox + x, oy + y, size, size);
    }

    public void reverseX() { velX = -velX; }
    public void reverseY() { velY = -velY; }

    /**
     * When championship mode disables resets we want the ball to stay in play.
     * This method nudges the ball back inside the horizontal bounds and reverses its X velocity.
     */
    public void bounceHorizontalInside() {
        if (x < 0) {
            x = 0;
            velX = Math.abs(velX); // ensure moving right
        } else if (x > w - size) {
            x = w - size;
            velX = -Math.abs(velX); // ensure moving left
        }
    }

    // default reset (no delay)
    public void reset(double startX, double startY) {
        reset(startX, startY, 0.0);
    }

    // reset with configurable delay (seconds). while delayed the ball stays in place
    public void reset(double startX, double startY, double delaySeconds) {
        x = startX;
        y = startY;
        startDelayRemaining = Math.max(0.0, delaySeconds);

        // pick the eventual velocity but don't apply until delay expires
        double dir = random.nextBoolean() ? 1.0 : -1.0;
        pendingVelX = dir * baseSpeed;
        pendingVelY = (random.nextDouble() * 2 - 1) * baseSpeed * 0.6;

        if (startDelayRemaining > 0.0) {
            velX = 0.0;
            velY = 0.0;
        } else {
            velX = pendingVelX;
            velY = pendingVelY;
        }
    }

    public boolean intersects(double rx, double ry, double rw, double rh) {
        return x < rx + rw && x + size > rx && y < ry + rh && y + size > ry;
    }

    // getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelX() { return velX; }
    public double getVelY() { return velY; }
    public double getSize() { return size; }
    public double getArenaW() { return w; }
    public double getArenaH() { return h; }
}
