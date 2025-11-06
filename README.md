# 🏓 Pong-IA

Este projeto é uma recriação do clássico jogo **Pong**, com um diferencial: uma **inteligência artificial** controla um dos jogadores.  
O objetivo foi desenvolver um sistema simples em Java capaz de simular aprendizado e tomada de decisão dentro do jogo.

---

## 🎮 Sobre o Projeto

O Pong-IA foi desenvolvido com foco em **estudo e experimentação de algoritmos de IA** aplicados a jogos.  
A ideia é que a máquina consiga reagir ao movimento da bola, aprendendo a se posicionar corretamente para interceptá-la.

O código foi escrito totalmente em **Java**, utilizando conceitos básicos de **redes neurais feedforward** com **backpropagation**.  
A IA passa por um processo de treino e, conforme evolui, melhora suas respostas durante as partidas.

---

## ⚙️ Tecnologias Utilizadas

- **Java**
- **Maven** (para build e execução)
- **JavaFX** (para interface gráfica)
- **Lógica de redes neurais artificiais** (Feedforward + Backpropagation)

---

## 🚀 Como Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/cherohn/Pong-IA.git

   git clone https://github.com/cherohn/Pong-IA.git
   cd Pong-IA
   ./mvnw clean compile exec:java
   mvnw.cmd clean compile exec:java
    ```
   
## 🧠 Estrutura da IA

A inteligência artificial foi implementada a partir de uma **rede neural simples** com:

- **9 neurônios de entrada**
- **14 neurônios na camada oculta**
- **1 neurônio de saída**

Ela utiliza pesos aleatórios no início e ajusta os valores por meio do **algoritmo de backpropagation**, buscando melhorar seu desempenho ao longo do tempo.

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

## 👨‍💻 Autor

Desenvolvido por **cherohn**  e **Erick801**
📎 [github.com/cherohn](https://github.com/cherohn)
📎 [github.com/Erick801](https://github.com/Erick801)

---

## 🪪 Licença

Este projeto é de código aberto e pode ser utilizado para fins educacionais ou de estudo.  
Sinta-se livre para clonar, modificar e aprender com ele.
