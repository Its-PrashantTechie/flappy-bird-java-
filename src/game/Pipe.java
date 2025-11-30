package game;

import java.awt.*;
import java.util.Random;

public class Pipe {
    public static int x = Constants.WIDTH;
    public static int pipeWidth = 80;
    public static int gap = 150;
    public static int topHeight;
    public static int speed = 3;

    private static Random rand = new Random();

    static {
        reset();
    }

    public static void update() {
        x -= speed;
        if (x + pipeWidth < 0) {
            reset();
        }
    }

    public static void reset() {
        x = Constants.WIDTH;
        topHeight = rand.nextInt(200) + 100;
    }

    public static void draw(Graphics g) {
        g.setColor(Color.green);
        g.fillRect(x, 0, pipeWidth, topHeight);
        g.fillRect(x, topHeight + gap, pipeWidth, Constants.HEIGHT - (topHeight + gap));
    }
}
