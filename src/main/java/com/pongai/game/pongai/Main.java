package com.pongai.game.pongai;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int WIDTH = 1000;  // um pouco maior para caber a grade 10x10
    public static final int HEIGHT = 800;

    private AnimationTimer timer;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pong IA — Coevolutivo 10x10 (GA + Backprop)");

        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ArenaGame game = new ArenaGame(gc, WIDTH, HEIGHT);

        // Toggle turbo (T) e pausar evolução (P) e refino por backprop (B)
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case T -> game.toggleTurbo();
                case P -> game.togglePause();
                case B -> game.toggleBackpropRefine();
            }
        });

        timer = new AnimationTimer() {
            private long last = 0;
            @Override
            public void handle(long now) {
                if (last == 0) { last = now; return; }
                double delta = (now - last) / 1_000_000_000.0;
                last = now;

                game.update(delta);
                game.render();
            }
        };
        timer.start();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        if (timer != null) timer.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
