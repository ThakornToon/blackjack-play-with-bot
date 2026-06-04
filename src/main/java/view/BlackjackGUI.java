package view;

import model.BlackjackGameModel;
import javax.swing.*;
import java.awt.*;

/**
 * BlackjackGUI.java - Main JFrame container
 * ใช้ CardLayout สลับระหว่างหน้าเมนูและเกม
 * Help แสดงเป็น popup dialog แทนการสลับ panel
 */
public class BlackjackGUI extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final MenuPanel menuPanel;
    private GamePanel gamePanel;
    private BlackjackGameModel model;

    public BlackjackGUI() {
        setTitle("Blackjack - Dealer Rotation (4 Players)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);  // add layoutManager to JPanel

        model = new BlackjackGameModel();
        menuPanel = new MenuPanel(this);
        gamePanel = new GamePanel(model, this);

        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");

        add(mainPanel);  // add to JFrame
        cardLayout.show(mainPanel, "Menu");  // cardLayout manage mainPanel to show Menu
    }

    public void showGame() {
        if (model.isGameOver()) {
            resetGame();
        }
        cardLayout.show(mainPanel, "Game");
        gamePanel.refresh();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "Menu");
    }

    public void resetGame() {
        model = new BlackjackGameModel();
        gamePanel = new GamePanel(model, this);

        mainPanel.removeAll();
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");

        // ใช้คู่กันเสมอ เมื่อมีการเปลี่ยนแปลง component แบบไดนามิก
        mainPanel.revalidate();  // ให้ Layout Manager คำนวณตำแหน่งใหม่
        mainPanel.repaint();  // วาดหน้าจอใหม่
    }

    /**
     * Open the help screen as a modal popup dialog.
     * กฎเกมแสดงเป็น popup dialog แทนที่จะสลับ panel
     */
    public void showHelp() {
        HelpDialog helpDialog = new HelpDialog(this);
        helpDialog.setVisible(true);  // blocks until dialog is closed (modal)
    }
}