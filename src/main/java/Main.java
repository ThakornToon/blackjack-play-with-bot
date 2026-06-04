import view.BlackjackGUI;

import javax.swing.*;

/**
 * Main.java - Entry point for Blackjack game
 * จุดเริ่มต้นของโปรแกรม Blackjack
 */
public class Main {
    public static void main(String[] args) {
//        // if you want button color add this before make GUI
//        try {
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Use SwingUtilities.invokeLater to ensure thread safety for GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            BlackjackGUI gui = new BlackjackGUI();
            gui.setVisible(true);
        });
    }
}