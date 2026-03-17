package game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private boolean running = true;
    private boolean gameOver = false;

    // Selected mode (null means we're still on the menu screen)
    private GameMode mode = null;

    // Single player bird (used in human mode)
    private Bird playerBird;
    private int playerScore = 0;
    private static int humanBestScore = 0; // Persistent across games

    // GA mode: many AI birds and a population
    private static final int POPULATION_SIZE = 40;
    private Population population;
    private java.util.List<AIBird> aiBirds = new java.util.ArrayList<>();
    private int generation = 1;
    private double bestEverFitness = 0.0;
    private int currentBestScore = 0;
    private static int aiBestScore = 0; // Persistent across AI games

    // Pipes
    private PipeManager pipeManager;

    // Menu button rectangles
    private Rectangle humanModeButton;
    private Rectangle aiModeButton;

    // Clouds
    private int cloudX1 = 0;
    private int cloudX2;
    private static final int cloudSpeed = 1;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
        setBackground(Color.cyan);
        setFocusable(true);

        pipeManager = new PipeManager();

        cloudX2 = Constants.WIDTH;

        // Initialize menu buttons
        int buttonWidth = 140;
        int buttonHeight = 60;
        int buttonY = Constants.HEIGHT / 2 - 20;
        humanModeButton = new Rectangle(Constants.WIDTH / 4 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight);
        aiModeButton = new Rectangle(3 * Constants.WIDTH / 4 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (mode == null) {
                    // Title screen – choose mode
                    switch (code) {
                        case KeyEvent.VK_1:
                        case KeyEvent.VK_P:
                            startHumanMode();
                            break;
                        case KeyEvent.VK_2:
                        case KeyEvent.VK_A:
                            startGAMode();
                            break;
                        default:
                            break;
                    }
                } else {
                    // In-game controls depend on mode
                    switch (mode) {
                        case HUMAN:
                            if (gameOver) {
                                // Game over options
                                switch (code) {
                                    case KeyEvent.VK_SPACE:
                                        resetGameHuman();
                                        break;
                                    case KeyEvent.VK_1:
                                    case KeyEvent.VK_H:
                                        startHumanMode();
                                        break;
                                    case KeyEvent.VK_2:
                                    case KeyEvent.VK_A:
                                        startGAMode();
                                        break;
                                    default:
                                        break;
                                }
                            } else if (code == KeyEvent.VK_SPACE) {
                                playerBird.jump();
                            }
                            break;
                        case AI:
                            if (code == KeyEvent.VK_SPACE) {
                                // restart evolution from scratch
                                startGAMode();
                            } else if (gameOver) {
                                // Allow switching back to menu
                                switch (code) {
                                    case KeyEvent.VK_1:
                                    case KeyEvent.VK_H:
                                        startHumanMode();
                                        break;
                                    case KeyEvent.VK_2:
                                    case KeyEvent.VK_A:
                                        startGAMode();
                                        break;
                                    case KeyEvent.VK_ESCAPE:
                                        mode = null;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        // Add mouse listener for menu buttons
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mode == null) {
                    Point click = e.getPoint();
                    if (humanModeButton.contains(click)) {
                        startHumanMode();
                    } else if (aiModeButton.contains(click)) {
                        startGAMode();
                    }
                }
            }
        });

        // Start with menu screen
        startGame();
    }

    private void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void startHumanMode() {
        mode = GameMode.HUMAN;
        gameOver = false;
        playerScore = 0;
        pipeManager.reset();
        playerBird = new Bird(100, 250);
    }

    private void startGAMode() {
        mode = GameMode.AI;
        gameOver = false;
        initGAMode();
    }

    private void resetGameHuman() {
        gameOver = false;
        if (playerScore > humanBestScore) {
            humanBestScore = playerScore;
        }
        playerScore = 0;
        playerBird.reset(100, 250);
        pipeManager.reset();
    }

    @Override
    public void run() {
        while (running) {
            updateGame();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                // Stop the loop cleanly if interrupted
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    private void updateGame() {
        if (mode == null) {
            // Menu screen: just move clouds for some life
            moveClouds();
        } else if (mode == GameMode.HUMAN) {
            updateHuman();
        } else {
            updateGA();
        }
    }

    private void updateHuman() {
        if (!gameOver) {
            playerBird.update();
            pipeManager.update();
            moveClouds();
            checkCollisionHuman();
            updatePlayerScore();
        }
    }

    private void updateGA() {
        if (population == null) {
            // Not initialized yet, just update pipes and clouds
            pipeManager.update();
            moveClouds();
            return;
        }
        
        pipeManager.update();
        moveClouds();
        updateAIScores();

        boolean allDead = true;
        for (AIBird ai : aiBirds) {
            if (ai.alive) {
                ai.update(pipeManager);
                checkCollisionForAIBird(ai);
            }
            if (ai.alive) {
                allDead = false;
            }
        }

        if (allDead) {
            nextGeneration();
        }
    }

    private void moveClouds() {
        cloudX1 -= cloudSpeed;
        cloudX2 -= cloudSpeed;
        if (cloudX1 + Constants.WIDTH < 0) cloudX1 = Constants.WIDTH;
        if (cloudX2 + Constants.WIDTH < 0) cloudX2 = Constants.WIDTH;
    }

    private void checkCollisionHuman() {
        if (playerBird.y + playerBird.size > Constants.HEIGHT - 50) {
            if (!gameOver) {
                gameOver = true;
                if (playerScore > humanBestScore) {
                    humanBestScore = playerScore;
                }
            }
            return;
        }

        Rectangle birdRect = new Rectangle(playerBird.x, playerBird.y, playerBird.size, playerBird.size);

        for (Pipe p : pipeManager.getPipes()) {
            Rectangle topPipe = new Rectangle(p.x, 0, p.pipeWidth, p.topHeight);
            
            // Calculate bottom pipe height correctly
            int groundHeight = 50;
            int bottomPipeY = p.topHeight + p.gap;
            int bottomPipeHeight = Constants.HEIGHT - groundHeight - bottomPipeY;
            
            // Only check bottom pipe collision if it exists
            if (bottomPipeHeight > 0) {
                Rectangle bottomPipe = new Rectangle(p.x, bottomPipeY, p.pipeWidth, bottomPipeHeight);
                if (birdRect.intersects(topPipe) || birdRect.intersects(bottomPipe)) {
                    if (!gameOver) {
                        gameOver = true;
                        if (playerScore > humanBestScore) {
                            humanBestScore = playerScore;
                        }
                    }
                    break;
                }
            } else if (birdRect.intersects(topPipe)) {
                if (!gameOver) {
                    gameOver = true;
                    if (playerScore > humanBestScore) {
                        humanBestScore = playerScore;
                    }
                }
                break;
            }
        }
    }

    private void checkCollisionForAIBird(AIBird ai) {
        Bird b = ai.bird;
        if (!ai.alive) return;

        if (b.y + b.size > Constants.HEIGHT - 50) {
            ai.kill();
        } else {
            Rectangle birdRect = new Rectangle(b.x, b.y, b.size, b.size);

            for (Pipe p : pipeManager.getPipes()) {
                Rectangle topPipe = new Rectangle(p.x, 0, p.pipeWidth, p.topHeight);
                
                // Calculate bottom pipe height correctly
                int groundHeight = 50;
                int bottomPipeY = p.topHeight + p.gap;
                int bottomPipeHeight = Constants.HEIGHT - groundHeight - bottomPipeY;
                
                // Only check bottom pipe collision if it exists
                if (bottomPipeHeight > 0) {
                    Rectangle bottomPipe = new Rectangle(p.x, bottomPipeY, p.pipeWidth, bottomPipeHeight);
                    if (birdRect.intersects(topPipe) || birdRect.intersects(bottomPipe)) {
                        ai.kill();
                        break;
                    }
                } else if (birdRect.intersects(topPipe)) {
                    ai.kill();
                    break;
                }
            }
        }
    }

    private void updatePlayerScore() {
        for (Pipe p : pipeManager.getPipes()) {
            // Check if bird has passed the pipe
            if (!p.passed && playerBird.x > p.x + p.pipeWidth) {
                p.passed = true;
                playerScore++;
                break; // Only count one pipe at a time
            }
        }
    }

    private void updateAIScores() {
        if (aiBirds.isEmpty()) return;
        int birdX = aiBirds.get(0).bird.x;
        
        for (Pipe p : pipeManager.getPipes()) {
            // Check if bird has passed the pipe
            if (!p.passed && birdX > p.x + p.pipeWidth) {
                p.passed = true;
                for (AIBird ai : aiBirds) {
                    if (ai.alive) {
                        ai.pipesPassed++;
                        if (ai.pipesPassed > currentBestScore) {
                            currentBestScore = ai.pipesPassed;
                        }
                    }
                }
                break; // Only count one pipe at a time
            }
        }
    }

    private void initGAMode() {
        pipeManager.reset();
        population = new Population(POPULATION_SIZE);
        aiBirds = new java.util.ArrayList<>();
        generation = 1;
        bestEverFitness = 0.0;
        currentBestScore = 0;

        for (Genome g : population.getGenomes()) {
            aiBirds.add(new AIBird(g.brain.copy(), 100, 250));
        }
    }

    private void nextGeneration() {
        // Update AI best score before evolution
        if (currentBestScore > aiBestScore) {
            aiBestScore = currentBestScore;
        }
        
        // copy fitness back into genomes
        Genome[] genomes = population.getGenomes();
        for (int i = 0; i < genomes.length && i < aiBirds.size(); i++) {
            genomes[i].fitness = aiBirds.get(i).fitness;
            if (genomes[i].fitness > bestEverFitness) {
                bestEverFitness = genomes[i].fitness;
            }
        }

        population.evolve();
        generation++;
        pipeManager.reset();
        currentBestScore = 0;

        aiBirds.clear();
        for (Genome g : population.getGenomes()) {
            aiBirds.add(new AIBird(g.brain.copy(), 100, 250));
        }
    }

    private AIBird getBestAIBird() {
        if (aiBirds == null || aiBirds.isEmpty()) return null;
        AIBird best = aiBirds.get(0);
        for (AIBird ai : aiBirds) {
            if (ai.fitness > best.fitness) {
                best = ai;
            }
        }
        return best;
    }

    private int getAliveBirdCount() {
        if (aiBirds == null) return 0;
        int count = 0;
        for (AIBird ai : aiBirds) {
            if (ai.alive) count++;
        }
        return count;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.cyan);
        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Clouds
        drawCloud(g, cloudX1, 60);
        drawCloud(g, cloudX2, 100);

        // If mode not yet chosen, show visual menu and return
        if (mode == null) {
            drawMenu(g);
            // Draw ground behind menu
            drawGround(g);
            return;
        }

        // Pipes (draw before ground so they appear above it)
        for (Pipe p : pipeManager.getPipes()) {
            p.draw(g);
        }

        // Ground (draw last so it appears on top)
        drawGround(g);

        if (mode == GameMode.HUMAN) {
            // Single bird
            playerBird.draw(g, true);

            // Score display
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + playerScore, 10, 25);
            g.drawString("Best: " + humanBestScore, 10, 45);

            // Game Over Text
            if (gameOver) {
                g.setColor(Color.red);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                g.drawString("GAME OVER", 70, Constants.HEIGHT / 2);
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Press SPACE to restart", 90, Constants.HEIGHT / 2 + 25);
                g.drawString("Press 1 for Human | 2 for AI", 85, Constants.HEIGHT / 2 + 45);
                g.drawString("Press ESC for menu", 105, Constants.HEIGHT / 2 + 65);
            }
        } else {
            // GA mode: draw all AI birds, highlight the best
            AIBird best = getBestAIBird();
            for (AIBird ai : aiBirds) {
                if (!ai.alive) continue;
                boolean highlight = (ai == best);
                ai.bird.draw(g, highlight);
            }

            // HUD: generation, scores and fitness
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Generation: " + generation, 10, 20);
            g.drawString("Current Best: " + currentBestScore, 10, 35);
            g.drawString("All-Time Best: " + aiBestScore, 10, 50);
            g.drawString("Best Fitness: " + (int) bestEverFitness, 10, 65);
            g.drawString("Alive: " + getAliveBirdCount(), 10, 80);
        }
    }

    private void drawCloud(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillOval(x + 30, y, 60, 40);
        g.fillOval(x, y + 10, 60, 40);
        g.fillOval(x + 60, y + 10, 60, 40);
        g.fillOval(x + 30, y + 20, 60, 40);
    }

    private void drawMenu(Graphics g) {
        // Draw welcome title
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        String welcome = "Welcome To Flappy Bird";
        int welcomeWidth = g.getFontMetrics().stringWidth(welcome);
        g.drawString(welcome, (Constants.WIDTH - welcomeWidth) / 2, 100);

        // Draw Human Mode button
        g.setColor(new Color(52, 152, 219)); // Nice blue
        g.fillRect(humanModeButton.x, humanModeButton.y, humanModeButton.width, humanModeButton.height);
        g.setColor(Color.white);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(humanModeButton.x, humanModeButton.y, humanModeButton.width, humanModeButton.height);
        
        // Human Mode text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String humanText = "Human Mode";
        int humanTextWidth = g.getFontMetrics().stringWidth(humanText);
        g.drawString(humanText, 
                    humanModeButton.x + (humanModeButton.width - humanTextWidth) / 2,
                    humanModeButton.y + humanModeButton.height / 2 + 6);

        // Draw AI Mode button
        g.setColor(new Color(46, 204, 113)); // Nice green
        g.fillRect(aiModeButton.x, aiModeButton.y, aiModeButton.width, aiModeButton.height);
        g.setColor(Color.white);
        g2d.drawRect(aiModeButton.x, aiModeButton.y, aiModeButton.width, aiModeButton.height);
        
        // AI Mode text
        g.setColor(Color.white);
        String aiText = "AI Mode";
        int aiTextWidth = g.getFontMetrics().stringWidth(aiText);
        g.drawString(aiText, 
                    aiModeButton.x + (aiModeButton.width - aiTextWidth) / 2,
                    aiModeButton.y + aiModeButton.height / 2 + 6);

        // Instructions
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String instruction1 = "Click a button or use keyboard:";
        String instruction2 = "1 or P = Human | 2 or A = AI";
        
        int instruction1Width = g.getFontMetrics().stringWidth(instruction1);
        int instruction2Width = g.getFontMetrics().stringWidth(instruction2);
        
        g.drawString(instruction1, (Constants.WIDTH - instruction1Width) / 2, 350);
        g.drawString(instruction2, (Constants.WIDTH - instruction2Width) / 2, 370);
    }

    private void drawGround(Graphics g) {
        int groundHeight = 50;
        int groundY = Constants.HEIGHT - groundHeight;
        
        // Main ground color (brownish)
        g.setColor(new Color(139, 90, 43)); // Saddle brown
        g.fillRect(0, groundY, Constants.WIDTH, groundHeight);
        
        // Add texture with darker lines
        g.setColor(new Color(101, 67, 33)); // Darker brown
        for (int i = 0; i < Constants.WIDTH; i += 20) {
            g.drawLine(i, groundY, i + 10, groundY + groundHeight);
        }
        
        // Add some grass texture on top
        g.setColor(new Color(34, 139, 34)); // Forest green
        for (int i = 0; i < Constants.WIDTH; i += 15) {
            g.drawLine(i, groundY, i + 5, groundY - 5);
            g.drawLine(i + 3, groundY, i + 8, groundY - 3);
        }
        
        // Add a darker edge line
        g.setColor(new Color(89, 59, 25)); // Very dark brown
        g.drawLine(0, groundY, Constants.WIDTH, groundY);
    }
}
