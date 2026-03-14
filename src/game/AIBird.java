package game;

/**
 * A single AI-controlled bird: wraps a Bird instance, its brain, and fitness.
 */
public class AIBird {

    public final Bird bird;
    public final NeuralNetwork brain;

    public double fitness;
    public boolean alive = true;

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

        double[] input = new double[]{
                bird.y / (double) Constants.HEIGHT,
                bird.velocity / 10.0,
                (next.x - bird.x) / (double) Constants.WIDTH,
                next.topHeight / (double) Constants.HEIGHT,
                gapCenterY / (double) Constants.HEIGHT
        };

        double jumpProb = brain.forward(input);
        if (jumpProb > 0.5) {
            bird.jump();
        }

        bird.update();
        fitness += 1.0;
    }

    public void kill() {
        bird.kill();
        alive = false;
    }
}

