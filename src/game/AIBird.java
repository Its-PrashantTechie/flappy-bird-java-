package game;

/**
 * A single AI-controlled bird: wraps a Bird instance, its brain, and fitness.
 */
public class AIBird {

    public final Bird bird;
    public final NeuralNetwork brain;

    public double fitness;
    public boolean alive = true;
    public int pipesPassed = 0;

    public AIBird(NeuralNetwork brain, int startX, int startY) {
        this.brain = brain;
        this.bird = new Bird(startX, startY);
        this.fitness = 0.0;
        this.alive = true;
    }

    public void reset(int startX, int startY) {
        bird.reset(startX, startY);
        fitness = 0.0;
        alive = true;
        pipesPassed = 0;
    }

    /**
     * One simulation step for GA mode.
     * - Builds inputs from bird and closest pipe.
     * - Lets the brain decide whether to jump.
     * - Updates physics and fitness.
     */
    public void update(PipeManager pipeManager) {
        if (!alive || !bird.alive) return;

        Pipe next = pipeManager.getNextPipeForX(bird.x);
        if (next == null) {
            bird.update();
            fitness += 1.0;
            return;
        }

        double gapCenterY = next.topHeight + next.gap / 2.0;
        double distanceToPipe = next.x - bird.x;
        double distanceToGap = gapCenterY - bird.y;

        double[] input = new double[]{
                bird.y / (double) Constants.HEIGHT,           // bird height
                bird.velocity / 15.0,                          // bird velocity (normalized)
                distanceToPipe / (double) Constants.WIDTH,    // distance to next pipe
                next.topHeight / (double) Constants.HEIGHT,   // top pipe height
                gapCenterY / (double) Constants.HEIGHT,       // gap center height
                distanceToGap / (double) Constants.HEIGHT     // vertical distance to gap
        };

        double jumpProb = brain.forward(input);
        if (jumpProb > 0.3) { // Lower threshold for more aggressive jumping
            bird.jump();
        }

        bird.update();
        fitness += 1.0;
        
        // Remove duplicate pipe counting - handled in GamePanel.updateAIScores()
    }

    public void kill() {
        bird.kill();
        alive = false;
    }
}

