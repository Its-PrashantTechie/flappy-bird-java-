package game;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Game {
    public static void main(String[] args) {
        String[] options = {"Human", "AI (Genetic)"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose game mode:",
                "Flappy Bird",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        GameMode mode = (choice == 1) ? GameMode.AI : GameMode.HUMAN;

        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel panel = new GamePanel(mode);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
 
