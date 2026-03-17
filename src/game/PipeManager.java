package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages scrolling pipes for both human and GA modes.
 */
public class PipeManager {

    private static final int PIPE_WIDTH = 80;
    private static final int GAP = 150;
    private static final int SPEED = 3;
    private static final int PIPE_SPACING = 220;
    private static final int PIPE_COUNT = 3;

    private final List<Pipe> pipes;
    private final Random rand = new Random();

    public PipeManager() {
        // Initialize pipes separately to avoid constructor call issues
        pipes = new ArrayList<>();
        reset();
    }

    public void reset() {
        pipes.clear();
        int startX = Constants.WIDTH + 100;
        for (int i = 0; i < PIPE_COUNT; i++) {
            int x = startX + i * PIPE_SPACING;
            pipes.add(createRandomPipeAt(x));
        }
    }

    private Pipe createRandomPipeAt(int x) {
        // Ensure top pipe leaves enough room for bottom pipe
        int maxTopHeight = Constants.HEIGHT - GAP - 100; // Leave room for bottom pipe
        int minTopHeight = 50;
        int topHeight = rand.nextInt(maxTopHeight - minTopHeight) + minTopHeight;
        return new Pipe(x, PIPE_WIDTH, GAP, topHeight);
    }

    public void update() {
        for (Pipe pipe : pipes) {
            pipe.x -= SPEED;
        }

        // recycle pipes that went off-screen
        for (int i = 0; i < pipes.size(); i++) {
            Pipe p = pipes.get(i);
            if (p.x + p.pipeWidth < 0) {
                // find rightmost pipe
                int maxX = 0;
                for (Pipe other : pipes) {
                    if (other.x > maxX) maxX = other.x;
                }
                p.x = maxX + PIPE_SPACING;
                int topHeight = rand.nextInt(200) + 100;
                p.topHeight = topHeight;
                p.passed = false;
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.green);
        for (Pipe p : pipes) {
            g.fillRect(p.x, 0, p.pipeWidth, p.topHeight);
            g.fillRect(p.x, p.topHeight + p.gap, p.pipeWidth,
                    Constants.HEIGHT - (p.topHeight + p.gap));
        }
    }

    /**
     * Returns the next pipe that is in front of (or at) the given x position.
     */
    public Pipe getNextPipeForX(int birdX) {
        Pipe best = null;
        int bestX = Integer.MAX_VALUE;
        for (Pipe p : pipes) {
            if (p.x + p.pipeWidth >= birdX && p.x < bestX) {
                best = p;
                bestX = p.x;
            }
        }
        // if all pipes are behind the bird, just return the leftmost one
        if (best == null && !pipes.isEmpty()) {
            best = pipes.get(0);
            for (Pipe p : pipes) {
                if (p.x < best.x) {
                    best = p;
                }
            }
        }
        return best;
    }

    public java.util.List<Pipe> getPipes() {
        return pipes;
    }
}

