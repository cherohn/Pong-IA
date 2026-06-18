# Pong AI — Neural Network from Scratch in Java

A classic Pong game where one paddle is controlled by a feedforward neural network trained via backpropagation, implemented entirely without external ML libraries.

Built as a deep dive into how neural networks actually work under the hood — not as a framework exercise, but as a first-principles implementation of the learning algorithm itself.

---

## How It Works

The AI paddle receives the current game state as input and decides whether to move up, down, or stay. The network learns through repeated gameplay, adjusting its weights via backpropagation every time it makes a mistake.

**Network architecture:**

```
Input layer:   9 neurons  (ball position, velocity, paddle positions, distances)
Hidden layer: 14 neurons  (sigmoid activation)
Output layer:  1 neuron   (paddle movement direction)
```

**Training loop:**
1. Game state is fed into the network as a 9-value input vector
2. Forward propagation produces a movement decision
3. After each frame, the error is calculated against the expected output
4. Backpropagation adjusts all weights to reduce that error
5. The AI improves in real time as the game runs

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | JavaFX |
| Build | Maven |
| ML | Custom implementation (no external libraries) |

---

## Running Locally

**Requirements:** Java 17+, Maven 3.8+

```bash
# Clone
git clone https://github.com/cherohn/Pong-IA.git
cd Pong-IA

# Run
./mvnw clean compile exec:java        # Linux / macOS
mvnw.cmd clean compile exec:java      # Windows
```

---

## Project Structure

```
Pong-IA/
├── src/main/java/com/pongai/game/pongai/
│   ├── NeuralNetwork.java   # Feedforward net + backpropagation
│   ├── Game.java            # Game loop and training integration
│   ├── Paddle.java          # Paddle logic (human and AI)
│   ├── Ball.java            # Ball physics
│   └── ...
├── pom.xml
└── README.md
```

---

## What I Learned

- How backpropagation works mathematically, not just conceptually
- The relationship between learning rate, convergence speed, and instability
- Why weight initialization matters (random vs. zero)
- How to connect a real-time game loop to a training cycle without freezing the UI

---

## Authors

- **Matheus Garcez** — [github.com/cherohn](https://github.com/cherohn)
- **Erick801** — [github.com/Erick801](https://github.com/Erick801)
