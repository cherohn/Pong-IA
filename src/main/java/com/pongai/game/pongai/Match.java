// java
// File: `src/main/java/com/pongai/game/pongai/Match.java`
package com.pongai.game.pongai;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Match {
    private final Genome leftG;
    private final Genome rightG;

    private final double offsetX, offsetY;
    private final double w, h;

    private final Ball ball;
    private final Paddle left, right;

    private int frames = 0;
    private static final double ROUND_START_DELAY = 0.7; // seconds to wait before ball moves

    // when false, rounds won't be reset on score; ball is bounced back into play instead
    private boolean allowReset = true;

    public Match(Genome leftGenome, Genome rightGenome, double offsetX, double offsetY, double width, double height) {
        this.leftG = leftGenome;
        this.rightG = rightGenome;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.w = width;
        this.h = height;

        // tamanhos proporcionais à célula
        double padW = Math.max(4, w * 0.02);
        double padH = Math.max(16, h * 0.18);

        this.ball = new Ball(w / 2.0, h / 2.0, w, h, Math.max(3, Math.min(w, h) * 0.04));

        // compute an adaptive paddle speed:
        double estimatedBallBase = Math.max(120, Math.min(w, h) * 0.6);
        double paddleSpeed = Math.max(estimatedBallBase * 1.05, Math.max(120, h * 1.2));

        this.left = new Paddle(padW * 2, h / 2.0 - padH / 2.0, null, leftG, padW, padH, paddleSpeed);
        this.right = new Paddle(w - padW * 3, h / 2.0 - padH / 2.0, null, rightG, padW, padH, paddleSpeed);
    }

    public void setAllowReset(boolean allow) { this.allowReset = allow; }

    public void update(double delta, int logicSteps) {
        for (int s = 0; s < logicSteps; s++) {
            ball.update(delta);
            left.updateAI(ball, delta, true, w, h);
            right.updateAI(ball, delta, false, w, h);

            // stronger movement reward and direction bonus (encourage chasing ball direction)
            double moveRewardBase = 0.0025;
            double dirBonus = 0.0;

            // reward paddles for moving (encourage activity) with higher base
            double moveRewardL = Math.abs(left.getVelY()) > 1 ? moveRewardBase * 3.0 : -0.0005;
            double moveRewardR = Math.abs(right.getVelY()) > 1 ? moveRewardBase * 3.0 : -0.0005;

            // reward when paddle moves in the same vertical direction as the ball
            double ballVelY = ball.getVelY();
            double dirThreshold = 20.0; // px/sec - require some vertical ball motion
            if (Math.abs(ballVelY) > dirThreshold) {
                if (Math.signum(left.getVelY()) == Math.signum(ballVelY) && Math.abs(left.getVelY()) > 1)
                    leftG.addFitness(0.015); // direct bonus for correct chasing direction
                if (Math.signum(right.getVelY()) == Math.signum(ballVelY) && Math.abs(right.getVelY()) > 1)
                    rightG.addFitness(0.015);
            }

            leftG.addFitness(0.002 + moveRewardL);
            rightG.addFitness(0.002 + moveRewardR);

            // bordas
            if (ball.getY() <= 0 || ball.getY() >= h - ball.getSize())
                ball.reverseY();

            // colisões
            if (ball.intersects(left.getX(), left.getY(), left.getWidth(), left.getHeight())) {
                ball.reverseX();
                leftG.addFitness(0.9);
            }
            if (ball.intersects(right.getX(), right.getY(), right.getWidth(), right.getHeight())) {
                ball.reverseX();
                rightG.addFitness(0.9);
            }

            // proximidade vertical — stronger shaping to push paddles to stay aligned with ball
            double distL = Math.abs(ball.getY() - (left.getY() + left.getHeight() / 2.0));
            double distR = Math.abs(ball.getY() - (right.getY() + right.getHeight() / 2.0));
            leftG.addFitness((80 - distL) * 0.00008);
            rightG.addFitness((80 - distR) * 0.00008);

            // fim de round se sair pela esquerda/direita
            if (ball.getX() < 0) {
                if (allowReset) {
                    rightG.addFitness(2.0);
                    leftG.addFitness(-1.0);
                    resetRound();
                } else {
                    // keep rallying: bounce ball back into play without resetting positions
                    ball.bounceHorizontalInside();
                }
            } else if (ball.getX() > w) {
                if (allowReset) {
                    leftG.addFitness(2.0);
                    rightG.addFitness(-1.0);
                    resetRound();
                } else {
                    ball.bounceHorizontalInside();
                }
            }

            frames++;
        }
    }

    private void resetRound() {
        ball.reset(w / 2.0, h / 2.0, ROUND_START_DELAY);
        left.resetY(h / 2.0 - left.getHeight() / 2.0);
        right.resetY(h / 2.0 - right.getHeight() / 2.0);
        frames = 0;
    }

    public void render(GraphicsContext gc) {
        // fundo da célula
        gc.setFill(Color.color(0.06, 0.06, 0.08));
        gc.fillRect(offsetX, offsetY, w, h);

        // linha central
        gc.setStroke(Color.color(1,1,1,0.2));
        for (double y = offsetY; y < offsetY + h; y += 8) {
            gc.strokeLine(offsetX + w / 2.0, y, offsetX + w / 2.0, y + 4);
        }

        // desenha objetos com deslocamento
        ball.render(gc, offsetX, offsetY);
        left.render(gc, offsetX, offsetY, Color.CORNFLOWERBLUE);
        right.render(gc, offsetX, offsetY, Color.ORANGERED);
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
}
