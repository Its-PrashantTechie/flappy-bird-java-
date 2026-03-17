package game;

import java.awt.*;

/**
 * Represents a single vertical pipe pair (top and bottom) at a given x-position.
 * Instances are managed by {@link PipeManager}.
 */
public class Pipe {
    public int x;
    public int pipeWidth;
    public int gap;
    public int topHeight;
    public boolean passed = false;

    public Pipe(int x, int pipeWidth, int gap, int topHeight) {
        this.x = x;
        this.pipeWidth = pipeWidth;
        this.gap = gap;
        this.topHeight = topHeight;
    }

    public void draw(Graphics g) {
        // Main pipe color (light grey)
        g.setColor(new Color(160, 160, 160)); // Light grey
        
        // Draw top pipe
        g.fillRect(x, 0, pipeWidth, topHeight);
        
        // Draw bottom pipe (from gap to ground)
        int groundHeight = 50;
        int bottomPipeY = topHeight + gap;
        int bottomPipeHeight = Constants.HEIGHT - groundHeight - bottomPipeY;
        
        // Only draw bottom pipe if it has positive height
        if (bottomPipeHeight > 0) {
            g.fillRect(x, bottomPipeY, pipeWidth, bottomPipeHeight);
        }
        
        // Pipe caps (darker, wider)
        g.setColor(new Color(120, 120, 120)); // Darker grey
        int capWidth = pipeWidth + 10;
        int capHeight = 30;
        int capX = x - 5;
        
        // Top pipe cap
        g.fillRect(capX, topHeight - capHeight, capWidth, capHeight);
        
        // Bottom pipe cap (only if bottom pipe exists)
        if (bottomPipeHeight > 0) {
            g.fillRect(capX, bottomPipeY, capWidth, capHeight);
        }
        
        // Add some shading/highlight for 3D effect
        g.setColor(new Color(180, 180, 180)); // Lighter grey
        g.fillRect(x + 5, 0, 5, topHeight);
        if (bottomPipeHeight > 0) {
            g.fillRect(x + 5, bottomPipeY, 5, bottomPipeHeight);
        }
    }
}

