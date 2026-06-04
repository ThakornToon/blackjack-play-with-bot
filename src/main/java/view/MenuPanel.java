package view;

import javax.swing.*;
import java.awt.*;

/**
 * MenuPanel.java - Main menu screen
 * หน้าเมนูหลักของเกม
 */
public class MenuPanel extends JPanel {
    private BlackjackGUI parent;

    public MenuPanel(BlackjackGUI parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setBackground(new Color(34, 139, 34));

        // Title
        JLabel title = new JLabel("BLACKJACK - DEALER ROTATION");
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(Color.YELLOW);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(title, gbc);

        // Start Game button
        JButton startBtn = createMenuButton("▶ START GAME", new Color(50, 205, 50));
        startBtn.addActionListener(e -> parent.showGame());
        gbc.gridy = 1;
        add(startBtn, gbc);

        // How to Play button
        JButton howBtn = createMenuButton("❓ HOW TO PLAY", new Color(70, 130, 180));
        howBtn.addActionListener(e -> parent.showHelp());
        gbc.gridy = 2;
        add(howBtn, gbc);

        // Exit button
        JButton exitBtn = createMenuButton("✖ EXIT", new Color(178, 34, 34));
        exitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        add(exitBtn, gbc);
    }

    /**
     * Create a styled menu button
     * สร้างปุ่มเมนูที่มีสไตล์
     */
    private JButton createMenuButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(15, 40, 15, 40)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 70));
        return btn;
    }
}