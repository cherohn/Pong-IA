package com.pongai.game.pongai;

import java.util.Random;

public class Utils {
    private static final Random rng = new Random();

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static double normalize(double v, double min, double max) {
        if (max - min == 0) return 0;
        return (v - min) / (max - min);
    }

    public static double randRange(double a, double b) {
        return a + (b - a) * rng.nextDouble();
    }
}
