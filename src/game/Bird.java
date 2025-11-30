package game;

import java.awt.*;

public class Bird {
    public static int x = 100;
    public static int y = 250;
    public static int size = 35;
    public static double velocity = 0;
    public static double gravity = 0.5;
    public static double lift = -8;

    // Animation
    private static int wingFlapFrame = 0;
    private static int wingFlapDirection = 1;

    public static void update() {
        velocity += gravity;
        y += velocity;

        // prevent flying out of top
        if (y < 0) {
            y = 0;
            velocity = 0;
        }

        // wing flap animation logic
        wingFlapFrame += wingFlapDirection;
        if (wingFlapFrame > 5 || wingFlapFrame < -5) {
            wingFlapDirection *= -1;
        }
    }

    public static void jump() {
        velocity = lift;
    }

    public static void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int wingY = y + 15 + wingFlapFrame;

        // Body
        g2.setColor(new Color(255, 215, 0)); // golden yellow
        g2.fillOval(x, y, size, size);

        // Belly
        g2.setColor(new Color(255, 255, 150));
        g2.fillOval(x + 8, y + 10, size - 15, size - 15);

        // Eye
        g2.setColor(Color.white);
        g2.fillOval(x + size - 15, y + 8, 10, 10);
        g2.setColor(Color.black);
        g2.fillOval(x + size - 11, y + 11, 5, 5);

        // Beak
        int[] bx = {x + size - 5, x + size + 10, x + size - 5};
        int[] by = {y + 15, y + 20, y + 25};
        g2.setColor(Color.orange);
        g2.fillPolygon(bx, by, 3);

        // Wing
        g2.setColor(new Color(255, 200, 0));
        g2.fillOval(x - 10, wingY, 25, 15);

        // Optional outline
        g2.setColor(new Color(150, 100, 0));
        g2.drawOval(x, y, size, size);
    }
}
