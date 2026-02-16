# 🏓 Pong AI – Neural Network From Scratch in Java

A recreation of the classic Pong game featuring a fully implemented neural network that controls one of the paddles.

This project was developed to explore artificial intelligence concepts by implementing a feedforward neural network trained with backpropagation — entirely from scratch, without external ML libraries.

---

## 🎮 Project Overview

Pong AI is a Java-based application where an artificial neural network learns to control a paddle in a 2D Pong environment.

The objective was to build and train a neural network capable of making real-time decisions based on the ball's movement and game state.

Unlike typical AI projects that rely on external frameworks, this implementation focuses on understanding and constructing the learning algorithm manually.

---

## 🧠 Neural Network Architecture

The AI is based on a fully connected feedforward neural network with backpropagation for weight adjustment.

Architecture:

9 input neurons

14 hidden neurons

1 output neuron

Key characteristics:

Random weight initialization

Forward propagation for prediction

Backpropagation for error correction

Continuous performance improvement during gameplay

The output neuron determines the paddle’s vertical movement in response to the ball’s trajectory.
---


## ⚙️ Technologies Used

- **Java**
- **Maven** (para build e execução)
- **JavaFX** (para interface gráfica)
- **Custom Neural Network Implementation** (Feedforward + Backpropagation)

---

## 🚀 Como Executar

Running the Project

Clone the repository:

git clone https://github.com/cherohn/Pong-IA.git
cd Pong-IA

Run with Maven:
On Linux / macOS
./mvnw clean compile exec:java

On Windows
mvnw.cmd clean compile exec:java

---

## 📂 Estrutura do Projeto

```text
Pong-IA/
├─ src/
│  └─ main/
│     └─ java/
│        └─ com/
│           └─ pongai/
│              └─ game/
│                 └─ pongai/
│                    ├─ Game.java
│                    ├─ NeuralNetwork.java
│                    ├─ Paddle.java
│                    ├─ Ball.java
│                    └─ ...
├─ pom.xml
├─ mvnw
├─ mvnw.cmd
└─ README.md
```
---

🧩 Learning Goals

This project was built to:

Understand neural network fundamentals

Implement backpropagation manually

Apply AI concepts to a real-time system

Strengthen object-oriented design skills in Java

## 👨‍💻 Authors

**cherohn**  
📎 [github.com/cherohn](https://github.com/cherohn)

**Erick801**  
📎 [github.com/Erick801](https://github.com/Erick801)


## 🪪 License

This project is open-source and intended for educational purposes.
Feel free to explore, modify, and experiment with the implementation.
