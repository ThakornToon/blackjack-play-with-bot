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
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private BlackjackGameModel model;

    public BlackjackGUI() {
        setTitle("Blackjack - Dealer Rotation (4 Players)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        model = new BlackjackGameModel();
        menuPanel = new MenuPanel(this);
        gamePanel = new GamePanel(model, this);

        // HelpPanel is no longer a card — it opens as a modal dialog via showHelp()
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");

        add(mainPanel);
        cardLayout.show(mainPanel, "Menu");
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

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Open the help screen as a modal popup dialog.
     * กฎเกมแสดงเป็น popup dialog แทนที่จะสลับ panel
     */
    public void showHelp() {
        HelpPanel helpDialog = new HelpPanel(this);
        helpDialog.setVisible(true);  // blocks until dialog is closed (modal)
    }
}