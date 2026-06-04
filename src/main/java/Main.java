import view.BlackjackGUI;

/**
 * Main.java - Entry point for Blackjack game
 * จุดเริ่มต้นของโปรแกรม Blackjack
 */
public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure thread safety for GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            BlackjackGUI gui = new BlackjackGUI();
            gui.setVisible(true);
        });
    }
}