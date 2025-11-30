package game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private boolean running = true;
    private boolean gameOver = false;

    // Clouds
    private int cloudX1 = 0;
    private int cloudX2;
    private int cloudSpeed = 1;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
        setBackground(Color.cyan);
        setFocusable(true);

        cloudX2 = Constants.WIDTH;

        // Controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (gameOver) resetGame();
                    else Bird.jump();
                }
            }
        });

        startGame();
    }

    private void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void resetGame() {
        gameOver = false;
        Bird.y = 250;
        Bird.velocity = 0;
        Pipe.reset();
    }

    @Override
    public void run() {
        while (running) {
            updateGame();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        if (!gameOver) {
            Bird.update();
            Pipe.update();
            moveClouds();
            checkCollision();
        }
    }

    private void moveClouds() {
        cloudX1 -= cloudSpeed;
        cloudX2 -= cloudSpeed;
        if (cloudX1 + Constants.WIDTH < 0) cloudX1 = Constants.WIDTH;
        if (cloudX2 + Constants.WIDTH < 0) cloudX2 = Constants.WIDTH;
    }

    private void checkCollision() {
        if (Bird.y + Bird.size > Constants.HEIGHT - 50) gameOver = true;

        Rectangle birdRect = new Rectangle(Bird.x, Bird.y, Bird.size, Bird.size);
        Rectangle topPipe = new Rectangle(Pipe.x, 0, Pipe.pipeWidth, Pipe.topHeight);
        Rectangle bottomPipe = new Rectangle(Pipe.x, Pipe.topHeight + Pipe.gap, Pipe.pipeWidth,
                Constants.HEIGHT - (Pipe.topHeight + Pipe.gap));

        if (birdRect.intersects(topPipe) || birdRect.intersects(bottomPipe)) gameOver = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.cyan);
        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Clouds
        drawCloud(g, cloudX1, 60);
        drawCloud(g, cloudX2, 100);

        // Ground
        g.setColor(Color.orange);
        g.fillRect(0, Constants.HEIGHT - 50, Constants.WIDTH, 50);

        // Bird + Pipes
        Bird.draw(g);
        Pipe.draw(g);

        // Game Over Text
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", 100, Constants.HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to restart", 130, Constants.HEIGHT / 2 + 40);
        }
    }

    private void drawCloud(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillOval(x + 30, y, 60, 40);
        g.fillOval(x, y + 10, 60, 40);
        g.fillOval(x + 60, y + 10, 60, 40);
        g.fillOval(x + 30, y + 20, 60, 40);
    }
}
